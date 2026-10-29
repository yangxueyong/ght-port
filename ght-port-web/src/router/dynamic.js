const views = import.meta.glob('../views/**/*.vue')

/** 将后端菜单树（DIR/MENU）转换为扁平的子路由 */
export function buildMenuRoutes(menus, list = []) {
  for (const m of menus || []) {
    if (m.menuType === 'MENU' && m.component) {
      const loader = views[`../views/${m.component}.vue`]
      if (loader) {
        list.push({
          path: m.path.replace(/^\//, ''),
          name: 'M_' + m.id,
          component: loader,
          meta: { title: m.menuName, icon: m.icon, perms: m.perms }
        })
      } else {
        console.warn('[路由] 未找到组件：', m.component)
      }
    }
    if (m.children && m.children.length) {
      buildMenuRoutes(m.children, list)
    }
  }
  return list
}

/** 找到第一个可访问的菜单路径作为首页 */
export function firstMenuPath(menus) {
  for (const m of menus || []) {
    if (m.menuType === 'MENU' && m.path) return m.path
    const child = firstMenuPath(m.children)
    if (child) return child
  }
  return null
}
