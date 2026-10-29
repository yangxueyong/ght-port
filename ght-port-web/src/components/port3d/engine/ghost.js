import * as THREE from 'three'
import { LEVEL } from './constants.js'

/** 街区网格尺寸（世界单位 = 画布像素） */
const CELL = 130
/** 元素与虚化建筑之间保留的间距 */
const CLEAR = 24
/** 底座外扩（与 stage.js 保持一致） */
const PAD = 40

/** 由网格坐标得到稳定的伪随机数，保证每次布局一致 */
function rand(ix, iz, k) {
  const s = Math.sin(ix * 127.1 + iz * 311.7 + k * 74.7) * 43758.5453
  return s - Math.floor(s)
}

/**
 * 虚化城市：在港口画布四周（以及画布内的空白处）填充半透明的示意建筑，让画面不显空洞。
 * 与港口元素重叠的建筑自动隐藏，元素移走后自动重新出现。建筑不参与拾取、不投射阴影。
 */
export function createGhostCity(scene) {
  const group = new THREE.Group()
  group.name = 'ghost-city'
  scene.add(group)
  const geo = new THREE.BoxGeometry(1, 1, 1)
  geo.translate(0, 0.5, 0)
  const mat = new THREE.MeshLambertMaterial({ color: '#ffffff', transparent: true, opacity: 0.5 })
  let mesh = null
  let blocks = []
  let W = 0
  let H = 0
  const m4 = new THREE.Matrix4()
  const q = new THREE.Quaternion()
  const pos = new THREE.Vector3()
  const scl = new THREE.Vector3()
  const base = new THREE.Color('#c9d7ee')
  const roof = new THREE.Color('#e8eef8')
  const c = new THREE.Color()

  /** 按画布尺寸生成街区与建筑（位置、尺寸、远近虚化程度） */
  function layout(w, h) {
    W = w
    H = h
    blocks = []
    const ext = Math.max(700, Math.max(w, h) * 0.6)
    const nx = Math.ceil((w / 2 + ext) / CELL)
    const nz = Math.ceil((h / 2 + ext) / CELL)
    for (let ix = -nx; ix <= nx; ix++) {
      for (let iz = -nz; iz <= nz; iz++) {
        const cx = ix * CELL
        const cz = iz * CELL
        const inside = Math.abs(cx) < w / 2 - CELL / 2 && Math.abs(cz) < h / 2 - CELL / 2
        const nearEdge = Math.abs(cx) < w / 2 + PAD + CELL / 2 && Math.abs(cz) < h / 2 + PAD + CELL / 2
        if (!inside && nearEdge) continue
        const dx = Math.max(0, Math.abs(cx) - w / 2)
        const dz = Math.max(0, Math.abs(cz) - h / 2)
        const dist = Math.hypot(dx, dz)
        if (dist > ext * (0.82 + rand(ix, iz, 9) * 0.18)) continue
        if (rand(ix, iz, 0) < (inside ? 0.25 : 0.12)) continue
        // 每个街区 1~3 栋，沿长边切分
        const n = 1 + Math.floor(rand(ix, iz, 1) * 3)
        const along = rand(ix, iz, 2) > 0.5
        const span = (CELL - 26) / n
        for (let k = 0; k < n; k++) {
          const fw = span * (0.62 + rand(ix, iz, 3 + k) * 0.3)
          const fd = (CELL - 26) * (0.45 + rand(ix, iz, 6 + k) * 0.45)
          const off = -((CELL - 26) / 2) + span * (k + 0.5)
          const r = rand(ix, iz, 12 + k)
          const hgt = inside ? 14 + r * 46 : 18 + r * r * 150 + (dist < 260 ? 0 : r * 40)
          blocks.push({
            x: cx + (along ? off : 0),
            z: cz + (along ? 0 : off),
            w: along ? fw : fd,
            d: along ? fd : fw,
            h: hgt,
            y: inside ? 0.2 : LEVEL.plinthBottom,
            // 远处更淡；画布内的更淡，避免喧宾夺主
            fade: inside ? 0.55 : Math.min(0.88, 0.28 + (dist / ext) * 0.65)
          })
        }
      }
    }
    if (mesh) {
      group.remove(mesh)
      mesh.dispose()
    }
    mesh = new THREE.InstancedMesh(geo, mat, Math.max(1, blocks.length))
    mesh.castShadow = false
    mesh.receiveShadow = false
    mesh.frustumCulled = false
    group.add(mesh)
  }

  /**
   * 根据当前港口元素隐藏被占用位置的建筑，并按背景色计算虚化颜色
   * @param {Object[]} elements 港口元素（画布坐标）
   * @param {THREE.Color} bg 场景背景色
   */
  function update(elements, bg) {
    if (!mesh) return
    // 元素在世界坐标中的外包矩形（含旋转）
    const boxes = (elements || []).map((e) => {
      const r = ((e.rotation || 0) * Math.PI) / 180
      const hw = (Math.abs(Math.cos(r)) * e.width + Math.abs(Math.sin(r)) * e.height) / 2
      const hd = (Math.abs(Math.sin(r)) * e.width + Math.abs(Math.cos(r)) * e.height) / 2
      const x = e.posX + e.width / 2 - W / 2
      const z = e.posY + e.height / 2 - H / 2
      return [x - hw - CLEAR, x + hw + CLEAR, z - hd - CLEAR, z + hd + CLEAR]
    })
    blocks.forEach((b, i) => {
      const hidden = boxes.some((a) => b.x + b.w / 2 > a[0] && b.x - b.w / 2 < a[1] && b.z + b.d / 2 > a[2] && b.z - b.d / 2 < a[3])
      pos.set(b.x, b.y, b.z)
      scl.set(hidden ? 0 : b.w, hidden ? 0 : b.h, hidden ? 0 : b.d)
      mesh.setMatrixAt(i, m4.compose(pos, q, scl))
      c.copy(base).lerp(roof, Math.min(1, b.h / 160)).lerp(bg, b.fade)
      mesh.setColorAt(i, c)
    })
    mesh.instanceMatrix.needsUpdate = true
    if (mesh.instanceColor) mesh.instanceColor.needsUpdate = true
  }

  function dispose() {
    if (mesh) mesh.dispose()
    geo.dispose()
    mat.dispose()
    scene.remove(group)
  }

  return { layout, update, dispose }
}
