import * as THREE from 'three'
import { COLORS } from './constants.js'

/**
 * 材质与贴图库：全部使用 Lambert（漫反射）/ Basic（自发光）材质，不产生镜面反光
 * 材质按颜色缓存复用，整个应用生命周期共享
 */
const matCache = new Map()
const texCache = new Map()

/** 哑光材质（按颜色 + 选项缓存） */
export function matte(color, opts = {}) {
  const key = `m|${color}|${opts.flat ? 1 : 0}|${opts.opacity ?? 1}|${opts.map || ''}|${opts.double ? 1 : 0}`
  if (!matCache.has(key)) {
    const m = new THREE.MeshLambertMaterial({ color, flatShading: !!opts.flat, side: opts.double ? THREE.DoubleSide : THREE.FrontSide })
    if (opts.opacity !== undefined && opts.opacity < 1) {
      m.transparent = true
      m.opacity = opts.opacity
      m.depthWrite = false
    }
    if (opts.map) m.map = texture(opts.map)
    matCache.set(key, m)
  }
  return matCache.get(key)
}

/** 自发光材质（指示灯、状态光带，不受光照影响） */
export function glow(color, opacity = 1) {
  const key = `g|${color}|${opacity}`
  if (!matCache.has(key)) {
    const m = new THREE.MeshBasicMaterial({ color, transparent: opacity < 1, opacity })
    matCache.set(key, m)
  }
  return matCache.get(key)
}

/** 线材质 */
export function lineMat(color, opts = {}) {
  const key = `l|${color}|${opts.dashed ? 1 : 0}|${opts.top ? 1 : 0}`
  if (!matCache.has(key)) {
    const m = opts.dashed
      ? new THREE.LineDashedMaterial({ color, dashSize: 8, gapSize: 6 })
      : new THREE.LineBasicMaterial({ color })
    if (opts.top) {
      m.depthTest = false
      m.transparent = true
    }
    matCache.set(key, m)
  }
  return matCache.get(key)
}

/** 绘制 canvas 贴图 */
function canvasTex(w, h, draw, repeat = true) {
  const c = document.createElement('canvas')
  c.width = w
  c.height = h
  draw(c.getContext('2d'), w, h)
  const t = new THREE.CanvasTexture(c)
  t.colorSpace = THREE.SRGBColorSpace
  t.anisotropy = 4
  if (repeat) {
    t.wrapS = THREE.RepeatWrapping
    t.wrapT = THREE.RepeatWrapping
  }
  return t
}

const TEXTURES = {
  /** 底座网格 */
  grid: () =>
    canvasTex(128, 128, (g, w, h) => {
      g.fillStyle = COLORS.plinth
      g.fillRect(0, 0, w, h)
      g.strokeStyle = COLORS.grid
      g.lineWidth = 2
      g.strokeRect(0, 0, w, h)
      g.strokeStyle = 'rgba(160,180,210,0.25)'
      g.lineWidth = 1
      g.beginPath()
      g.moveTo(w / 2, 0)
      g.lineTo(w / 2, h)
      g.moveTo(0, h / 2)
      g.lineTo(w, h / 2)
      g.stroke()
    }),
  /** 水面流动纹理 */
  water: () =>
    canvasTex(256, 128, (g, w, h) => {
      g.fillStyle = COLORS.water
      g.fillRect(0, 0, w, h)
      g.strokeStyle = 'rgba(255,255,255,0.22)'
      g.lineCap = 'round'
      const lines = [[20, 22, 70], [120, 40, 60], [60, 70, 90], [180, 92, 50], [10, 108, 40], [200, 16, 36]]
      lines.forEach(([x, y, l]) => {
        g.lineWidth = 3
        g.beginPath()
        g.moveTo(x, y)
        g.bezierCurveTo(x + l * 0.3, y - 3, x + l * 0.7, y + 3, x + l, y)
        g.stroke()
      })
    }),
  /** 集装箱波纹板 */
  rib: () =>
    canvasTex(64, 64, (g, w, h) => {
      g.fillStyle = '#ffffff'
      g.fillRect(0, 0, w, h)
      for (let x = 0; x < w; x += 8) {
        g.fillStyle = 'rgba(0,0,0,0.10)'
        g.fillRect(x, 0, 3, h)
      }
      g.fillStyle = 'rgba(0,0,0,0.08)'
      g.fillRect(0, 0, w, 3)
      g.fillRect(0, h - 3, w, 3)
    }),
  /** 黄黑警示 */
  hazard: () =>
    canvasTex(64, 64, (g, w, h) => {
      g.fillStyle = '#1f2937'
      g.fillRect(0, 0, w, h)
      g.fillStyle = COLORS.yellow
      for (let i = -h; i < w; i += 32) {
        g.beginPath()
        g.moveTo(i, h)
        g.lineTo(i + 16, h)
        g.lineTo(i + 16 + h, 0)
        g.lineTo(i + h, 0)
        g.closePath()
        g.fill()
      }
    }),
  /** 红白道闸 */
  stripe: () =>
    canvasTex(64, 16, (g, w, h) => {
      g.fillStyle = '#ffffff'
      g.fillRect(0, 0, w, h)
      g.fillStyle = COLORS.red
      for (let x = 0; x < w; x += 32) g.fillRect(x, 0, 16, h)
    }),
  /** 混凝土地坪 */
  concrete: () =>
    canvasTex(128, 128, (g, w, h) => {
      g.fillStyle = '#e8edf4'
      g.fillRect(0, 0, w, h)
      g.strokeStyle = 'rgba(148,163,184,0.35)'
      g.lineWidth = 2
      g.strokeRect(1, 1, w - 2, h - 2)
    }),
  /** 草地 */
  grass: () =>
    canvasTex(64, 64, (g, w, h) => {
      g.fillStyle = COLORS.grass
      g.fillRect(0, 0, w, h)
      g.fillStyle = 'rgba(80,160,110,0.18)'
      for (let i = 0; i < 18; i++) g.fillRect((i * 37) % w, (i * 23) % h, 3, 3)
    }),
  /** 沙地 */
  sand: () =>
    canvasTex(64, 64, (g, w, h) => {
      g.fillStyle = COLORS.sand
      g.fillRect(0, 0, w, h)
      g.fillStyle = 'rgba(180,150,90,0.18)'
      for (let i = 0; i < 22; i++) g.fillRect((i * 29) % w, (i * 41) % h, 2, 2)
    })
}

/** 获取共享贴图 */
export function texture(name) {
  if (!texCache.has(name)) texCache.set(name, TEXTURES[name]())
  return texCache.get(name)
}

/** 每帧推进共享动画贴图（水流） */
export function tickTextures(dt) {
  const w = texCache.get('water')
  if (w) w.offset.x = (w.offset.x - dt * 0.06) % 1
}

/** 文本贴图（每次新建，由调用方负责释放） */
export function textTexture(text, opts = {}) {
  const font = opts.font || '700 64px "PingFang SC","Microsoft YaHei",sans-serif'
  const probe = document.createElement('canvas').getContext('2d')
  probe.font = font
  const pad = opts.pad ?? 16
  const w = Math.ceil(probe.measureText(text).width + pad * 2)
  const h = opts.height || 96
  const tex = canvasTex(
    w,
    h,
    (g) => {
      g.font = font
      g.textBaseline = 'middle'
      g.fillStyle = opts.color || '#ffffff'
      g.fillText(text, pad, h / 2 + 2)
    },
    false
  )
  return { tex, aspect: w / h }
}
