import useUserStore from '@/store/modules/user'

export function checkPermi(value) {
  if (value && value instanceof Array && value.length > 0) {
    const permissions = useUserStore().permissions
    const allPermission = '*:*:*'

    return permissions.some(permission => {
      return allPermission === permission || value.includes(permission)
    })
  }
  console.error('need permissions, for example: checkPermi="[\'system:user:add\',\'system:user:edit\']"')
  return false
}

export function checkRole(value) {
  if (value && value instanceof Array && value.length > 0) {
    const roles = useUserStore().roles
    const superAdmin = 'admin'

    return roles.some(role => {
      return superAdmin === role || value.includes(role)
    })
  }
  console.error('need roles, for example: checkRole="[\'admin\',\'editor\']"')
  return false
}
