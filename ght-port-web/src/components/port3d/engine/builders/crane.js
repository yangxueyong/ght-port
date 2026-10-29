import * as THREE from 'three'
import { COLORS } from '../constants.js'
import { matte, glow } from '../materials.js'
import { box, rbox, strut, sphere } from '../geom.js'

/**
 * 岸桥（集装箱装卸桥）：原点位于两腿中间地面，-z 方向为海侧（悬臂伸出）
 * @param {Object} o {width 门腿横向间距, span 前后腿距, height 门架高度, out 外伸距, back 后伸距, mode WORK|IDLE|MAINT, color}
 * @returns {THREE.Group} userData.tick 驱动小车往返
 */
export function stsCrane(o) {
  const g = new THREE.Group()
  const color = matte(o.color || COLORS.blue)
  const white = matte(COLORS.white)
  const dark = matte(COLORS.dark)
  const W = o.width
  const S = o.span
  const H = o.height
  const legT = Math.max(1.6, W * 0.09)
  const zs = [-S / 2, S / 2]
  const xs = [-W / 2, W / 2]
  // 行走台车 + 门腿
  xs.forEach((x) => {
    zs.forEach((z) => {
      box(g, dark, [x, 0, z], [legT * 1.6, 1.6, legT * 2.6])
      box(g, color, [x, 1.6, z], [legT, H - 1.6, legT])
    })
    // 下横梁（连接前后腿）
    box(g, color, [x, H * 0.32, 0], [legT * 0.8, legT * 0.8, S])
    // 斜撑
    strut(g, color, [x, H * 0.36, zs[0]], [x, H - 1, zs[1]], legT * 0.28)
  })
  // 门框上横梁
  zs.forEach((z) => box(g, color, [0, H - legT, z], [W + legT, legT, legT]))
  // 机房
  rbox(g, white, [0, H, S * 0.15], [W * 0.8, H * 0.12, S * 0.5], 0.8)
  box(g, matte(COLORS.blueMid), [0, H + H * 0.05, S * 0.15 + S * 0.25 + 0.05], [W * 0.5, H * 0.03, 0.2])
  // A 字架顶
  const apexY = H * 1.42
  xs.forEach((x) => {
    strut(g, color, [x * 0.7, H, zs[0]], [x * 0.25, apexY, 0], legT * 0.32)
    strut(g, color, [x * 0.7, H, zs[1]], [x * 0.25, apexY, 0], legT * 0.32)
  })
  box(g, color, [0, apexY - 1, 0], [W * 0.6, 1.6, 2])
  sphere(g, glow('#ef4444'), [0, apexY + 1.4, 0], 0.9)

  // 悬臂组（绕海侧门腿顶部转动：维修/闲置时仰起）
  const boomPivot = new THREE.Group()
  boomPivot.position.set(0, H + 0.4, zs[0])
  g.add(boomPivot)
  const out = o.out
  const back = o.back
  xs.forEach((x) => box(boomPivot, color, [x * 0.55, 0, -out / 2 + 0.5], [legT * 0.7, legT * 0.9, out]))
  box(boomPivot, color, [0, 0, -out + 1], [W * 0.6, legT * 0.9, 2])
  // 后伸臂
  const backArm = new THREE.Group()
  backArm.position.set(0, H + 0.4, zs[1])
  g.add(backArm)
  xs.forEach((x) => box(backArm, color, [x * 0.55, 0, back / 2], [legT * 0.7, legT * 0.9, back]))
  box(backArm, white, [0, legT * 0.9, back - 4], [W * 0.7, 3, 6])
  // 拉杆
  xs.forEach((x) => {
    strut(g, color, [x * 0.25, apexY, 0], [x * 0.5, H + 1, zs[1] + back], 0.35)
  })
  const tie = new THREE.Group()
  boomPivot.add(tie)
  xs.forEach((x) => {
    strut(tie, color, [x * 0.25, apexY - H - 0.4, -zs[0]], [x * 0.5, 1, -out * 0.55], 0.35)
    strut(tie, color, [x * 0.25, apexY - H - 0.4, -zs[0]], [x * 0.5, 1, -out + 2], 0.35)
  })

  // 小车 + 司机室 + 吊具 + 吊箱
  const trolley = new THREE.Group()
  boomPivot.add(trolley)
  box(trolley, white, [0, -1.2, 0], [W * 0.5, 2.2, 5])
  box(trolley, matte(COLORS.glass), [W * 0.18, -3.6, -1], [3, 2.6, 3])
  const hoist = new THREE.Group()
  trolley.add(hoist)
  const cable = box(hoist, dark, [0, -1, 0], [0.25, 1, 0.25])
  const spreader = new THREE.Group()
  hoist.add(spreader)
  box(spreader, matte(COLORS.yellow), [0, -0.8, 0], [W * 0.5, 0.8, 4.6])
  const load = rbox(spreader, matte(o.loadColor || COLORS.blueMid), [0, -6.2, 0], [W * 0.46, 5.2, 4.4], 0.4)

  const mode = o.mode || 'IDLE'
  if (mode !== 'WORK') {
    boomPivot.rotation.x = mode === 'MAINT' ? 1.15 : 0.9
    trolley.position.z = -6
    load.visible = false
    spreader.position.y = -2
    cable.scale.y = 2
    cable.position.y = -1
  }
  const phase = o.phase || 0
  g.userData.tick = (t) => {
    if (mode !== 'WORK') return
    // 周期：海侧下降抓箱 -> 上升 -> 陆侧下降放箱 -> 返回
    const k = (t * 0.12 + phase) % 1
    const sea = -out * 0.62
    const land = 4
    let z = sea
    let drop = 0
    if (k < 0.2) { z = sea; drop = Math.sin((k / 0.2) * Math.PI) }
    else if (k < 0.45) { z = sea + (land - sea) * ((k - 0.2) / 0.25) }
    else if (k < 0.65) { z = land; drop = Math.sin(((k - 0.45) / 0.2) * Math.PI) }
    else { z = land + (sea - land) * ((k - 0.65) / 0.35) }
    trolley.position.z = z
    const depth = 2 + drop * (H * 0.55)
    spreader.position.y = -depth
    cable.scale.y = depth
    cable.position.y = -depth / 2
    load.visible = k < 0.55
  }
  return g
}
