import * as THREE from 'three'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'
import { LEVEL } from './constants.js'
import { tickTextures } from './materials.js'
import { createStage } from './stage.js'
import { buildElement } from './builders/index.js'
import { worldInfo } from './river.js'
import { buildSelection, scaleHandles } from './selection.js'
import { createGhostCity } from './ghost.js'

const ISO = { phi: 0.98, theta: -0.52 }
const VIEWS = { iso: ISO, top: { phi: 0.0001, theta: 0 }, front: { phi: 1.22, theta: 0 } }
const CLICK_TOLERANCE = 5

/**
 * 3D 港口场景：渲染、轨道相机（左键旋转 / 右键平移 / 滚轮缩放）、拾取与选中手柄
 * 编辑逻辑（移动、缩放、旋转、控制点）由外部通过回调实现
 */
export class PortScene {
  /**
   * @param {HTMLElement} container 容器
   * @param {Object} opts {mode: edit|view|berth, autoRotate, handlers}
   */
  constructor(container, opts = {}) {
    this.container = container
    this.mode = opts.mode || 'view'
    this.h = opts.handlers || {}
    this.W = 1600
    this.H = 900
    this.items = new Map()
    this.data = { elements: [], berthMap: {} }
    this.selectedUid = null
    this.hoverUid = null
    this.selection = null
    this.clock = new THREE.Clock()

    const r = new THREE.WebGLRenderer({ antialias: true, preserveDrawingBuffer: !!opts.preserve })
    r.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2))
    r.shadowMap.enabled = true
    r.shadowMap.type = THREE.PCFSoftShadowMap
    r.outputColorSpace = THREE.SRGBColorSpace
    r.domElement.style.display = 'block'
    r.domElement.style.outline = 'none'
    container.appendChild(r.domElement)
    this.renderer = r

    this.scene = new THREE.Scene()
    this.stage = createStage(this.scene)
    this.stage.setTheme('DAY')
    // 四周的虚化示意建筑（opts.ghost === false 时不显示）
    this.ghost = opts.ghost === false ? null : createGhostCity(this.scene)
    this.ghost?.layout(this.W, this.H)
    this.root = new THREE.Group()
    this.scene.add(this.root)

    this.camera = new THREE.PerspectiveCamera(32, 1, 20, 24000)
    const c = new OrbitControls(this.camera, r.domElement)
    c.enableDamping = true
    c.dampingFactor = 0.09
    c.screenSpacePanning = false
    c.maxPolarAngle = Math.PI * 0.46
    c.minDistance = 40
    c.maxDistance = 9000
    c.zoomToCursor = true
    c.mouseButtons = { LEFT: THREE.MOUSE.ROTATE, MIDDLE: THREE.MOUSE.DOLLY, RIGHT: THREE.MOUSE.PAN }
    c.autoRotate = !!opts.autoRotate
    c.autoRotateSpeed = 0.35
    this.controls = c
    c.addEventListener('change', () => this.h.onViewChange?.())
    // 用户手动调整过视角后，容器尺寸变化不再自动适应；同时中断镜头飞行
    c.addEventListener('start', () => {
      this.userMoved = true
      this.flight = null
    })

    this.raycaster = new THREE.Raycaster()
    this.ndc = new THREE.Vector2()
    this.plane = new THREE.Plane(new THREE.Vector3(0, 1, 0), -LEVEL.land)
    this.bindEvents()
    this.ro = new ResizeObserver(() => this.resize())
    this.ro.observe(container)
    this.resize()
    this.stage.resize(this.W, this.H)
    this.fit()
    r.setAnimationLoop(() => this.frame())
  }

  /* ======================== 数据同步 ======================== */

  /** 设置场景数据 */
  setData({ elements, berthMap, width, height, theme }) {
    const W = width || 1600
    const H = height || 900
    if (W !== this.W || H !== this.H) {
      this.W = W
      this.H = H
      this.stage.resize(W, H)
      this.ghost?.layout(W, H)
      this.items.forEach((it) => (it.sig = null))
    }
    if (theme && theme !== this.theme) {
      this.theme = theme
      this.stage.setTheme(theme)
    }
    this.data = { elements: elements || [], berthMap: berthMap || {} }
    this.sync()
  }

  /** 设置选中元素 */
  setSelected(uid) {
    this.selectedUid = uid || null
    this.refreshSelection(true)
  }

  /** 切换模式 */
  setMode(mode) {
    this.mode = mode
    this.items.forEach((it) => (it.sig = null))
    this.sync()
  }

  /** 增量同步：外观变化才重建，仅位置变化只更新变换 */
  sync() {
    const { elements, berthMap } = this.data
    const rivers = elements.filter((e) => e.elementType === 'RIVER')
    const riverSig = JSON.stringify(rivers.map((r) => [r.uid, r.posX, r.posY, r.width, r.height, r.rotation, r.cfg]))
    const riverInfo = rivers.map((r) => worldInfo(r, this.W, this.H))
    const lands = elements.filter((e) => e.elementType === 'LAND').sort((a, b) => (a.layerIndex ?? 0) - (b.layerIndex ?? 0))
    const alive = new Set()
    let selChanged = false
    elements.forEach((el) => {
      alive.add(el.uid)
      const berth = el.berthId ? berthMap[el.berthId] : null
      const sig = JSON.stringify([el.elementType, el.width, el.height, el.cfg, el.berthCode, el.berthName, berth?.status, this.mode]) + (el.elementType === 'RIVER' ? riverSig : '')
      let it = this.items.get(el.uid)
      if (!it || it.sig !== sig) {
        if (it) this.removeItem(el.uid)
        it = this.buildItem(el, { berth, rivers: riverInfo, riverIndex: rivers.indexOf(el) })
        it.sig = sig
        this.items.set(el.uid, it)
        if (el.uid === this.selectedUid) selChanged = true
      }
      it.el = el
      const lift = el.elementType === 'LAND' ? lands.indexOf(el) * 0.03 : 0
      it.holder.position.set(el.posX + el.width / 2 - this.W / 2, lift, el.posY + el.height / 2 - this.H / 2)
      it.baseY = lift
      it.holder.rotation.y = (-(el.rotation || 0) * Math.PI) / 180
    })
    ;[...this.items.keys()].forEach((uid) => !alive.has(uid) && this.removeItem(uid))
    this.ghost?.update(elements, this.scene.background || new THREE.Color('#e9eff7'))
    this.refreshSelection(selChanged)
  }

  /** 构建单个元素 */
  buildItem(el, extra) {
    const own = []
    const ctx = { ...extra, W: this.W, H: this.H, own: (o) => own.push(o), noTag: false }
    const content = buildElement(el, ctx)
    const noCast = content.userData.noShadowCast
    content.traverse((o) => {
      if (o.isMesh) {
        o.castShadow = !noCast
        o.receiveShadow = true
      }
    })
    const holder = new THREE.Group()
    holder.add(content)
    holder.userData.uid = el.uid
    this.root.add(holder)
    return { holder, content, own, el, baseY: 0 }
  }

  /** 移除元素并释放其独占资源 */
  removeItem(uid) {
    const it = this.items.get(uid)
    if (!it) return
    if (this.selection && this.selection.uid === uid) this.clearSelection()
    this.root.remove(it.holder)
    it.own.forEach((o) => (o.isInstancedMesh ? o.dispose() : o.dispose?.()))
    this.items.delete(uid)
  }

  /* ======================== 选中框 ======================== */

  clearSelection() {
    if (!this.selection) return
    this.selection.group.parent?.remove(this.selection.group)
    this.selection.dispose()
    this.selection = null
  }

  /** 重建或保持选中框（元素外观、尺寸变化时重建） */
  refreshSelection(force) {
    const it = this.selectedUid ? this.items.get(this.selectedUid) : null
    if (!it || this.mode !== 'edit') {
      this.clearSelection()
      return
    }
    const key = JSON.stringify([it.el.width, it.el.height, it.el.cfg?.points, it.el.cfg?.riverWidth, it.el.cfg?.peak, it.el.cfg?.seed])
    if (!force && this.selection && this.selection.uid === it.el.uid && this.selection.key === key && this.selection.group.parent === it.holder) return
    this.clearSelection()
    const sel = buildSelection(it.el, it.content)
    sel.uid = it.el.uid
    sel.key = key
    it.holder.add(sel.group)
    this.selection = sel
  }

  /* ======================== 相机 ======================== */

  /** 适应画布 */
  fit(view = 'iso') {
    this.lastView = view
    this.userMoved = false
    const v = VIEWS[view] || ISO
    const aspect = this.camera.aspect || 1.6
    const fov = (this.camera.fov * Math.PI) / 180
    // 有浮层遮挡时，只在未遮挡区域内完整显示港口
    const ins = this.insets || {}
    const cw = this.container.clientWidth || 1
    const ch = this.container.clientHeight || 1
    const fx = Math.max(0.3, (cw - (ins.left || 0) - (ins.right || 0)) / cw)
    const fy = Math.max(0.3, (ch - (ins.top || 0) - (ins.bottom || 0)) / ch)
    const need = Math.max(this.W / aspect / fx, (this.H * (view === 'top' ? 1 : 1.25)) / fy)
    const d = Math.min(this.controls.maxDistance * 0.9, (need / (2 * Math.tan(fov / 2))) * (view === 'top' ? 1.08 : 0.88))
    this.fitDist = d
    this.controls.target.set(0, 0, view === 'top' ? 0 : this.H * 0.04)
    this.camera.position.setFromSpherical(new THREE.Spherical(d, v.phi, v.theta)).add(this.controls.target)
    this.camera.updateProjectionMatrix()
    this.controls.update()
  }

  /** 当前镜头状态（视角、相机位置、观察点），用于记忆视角 */
  getCamera() {
    const p = this.camera.position
    const t = this.controls.target
    return { view: this.lastView || 'iso', pos: [p.x, p.y, p.z], target: [t.x, t.y, t.z] }
  }

  /** 恢复镜头状态；先按视角适应一次以得到缩放基准，再还原到记忆的位置 */
  setCamera(s) {
    if (!s || !Array.isArray(s.pos) || !Array.isArray(s.target)) return false
    if (![...s.pos, ...s.target].every(Number.isFinite)) return false
    this.flight = null
    this.fit(s.view || 'iso')
    this.controls.target.set(...s.target)
    this.camera.position.set(...s.pos)
    this.controls.update()
    this.userMoved = true
    return true
  }

  /**
   * 镜头飞向元素并显示定位光圈（泊位轮播联动）；保持当前俯仰与方位角，只改变焦点与距离
   * @param {string|null} uid 元素 uid，为空时清除定位并恢复全景
   */
  focusUid(uid) {
    const it = uid ? this.items.get(uid) : null
    if (!it) {
      this.clearFocusMarker()
      this.flight = null
      this.fit(this.lastView || 'iso')
      return
    }
    const target = new THREE.Vector3(it.holder.position.x, 0, it.holder.position.z)
    const off = this.camera.position.clone().sub(this.controls.target)
    const sph = new THREE.Spherical().setFromVector3(off)
    const size = Math.hypot(it.el.width, it.el.height)
    sph.radius = THREE.MathUtils.clamp(size * 4.8, 820, this.controls.maxDistance * 0.6)
    this.flight = {
      t0: this.clock.elapsedTime,
      dur: 1.3,
      fromTarget: this.controls.target.clone(),
      toTarget: target,
      fromPos: this.camera.position.clone(),
      toPos: new THREE.Vector3().setFromSpherical(sph).add(target)
    }
    this.userMoved = true
    this.showFocusMarker(it)
  }

  /**
   * 当前定位元素在屏幕上的椭圆范围（相对容器的像素），用于“定位时周围虚化”；未定位时返回 null
   * @returns {{cx: number, cy: number, rx: number, ry: number}|null}
   */
  focusScreenRect() {
    const fm = this.focusMarker
    if (!fm || !fm.box) return null
    const w = this.container.clientWidth
    const h = this.container.clientHeight
    const b = fm.box
    let x0 = Infinity
    let y0 = Infinity
    let x1 = -Infinity
    let y1 = -Infinity
    const v = this._fv || (this._fv = new THREE.Vector3())
    for (let i = 0; i < 8; i++) {
      v.set(i & 1 ? b.max.x : b.min.x, i & 2 ? b.max.y : b.min.y, i & 4 ? b.max.z : b.min.z).project(this.camera)
      const sx = ((v.x + 1) / 2) * w
      const sy = ((1 - v.y) / 2) * h
      x0 = Math.min(x0, sx)
      x1 = Math.max(x1, sx)
      y0 = Math.min(y0, sy)
      y1 = Math.max(y1, sy)
    }
    return { cx: (x0 + x1) / 2, cy: (y0 + y1) / 2, rx: (x1 - x0) / 2, ry: (y1 - y0) / 2 }
  }

  /** 定位光圈：地面向外扩散的波浪圈 + 竖直光柱 */
  showFocusMarker(it) {
    this.clearFocusMarker()
    const r = Math.max(it.el.width, it.el.height) * 0.62 + 12
    const g = new THREE.Group()
    // 只保留向外扩散的波浪圈（不显示静止的内圈）
    const ringGeo = new THREE.RingGeometry(r * 0.94, r, 64)
    ringGeo.rotateX(-Math.PI / 2)
    const waveMat = new THREE.MeshBasicMaterial({ color: '#22c3e6', transparent: true, opacity: 0.9, depthWrite: false, side: THREE.DoubleSide })
    const wave = new THREE.Mesh(ringGeo, waveMat)
    wave.position.y = 9
    const beamGeo = new THREE.CylinderGeometry(3, 3, 140, 16, 1, true)
    const beamMat = new THREE.MeshBasicMaterial({ color: '#22c3e6', transparent: true, opacity: 0.35, depthWrite: false })
    const beam = new THREE.Mesh(beamGeo, beamMat)
    beam.position.y = 80
    g.add(wave, beam)
    g.renderOrder = 30
    g.position.set(it.holder.position.x, 0, it.holder.position.z)
    this.scene.add(g)
    // 元素外包盒（世界坐标，限高避免高耸部件撑大范围），用于计算屏幕上的清晰区域
    it.holder.updateMatrixWorld(true)
    const box = new THREE.Box3().setFromObject(it.content)
    box.max.y = Math.min(box.max.y, 80)
    this.focusMarker = { group: g, wave, waveMat, box, own: [waveMat, ringGeo, beamGeo, beamMat] }
  }

  clearFocusMarker() {
    if (!this.focusMarker) return
    this.scene.remove(this.focusMarker.group)
    this.focusMarker.own.forEach((o) => o.dispose())
    this.focusMarker = null
  }

  /** 按系数缩放（>1 拉远） */
  zoomBy(f) {
    this.userMoved = true
    const off = this.camera.position.clone().sub(this.controls.target)
    const len = THREE.MathUtils.clamp(off.length() * f, this.controls.minDistance, this.controls.maxDistance)
    off.setLength(len)
    this.camera.position.copy(this.controls.target).add(off)
    this.controls.update()
  }

  /** 当前视角下，相机焦点处 1 个屏幕像素对应的世界单位（用于把鼠标位移换算为高度） */
  worldPerPixel() {
    const dist = this.camera.position.distanceTo(this.controls.target)
    const fov = (this.camera.fov * Math.PI) / 180
    return (2 * dist * Math.tan(fov / 2)) / (this.container.clientHeight || 1)
  }

  /** 当前缩放百分比 */
  zoomPct() {
    return ((this.fitDist || 1) / this.camera.position.distanceTo(this.controls.target)) * 100
  }

  /** 视图中心对应的画布坐标 */
  centerCanvasPoint() {
    const t = this.controls.target
    return { x: t.x + this.W / 2, y: t.z + this.H / 2 }
  }

  /** 屏幕坐标 -> 画布坐标（投影到陆地标高平面） */
  toCanvasPoint(clientX, clientY) {
    this.setRay(clientX, clientY)
    const p = new THREE.Vector3()
    if (!this.raycaster.ray.intersectPlane(this.plane, p)) return null
    return { x: p.x + this.W / 2, y: p.z + this.H / 2 }
  }

  /* ======================== 拾取与事件 ======================== */

  setRay(clientX, clientY) {
    const rect = this.renderer.domElement.getBoundingClientRect()
    this.ndc.set(((clientX - rect.left) / rect.width) * 2 - 1, -((clientY - rect.top) / rect.height) * 2 + 1)
    this.raycaster.setFromCamera(this.ndc, this.camera)
  }

  /** 是否可点击 */
  pickable(it) {
    if (this.mode === 'edit') return true
    return it.el.elementType === 'BERTH' && !!(it.el.berthId && this.data.berthMap[it.el.berthId])
  }

  /** 拾取：优先手柄，其次元素 */
  pick(clientX, clientY) {
    this.setRay(clientX, clientY)
    if (this.selection) {
      const hit = this.raycaster.intersectObjects(this.selection.handles, true)[0]
      if (hit) return { handle: hit.object.userData.handle }
    }
    const hits = this.raycaster.intersectObjects(this.root.children, true)
    for (const hit of hits) {
      if (hit.object.userData.handle || hit.object.type === 'LineSegments') continue
      let o = hit.object
      while (o && !o.userData.uid) o = o.parent
      if (!o) continue
      const it = this.items.get(o.userData.uid)
      if (it && this.pickable(it)) return { uid: o.userData.uid, point: hit.point }
      if (it && this.mode !== 'edit') continue
    }
    return null
  }

  bindEvents() {
    const el = this.renderer.domElement
    this.onDown = (e) => {
      this.down = { x: e.clientX, y: e.clientY, button: e.button }
      if (e.button !== 0 || this.mode !== 'edit') return
      const hit = this.pick(e.clientX, e.clientY)
      if (!hit) return
      this.controls.enabled = false
      this.dragging = true
      if (hit.handle) this.h.onHandleDown?.(e, hit.handle.kind, hit.handle.idx)
      else this.h.onElementDown?.(e, hit.uid)
    }
    this.onUp = (e) => {
      const d = this.down
      this.down = null
      const wasDrag = this.dragging
      this.dragging = false
      this.controls.enabled = true
      if (!d || wasDrag || d.button !== 0) return
      if (Math.hypot(e.clientX - d.x, e.clientY - d.y) > CLICK_TOLERANCE) return
      const hit = this.pick(e.clientX, e.clientY)
      if (hit && hit.uid) this.h.onElementClick?.(hit.uid, e)
      else if (!hit) this.h.onBgClick?.(e)
    }
    this.onDbl = (e) => {
      const hit = this.pick(e.clientX, e.clientY)
      if (hit && hit.handle) this.h.onHandleDblClick?.(e, hit.handle.kind, hit.handle.idx)
      else if (hit && hit.uid) this.h.onDblClick?.(e, hit.uid)
    }
    this.onMove = (e) => {
      if (this.down || this.moveQueued) return
      this.moveQueued = true
      requestAnimationFrame(() => {
        this.moveQueued = false
        const hit = this.pick(e.clientX, e.clientY)
        const uid = hit?.uid || null
        el.style.cursor = hit ? (hit.handle ? 'crosshair' : this.mode === 'edit' ? 'move' : 'pointer') : 'grab'
        if (uid !== this.hoverUid) {
          this.hoverUid = uid
          this.h.onHover?.(uid)
        }
      })
    }
    this.onLeave = () => {
      this.hoverUid = null
    }
    this.onCtx = (e) => e.preventDefault()
    // 捕获阶段先于 OrbitControls 判断是否命中元素
    this.container.addEventListener('pointerdown', this.onDown, true)
    window.addEventListener('pointerup', this.onUp)
    el.addEventListener('dblclick', this.onDbl)
    el.addEventListener('pointermove', this.onMove)
    el.addEventListener('pointerleave', this.onLeave)
    el.addEventListener('contextmenu', this.onCtx)
  }

  /* ======================== 渲染循环 ======================== */

  /**
   * 设置被浮层遮挡的边距（像素），画面焦点移到未遮挡区域的中心（用于全屏背景场景）
   * @param {{left?: number, right?: number, top?: number, bottom?: number}} insets
   */
  setInsets(insets) {
    this.insets = insets || null
    this.resize()
  }

  resize() {
    const w = this.container.clientWidth || 300
    const h = this.container.clientHeight || 200
    this.renderer.setSize(w, h)
    const ins = this.insets
    if (ins) this.camera.setViewOffset(w, h, ((ins.right || 0) - (ins.left || 0)) / 2, ((ins.bottom || 0) - (ins.top || 0)) / 2, w, h)
    else this.camera.clearViewOffset()
    this.camera.aspect = w / h
    this.camera.updateProjectionMatrix()
    if (this.fitDist && !this.userMoved) this.fit(this.lastView || 'iso')
  }

  frame() {
    const dt = Math.min(0.05, this.clock.getDelta())
    const t = this.clock.elapsedTime
    tickTextures(dt)
    this.items.forEach((it, uid) => {
      it.content.userData.tick?.(t)
      // 悬停抬升（泊位看板）
      const target = it.baseY + (this.mode !== 'edit' && uid === this.hoverUid ? 3 : 0)
      it.holder.position.y += (target - it.holder.position.y) * 0.2
    })
    if (this.selection) scaleHandles(this.selection.handles, this.camera)
    if (this.flight) {
      const f = this.flight
      const p = Math.min(1, (t - f.t0) / f.dur)
      const e = p < 0.5 ? 4 * p * p * p : 1 - Math.pow(-2 * p + 2, 3) / 2
      this.controls.target.lerpVectors(f.fromTarget, f.toTarget, e)
      this.camera.position.lerpVectors(f.fromPos, f.toPos, e)
      if (p >= 1) this.flight = null
    }
    if (this.focusMarker) {
      const k = (t * 0.7) % 1
      this.focusMarker.wave.scale.setScalar(0.6 + k * 0.9)
      this.focusMarker.waveMat.opacity = 0.85 * (1 - k)
    }
    this.controls.update()
    this.renderer.render(this.scene, this.camera)
    this.h.onAfterFrame?.()
  }

  /** 释放全部资源 */
  dispose() {
    this.renderer.setAnimationLoop(null)
    this.ro.disconnect()
    this.container.removeEventListener('pointerdown', this.onDown, true)
    window.removeEventListener('pointerup', this.onUp)
    ;[...this.items.keys()].forEach((uid) => this.removeItem(uid))
    this.clearSelection()
    this.clearFocusMarker()
    this.ghost?.dispose()
    this.stage.dispose()
    this.controls.dispose()
    this.renderer.dispose()
    this.renderer.domElement.remove()
  }
}
