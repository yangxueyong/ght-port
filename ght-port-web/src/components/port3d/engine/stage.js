import * as THREE from 'three'
import { COLORS, LEVEL } from './constants.js'
import { matte } from './materials.js'
import { box, tiledMat } from './geom.js'
import { makeFlatText } from './label.js'

/** 主题灯光参数 */
const THEMES = {
  DAY: { bg: '#e9eff7', sky: '#ffffff', ground: '#cfdcf0', hemi: 1.55, sun: '#ffffff', sunI: 2.1 },
  DUSK: { bg: '#f3e9e4', sky: '#fff1e2', ground: '#d9cbe0', hemi: 1.35, sun: '#ffd2a6', sunI: 2.0 },
  NIGHT: { bg: '#d9e1ef', sky: '#dbe6ff', ground: '#9fb0cf', hemi: 1.05, sun: '#c7d6ff', sunI: 1.3 }
}

/**
 * 舞台：背景、灯光、沙盘底座、远景网格
 */
export function createStage(scene) {
  const hemi = new THREE.HemisphereLight('#ffffff', '#cfdcf0', 1.5)
  scene.add(hemi)
  const sun = new THREE.DirectionalLight('#ffffff', 2)
  sun.castShadow = true
  sun.shadow.mapSize.set(4096, 4096)
  sun.shadow.bias = -0.0005
  sun.shadow.normalBias = 0.8
  scene.add(sun)
  scene.add(sun.target)
  const floor = new THREE.GridHelper(8000, 100, '#c9d6ea', '#dce4f0')
  floor.position.y = LEVEL.plinthBottom - 0.5
  scene.add(floor)
  const plinth = new THREE.Group()
  scene.add(plinth)
  let own = []

  /** 按画布尺寸重建底座与阴影范围 */
  function resize(W, H) {
    own.forEach((o) => o.dispose?.())
    own = []
    const reg = (o) => own.push(o)
    plinth.clear()
    const pad = 40
    const PW = W + pad * 2
    const PH = H + pad * 2
    const depth = -LEVEL.plinthBottom
    const side = matte(COLORS.plinthSide)
    const top = tiledMat('grid', PW / 40, PH / 40, reg)
    const base = box(plinth, side, [0, LEVEL.plinthBottom, 0], [PW, depth, PH])
    base.material = [side, side, top, side, side, side]
    base.receiveShadow = true
    // 顶边港湾蓝描边
    const edge = matte(COLORS.blue)
    box(plinth, edge, [0, -1.2, PH / 2], [PW + 1, 1.4, 1])
    box(plinth, edge, [0, -1.2, -PH / 2], [PW + 1, 1.4, 1])
    box(plinth, edge, [PW / 2, -1.2, 0], [1, 1.4, PH + 1])
    box(plinth, edge, [-PW / 2, -1.2, 0], [1, 1.4, PH + 1])
    // 底座铭牌
    const txt = makeFlatText({ text: '港慧通 · 数字孪生港口', color: '#2f5bd3', size: 9 }, reg)
    txt.rotation.x = Math.PI / 2
    txt.position.set(-PW / 2 + 120, LEVEL.plinthBottom / 2 - 1, PH / 2 + 0.6)
    plinth.add(txt)
    const R = Math.max(W, H) * 0.62
    sun.position.set(-W * 0.3, Math.max(W, H) * 0.75, -H * 0.55)
    Object.assign(sun.shadow.camera, { left: -R, right: R, top: R, bottom: -R, near: 10, far: Math.max(W, H) * 3 })
    sun.shadow.camera.updateProjectionMatrix()
  }

  /** 切换主题 */
  function setTheme(name) {
    const t = THEMES[name] || THEMES.DAY
    scene.background = new THREE.Color(t.bg)
    hemi.color.set(t.sky)
    hemi.groundColor.set(t.ground)
    hemi.intensity = t.hemi
    sun.color.set(t.sun)
    sun.intensity = t.sunI
  }

  return { resize, setTheme, dispose: () => own.forEach((o) => o.dispose?.()) }
}
