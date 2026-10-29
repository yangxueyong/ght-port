import { post } from '@/utils/request'

export const berthList = (d) => post('/biz/berth/list', d)
export const berthDetail = (id) => post('/biz/berth/detail', { id })
export const berthWorkTime = (d) => post('/biz/berth/workTime', d)
export const berthStatus = (d) => post('/biz/berth/status', d)
export const berthFinish = (d) => post('/biz/berth/finish', d)
export const berthArrive = (d) => post('/biz/berth/arrive', d)

export const dispatchAssign = (d) => post('/biz/dispatch/assign', d)
export const dispatchList = (d) => post('/biz/dispatch/list', d)
export const dispatchDepart = (d) => post('/biz/dispatch/depart', d)
export const dispatchCancel = (id) => post('/biz/dispatch/cancel', { id })

export const cargoTypeList = (keyword) => post('/biz/cargo-type/list', { keyword })
export const cargoTypeSave = (d) => post('/biz/cargo-type/save', d)
export const cargoTypeDelete = (id) => post('/biz/cargo-type/delete', { id })
export const berthLoad = (d) => post('/biz/berth/load', d)
export const cargoPage = (d) => post('/biz/berth/cargo/page', d)
export const cargoDetail = (id) => post('/biz/berth/cargo/detail', { id })

export const shipLoadAssign = (d) => post('/biz/ship-load/assign', d)
export const shipLoadList = (d) => post('/biz/ship-load/list', d)
export const shipLoadPage = (d) => post('/biz/ship-load/page', d)
export const shipLoadConfirm = (d) => post('/biz/ship-load/confirm', d)
export const shipLoadCancel = (id) => post('/biz/ship-load/cancel', { id })
