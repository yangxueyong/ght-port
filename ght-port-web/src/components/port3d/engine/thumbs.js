import * as THREE from 'three'
import { buildElement } from './builders/index.js'
import { LEVEL, COLORS } from './constants.js'
import { matte, texture } from './materials.js'
import { box } from './geom.js'

/**
 * 3D 缩略图：共用一个离屏渲染器，把单个元素渲染成图片（元素库、泊位卡片使用）
 */
let renderer = null
const cache = new Map()

function getRenderer() {
  if (!renderer) {
    renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true, preserveDrawingBuffer: true })
    renderer.setPixelRatio(2)
    renderer.outputColorSpace = THREE.SRGBColorSpace
    renderer.shadowMap.enabled = true
    renderer.shadowMap.type = THREE.PCFSoftShadowMap
  }
  return renderer
}

/** 水面底板（泊位缩略图用） */
function waterBase(g, w, h) {
  const water = new THREE.Mesh(new THREE.PlaneGeometry(w * 1.6, h * 1.4).rotateX(-Math.PI / 2), matte('#ffffff', { map: 'water' }))
  texture('water')
  water.position.set(0, LEVEL.water, -h * 0.75)
  water.receiveShadow = true
  g.add(water)
  box(g, matte(COLORS.offWhite), [0, 0, h * 0.2], [w * 1.6, LEVEL.land, h * 1.1]).receiveShadow = true
  return water.geometry
}

/**
 * 渲染元素缩略图
 * @param {Object} el 元素模型（elementType, width, height, cfg, berthCode, berthName）
 * @param {Object} opts {berth, w, h 像素, water 是否带水面, key 缓存键}
 * @returns {string} dataURL
 */
export function renderThumb(el, opts = {}) {
  const key = opts.key || JSON.stringify([el.elementType, el.width, el.height, el.cfg, el.berthCode, el.berthName, opts.berth?.status, opts.w, opts.h])
  if (cache.has(key)) return cache.get(key)
  const pw = opts.w || 120
  const ph = opts.h || 90
  const r = getRenderer()
  r.setSize(pw, ph, false)
  const scene = new THREE.Scene()
  scene.add(new THREE.HemisphereLight('#ffffff', '#cfdcf0', 1.6))
  const sun = new THREE.DirectionalLight('#ffffff', 2.1)
  const R = Math.max(el.width, el.height) * 1.2
  sun.position.set(-R * 0.5, R * 1.2, -R * 0.4)
  sun.castShadow = true
  sun.shadow.mapSize.set(1024, 1024)
  Object.assign(sun.shadow.camera, { left: -R, right: R, top: R, bottom: -R, near: 1, far: R * 4 })
  scene.add(sun)
  const own = []
  const content = buildElement(el, { own: (o) => own.push(o), berth: opts.berth, rivers: [], W: el.width, H: el.height, noTag: true })
  content.traverse((o) => {
    if (o.isMesh) {
      o.castShadow = true
      o.receiveShadow = true
    }
  })
  scene.add(content)
  if (opts.water) own.push(waterBase(scene, el.width, el.height))
  const bb = new THREE.Box3().setFromObject(content)
  const size = bb.getSize(new THREE.Vector3())
  const center = bb.getCenter(new THREE.Vector3())
  const cam = new THREE.PerspectiveCamera(30, pw / ph, 1, 10000)
  const radius = size.length() / 2
  const d = (radius / Math.sin((30 * Math.PI) / 360)) * (opts.zoom || 0.82)
  cam.position.copy(center).add(new THREE.Vector3().setFromSpherical(new THREE.Spherical(d, 0.95, -0.6)))
  cam.lookAt(center)
  r.setClearColor(0x000000, 0)
  r.render(scene, cam)
  const url = r.domElement.toDataURL('image/png')
  own.forEach((o) => (o.isInstancedMesh ? o.dispose() : o.dispose?.()))
  cache.set(key, url)
  return url
}
