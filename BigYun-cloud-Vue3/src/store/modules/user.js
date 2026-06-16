import router from '@/router'
import cache from '@/plugins/cache'
import { ElMessageBox } from 'element-plus'
import { loginNew, logout, getInfo } from '@/api/login'
import { getToken, setToken, removeToken } from '@/utils/auth'
import { isEmpty } from '@/utils/validate'
import defAva from '@/assets/images/profile.jpg'

const useUserStore = defineStore(
  'user',
  {
    state: () => ({
      token: getToken(),
      id: '',
      name: '',
      nickName: '',
      avatar: '',
      roles: [],
      roleNames: [],
      permissions: []
    }),
    actions: {
      // 统一登录入口，支持账号、手机号、邮箱和扫码授权码。
      login(userInfo) {
        const { type, username, password, code, uuid, phone, email, grantCode, sid } = userInfo
        const loginReq = {
          type: type || '1',
          userName: username ? username.trim() : undefined,
          password,
          code,
          uuid,
          phone,
          email,
          grantCode,
          sid
        }

        return new Promise((resolve, reject) => {
          loginNew(loginReq).then(res => {
            const data = res.data
            setToken(data.access_token)
            this.token = data.access_token
            resolve()
          }).catch(error => {
            reject(error)
          })
        })
      },
      // 获取当前用户信息。
      getInfo() {
        return new Promise((resolve, reject) => {
          getInfo().then(res => {
            const user = res.user
            const avatar = isEmpty(user.avatar) ? defAva : user.avatar
            if (res.roles && res.roles.length > 0) {
              this.roles = res.roles
              this.permissions = res.permissions
            } else {
              this.roles = ['ROLE_DEFAULT']
            }
            this.roleNames = (user.roles || []).map(role => role.roleName).filter(Boolean)
            this.id = user.userId
            this.name = user.userName
            this.nickName = user.nickName
            this.avatar = avatar
            cache.session.set('pwrChrtype', res.pwdChrtype)
            if (res.isDefaultModifyPwd) {
              ElMessageBox.confirm('您的密码还是初始密码，请及时修改密码。', '安全提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
              }).then(() => {
                router.push({ name: 'Profile', params: { activeTab: 'resetPwd' } })
              }).catch(() => {})
            }
            if (!res.isDefaultModifyPwd && res.isPasswordExpired) {
              ElMessageBox.confirm('您的密码已过期，请尽快修改密码。', '安全提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
              }).then(() => {
                router.push({ name: 'Profile', params: { activeTab: 'resetPwd' } })
              }).catch(() => {})
            }
            resolve(res)
          }).catch(error => {
            reject(error)
          })
        })
      },
      // 退出系统。
      logOut() {
        return new Promise((resolve, reject) => {
          logout(this.token).then(() => {
            this.token = ''
            this.roles = []
            this.permissions = []
            removeToken()
            resolve()
          }).catch(error => {
            reject(error)
          })
        })
      }
    }
  })

export default useUserStore
