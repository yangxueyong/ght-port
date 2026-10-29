/** 元素注册表：类型 -> 名称、默认尺寸、默认外观属性、分组（3D 建模见 components/port3d/engine/builders） */
export const ELEMENT_TYPES = {
  RIVER: { name: '江河', w: 800, h: 260, cfg: { label: '江河', riverWidth: 120, points: [{ u: 0, v: 0.55 }, { u: 0.3, v: 0.28 }, { u: 0.65, v: 0.72 }, { u: 1, v: 0.45 }] }, group: '地形' },
  LAND: { name: '陆地', w: 600, h: 400, cfg: { variant: 'concrete' }, group: '地形' },
  ROAD: { name: '道路', w: 400, h: 50, cfg: {}, group: '地形' },
  MOUNTAIN: { name: '山体', w: 320, h: 240, cfg: { peak: 140, seed: 1.3 }, group: '地形' },
  BERTH: { name: '泊位', w: 190, h: 86, cfg: {}, group: '港口设施' },
  CRANE: { name: '门机', w: 36, h: 104, cfg: {}, group: '港口设施' },
  YARD: { name: '堆场', w: 280, h: 150, cfg: { label: '堆场', containers: true }, group: '港口设施' },
  WAREHOUSE: { name: '仓库', w: 260, h: 140, cfg: { label: '仓库' }, group: '港口设施' },
  TANK: { name: '储罐', w: 80, h: 80, cfg: {}, group: '港口设施' },
  GATE: { name: '闸口', w: 110, h: 64, cfg: { label: '闸口' }, group: '港口设施' },
  BUILDING: { name: '办公楼', w: 220, h: 130, cfg: { label: '办公楼' }, group: '配套' },
  TREE: { name: '绿植', w: 56, h: 56, cfg: {}, group: '配套' },
  SHIP: { name: '船舶', w: 240, h: 60, cfg: { color: '#1e3a8a', label: '' }, group: '配套' },
  LABEL: { name: '文字', w: 220, h: 30, cfg: { text: '文字标注', fontSize: 18, color: '#0f2a6b' }, group: '配套' }
}

/** 将后端元素转换为前端编辑模型 */
export function toModel(e, idx) {
  let cfg = {}
  try {
    cfg = e.props ? JSON.parse(e.props) : {}
  } catch (err) {
    cfg = {}
  }
  return {
    uid: e.id || 'n' + Date.now() + '_' + idx,
    elementType: e.elementType,
    posX: Number(e.posX),
    posY: Number(e.posY),
    width: Number(e.width),
    height: Number(e.height),
    rotation: Number(e.rotation || 0),
    layerIndex: e.layerIndex ?? idx,
    cfg,
    berthId: e.berthId || null,
    berthName: e.berthName || '',
    berthCode: e.berthCode || ''
  }
}

/** 将前端编辑模型转换为后端保存入参 */
export function toPayload(m, idx) {
  return {
    elementType: m.elementType,
    posX: round(m.posX),
    posY: round(m.posY),
    width: round(m.width),
    height: round(m.height),
    rotation: round(m.rotation || 0),
    layerIndex: idx,
    props: JSON.stringify(m.cfg || {}),
    berthId: m.berthId || null,
    berthCode: m.berthCode || null,
    berthName: m.berthName || null
  }
}

const round = (v) => Math.round(Number(v) * 100) / 100
