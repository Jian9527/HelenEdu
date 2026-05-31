import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(uni.getStorageSync('token') || '')
  const userInfo = ref(JSON.parse(uni.getStorageSync('userInfo') || '{}'))

  const setLoginInfo = (loginData) => {
    token.value = loginData.token
    userInfo.value = {
      id: loginData.userId,
      name: loginData.name,
      role: loginData.role,
      avatarUrl: loginData.avatarUrl,
      subject: loginData.subject || ''
    }
    uni.setStorageSync('token', loginData.token)
    uni.setStorageSync('userInfo', JSON.stringify(userInfo.value))
  }

  const updateUserInfo = (info) => {
    userInfo.value = { ...userInfo.value, ...info }
    uni.setStorageSync('userInfo', JSON.stringify(userInfo.value))
  }

  const logout = () => {
    token.value = ''
    userInfo.value = {}
    uni.removeStorageSync('token')
    uni.removeStorageSync('userInfo')
    uni.reLaunch({ url: '/pages/login/index' })
  }

  const isLoggedIn = () => !!token.value

  const getRoleName = () => {
    const roleMap = { 1: '学生', 2: '教师', 3: '管理员' }
    return roleMap[userInfo.value.role] || ''
  }

  // 根据角色获取首页路径
  const getHomePage = () => {
    const role = userInfo.value.role
    switch (role) {
      case 1: return '/pages/student/homework-list'
      case 2: return '/pages/teacher/class-list'
      case 3: return '/pages/admin/dashboard'
      default: return '/pages/login/index'
    }
  }

  return {
    token,
    userInfo,
    setLoginInfo,
    updateUserInfo,
    logout,
    isLoggedIn,
    getRoleName,
    getHomePage
  }
})
