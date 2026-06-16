import useUserStore from '@/store/modules/user'

function authPermission(permission) {
  const allPermission = '*:*:*'
  const permissions = useUserStore().permissions
  if (permission && permission.length > 0) {
    return permissions.some(value => {
      return allPermission === value || value === permission
    })
  }
  return false
}

function authRole(role) {
  const superAdmin = 'admin'
  const roles = useUserStore().roles
  if (role && role.length > 0) {
    return roles.some(value => {
      return superAdmin === value || value === role
    })
  }
  return false
}

export default {
  hasPermi(permission) {
    return authPermission(permission)
  },
  hasPermiOr(permissions) {
    return permissions.some(item => {
      return authPermission(item)
    })
  },
  hasPermiAnd(permissions) {
    return permissions.every(item => {
      return authPermission(item)
    })
  },
  hasRole(role) {
    return authRole(role)
  },
  hasRoleOr(roles) {
    return roles.some(item => {
      return authRole(item)
    })
  },
  hasRoleAnd(roles) {
    return roles.every(item => {
      return authRole(item)
    })
  }
}
