import { post } from '@/utils/request'

export const shipperPage = (d) => post('/biz/shipping-company/page', d)
export const shipperSave = (d) => post('/biz/shipping-company/save', d)
export const shipperDetail = (id) => post('/biz/shipping-company/detail', { id })
export const shipComplete = (d) => post('/biz/shipping-company/ship/complete', d)
export const shipperDelete = (id) => post('/biz/shipping-company/delete', { id })
export const shipOptions = (keyword, companyId) => post('/biz/ship/options', { keyword, parentId: companyId })
export const shipSave = (d) => post('/biz/ship/save', d)
export const shipDelete = (id) => post('/biz/ship/delete', { id })
