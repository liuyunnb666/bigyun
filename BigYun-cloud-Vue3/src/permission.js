import router from './router'
import { ElMessage } from 'element-plus'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { getToken } from '@/utils/auth'
import { isHttp, isPathMatch } from '@/utils/validate'
import { isRelogin } from '@/utils/request'
import auth from '@/plugins/auth'
import useUserStore from '@/store/modules/user'
import useLockStore from '@/store/modules/lock'
import useSettingsStore from '@/store/modules/settings'
import usePermissionStore from '@/store/modules/permission'

NProgress.configure({ showSpinner: false })

const whiteList = ['/login', '/register']

const isWhiteList = (path) => {
  return whiteList.some(pattern => isPathMatch(pattern, path))
}

const hasRouteRoleAccess = (to) => {
  return to.matched.every(record => {
    const roles = record.meta?.roles
    return !Array.isArray(roles) || roles.length === 0 || auth.hasRoleOr(roles)
  })
}

router.beforeEach(async (to) => {
  NProgress.start()
  if (getToken()) {
    to.meta.title && useSettingsStore().setTitle(to.meta.title)
    const isLock = useLockStore().isLock
    if (to.path === '/login') {
      NProgress.done()
      return { path: '/' }
    }
    if (isWhiteList(to.path)) {
      return true
    }
    if (isLock && to.path !== '/lock') {
      NProgress.done()
      return { path: '/lock' }
    }
    if (!isLock && to.path === '/lock') {
      NProgress.done()
      return { path: '/' }
    }
    if (useUserStore().roles.length === 0) {
      isRelogin.show = true
      try {
        await useUserStore().getInfo()
        isRelogin.show = false
        const accessRoutes = await usePermissionStore().generateRoutes()
        accessRoutes.forEach(route => {
          if (!isHttp(route.path)) {
            router.addRoute(route)
          }
        })
        if (!hasRouteRoleAccess(to)) {
          NProgress.done()
          return { path: '/401', replace: true }
        }
        return { ...to, replace: true }
      } catch (err) {
        await useUserStore().logOut()
        ElMessage.error(err)
        return { path: '/' }
      }
    }
    if (!hasRouteRoleAccess(to)) {
      NProgress.done()
      return { path: '/401', replace: true }
    }
    return true
  }
  if (isWhiteList(to.path)) {
    return true
  }
  NProgress.done()
  return `/login?redirect=${to.fullPath}`
})

router.afterEach(() => {
  NProgress.done()
})
