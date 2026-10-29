/** 前端字典：与后端英文状态码一一对应 */
export const BERTH_STATUS = {
  IDLE: { label: '未作业', color: '#12b886', bg: '#e6f8f1' },
  WORKING: { label: '正在作业', color: '#f59f00', bg: '#fff4de' },
  MAINTENANCE: { label: '维修中', color: '#e5484d', bg: '#fdeceb' }
}

export const VEHICLE_STATUS = {
  IDLE: { label: '空闲', color: '#12b886', bg: '#e6f8f1' },
  IN_TRANSIT: { label: '运输中', color: '#1f7ae0', bg: '#e8f1fd' }
}

export const TRIP_STATUS = {
  IN_TRANSIT: { label: '运输中', color: '#1f7ae0', bg: '#e8f1fd' },
  COMPLETED: { label: '已完成', color: '#12b886', bg: '#e6f8f1' }
}

export const DISPATCH_STATUS = {
  WAITING: { label: '待装载', color: '#f59f00', bg: '#fff4de' },
  DEPARTED: { label: '已出发', color: '#1f7ae0', bg: '#e8f1fd' },
  CANCELLED: { label: '已取消', color: '#a9b4c6', bg: '#f1f3f7' }
}

export const PASS_STATUS = {
  UNUSED: { label: '未核销', color: '#f59f00', bg: '#fff4de' },
  VERIFIED: { label: '已核销', color: '#12b886', bg: '#e6f8f1' }
}

export const ENABLE_STATUS = {
  ENABLED: { label: '启用', color: '#12b886', bg: '#e6f8f1' },
  DISABLED: { label: '停用', color: '#a9b4c6', bg: '#f1f3f7' }
}

export const VEHICLE_TYPES = [
  { value: 'LIGHT_VAN', label: '轻型厢式货车', length: 4.2 },
  { value: 'MEDIUM_BOX', label: '中型厢式货车', length: 6.8 },
  { value: 'HEAVY_CARGO', label: '重型栏板货车', length: 9.6 },
  { value: 'SEMI_TRAILER', label: '半挂牵引车', length: 17.5 },
  { value: 'DUMP', label: '自卸车', length: 8.6 },
  { value: 'TANKER', label: '罐式车', length: 11 },
  { value: 'FLATBED', label: '平板车', length: 13 },
  { value: 'CONTAINER', label: '集装箱车', length: 12.5 }
]

export const SHIP_TYPES = [
  { value: 'BULK', label: '散货船' },
  { value: 'CONTAINER', label: '集装箱船' },
  { value: 'TANKER', label: '油船' },
  { value: 'GENERAL', label: '杂货船' },
  { value: 'RORO', label: '滚装船' },
  { value: 'BARGE', label: '驳船' }
]

export const USER_TYPES = [
  { value: 'ADMIN', label: '管理员' },
  { value: 'STAFF', label: '港口员工' },
  { value: 'CARRIER', label: '承运商' },
  { value: 'DRIVER', label: '司机' },
  { value: 'GUARD', label: '门卫' }
]

export const PALETTE_COLORS = ['#e67e22', '#2980b9', '#27ae60', '#c0392b', '#8e44ad', '#16a085', '#f1c40f', '#34495e', '#e84393', '#95a5a6']

export const labelOf = (list, v) => (list.find((i) => i.value === v) || {}).label || v || '-'

export const SHIP_LOAD_STATUS = {
  WAITING: { label: '待装船', color: '#f59f00', bg: '#fff4de' },
  LOADED: { label: '已装船', color: '#12b886', bg: '#e6f8f1' },
  CANCELLED: { label: '已取消', color: '#a9b4c6', bg: '#f1f3f7' }
}

export const CARGO_STATUS = {
  IN_PROGRESS: { label: '作业中', color: '#f59f00', bg: '#fff4de' },
  COMPLETED: { label: '已完成', color: '#12b886', bg: '#e6f8f1' }
}

/** 货物作业方向 */
export const CARGO_DIRECTION = {
  UNLOAD: { label: '到货', color: '#1f7ae0', bg: '#e8f1fd' },
  LOAD: { label: '装货', color: '#7c3aed', bg: '#f1ebfe' }
}

/** 货物形态 */
export const PACK_TYPES = [
  { value: 'BULK', label: '散货' },
  { value: 'CONTAINER', label: '集装箱' }
]
