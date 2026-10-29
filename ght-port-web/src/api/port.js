import { post } from '@/utils/request'

export const portList = () => post('/biz/port/list')
export const portDetail = (id) => post('/biz/port/detail', { id })
export const portSave = (d) => post('/biz/port/save', d)
export const portDelete = (id) => post('/biz/port/delete', { id })
export const portSaveMap = (d) => post('/biz/port/saveMap', d)
export const dashboardOverview = () => post('/biz/dashboard/overview')
export const cockpitOverview = (portId) => post('/biz/cockpit/overview', { portId })
