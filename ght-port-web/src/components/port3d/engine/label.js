import * as THREE from 'three'
import { textTexture } from './materials.js'

const FONT = '"PingFang SC","HarmonyOS Sans SC","Microsoft YaHei",sans-serif'

/** 圆角矩形路径 */
function roundRect(g, x, y, w, h, r) {
  g.beginPath()
  g.moveTo(x + r, y)
  g.arcTo(x + w, y, x + w, y + h, r)
  g.arcTo(x + w, y + h, x, y + h, r)
  g.arcTo(x, y + h, x, y, r)
  g.arcTo(x, y, x + w, y, r)
  g.closePath()
}

/**
 * 悬浮信息牌（科技感卡片 + 引线），始终面向相机
 * @param {Object} o {title, sub, color, height(世界单位离地高度), scale}
 * @param {Function} own 资源登记函数
 * @returns {THREE.Group}
 */
export function makeTag(o, own) {
  const dpr = 2
  const probe = document.createElement('canvas').getContext('2d')
  probe.font = `700 ${26 * dpr}px ${FONT}`
  const tw = probe.measureText(o.title || '').width
  probe.font = `500 ${19 * dpr}px ${FONT}`
  const sw = probe.measureText(o.sub || '').width
  const w = Math.ceil(Math.max(tw, sw + 26 * dpr) + 44 * dpr)
  const h = (o.sub ? 82 : 56) * dpr
  const c = document.createElement('canvas')
  c.width = w
  c.height = h + 14 * dpr
  const g = c.getContext('2d')
  const color = o.color || '#2f5bd3'
  // 卡片底
  g.shadowColor = 'rgba(30,58,138,0.25)'
  g.shadowBlur = 10 * dpr
  g.shadowOffsetY = 3 * dpr
  roundRect(g, 4 * dpr, 4 * dpr, w - 8 * dpr, h - 8 * dpr, 12 * dpr)
  g.fillStyle = 'rgba(255,255,255,0.96)'
  g.fill()
  g.shadowColor = 'transparent'
  g.lineWidth = 2 * dpr
  g.strokeStyle = 'rgba(47,91,211,0.28)'
  g.stroke()
  // 左侧色条
  g.fillStyle = color
  roundRect(g, 4 * dpr, 4 * dpr, 8 * dpr, h - 8 * dpr, 4 * dpr)
  g.fill()
  // 底部指示三角
  g.fillStyle = 'rgba(255,255,255,0.96)'
  g.beginPath()
  g.moveTo(w / 2 - 10 * dpr, h - 5 * dpr)
  g.lineTo(w / 2 + 10 * dpr, h - 5 * dpr)
  g.lineTo(w / 2, h + 8 * dpr)
  g.closePath()
  g.fill()
  // 文字
  g.fillStyle = '#0f172a'
  g.font = `700 ${26 * dpr}px ${FONT}`
  g.textBaseline = 'middle'
  g.fillText(o.title || '', 24 * dpr, (o.sub ? 30 : 28) * dpr)
  if (o.sub) {
    g.fillStyle = color
    g.beginPath()
    g.arc(30 * dpr, 58 * dpr, 6 * dpr, 0, Math.PI * 2)
    g.fill()
    g.fillStyle = '#475569'
    g.font = `500 ${19 * dpr}px ${FONT}`
    g.fillText(o.sub, 44 * dpr, 59 * dpr)
  }
  const tex = new THREE.CanvasTexture(c)
  tex.colorSpace = THREE.SRGBColorSpace
  tex.anisotropy = 4
  const mat = new THREE.SpriteMaterial({ map: tex, depthTest: false, transparent: true })
  const sp = new THREE.Sprite(mat)
  const k = (o.scale || 1) * 0.42
  sp.scale.set((c.width / dpr) * k, (c.height / dpr) * k, 1)
  sp.center.set(0.5, 0)
  sp.renderOrder = 20
  own?.(tex)
  own?.(mat)
  const grp = new THREE.Group()
  const lift = o.height || 30
  sp.position.y = lift + 6
  grp.add(sp)
  // 引线
  const lg = new THREE.BufferGeometry().setFromPoints([new THREE.Vector3(0, lift * 0.45, 0), new THREE.Vector3(0, lift + 6, 0)])
  const lm = new THREE.LineBasicMaterial({ color, transparent: true, opacity: 0.7, depthTest: false })
  const line = new THREE.Line(lg, lm)
  line.renderOrder = 19
  grp.add(line)
  own?.(lg)
  own?.(lm)
  grp.userData.isTag = true
  return grp
}

/**
 * 平铺在地面/水面上的文字
 * @param {Object} o {text, color, size(字高，世界单位), opacity}
 */
export function makeFlatText(o, own) {
  const { tex, aspect } = textTexture(o.text, { color: o.color, font: `800 64px ${FONT}` })
  const h = o.size || 20
  const geo = new THREE.PlaneGeometry(h * aspect, h)
  geo.rotateX(-Math.PI / 2)
  const mat = new THREE.MeshBasicMaterial({ map: tex, transparent: true, opacity: o.opacity ?? 1, depthWrite: false })
  const m = new THREE.Mesh(geo, mat)
  m.renderOrder = 2
  own?.(tex)
  own?.(geo)
  own?.(mat)
  return m
}
