import * as THREE from 'three'
import { COLORS, CONTAINER_COLORS, LEVEL } from '../constants.js'
import { matte, glow, texture } from '../materials.js'
import { box, rbox, cyl, containers, rand, seedOf } from '../geom.js'
import { makeTag, makeFlatText } from '../label.js'
import { stsCrane } from './crane.js'

/** 信息牌（有名称时显示） */
function tagOf(g, el, ctx, sub, height) {
  const title = el.cfg?.label
  if (!title || ctx.noTag) return
  const t = makeTag({ title, sub, color: COLORS.blue, height }, ctx.own)
  g.add(t)
}

/** 堆场：地坪 + 集装箱区 / 散货堆 + 轮胎吊往返 */
export function buildYard(el, ctx) {
  const g = new THREE.Group()
  const { width: w, height: h } = el
  box(g, matte(COLORS.offWhite), [0, 0, 0], [w, LEVEL.land + 0.2, h])
  const y0 = LEVEL.land + 0.2
  const seed = seedOf(el.uid || 'yard') + w
  // 分区标线
  for (let x = -w / 2 + 6; x <= w / 2 - 6; x += (w - 12) / 2) box(g, matte(COLORS.bluePale), [x, y0, 0], [0.8, 0.06, h - 8])
  if (el.cfg?.containers === true) {
    // 整齐的箱区：每个箱块统一层高、两种配色，块间留通道
    const list = []
    const blockRows = Math.max(1, Math.floor((h - 14) / 34))
    const rowH = (h - 14) / blockRows
    const bayL = 13.6
    const perBlock = 4
    for (let r = 0; r < blockRows; r++) {
      const zc = -h / 2 + 7 + rowH * (r + 0.5)
      const lanes = Math.max(2, Math.floor((rowH - 10) / 5.2))
      let x = -w / 2 + 10 + bayL / 2
      let blk = 0
      while (x + bayL * perBlock <= w / 2 - 8 + bayL / 2) {
        const tiers = 1 + Math.floor(rand(seed + r * 31 + blk * 7) * 3)
        const ca = CONTAINER_COLORS[Math.floor(rand(seed + blk * 13 + r) * CONTAINER_COLORS.length)]
        const cb = CONTAINER_COLORS[Math.floor(rand(seed * 2 + blk * 5 + r) * CONTAINER_COLORS.length)]
        for (let i = 0; i < perBlock; i++) {
          for (let l = 0; l < lanes; l++) {
            const top = tiers - (l === lanes - 1 && i % 2 ? 1 : 0)
            for (let t = 0; t < top; t++) {
              list.push({ x: x + i * bayL, y: y0 + t * 4.7, z: zc - (lanes * 5.2) / 2 + 2.6 + l * 5.2, sx: bayL - 0.8, sy: 4.5, sz: 4.8, color: (l + t) % 3 === 0 ? cb : ca })
            }
          }
        }
        x += bayL * perBlock + 6
        blk++
      }
    }
    containers(g, list, ctx.own)
    // 轮胎吊（RTG）
    const rtg = new THREE.Group()
    const span = Math.min(h - 10, rowH + 4)
    const blue = matte(COLORS.blue)
    ;[-span / 2, span / 2].forEach((z) => {
      ;[-5, 5].forEach((x) => {
        box(rtg, blue, [x, 2, z], [1.4, 20, 1.4])
        box(rtg, matte(COLORS.rubber), [x, 0, z], [3, 2, 2.4])
      })
      box(rtg, blue, [0, 20, z], [12, 1.6, 1.6])
    })
    ;[-5, 5].forEach((x) => box(rtg, blue, [x, 20, 0], [1.6, 1.8, span]))
    box(rtg, matte(COLORS.white), [0, 21.8, -span / 2 + 3], [6, 3, 4])
    rtg.position.set(0, y0, -h / 2 + 7 + rowH / 2)
    g.add(rtg)
    g.userData.tick = (t) => {
      rtg.position.x = Math.sin(t * 0.25) * (w / 2 - 22)
    }
  } else {
    // 散货堆
    const heaps = Math.max(2, Math.floor(w / 80))
    for (let i = 0; i < heaps; i++) {
      const r = Math.min((w / heaps) * 0.42, h * 0.36)
      const geo = new THREE.ConeGeometry(r, Math.min(16, r * 0.42), 10, 1)
      ctx.own(geo)
      const m = new THREE.Mesh(geo, matte(i % 2 ? COLORS.coal : COLORS.heap, { flat: true }))
      m.position.set(-w / 2 + (w / heaps) * (i + 0.5), y0 + Math.min(16, r * 0.42) / 2, -h * 0.06)
      m.scale.z = Math.min(1.6, (h * 0.66) / (r * 2))
      m.rotation.y = rand(seed + i) * Math.PI
      g.add(m)
    }
    // 皮带机
    box(g, matte(COLORS.gray), [0, y0 + 9, h / 2 - 6], [w - 20, 1.2, 3])
    for (let x = -w / 2 + 16; x < w / 2 - 10; x += 30) box(g, matte(COLORS.blue), [x, y0, h / 2 - 6], [1, 9, 1])
  }
  tagOf(g, el, ctx, el.cfg?.containers === true ? '集装箱堆场' : '散货堆场', 42)
  return g
}

/** 仓库：白色墙体 + 港湾蓝坡屋顶 + 卷帘门 + 高窗带 */
export function buildWarehouse(el, ctx) {
  const g = new THREE.Group()
  const { width: w, height: h } = el
  const H = Math.max(18, Math.min(30, h * 0.18))
  rbox(g, matte(COLORS.white), [0, 0, 0], [w, H, h], 1.2)
  // 坡屋顶
  const s = new THREE.Shape()
  const rh = Math.min(14, h * 0.12)
  s.moveTo(-h / 2 - 2, 0)
  s.lineTo(0, rh)
  s.lineTo(h / 2 + 2, 0)
  s.lineTo(h / 2 + 2, -1)
  s.lineTo(-h / 2 - 2, -1)
  s.closePath()
  const geo = new THREE.ExtrudeGeometry(s, { depth: w + 4, bevelEnabled: false })
  geo.translate(0, 0, -(w + 4) / 2)
  geo.rotateY(Math.PI / 2)
  ctx.own(geo)
  const roof = new THREE.Mesh(geo, matte(COLORS.blue))
  roof.position.y = H
  g.add(roof)
  // 屋脊天窗
  box(g, matte(COLORS.bluePale), [0, H + rh - 0.6, 0], [w * 0.8, 1.4, 3])
  // 卷帘门（陆侧 +z、海侧 -z 各一排）
  const doors = Math.max(1, Math.floor(w / 46))
  for (let i = 0; i < doors; i++) {
    const x = -w / 2 + (w / doors) * (i + 0.5)
    box(g, matte(COLORS.blueLight), [x, 0, h / 2 + 0.2], [w / doors * 0.5, H * 0.62, 0.6])
    box(g, matte(COLORS.blue), [x, H * 0.62, h / 2 + 0.3], [w / doors * 0.56, 1, 1])
    box(g, matte(COLORS.blueLight), [x, 0, -h / 2 - 0.2], [w / doors * 0.5, H * 0.62, 0.6])
  }
  // 高窗带
  box(g, matte(COLORS.navy), [w / 2 + 0.15, H * 0.72, 0], [0.4, H * 0.12, h * 0.8])
  box(g, matte(COLORS.navy), [-w / 2 - 0.15, H * 0.72, 0], [0.4, H * 0.12, h * 0.8])
  tagOf(g, el, ctx, '仓储设施', H + rh + 16)
  return g
}

/** 办公楼：分层玻璃带 + 屋面设备 */
export function buildBuilding(el, ctx) {
  const g = new THREE.Group()
  const { width: w, height: h } = el
  const floors = Math.max(3, Math.min(7, Math.round(Math.min(w, h) / 24)))
  const fh = 6.4
  const white = matte(COLORS.white)
  const glass = matte(COLORS.glass)
  rbox(g, white, [0, 0, 0], [w, 3, h], 0.8)
  for (let i = 0; i < floors; i++) {
    const y = 3 + i * fh
    box(g, glass, [0, y, 0], [w - 3, fh * 0.55, h - 3])
    rbox(g, white, [0, y + fh * 0.55, 0], [w, fh * 0.45, h], 0.6)
    // 竖向窗框
    for (let x = -w / 2 + 8; x < w / 2 - 4; x += 10) box(g, white, [x, y, h / 2 - 1.6], [1, fh * 0.55, 0.4])
  }
  const top = 3 + floors * fh
  // 女儿墙 + 屋面设备
  box(g, matte(COLORS.blue), [0, top, h / 2 - 0.6], [w, 1.4, 1.2])
  box(g, matte(COLORS.blue), [0, top, -h / 2 + 0.6], [w, 1.4, 1.2])
  for (let i = 0; i < 3; i++) rbox(g, matte(COLORS.gray), [-w / 4 + i * (w / 4), top, -h / 6], [w / 7, 4, h / 5], 0.5)
  cyl(g, matte(COLORS.gray), [w / 3, top, h / 5], [3, 3, 3, 16])
  box(g, matte(COLORS.white), [w / 2 - 8, top, h / 2 - 8], [0.8, 14, 0.8])
  const lamp = box(g, glow('#ef4444'), [w / 2 - 8, top + 14, h / 2 - 8], [1.4, 1.4, 1.4])
  // 入口雨棚
  box(g, matte(COLORS.blue), [0, 8, h / 2 + 4], [w * 0.36, 1, 8])
  box(g, white, [-w * 0.15, 0, h / 2 + 7], [1, 8, 1])
  box(g, white, [w * 0.15, 0, h / 2 + 7], [1, 8, 1])
  tagOf(g, el, ctx, '港区指挥中心', top + 18)
  g.userData.tick = (t) => {
    lamp.visible = Math.sin(t * 3) > -0.3
  }
  return g
}

/** 储罐：白罐体 + 蓝色腰带 + 爬梯 */
export function buildTank(el, ctx) {
  const g = new THREE.Group()
  const r = Math.min(el.width, el.height) / 2 - 2
  const H = r * 1.15
  rbox(g, matte(COLORS.offWhite), [0, 0, 0], [el.width, 2, el.height], 0.6)
  cyl(g, matte(COLORS.white), [0, 2, 0], [r, r, H, 36])
  cyl(g, matte(COLORS.blue), [0, 2 + H * 0.62, 0], [r + 0.15, r + 0.15, H * 0.12, 36])
  const geo = new THREE.ConeGeometry(r + 0.4, r * 0.25, 36)
  ctx.own(geo)
  const roof = new THREE.Mesh(geo, matte(COLORS.white))
  roof.position.y = 2 + H + r * 0.125
  g.add(roof)
  box(g, matte(COLORS.steel), [r * 0.7, 2, r * 0.72], [1.4, H, 0.4])
  cyl(g, matte(COLORS.blueMid), [0, 2 + H + r * 0.25, 0], [1.4, 1.4, 1.6, 12])
  return g
}

/** 闸口：雨棚 + 立柱灯带 + 道闸起落 + 岗亭 */
export function buildGate(el, ctx) {
  const g = new THREE.Group()
  const { width: w, height: h } = el
  box(g, matte(COLORS.road), [0, 0, 0], [w, LEVEL.road, h])
  const y0 = LEVEL.road
  const lanes = Math.max(2, Math.floor(w / 40))
  const lw = w / lanes
  const H = 16
  const white = matte(COLORS.white)
  // 车道分隔岛 + 立柱
  for (let i = 0; i <= lanes; i++) {
    const x = -w / 2 + i * lw
    rbox(g, matte(COLORS.offWhite), [x, y0, 0], [4, 1.2, h * 0.5], 0.4)
    rbox(g, white, [x, y0 + 1.2, 0], [2.6, H, 2.6], 0.5)
    box(g, glow('#38bdf8'), [x, y0 + 3, 1.35], [0.6, H - 5, 0.2])
  }
  // 雨棚
  rbox(g, white, [0, y0 + H + 1.2, 0], [w + 8, 3, h * 0.62], 0.8)
  box(g, matte(COLORS.blue), [0, y0 + H + 1.6, h * 0.31 + 0.2], [w + 8, 2.2, 0.5])
  box(g, matte(COLORS.blue), [0, y0 + H + 1.6, -h * 0.31 - 0.2], [w + 8, 2.2, 0.5])
  // 道闸
  const stripe = new THREE.MeshLambertMaterial({ map: texture('stripe') })
  ctx.own(stripe)
  const arms = []
  for (let i = 0; i < lanes; i++) {
    const x = -w / 2 + i * lw + 2.5
    box(g, matte(COLORS.dark), [x, y0 + 1.2, -h * 0.18], [2, 4, 2])
    const pivot = new THREE.Group()
    pivot.position.set(x, y0 + 4.6, -h * 0.18)
    box(pivot, stripe, [(lw - 6) / 2, -0.4, 0], [lw - 6, 0.8, 0.8])
    g.add(pivot)
    arms.push({ pivot, phase: i * 0.5 })
    // 车道箭头
    box(g, matte(COLORS.white), [x + lw / 2 - 2.5, y0, h * 0.3], [1.2, 0.08, 8])
  }
  // 岗亭
  rbox(g, white, [w / 2 + 10, y0 - 0.2, 0], [10, 10, 10], 0.8)
  box(g, matte(COLORS.glass), [w / 2 + 10, y0 + 4, 5.05], [7, 3.6, 0.2])
  box(g, matte(COLORS.blue), [w / 2 + 10, y0 + 9.8, 0], [11, 1.2, 11])
  tagOf(g, el, ctx, '智能闸口 · 扫码核销', H + 22)
  g.userData.tick = (t) => {
    arms.forEach((a) => {
      const k = (t * 0.12 + a.phase) % 1
      const open = k < 0.35 ? 0 : k < 0.45 ? (k - 0.35) / 0.1 : k < 0.75 ? 1 : k < 0.85 ? 1 - (k - 0.75) / 0.1 : 0
      a.pivot.rotation.z = open * 1.3
    })
  }
  return g
}

/** 门机（独立岸桥元素）：沿 z 方向，-z 为悬臂侧 */
export function buildCrane(el) {
  const g = new THREE.Group()
  const { width: w, height: h } = el
  const span = h * 0.42
  const c = stsCrane({ width: Math.max(8, w * 0.8), span, height: Math.max(30, h * 0.5), out: h * 0.5, back: h * 0.12, mode: 'IDLE' })
  c.position.set(0, 0, h / 2 - h * 0.12 - span / 2)
  g.add(c)
  return g
}

/** 绿植：低多边形树丛 */
export function buildTree(el, ctx) {
  const g = new THREE.Group()
  const { width: w, height: h } = el
  const n = Math.max(1, Math.min(9, Math.round((w * h) / 1600)))
  const seed = w * 7 + h
  rbox(g, matte(COLORS.offWhite), [0, 0, 0], [w, 1.6, h], 0.6)
  rbox(g, matte(COLORS.grass), [0, 1.2, 0], [w - 2, 0.6, h - 2], 0.3)
  const cols = Math.ceil(Math.sqrt(n))
  for (let i = 0; i < n; i++) {
    const cx = -w / 2 + (w / cols) * ((i % cols) + 0.5) + (rand(seed + i) - 0.5) * 4
    const cz = -h / 2 + (h / Math.ceil(n / cols)) * (Math.floor(i / cols) + 0.5) + (rand(seed + i * 3) - 0.5) * 4
    const s = Math.min(w / cols, h / Math.ceil(n / cols)) * (0.36 + rand(seed + i * 5) * 0.1)
    cyl(g, matte(COLORS.trunk), [cx, 1.6, cz], [s * 0.12, s * 0.16, s * 0.9, 8])
    const geo = new THREE.IcosahedronGeometry(s, 0)
    ctx.own(geo)
    const m = new THREE.Mesh(geo, matte(i % 2 ? COLORS.tree : COLORS.treeDark, { flat: true }))
    m.position.set(cx, 1.6 + s * 1.5, cz)
    m.scale.y = 1.15
    m.rotation.y = rand(seed + i) * 3
    g.add(m)
  }
  return g
}

/** 地面文字 */
export function buildLabel(el, ctx) {
  const g = new THREE.Group()
  const size = Math.min(el.height, Number(el.cfg?.fontSize) || 18) * 1.1
  const txt = makeFlatText({ text: el.cfg?.text || '文字', color: el.cfg?.color || COLORS.navy, size }, ctx.own)
  txt.position.y = LEVEL.land + 0.4
  g.add(txt)
  g.userData.noShadowCast = true
  return g
}
