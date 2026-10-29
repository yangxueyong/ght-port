import * as THREE from 'three'
import { COLORS, CONTAINER_COLORS, LEVEL } from '../constants.js'
import { matte, glow } from '../materials.js'
import { box, rbox, cyl, containers, rand } from '../geom.js'

/** 船体平面轮廓（船首朝 +x） */
function hullShape(L, B, inset = 0) {
  const s = new THREE.Shape()
  const hl = L / 2 - inset
  const hb = B / 2 - inset
  const bow = L * 0.2
  s.moveTo(-hl, -hb * 0.86)
  s.quadraticCurveTo(-hl, -hb, -hl + hb * 0.25, -hb)
  s.lineTo(hl - bow, -hb)
  s.bezierCurveTo(hl - bow * 0.35, -hb, hl, -hb * 0.35, hl, 0)
  s.bezierCurveTo(hl, hb * 0.35, hl - bow * 0.35, hb, hl - bow, hb)
  s.lineTo(-hl + hb * 0.25, hb)
  s.quadraticCurveTo(-hl, hb, -hl, hb * 0.86)
  s.closePath()
  return s
}

/** 挤出船体段 */
function hullPart(L, B, inset, y0, height, mat, own) {
  const geo = new THREE.ExtrudeGeometry(hullShape(L, B, inset), { depth: height, bevelEnabled: true, bevelThickness: 0.4, bevelSize: 0.4, bevelSegments: 2, curveSegments: 16 })
  geo.rotateX(-Math.PI / 2)
  own?.(geo)
  const m = new THREE.Mesh(geo, mat)
  m.position.y = y0
  return m
}

/**
 * 集装箱船（微缩硬表面模型）：船长沿 x，船宽沿 z，原点为水线中心
 * @param {Object} o {length, beam, color 船壳色, cargo 是否载箱, seed}
 */
export function buildShipModel(o, own) {
  const g = new THREE.Group()
  const L = o.length
  const B = o.beam
  const hullH = Math.max(5, B * 0.42)
  const hullColor = o.color || COLORS.navy
  // 船底红色 + 船壳 + 白色舷墙线 + 甲板
  g.add(hullPart(L, B, 0.6, -1.4, 2.4, matte(COLORS.red), own))
  g.add(hullPart(L, B, 0, 1, hullH - 1.6, matte(hullColor), own))
  g.add(hullPart(L, B, 0, hullH - 0.6, 0.6, matte(COLORS.white), own))
  g.add(hullPart(L, B, 1.4, hullH - 0.4, 0.5, matte(COLORS.deck), own))
  const deckY = hullH + 0.2
  // 船尾上层建筑
  const hx = -L / 2 + L * 0.1
  const hw = Math.max(6, L * 0.1)
  const levels = 4
  const lh = Math.max(2.2, B * 0.15)
  for (let i = 0; i < levels; i++) {
    const shrink = i * 0.06
    rbox(g, matte(COLORS.white), [hx, deckY + i * lh, 0], [hw * (1 - shrink), lh - 0.35, B * (0.78 - shrink)], 0.5)
    box(g, matte(COLORS.navy), [hx + hw * (1 - shrink) / 2 + 0.05, deckY + i * lh + lh * 0.35, 0], [0.2, lh * 0.32, B * (0.7 - shrink)])
  }
  // 驾驶台两翼
  const bridgeY = deckY + levels * lh
  rbox(g, matte(COLORS.white), [hx + hw * 0.15, bridgeY, 0], [hw * 0.7, lh * 0.9, B * 0.98], 0.4)
  box(g, matte(COLORS.glass), [hx + hw * 0.5 + 0.05, bridgeY + lh * 0.25, 0], [0.2, lh * 0.4, B * 0.9])
  // 雷达桅
  box(g, matte(COLORS.white), [hx, bridgeY + lh * 0.9, 0], [0.8, lh * 1.4, 0.8])
  box(g, matte(COLORS.steel), [hx, bridgeY + lh * 2.1, 0], [0.6, 0.4, B * 0.35])
  // 烟囱
  rbox(g, matte(hullColor), [hx - hw * 0.65, deckY, 0], [hw * 0.45, lh * levels * 0.95, B * 0.28], 0.6)
  box(g, matte(COLORS.dark), [hx - hw * 0.65, deckY + lh * levels * 0.95, 0], [hw * 0.4, 0.4, B * 0.24])
  // 航行灯
  box(g, glow('#ef4444'), [hx + hw * 0.15, bridgeY + 0.6, -B * 0.49 - 0.1], [0.8, 0.8, 0.3])
  box(g, glow('#22c55e'), [hx + hw * 0.15, bridgeY + 0.6, B * 0.49 + 0.1], [0.8, 0.8, 0.3])
  // 船首桅与锚机
  box(g, matte(COLORS.white), [L / 2 - L * 0.08, deckY, 0], [0.7, B * 0.45, 0.7])
  cyl(g, matte(COLORS.steel), [L / 2 - L * 0.13, deckY, B * 0.18], [0.9, 0.9, 1.2, 10])
  cyl(g, matte(COLORS.steel), [L / 2 - L * 0.13, deckY, -B * 0.18], [0.9, 0.9, 1.2, 10])
  // 集装箱
  if (o.cargo !== false) {
    const list = []
    const x0 = hx + hw * 0.75 + 1
    const x1 = L / 2 - L * 0.17
    const bays = Math.max(2, Math.floor((x1 - x0) / 12.5))
    const bl = (x1 - x0) / bays
    const rows = Math.max(2, Math.floor((B * 0.82) / 4.4))
    const rw = (B * 0.82) / rows
    const ch = Math.max(2.6, rw * 0.95)
    const seed = o.seed || 1
    for (let b = 0; b < bays; b++) {
      for (let r = 0; r < rows; r++) {
        const tiers = 1 + Math.floor(rand(seed + b * 13 + r * 7) * 3)
        for (let t = 0; t < tiers; t++) {
          list.push({
            x: x0 + bl * (b + 0.5),
            y: deckY + t * ch,
            z: -B * 0.41 + rw * (r + 0.5),
            sx: bl - 0.8,
            sy: ch - 0.25,
            sz: rw - 0.5,
            color: CONTAINER_COLORS[Math.floor(rand(seed * 3 + b * 5 + r * 11 + t) * CONTAINER_COLORS.length)]
          })
        }
      }
    }
    containers(g, list, own)
  }
  return g
}

/** 船舶元素：漂浮于水面并轻微起伏 */
export function buildShip(el, ctx) {
  const g = new THREE.Group()
  const model = buildShipModel({ length: el.width, beam: el.height, color: el.cfg?.color, seed: el.width + el.height }, ctx.own)
  model.position.y = LEVEL.water
  g.add(model)
  g.userData.tick = (t) => {
    model.position.y = LEVEL.water + Math.sin(t * 1.4) * 0.25
    model.rotation.x = Math.sin(t * 1.1) * 0.006
  }
  return g
}
