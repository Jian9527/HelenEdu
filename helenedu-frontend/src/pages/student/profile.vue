<template>
  <view class="container">
    <view class="profile-header">
      <view class="avatar">
        <text class="avatar-text">{{ avatarText }}</text>
      </view>
      <text class="user-name">{{ userInfo.name || '用户' }}</text>
      <text class="user-role">{{ roleName }}</text>
    </view>

    <view class="card menu-list">
      <view class="menu-item" @click="showInfo">
        <text class="menu-label">个人信息</text>
        <text class="menu-arrow">></text>
      </view>
      <view class="menu-item" @click="showAbout">
        <text class="menu-label">关于</text>
        <text class="menu-arrow">></text>
      </view>
    </view>

    <button class="btn-logout" @click="handleLogout">退出登录</button>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)

const avatarText = computed(() => {
  return userInfo.value.name ? userInfo.value.name.charAt(0) : '?'
})

const roleName = computed(() => {
  const map = { 1: '学生', 2: '教师', 3: '管理员' }
  return map[userInfo.value.role] || ''
})

const showInfo = () => {
  uni.showModal({
    title: '个人信息',
    content: `姓名: ${userInfo.value.name}\n角色: ${roleName.value}`,
    showCancel: false
  })
}

const showAbout = () => {
  uni.showModal({
    title: '关于 HelenEdu',
    content: '轻量级作业管理平台 v1.0.0',
    showCancel: false
  })
}

const handleLogout = () => {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        userStore.logout()
      }
    }
  })
}
</script>

<style scoped>
.profile-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60rpx 0 40rpx;
}

.avatar {
  width: 140rpx;
  height: 140rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20rpx;
}

.avatar-text {
  font-size: 56rpx;
  color: #fff;
  font-weight: bold;
}

.user-name {
  font-size: 36rpx;
  font-weight: bold;
  margin-bottom: 8rpx;
}

.user-role {
  font-size: 26rpx;
  color: #999;
}

.menu-list {
  margin-top: 20rpx;
}

.menu-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-label {
  font-size: 30rpx;
}

.menu-arrow {
  color: #ccc;
  font-size: 28rpx;
}

.btn-logout {
  margin-top: 60rpx;
  background: #fff;
  color: #f44336;
  border: 1rpx solid #f44336;
  border-radius: 12rpx;
  font-size: 30rpx;
}

.btn-logout::after {
  border: none;
}
</style>
