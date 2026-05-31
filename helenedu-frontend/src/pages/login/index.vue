<template>
  <view class="login-page">
    <view class="login-header">
      <view class="logo-img" :style="{ backgroundImage: 'url(' + logoUrl + ')' }"></view>
      <text class="app-name">海伦云作业</text>
    </view>
    <view class="login-body">
      <!-- 微信小程序登录 -->
      <!-- #ifdef MP-WEIXIN -->
      <button class="btn-login" @click="handleWxLogin">
        微信一键登录
      </button>
      <!-- #endif -->
      
      <!-- H5 端手机号登录（开发调试用） -->
      <!-- #ifdef H5 -->
      <view class="dev-login">
        <input class="input-phone" v-model="phone" placeholder="输入手机号（开发模式）" type="number" maxlength="11" />
        <button class="btn-login" @click="handleDevLogin" :disabled="!phone">
          登录
        </button>
        <text class="dev-tip">开发环境：输入已注册手机号即可登录</text>
      </view>
      <!-- #endif -->
    </view>
    
    <view class="login-footer">
      <text class="footer-text">登录即表示同意用户协议和隐私政策</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { useUserStore } from '../../store/user'
import { wxLogin } from '../../api/auth'
import { post } from '../../utils/request'
import logoImg from '../../assets/logo.png'

const logoUrl = logoImg
const userStore = useUserStore()
const phone = ref('')

// 微信登录
const handleWxLogin = () => {
  uni.login({
    provider: 'weixin',
    success: async (loginRes) => {
      try {
        const data = await wxLogin({ code: loginRes.code })
        userStore.setLoginInfo(data)
        redirectToHome(data.role)
      } catch (err) {
        console.error('登录失败', err)
      }
    },
    fail: () => {
      uni.showToast({ title: '微信登录失败', icon: 'none' })
    }
  })
}

// H5 开发模式登录
const handleDevLogin = async () => {
  if (!phone.value || phone.value.length < 11) {
    uni.showToast({ title: '请输入正确手机号', icon: 'none' })
    return
  }
  try {
    const data = await post('/api/auth/wx-login', {
      code: 'dev_' + phone.value,
      nickName: '开发用户'
    })
    userStore.setLoginInfo(data)
    redirectToHome(data.role)
  } catch (err) {
    console.error('登录失败', err)
  }
}

// 根据角色跳转
const redirectToHome = (role) => {
  const routes = {
    1: '/pages/student/homework-list',
    2: '/pages/teacher/class-list',
    3: '/pages/admin/dashboard'
  }
  uni.reLaunch({ url: routes[role] || '/pages/login/index' })
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 0 60rpx;
}

.login-header {
  padding-top: 160rpx;
  text-align: center;
}

.app-name {
  display: block;
  font-size: 64rpx;
  font-weight: bold;
  color: #fff;
  margin-bottom: 16rpx;
}

.app-desc {
  display: block;
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.8);
}

.login-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.logo-img {
  width: 180rpx;
  height: 180rpx;
  margin: 0 auto 30rpx;
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
}

.btn-login {
  width: 100%;
  height: 96rpx;
  line-height: 96rpx;
  background-color: #fff;
  color: #667eea;
  font-size: 34rpx;
  font-weight: bold;
  border-radius: 48rpx;
  border: none;
}

.btn-login::after {
  border: none;
}

.dev-login {
  width: 100%;
}

.input-phone {
  width: 100%;
  height: 96rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 48rpx;
  padding: 0 40rpx;
  margin-bottom: 30rpx;
  color: #fff;
  font-size: 30rpx;
  box-sizing: border-box;
}

.input-phone::placeholder {
  color: rgba(255, 255, 255, 0.6);
}

.dev-tip {
  display: block;
  text-align: center;
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.6);
  margin-top: 20rpx;
}

.login-footer {
  padding-bottom: 60rpx;
  text-align: center;
}

.footer-text {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.6);
}
</style>
