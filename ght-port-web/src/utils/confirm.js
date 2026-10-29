import { ElMessageBox } from 'element-plus'

/**
 * 调用需要“二次确认”的接口：后端返回 needConfirm=true 时弹窗提示，用户确认后携带 force=true 重新提交
 * @param {Function} apiFn 接口函数，参数为请求体
 * @param {Object} payload 请求体
 * @returns {Promise<Object|null>} 完成时返回接口结果，用户取消返回 null
 */
export async function withConfirm(apiFn, payload) {
  const first = await apiFn({ ...payload, force: false })
  if (!first || !first.needConfirm) {
    return first
  }
  try {
    await ElMessageBox.confirm(first.message, '正在作业', {
      type: 'warning',
      confirmButtonText: '继续操作',
      cancelButtonText: '再想想',
      customClass: 'ght-confirm'
    })
  } catch (e) {
    return null
  }
  return apiFn({ ...payload, force: true })
}
