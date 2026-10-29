import { buildLand, buildRoad, buildRiver } from './terrain.js'
import { buildBerth } from './berth.js'
import { buildShip } from './ship.js'
import { buildMountain } from './mountain.js'
import { buildYard, buildWarehouse, buildBuilding, buildTank, buildGate, buildCrane, buildTree, buildLabel } from './facility.js'

/** 元素类型 -> 3D 建模函数 */
export const BUILDERS = {
  RIVER: buildRiver,
  LAND: buildLand,
  ROAD: buildRoad,
  BERTH: buildBerth,
  CRANE: buildCrane,
  YARD: buildYard,
  WAREHOUSE: buildWarehouse,
  TANK: buildTank,
  GATE: buildGate,
  BUILDING: buildBuilding,
  TREE: buildTree,
  SHIP: buildShip,
  LABEL: buildLabel,
  MOUNTAIN: buildMountain
}

/** 构建元素模型（局部坐标，原点为元素中心） */
export function buildElement(el, ctx) {
  const fn = BUILDERS[el.elementType] || buildLabel
  return fn(el, ctx)
}
