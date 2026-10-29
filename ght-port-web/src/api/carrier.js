import { post } from '@/utils/request'

export const carrierPage = (d) => post('/biz/carrier/page', d)
export const carrierOptions = (keyword) => post('/biz/carrier/options', { keyword })
export const carrierSave = (d) => post('/biz/carrier/save', d)
export const carrierDelete = (id) => post('/biz/carrier/delete', { id })
export const carrierDetail = (id) => post('/biz/carrier/detail', { id })
export const carrierMine = () => post('/biz/carrier/mine')

export const vehiclePage = (d) => post('/biz/vehicle/page', d)
export const vehicleOptions = (carrierId, keyword) => post('/biz/vehicle/options', { parentId: carrierId, keyword })
export const vehicleSave = (d) => post('/biz/vehicle/save', d)
export const vehicleDelete = (id) => post('/biz/vehicle/delete', { id })

export const tripPage = (d) => post('/biz/trip/page', d)
export const tripComplete = (d) => post('/biz/trip/complete', d)
