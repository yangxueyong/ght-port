import { post } from '@/utils/request'

export const tenantPage = (d) => post('/sys/tenant/page', d)
export const tenantSave = (d) => post('/sys/tenant/save', d)
export const tenantStatus = (d) => post('/sys/tenant/status', d)
export const tenantDelete = (id) => post('/sys/tenant/delete', { id })

export const userPage = (d) => post('/sys/user/page', d)
export const userSave = (d) => post('/sys/user/save', d)
export const userDelete = (id) => post('/sys/user/delete', { id })
export const userResetPwd = (d) => post('/sys/user/resetPwd', d)
export const userStatus = (d) => post('/sys/user/status', d)

export const rolePage = (d) => post('/sys/role/page', d)
export const roleList = () => post('/sys/role/list')
export const roleSave = (d) => post('/sys/role/save', d)
export const roleDelete = (id) => post('/sys/role/delete', { id })
export const roleMenuIds = (id) => post('/sys/role/menuIds', { id })
export const roleAssignMenus = (d) => post('/sys/role/assignMenus', d)

export const menuTree = () => post('/sys/menu/tree')
export const menuSave = (d) => post('/sys/menu/save', d)
export const menuDelete = (id) => post('/sys/menu/delete', { id })
