import * as THREE from 'three'
import { COLORS, CONTAINER_COLORS, LEVEL, STATUS_COLORS, STATUS_LABELS } from '../constants.js'
import { matte, glow, texture } from '../materials.js'
import { box, rbox, cyl, sphere, containers, rand, seedOf } from '../geom.js'
import { makeTag } from '../label.js'
import { stsCrane } from './crane.js'
import { buildShipModel } from './ship.js'
import { agv } from './vehicles.js'

const SHIP_COLORS = ['#1e3a8a', '#2f5bd3', '#0f766e', '#334155', '#4338ca']

/** 码头主体：岸壁 + 面板 + 压顶 + 护舷 + 系缆桩 + 轨道 */
function quay(g, w, h) {
  const D = LEVEL.deck
  rbox(g, matte(COLORS.white), [0, 0, 0], [w, D, h], 0.8)
  box(g, matte(COLORS.deck), [0, D - 0.1, 2], [w - 4, 0.3, h - 8])
  // 海侧压顶（港湾蓝）
  box(g, matte(COLORS.blue), [0, D - 1.6, -h / 2 + 0.2], [w - 0.6, 1.8, 1])
  // 护舷
  const fender = matte(COLORS.rubber)
  for (let x = -w / 2 + 12; x <= w / 2 - 10; x += 24) box(g, fender, [x, LEVEL.water - 0.5, -h / 2 - 0.9], [6, 3.6, 1.8])
  // 系缆桩
  const steel = matte(COLORS.dark)
  for (let x = -w / 2 + 24; x <= w / 2 - 12; x += 28) {
    cyl(g, steel, [x, D, -h / 2 + 3.4], [1, 1.2, 1.6, 10])
    cyl(g, steel, [x, D + 1.6, -h / 2 + 3.4], [1.5, 1.5, 0.4, 10])
  }
  // 岸桥轨道
  const rail = matte(COLORS.steel)
  box(g, rail, [0, D + 0.2, -h / 2 + 8], [w - 4, 0.3, 0.9])
  box(g, rail, [0, D + 0.2, -h / 2 + 8 + h * 0.42], [w - 4, 0.3, 0.9])
  // 车道标线
  for (let x = -w / 2 + 10; x < w / 2 - 8; x += 20) box(g, matte(COLORS.white), [x, D + 0.2, h * 0.32], [10, 0.08, 0.8])
}

/** 状态光边 + 角标灯 */
function statusFrame(g, w, h, color) {
  const D = LEVEL.deck + 0.25
  const mat = glow(color)
  const t = 1.1
  box(g, mat, [0, D, h / 2 - t / 2], [w, 0.35, t])
  box(g, mat, [-w / 2 + t / 2, D, 0], [t, 0.35, h])
  box(g, mat, [w / 2 - t / 2, D, 0], [t, 0.35, h])
  // 信号塔
  const px = w / 2 - 6
  const pz = h / 2 - 6
  box(g, matte(COLORS.white), [px, LEVEL.deck, pz], [1.2, 14, 1.2])
  const lamp = sphere(g, mat, [px, LEVEL.deck + 15.2, pz], 1.8)
  const halo = sphere(g, glow(color, 0.25), [px, LEVEL.deck + 15.2, pz], 3.4)
  return { lamp, halo }
}

/** 维修中：警示围挡 + 锥桶 */
function maintenanceProps(g, w, h) {
  const hz = new THREE.MeshLambertMaterial({ map: texture('hazard') })
  const D = LEVEL.deck
  for (let x = -w / 2 + 20; x < w / 2 - 20; x += 34) {
    box(g, hz, [x, D, -h / 2 + 14], [22, 3, 1])
    box(g, matte(COLORS.white), [x - 10, D, -h / 2 + 14], [1, 3.6, 1])
    box(g, matte(COLORS.white), [x + 10, D, -h / 2 + 14], [1, 3.6, 1])
  }
  const cone = matte(COLORS.orange)
  for (let i = 0; i < 5; i++) {
    const x = -w / 2 + 16 + i * ((w - 32) / 4)
    cyl(g, cone, [x, D, h * 0.12], [0.3, 1.6, 4, 10])
    box(g, matte(COLORS.white), [x, D + 1.6, h * 0.12], [2, 0.6, 2])
  }
  return hz
}

/** 后方堆箱 */
function stackRows(g, w, h, seed, own) {
  const list = []
  const z = h / 2 - 14
  for (let x = -w / 2 + 14, i = 0; x < w / 2 - 40; x += 15, i++) {
    ;[0, 1].forEach((row) => {
      const tiers = 1 + Math.floor(rand(seed + i * 5 + row * 17) * 3)
      for (let t = 0; t < tiers; t++) {
        const color = CONTAINER_COLORS[Math.floor(rand(seed * 7 + i * 3 + row * 11 + t) * CONTAINER_COLORS.length)]
        list.push({ x, y: LEVEL.deck + 0.2 + t * 4.8, z: z - row * 6, sx: 13, sy: 4.6, sz: 5, color })
      }
    })
  }
  containers(g, list, own)
}

/**
 * 泊位：码头 + 岸桥 + 状态光边 + 信息牌；作业中有靠泊船舶与装卸动画
 * 局部 -z 方向为临水侧
 */
export function buildBerth(el, ctx) {
  const g = new THREE.Group()
  const { width: w, height: h } = el
  const berth = ctx.berth
  const status = berth?.status || 'IDLE'
  const color = STATUS_COLORS[status] || STATUS_COLORS.IDLE
  const code = el.berthCode || berth?.berthCode || '新泊位'
  const name = el.berthName || berth?.berthName || '待保存'
  const seed = seedOf(code)
  quay(g, w, h)
  const { lamp, halo } = statusFrame(g, w, h, color)
  if (status !== 'MAINTENANCE') stackRows(g, w, h, seed, ctx.own)
  else ctx.own(maintenanceProps(g, w, h))

  // 岸桥
  const n = Math.max(1, Math.min(4, Math.floor(w / 85)))
  const craneW = Math.min(16, w / (n * 2.4))
  const span = h * 0.42
  const cranes = []
  for (let i = 0; i < n; i++) {
    const c = stsCrane({
      width: craneW,
      span,
      height: Math.max(26, h * 0.5),
      out: Math.max(34, h * 0.62),
      back: Math.max(10, h * 0.16),
      mode: status === 'WORKING' ? 'WORK' : status === 'MAINTENANCE' ? 'MAINT' : 'IDLE',
      phase: i * 0.37,
      loadColor: CONTAINER_COLORS[(seed + i) % CONTAINER_COLORS.length]
    })
    const x = status === 'WORKING' ? -w / 2 + (w / n) * (i + 0.5) : -w / 2 + craneW + i * (craneW * 2.2)
    c.position.set(x, LEVEL.deck + 0.3, -h / 2 + 8 + span / 2)
    g.add(c)
    cranes.push(c)
  }

  // 靠泊船舶 + AGV
  let ship = null
  const agvs = []
  if (status === 'WORKING') {
    const beam = Math.max(16, Math.min(h * 0.62, 46))
    ship = buildShipModel({ length: w * 0.92, beam, color: SHIP_COLORS[seed % SHIP_COLORS.length], seed }, ctx.own)
    ship.position.set(0, LEVEL.water, -h / 2 - 2.4 - beam / 2)
    g.add(ship)
    ;[0, 1].forEach((i) => {
      const a = agv(CONTAINER_COLORS[(seed + i * 2) % CONTAINER_COLORS.length])
      a.position.set(0, LEVEL.deck + 0.2, -h / 2 + 8 + span / 2 + (i ? 7 : -7))
      g.add(a)
      agvs.push({ a, phase: i * 0.5 })
    })
  }

  // 信息牌
  if (!ctx.noTag) {
    const tag = makeTag({ title: `${code} · ${name}`, sub: STATUS_LABELS[status], color, height: Math.max(48, h * 0.62) }, ctx.own)
    tag.position.set(w * 0.18, LEVEL.deck, h * 0.15)
    g.add(tag)
  }

  g.userData.tick = (t) => {
    cranes.forEach((c) => c.userData.tick(t))
    if (ship) {
      ship.position.y = LEVEL.water + Math.sin(t * 1.2) * 0.2
      ship.rotation.x = Math.sin(t * 0.9) * 0.004
    }
    agvs.forEach((o) => {
      const k = (t * 0.07 + o.phase) % 1
      const x = Math.sin(k * Math.PI * 2) * (w / 2 - 16)
      o.a.position.x = x
      o.a.rotation.y = Math.cos(k * Math.PI * 2) >= 0 ? 0 : Math.PI
    })
    const pulse = status === 'IDLE' ? 0.5 + 0.5 * Math.sin(t * 1.6) : 0.5 + 0.5 * Math.sin(t * 5)
    halo.scale.setScalar(0.8 + pulse * 0.6)
    lamp.visible = status !== 'MAINTENANCE' || pulse > 0.3
  }
  return g
}
