// API 基础配置
// H5模式自动使用当前访问地址的域名/IP，便于手机访问
let BASE_URL = 'http://localhost:8888'
// #ifdef H5
if (typeof window !== 'undefined') {
  const hostname = window.location.hostname
  BASE_URL = `http://${hostname}:8888`
}
// #endif

/**
 * 封装 uni.request
 */
const request = (options) => {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token')
    
    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data,
      header: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : '',
        ...options.header
      },
      success: (res) => {
        if (res.statusCode === 401) {
          // Token 过期，跳转登录
          uni.removeStorageSync('token')
          uni.removeStorageSync('userInfo')
          uni.reLaunch({ url: '/pages/login/index' })
          reject(res)
          return
        }
        
        if (res.statusCode === 200 && res.data.code === 200) {
          resolve(res.data.data)
        } else {
          const message = res.data?.message || '请求失败'
          uni.showToast({ title: message, icon: 'none' })
          reject(res.data)
        }
      },
      fail: (err) => {
        uni.showToast({ title: '网络错误', icon: 'none' })
        reject(err)
      }
    })
  })
}

// 便捷方法
export const get = (url, data) => request({ url, method: 'GET', data })
export const post = (url, data) => request({ url, method: 'POST', data })
export const put = (url, data) => request({ url, method: 'PUT', data })
export const del = (url, data) => request({ url, method: 'DELETE', data })

/**
 * 上传文件
 */
export const uploadFile = (filePath) => {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token')
    uni.uploadFile({
      url: BASE_URL + '/api/file/upload',
      filePath: filePath,
      name: 'file',
      header: {
        'Authorization': token ? `Bearer ${token}` : ''
      },
      success: (res) => {
        const data = JSON.parse(res.data)
        if (data.code === 200) {
          resolve(data.data)
        } else {
          uni.showToast({ title: data.message || '上传失败', icon: 'none' })
          reject(data)
        }
      },
      fail: (err) => {
        uni.showToast({ title: '上传失败', icon: 'none' })
        reject(err)
      }
    })
  })
}

export default request

/**
 * 选择文件（支持图片、文档等）
 */
export const chooseFile = (options = {}) => {
  const { type = 'all', count = 5 } = options
  return new Promise((resolve, reject) => {
    // #ifdef H5
    if (type === 'image') {
      uni.chooseImage({
        count,
        sizeType: ['compressed'],
        success: (res) => resolve(res.tempFiles.map(f => ({ path: f.path, name: f.name || f.path.split('/').pop() }))),
        fail: reject
      })
    } else {
      // H5模式使用文件选择器
      uni.chooseFile({
        count,
        type: type === 'image' ? 'image' : 'file',
        success: (res) => resolve(res.tempFiles.map(f => ({ path: f.path, name: f.name || f.path.split('/').pop() }))),
        fail: reject
      })
    }
    // #endif
    // #ifdef MP-WEIXIN
    if (type === 'image') {
      uni.chooseImage({
        count,
        sizeType: ['compressed'],
        success: (res) => resolve(res.tempFiles.map(f => ({ path: f.path, name: f.path.split('/').pop() }))),
        fail: reject
      })
    } else {
      uni.chooseMessageFile({
        count,
        type: type === 'image' ? 'image' : 'file',
        success: (res) => resolve(res.tempFiles.map(f => ({ path: f.path, name: f.name }))),
        fail: reject
      })
    }
    // #endif
  })
}
