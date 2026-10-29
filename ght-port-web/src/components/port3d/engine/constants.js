/**
 * 3D 港口场景常量：配色、标高、状态色
 * 整体采用“清爽亮丽”的白 + 港湾蓝微缩模型风格，所有材质均为哑光（无高光反射）
 */
export const COLORS = {
  plinth: '#f4f7fb',
  plinthSide: '#dfe6f0',
  grid: '#d6e0ee',
  white: '#f8fafc',
  offWhite: '#eef2f7',
  deck: '#e6ecf4',
  gray: '#cbd5e1',
  steel: '#94a3b8',
  dark: '#334155',
  rubber: '#2b3445',
  blue: '#2f5bd3',
  blueMid: '#4f7fe8',
  blueLight: '#8fb1f3',
  bluePale: '#cddcfb',
  navy: '#1e3a8a',
  glass: '#5b8def',
  water: '#4a82de',
  waterEdge: '#3a6fca',
  bank: '#f1f4f9',
  bankLine: '#a9bfe6',
  grass: '#bfe6cb',
  sand: '#efe3c3',
  road: '#d3dbe6',
  roadLine: '#ffffff',
  tree: '#7fcc9d',
  treeDark: '#58b483',
  trunk: '#b9906c',
  red: '#e5484d',
  orange: '#f59e0b',
  yellow: '#facc15',
  heap: '#c8b48a',
  coal: '#5b6474'
}

/** 泊位状态色 */
export const STATUS_COLORS = {
  IDLE: '#22c55e',
  WORKING: '#f59e0b',
  MAINTENANCE: '#ef4444'
}

/** 泊位状态文字 */
export const STATUS_LABELS = {
  IDLE: '未作业',
  WORKING: '正在作业',
  MAINTENANCE: '维修中'
}

/** 集装箱配色（蓝白系） */
export const CONTAINER_COLORS = ['#2f5bd3', '#4f7fe8', '#8fb1f3', '#cddcfb', '#f8fafc', '#1e3a8a', '#6b93ec']

/** 标高（世界单位 = 画布像素） */
export const LEVEL = {
  plinthBottom: -18,
  land: 3,
  road: 3.4,
  water: 3.6,
  bank: 5.2,
  deck: 7.5
}

/** 选中/手柄颜色 */
export const UI_COLORS = {
  select: '#2563eb',
  handle: '#ffffff',
  point: '#2563eb'
}
