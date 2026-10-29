import * as THREE from 'three'
import { COLORS, LEVEL } from '../constants.js'
import { matte, texture } from '../materials.js'
import { box, tiledMat } from '../geom.js'
import { makeFlatText } from '../label.js'
import { sampleCenterline, riverWidth, localToWorld, nearestOnPolyline } from '../river.js'
import { truck } from './vehicles.js'

/** 陆地：带材质的地台 */
export function buildLand(el, ctx) {
  const g = new THREE.Group()
  const { width: w, height: h } = el
  const variant = el.cfg?.variant || 'concrete'
  const cell = variant === 'concrete' ? 80 : 40
  const top = tiledMat(variant, w / cell, h / cell, ctx.own)
  const side = matte(COLORS.plinthSide)
  const m = box(g, side, [0, 0, 0], [w, LEVEL.land, h])
  m.material = [side, side, top, side, side, side]
  return g
}

/** 道路：路面 + 标线 + 往返集卡 */
export function buildRoad(el) {
  const g = new THREE.Group()
  const { width: w, height: h } = el
  const horizontal = w >= h
  const len = horizontal ? w : h
  const wid = horizontal ? h : w
  const inner = new THREE.Group()
  if (!horizontal) inner.rotation.y = Math.PI / 2
  g.add(inner)
  box(inner, matte(COLORS.road), [0, 0, 0], [len, LEVEL.road, wid])
  const line = matte(COLORS.roadLine)
  box(inner, line, [0, LEVEL.road, wid / 2 - 2.5], [len - 4, 0.12, 1])
  box(inner, line, [0, LEVEL.road, -wid / 2 + 2.5], [len - 4, 0.12, 1])
  for (let x = -len / 2 + 12; x < len / 2 - 8; x += 26) box(inner, matte(COLORS.yellow), [x, LEVEL.road, 0], [12, 0.12, 0.9])
  // 往返集卡（动画）
  const lanes = wid >= 22 ? [wid / 4, -wid / 4] : [0]
  const trucks = lanes.map((z, i) => {
    const t = truck(i % 2 ? COLORS.blueLight : COLORS.blue, true)
    t.scale.setScalar(Math.min(1, wid / 30))
    t.position.set(0, LEVEL.road, z)
    if (i % 2) t.rotation.y = Math.PI
    inner.add(t)
    return { t, dir: i % 2 ? -1 : 1, phase: i * 0.45 }
  })
  g.userData.tick = (time) => {
    trucks.forEach((o) => {
      const p = ((time * 0.045 + o.phase) % 1) * (len - 30) - (len - 30) / 2
      o.t.position.x = p * o.dir
    })
  }
  return g
}

/**
 * 生成沿中心线的条带几何
 * @param {Object} s 采样结果
 * @param {number[]} off [起始偏移, 结束偏移]
 * @param {number[]} ys [起始高度, 结束高度]
 * @param {boolean[]} mask 每个采样段是否生成
 */
function stripGeo(s, off, ys, mask) {
  const pos = []
  const uv = []
  const idx = []
  const { pts, nrm, len } = s
  for (let i = 0; i < pts.length; i++) {
    const p = pts[i]
    const n = nrm[i]
    pos.push(p.x + n.x * off[0], ys[0], p.z + n.z * off[0])
    pos.push(p.x + n.x * off[1], ys[1], p.z + n.z * off[1])
    uv.push(len[i] / 160, off[0] / 160, len[i] / 160, off[1] / 160)
    if (i > 0 && (!mask || mask[i - 1])) {
      const a = (i - 1) * 2
      idx.push(a, a + 1, a + 2, a + 1, a + 3, a + 2)
    }
  }
  const geo = new THREE.BufferGeometry()
  geo.setAttribute('position', new THREE.Float32BufferAttribute(pos, 3))
  geo.setAttribute('uv', new THREE.Float32BufferAttribute(uv, 2))
  geo.setIndex(idx)
  geo.computeVertexNormals()
  return geo
}

/** 判断采样段是否落在其他河道水域内（交汇处不生成河岸） */
function bankMask(el, ctx, s, off) {
  const others = (ctx.rivers || []).filter((r) => r.uid !== el.uid)
  const inside = s.pts.map((p, i) => {
    const q = { x: p.x + s.nrm[i].x * off, y: 0, z: p.z + s.nrm[i].z * off }
    const wp = localToWorld(el, ctx.W, ctx.H, q)
    return others.some((r) => nearestOnPolyline(r.pts, wp.x, wp.z).d < r.half + 2)
  })
  return inside.slice(1).map((v, i) => !v && !inside[i])
}

/** 江河：可弯曲水道 + 两岸护岸 + 流动水纹 + 河名 */
export function buildRiver(el, ctx) {
  const g = new THREE.Group()
  const s = sampleCenterline(el)
  const half = riverWidth(el) / 2
  const bw = 9
  const own = ctx.own
  const add = (geo, mat) => {
    own(geo)
    const m = new THREE.Mesh(geo, mat)
    g.add(m)
    return m
  }
  const waterMat = matte('#ffffff', { map: 'water', double: true })
  texture('water')
  const lift = (ctx.riverIndex || 0) * 0.03
  add(stripGeo(s, [-half, half], [LEVEL.water + lift, LEVEL.water + lift]), waterMat).userData.water = true
  const bankMat = matte(COLORS.bank, { double: true })
  const lineMat = matte(COLORS.bankLine, { double: true })
  ;[1, -1].forEach((side) => {
    const a = side * half
    const b = side * (half + bw)
    const mask = bankMask(el, ctx, s, side * (half + bw / 2))
    add(stripGeo(s, [a, b], [LEVEL.bank, LEVEL.bank], mask), bankMat)
    add(stripGeo(s, [a, a], [0, LEVEL.bank], mask), bankMat)
    add(stripGeo(s, [b, b], [0, LEVEL.bank], mask), bankMat)
    add(stripGeo(s, [a, a + side * 1.6], [LEVEL.bank + 0.06, LEVEL.bank + 0.06], mask), lineMat)
  })
  // 两端剖面（微缩沙盘切面效果）
  const capMat = matte(COLORS.waterEdge, { double: true })
  ;[0, s.pts.length - 1].forEach((i) => {
    const p = s.pts[i]
    const n = s.nrm[i]
    const wp = localToWorld(el, ctx.W, ctx.H, p)
    const inOther = (ctx.rivers || []).some((r) => r.uid !== el.uid && nearestOnPolyline(r.pts, wp.x, wp.z).d < r.half)
    if (inOther) return
    const geo = new THREE.BufferGeometry()
    const y = LEVEL.water + lift
    geo.setAttribute(
      'position',
      new THREE.Float32BufferAttribute([p.x - n.x * half, 0, p.z - n.z * half, p.x + n.x * half, 0, p.z + n.z * half, p.x - n.x * half, y, p.z - n.z * half, p.x + n.x * half, y, p.z + n.z * half], 3)
    )
    geo.setIndex([0, 1, 2, 1, 3, 2])
    geo.computeVertexNormals()
    add(geo, capMat)
  })
  // 河名（平铺在水面，沿河道方向）
  if (el.cfg?.label) {
    const mid = Math.floor(s.pts.length / 2)
    const p = s.pts[mid]
    const t = s.pts[Math.min(s.pts.length - 1, mid + 1)].clone().sub(s.pts[Math.max(0, mid - 1)])
    let ang = Math.atan2(-t.z, t.x)
    if (Math.cos(ang) < 0) ang += Math.PI
    const txt = makeFlatText({ text: el.cfg.label, color: '#ffffff', size: Math.min(46, Math.max(16, half * 0.42)), opacity: 0.55 }, own)
    txt.position.set(p.x, LEVEL.water + lift + 0.4, p.z)
    txt.rotation.y = ang
    g.add(txt)
  }
  g.userData.noShadowCast = true
  return g
}
