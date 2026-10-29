import * as THREE from 'three'
import { RoundedBoxGeometry } from 'three/examples/jsm/geometries/RoundedBoxGeometry.js'
import { matte, texture } from './materials.js'

/**
 * 几何体工具：所有尺寸以“底部为基准”放置（y 为底面高度），便于搭建微缩模型
 */
const unitBox = new THREE.BoxGeometry(1, 1, 1)
const geoCache = new Map()

/** 普通立方体：pos=[x, 底面y, z]，size=[宽x, 高y, 深z] */
export function box(parent, mat, pos, size) {
  const m = new THREE.Mesh(unitBox, mat)
  m.scale.set(size[0], size[1], size[2])
  m.position.set(pos[0], pos[1] + size[1] / 2, pos[2])
  parent.add(m)
  return m
}

/** 倒角立方体（硬表面细节），按尺寸缓存几何体 */
export function rbox(parent, mat, pos, size, radius = 1) {
  const r = Math.min(radius, size[0] / 2.2, size[1] / 2.2, size[2] / 2.2)
  const key = `rb|${size.map((v) => v.toFixed(1)).join(',')}|${r.toFixed(2)}`
  if (!geoCache.has(key)) geoCache.set(key, new RoundedBoxGeometry(size[0], size[1], size[2], 2, r))
  const m = new THREE.Mesh(geoCache.get(key), mat)
  m.position.set(pos[0], pos[1] + size[1] / 2, pos[2])
  parent.add(m)
  return m
}

/** 圆柱：pos=[x, 底面y, z] */
export function cyl(parent, mat, pos, dims) {
  const [rt, rb, h, seg = 20] = dims
  const key = `cy|${rt}|${rb}|${h}|${seg}`
  if (!geoCache.has(key)) geoCache.set(key, new THREE.CylinderGeometry(rt, rb, h, seg))
  const m = new THREE.Mesh(geoCache.get(key), mat)
  m.position.set(pos[0], pos[1] + h / 2, pos[2])
  parent.add(m)
  return m
}

/** 球体 */
export function sphere(parent, mat, pos, r) {
  const key = `sp|${r}`
  if (!geoCache.has(key)) geoCache.set(key, new THREE.SphereGeometry(r, 16, 12))
  const m = new THREE.Mesh(geoCache.get(key), mat)
  m.position.set(pos[0], pos[1], pos[2])
  parent.add(m)
  return m
}

/** 两点之间的杆件（拉杆、斜撑） */
export function strut(parent, mat, a, b, r = 0.6) {
  const va = new THREE.Vector3(...a)
  const vb = new THREE.Vector3(...b)
  const len = va.distanceTo(vb)
  const m = new THREE.Mesh(unitBox, mat)
  m.scale.set(r * 2, len, r * 2)
  m.position.copy(va).add(vb).multiplyScalar(0.5)
  m.quaternion.setFromUnitVectors(new THREE.Vector3(0, 1, 0), vb.clone().sub(va).normalize())
  parent.add(m)
  return m
}

/** 集装箱实例化网格：list 项 {x, y, z, sx, sy, sz, color}，y 为底面 */
export function containers(parent, list, own) {
  if (!list.length) return null
  const geo = geoCache.get('ctn') || new RoundedBoxGeometry(1, 1, 1, 1, 0.06)
  geoCache.set('ctn', geo)
  const mesh = new THREE.InstancedMesh(geo, matte('#ffffff', { map: 'rib' }), list.length)
  const m4 = new THREE.Matrix4()
  const q = new THREE.Quaternion()
  const c = new THREE.Color()
  list.forEach((it, i) => {
    m4.compose(new THREE.Vector3(it.x, it.y + it.sy / 2, it.z), q, new THREE.Vector3(it.sx, it.sy, it.sz))
    mesh.setMatrixAt(i, m4)
    mesh.setColorAt(i, c.set(it.color))
  })
  mesh.instanceMatrix.needsUpdate = true
  if (mesh.instanceColor) mesh.instanceColor.needsUpdate = true
  parent.add(mesh)
  own?.(mesh, 'instanced')
  return mesh
}

/** 平铺贴图平面（水平放置） */
export function groundPlane(parent, mat, pos, size) {
  const g = new THREE.PlaneGeometry(size[0], size[1])
  g.rotateX(-Math.PI / 2)
  const m = new THREE.Mesh(g, mat)
  m.position.set(pos[0], pos[1], pos[2])
  parent.add(m)
  return m
}

/** 伪随机（确定性，保证同一元素每次重建外观一致） */
export function rand(seed) {
  const x = Math.sin(seed * 127.1 + 311.7) * 43758.5453
  return x - Math.floor(x)
}

/** 由字符串得到稳定种子 */
export function seedOf(str) {
  let h = 7
  for (let i = 0; i < str.length; i++) h = (h * 31 + str.charCodeAt(i)) % 100000
  return h
}

/** 克隆共享贴图并设置平铺次数（地坪等需按尺寸平铺） */
export function tiledMat(name, rx, ry, own) {
  const t = texture(name).clone()
  t.needsUpdate = true
  t.repeat.set(rx, ry)
  const m = new THREE.MeshLambertMaterial({ map: t })
  own?.(t)
  own?.(m)
  return m
}
