import * as THREE from 'three'
import { COLORS } from '../constants.js'
import { matte, glow } from '../materials.js'
import { box, rbox, cyl } from '../geom.js'

/**
 * 微缩集卡（车头朝 +x），长约 22、宽 6
 * @param {string} color 车厢/箱体颜色
 * @param {boolean} withBox 是否载箱
 */
export function truck(color = COLORS.blue, withBox = true) {
  const g = new THREE.Group()
  // 底盘
  box(g, matte(COLORS.dark), [0, 1.2, 0], [21, 1, 5])
  // 车头
  rbox(g, matte(COLORS.white), [8.5, 2, 0], [5, 5.2, 5.6], 0.9)
  box(g, matte(COLORS.navy), [10.9, 4.4, 0], [0.4, 1.9, 4.6])
  box(g, glow('#fff7d6'), [11.05, 2.6, 1.9], [0.2, 0.8, 1])
  box(g, glow('#fff7d6'), [11.05, 2.6, -1.9], [0.2, 0.8, 1])
  // 车厢 / 集装箱
  if (withBox) rbox(g, matte(color), [-2.5, 2.2, 0], [15, 5.6, 5.4], 0.5)
  else box(g, matte(COLORS.steel), [-2.5, 2.2, 0], [15, 0.6, 5.2])
  // 车轮
  const wheel = matte(COLORS.rubber)
  ;[[-8, 1], [-5, 1], [8, 1]].forEach(([x]) => {
    ;[2.7, -2.7].forEach((z) => {
      const w = cyl(g, wheel, [x, 0, z], [1.4, 1.4, 1, 12])
      w.rotation.x = Math.PI / 2
      w.position.y = 1.4
    })
  })
  return g
}

/** 无人 AGV 平板车（载一个集装箱） */
export function agv(color = COLORS.blueMid) {
  const g = new THREE.Group()
  rbox(g, matte(COLORS.yellow), [0, 0.4, 0], [16, 2, 6], 0.6)
  box(g, glow('#22c55e'), [8.05, 1.2, 0], [0.2, 0.6, 3])
  rbox(g, matte(color), [0, 2.4, 0], [14, 5, 5.2], 0.4)
  return g
}
