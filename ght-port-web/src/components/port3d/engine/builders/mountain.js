import * as THREE from 'three'
import { LEVEL } from '../constants.js'

/** 默认山体高度（世界单位 = 画布像素） */
export const DEFAULT_PEAK = 140

/** 山体配色：土褐色系，山脚浅土黄 → 山腰土褐 → 深褐岩脊 → 山顶积雪 */
const BANDS = [
  { t: 0, c: new THREE.Color('#d9b98c') },
  { t: 0.35, c: new THREE.Color('#b98b5a') },
  { t: 0.62, c: new THREE.Color('#946640') },
  { t: 0.82, c: new THREE.Color('#7a5232') },
  { t: 0.9, c: new THREE.Color('#f8fafc') }
]

function bandColor(t, snow, out) {
  const bands = snow ? BANDS : BANDS.slice(0, 4)
  if (t <= bands[0].t) return out.copy(bands[0].c)
  for (let i = 1; i < bands.length; i++) {
    if (t <= bands[i].t) {
      const a = bands[i - 1]
      const b = bands[i]
      return out.copy(a.c).lerp(b.c, (t - a.t) / (b.t - a.t))
    }
  }
  return out.copy(bands[bands.length - 1].c)
}

/**
 * 山体高度函数：椭圆底面内平滑隆起，叠加由种子决定的起伏（主峰 + 山脊）
 * @param {number} u 局部横坐标（-1 ~ 1）
 * @param {number} v 局部纵坐标（-1 ~ 1）
 * @param {number} s 种子
 * @returns {number} 0 ~ 1 的相对高度
 */
function shape(u, v, s) {
  const r = Math.sqrt(u * u + v * v)
  if (r >= 1) return 0
  const f = 1 - r
  const smooth = f * f * (3 - 2 * f)
  const ridge =
    0.2 * Math.sin(u * 3.1 + s) * Math.cos(v * 2.6 + s * 1.7) +
    0.1 * Math.sin(u * 6.7 + s * 2.3) * Math.sin(v * 5.9 + s * 0.6) +
    0.05 * Math.cos((u + v) * 11.3 + s * 3.1)
  return Math.max(0, smooth * (0.82 + ridge))
}

/**
 * 山体：低多边形地形网格，底面为元素宽高，高度由 cfg.peak 决定（可在编辑器中拖动绿色手柄调整）
 */
export function buildMountain(el, ctx) {
  const g = new THREE.Group()
  const { width: w, height: h } = el
  const peak = Math.max(10, Number(el.cfg?.peak) || DEFAULT_PEAK)
  const seed = Number(el.cfg?.seed ?? 1.3)
  const snow = el.cfg?.snow ?? peak >= 160
  // 椭圆底面的极坐标网格：外圈正好落在地面，避免出现长方形底板与四角
  const rings = Math.max(14, Math.min(48, Math.round(Math.max(w, h) / 14)))
  const segs = Math.max(48, Math.min(128, Math.round((w + h) / 8)))
  const verts = [0, 0, 0]
  for (let i = 1; i <= rings; i++) {
    const rr = i / rings
    for (let j = 0; j < segs; j++) {
      const a = (j / segs) * Math.PI * 2
      verts.push((Math.cos(a) * rr * w) / 2, 0, (Math.sin(a) * rr * h) / 2)
    }
  }
  const index = []
  for (let j = 0; j < segs; j++) index.push(0, 1 + ((j + 1) % segs), 1 + j)
  for (let i = 1; i < rings; i++) {
    const r0 = 1 + (i - 1) * segs
    const r1 = 1 + i * segs
    for (let j = 0; j < segs; j++) {
      const j1 = (j + 1) % segs
      index.push(r0 + j, r0 + j1, r1 + j, r0 + j1, r1 + j1, r1 + j)
    }
  }
  const geo = new THREE.BufferGeometry()
  geo.setAttribute('position', new THREE.Float32BufferAttribute(verts, 3))
  geo.setIndex(index)
  const pos = geo.attributes.position
  let max = 0
  const hs = new Float32Array(pos.count)
  for (let i = 0; i < pos.count; i++) {
    const v = shape((pos.getX(i) / w) * 2, (pos.getZ(i) / h) * 2, seed)
    hs[i] = v
    max = Math.max(max, v)
  }
  // 归一化，保证最高点正好等于设置的山体高度
  const k = max > 0 ? 1 / max : 0
  const colors = new Float32Array(pos.count * 3)
  const c = new THREE.Color()
  for (let i = 0; i < pos.count; i++) {
    const t = hs[i] * k
    pos.setY(i, LEVEL.land + t * peak)
    bandColor(t, snow, c).toArray(colors, i * 3)
  }
  geo.setAttribute('color', new THREE.BufferAttribute(colors, 3))
  // 拆成非索引几何，配合平面着色得到低多边形效果
  const flat = geo.toNonIndexed()
  geo.dispose()
  flat.computeVertexNormals()
  ctx.own(flat)
  const mat = new THREE.MeshLambertMaterial({ vertexColors: true, flatShading: true })
  ctx.own(mat)
  g.add(new THREE.Mesh(flat, mat))
  return g
}
