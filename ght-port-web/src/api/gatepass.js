import { post } from '@/utils/request'

export const gatePassPage = (d) => post('/biz/gate-pass/page', d)
export const gatePassMine = (d) => post('/biz/gate-pass/mine', d)
export const gatePassPreview = (token) => post('/biz/gate-pass/preview', { token }, { silent: true })
export const gatePassVerify = (token) => post('/biz/gate-pass/verify', { token }, { silent: true })
