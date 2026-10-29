import * as THREE from 'three'
import { LEVEL, UI_COLORS } from './constants.js'
import { sampleCenterline, controlPoints } from './river.js'

const handleGeo = new THREE.SphereGeometry(1, 20, 14)
const ringGeo = new THREE.TorusGeometry(1.25, 0.28, 8, 28)
const unitBox = new THREE.BoxGeometry(1, 1, 1)

/** 计算模型最高点（忽略信息牌）；cap 为上限，避免高塔类模型撑大选中框 */
function topOf(content, cap = 120) {
  let max = 0
  content.updateMatrixWorld(true)
  const inv = content.matrixWorld.clone().invert()
  const b = new THREE.Box3()
  content.traverse((o) => {
    if (o.userData.isTag) return
    let p = o.parent
    while (p && p !== content) {
      if (p.userData.isTag) return
      p = p.parent
    }
    if (!o.isMesh) return
    if (o.isInstancedMesh) {
      o.computeBoundingBox()
      b.copy(o.boundingBox)
    } else {
      if (!o.geometry.boundingBox) o.geometry.computeBoundingBox()
      b.copy(o.geometry.boundingBox)
    }
    b.applyMatrix4(o.matrixWorld).applyMatrix4(inv)
    max = Math.max(max, b.max.y)
  })
  return Math.min(max, cap)
}

/** 手柄小球 */
function handle(kind, idx, color) {
  const g = new THREE.Group()
  const core = new THREE.Mesh(handleGeo, new THREE.MeshBasicMaterial({ color: UI_COLORS.handle, depthTest: false, transparent: true }))
  const ring = new THREE.Mesh(ringGeo, new THREE.MeshBasicMaterial({ color, depthTest: false, transparent: true }))
  ring.rotation.x = Math.PI / 2
  core.renderOrder = 41
  ring.renderOrder = 42
  g.add(core, ring)
  core.userData.handle = { kind, idx }
  ring.userData.handle = { kind, idx }
  g.userData.isHandle = true
  return g
}

/**
 * 选中框：外包线框 + 底面投影 + 缩放/旋转手柄；江河额外显示中心线与控制点
 * @returns {{group: THREE.Group, handles: THREE.Object3D[], dispose: Function}}
 */
export function buildSelection(el, content) {
  const group = new THREE.Group()
  const { width: w, height: h } = el
  const own = []
  const handles = []
  const isMountain = el.elementType === 'MOUNTAIN'
  const top = Math.max(4, topOf(content, isMountain ? Infinity : 120)) + 1
  const boxGeo = new THREE.EdgesGeometry(new THREE.BoxGeometry(w + 6, top, h + 6))
  const lm = new THREE.LineBasicMaterial({ color: UI_COLORS.select, depthTest: false, transparent: true, opacity: 0.9 })
  const lines = new THREE.LineSegments(boxGeo, lm)
  lines.position.y = top / 2
  lines.renderOrder = 40
  group.add(lines)
  const pg = new THREE.PlaneGeometry(w + 6, h + 6)
  pg.rotateX(-Math.PI / 2)
  const pm = new THREE.MeshBasicMaterial({ color: UI_COLORS.select, transparent: true, opacity: 0.14, depthWrite: false })
  const plane = new THREE.Mesh(pg, pm)
  plane.position.y = 0.4
  group.add(plane)
  own.push(boxGeo, lm, pg, pm)
  // 底部选中光框（加粗，便于辨识）
  const fm = new THREE.MeshBasicMaterial({ color: UI_COLORS.select, depthTest: false, transparent: true, opacity: 0.95 })
  own.push(fm)
  const bar = (x, z, sx, sz) => {
    const m = new THREE.Mesh(unitBox, fm)
    m.scale.set(sx, 0.6, sz)
    m.position.set(x, 0.8, z)
    m.renderOrder = 39
    group.add(m)
  }
  const fw = w + 6
  const fh = h + 6
  const t = Math.max(1.6, Math.min(fw, fh) * 0.02)
  bar(0, -fh / 2, fw, t)
  bar(0, fh / 2, fw, t)
  bar(-fw / 2, 0, t, fh)
  bar(fw / 2, 0, t, fh)

  const add = (kind, idx, pos, color) => {
    const hd = handle(kind, idx, color)
    hd.position.copy(pos)
    group.add(hd)
    handles.push(hd)
    hd.children.forEach((c) => own.push(c.material))
  }
  add('resize', 0, new THREE.Vector3(w / 2 + 3, top, h / 2 + 3), UI_COLORS.select)
  add('rotate', 0, new THREE.Vector3(0, top, h / 2 + 26), '#f59e0b')
  const sg = new THREE.BufferGeometry().setFromPoints([new THREE.Vector3(0, top, h / 2 + 3), new THREE.Vector3(0, top, h / 2 + 26)])
  const stem = new THREE.Line(sg, lm)
  stem.renderOrder = 40
  group.add(stem)
  own.push(sg)

  // 山体：顶部中央的绿色手柄，上下拖动调整高度
  if (isMountain) {
    add('height', 0, new THREE.Vector3(0, top + 22, 0), '#16a34a')
    const hg = new THREE.BufferGeometry().setFromPoints([new THREE.Vector3(0, top, 0), new THREE.Vector3(0, top + 22, 0)])
    const hl = new THREE.Line(hg, lm)
    hl.renderOrder = 40
    group.add(hl)
    own.push(hg)
  }

  if (el.elementType === 'RIVER') {
    const s = sampleCenterline(el)
    const cg = new THREE.BufferGeometry().setFromPoints(s.pts.map((p) => new THREE.Vector3(p.x, LEVEL.water + 0.6, p.z)))
    const cm = new THREE.LineDashedMaterial({ color: '#ffffff', dashSize: 10, gapSize: 7, depthTest: false, transparent: true })
    const cl = new THREE.Line(cg, cm)
    cl.computeLineDistances()
    cl.renderOrder = 40
    group.add(cl)
    own.push(cg, cm)
    controlPoints(el).forEach((p, i, arr) => {
      add('point', i, new THREE.Vector3(p.x, LEVEL.bank + 2, p.z), i === 0 || i === arr.length - 1 ? UI_COLORS.point : '#22c55e')
    })
  }
  return {
    group,
    handles,
    dispose: () => own.forEach((o) => o.dispose())
  }
}

/** 根据相机距离保持手柄屏幕尺寸 */
export function scaleHandles(handles, camera) {
  const v = new THREE.Vector3()
  handles.forEach((h) => {
    h.getWorldPosition(v)
    h.scale.setScalar(Math.max(1.5, camera.position.distanceTo(v) * 0.0062))
  })
}
