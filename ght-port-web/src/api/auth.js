import { post } from '@/utils/request'

export const login = (data) => post('/auth/login', data)
export const userInfo = () => post('/auth/info')
export const changePassword = (data) => post('/auth/changePassword', data)
export const tenantOptions = () => post('/open/tenants')
export const publicGatePass = (token) => post('/open/gatePass/view', { token }, { silent: true })
