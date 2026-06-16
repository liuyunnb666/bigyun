import { createWebHistory, createRouter } from 'vue-router'
import Layout from '@/layout'

export const constantRoutes = [
  {
    path: '/redirect',
    component: Layout,
    hidden: true,
    children: [
      { path: '/redirect/:path(.*)', component: () => import('@/views/redirect/index.vue') }
    ]
  },
  { path: '/login', component: () => import('@/views/login'), hidden: true },
  { path: '/register', component: () => import('@/views/register'), hidden: true },
  {
    path: '',
    component: Layout,
    redirect: '/index',
    children: [
      { path: '/index', component: () => import('@/views/index'), name: 'Index', meta: { title: '首页', icon: 'dashboard', affix: true } }
    ]
  },
  {
    path: '/pay',
    component: Layout,
    redirect: '/pay/demo',
    alwaysShow: true,
    meta: { title: '支付骨架', icon: 'money' },
    children: [
      { path: 'demo', component: () => import('@/views/pay/demo/index.vue'), name: 'PayDemo', meta: { title: '支付示例', icon: 'money' } }
    ]
  },
  {
    path: '/demo',
    component: Layout,
    redirect: '/demo/index',
    alwaysShow: true,
    meta: { title: '示例模块', icon: 'example' },
    children: [
      { path: 'index', component: () => import('@/views/demo/index.vue'), name: 'DemoIndex', meta: { title: '基础示例', icon: 'example' } }
    ]
  },
  { path: '/lock', component: () => import('@/views/lock'), hidden: true, meta: { title: '锁定屏幕' } },
  {
    path: '/user',
    component: Layout,
    hidden: true,
    redirect: 'noredirect',
    children: [
      { path: 'profile/:activeTab?', component: () => import('@/views/system/user/profile/index'), name: 'Profile', meta: { title: '个人中心', icon: 'user' } }
    ]
  },
  { path: '/401', component: () => import('@/views/error/401'), hidden: true },
  { path: '/:pathMatch(.*)*', component: () => import('@/views/error/404'), hidden: true }
]

export const dynamicRoutes = [
  {
    path: '/provider',
    component: Layout,
    hidden: true,
    redirect: '/provider/config',
    permissions: ['provider:config:list'],
    children: [
      { path: 'config', component: () => import('@/views/provider/config/index.vue'), name: 'ProviderConfig', meta: { title: 'Provider 配置', icon: 'component' } },
      { path: 'capability', component: () => import('@/views/provider/capability/index.vue'), name: 'ProviderCapabilityCenter', meta: { title: '能力中心', icon: 'tree-table' } },
      { path: 'modelCatalog', component: () => import('@/views/provider/modelCatalog/index.vue'), name: 'ProviderModelCatalog', meta: { title: '模型目录', icon: 'list' } },
      { path: 'template', component: () => import('@/views/provider/template/index.vue'), name: 'ProviderApiTemplate', meta: { title: 'API 模板', icon: 'code' } }
    ]
  },
  {
    path: '/system/user-auth',
    component: Layout,
    hidden: true,
    permissions: ['system:user:edit'],
    children: [
      { path: 'role/:userId(\\d+)', component: () => import('@/views/system/user/authRole'), name: 'AuthRole', meta: { title: '分配角色', activeMenu: '/system/user' } }
    ]
  },
  {
    path: '/system/role-auth',
    component: Layout,
    hidden: true,
    permissions: ['system:role:edit'],
    children: [
      { path: 'user/:roleId(\\d+)', component: () => import('@/views/system/role/authUser'), name: 'AuthUser', meta: { title: '分配用户', activeMenu: '/system/role' } }
    ]
  },
  {
    path: '/system/dict-data',
    component: Layout,
    hidden: true,
    permissions: ['system:dict:list'],
    children: [
      { path: 'index/:dictId(\\d+)', component: () => import('@/views/system/dict/data'), name: 'Data', meta: { title: '字典数据', activeMenu: '/system/dict' } }
    ]
  },
  {
    path: '/monitor/job-log',
    component: Layout,
    hidden: true,
    permissions: ['monitor:job:list'],
    children: [
      { path: 'index/:jobId(\\d+)', component: () => import('@/views/monitor/job/log'), name: 'JobLog', meta: { title: '调度日志', activeMenu: '/monitor/job' } }
    ]
  },
  {
    path: '/tool/gen-edit',
    component: Layout,
    hidden: true,
    permissions: ['tool:gen:edit'],
    children: [
      { path: 'index/:tableId(\\d+)', component: () => import('@/views/tool/gen/editTable'), name: 'GenEdit', meta: { title: '修改生成配置', activeMenu: '/tool/gen' } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes: constantRoutes,
  scrollBehavior(to, from, savedPosition) {
    return savedPosition || { top: 0 }
  }
})

export default router