import { get, post } from '../utils/request'

// 微信登录
export const wxLogin = (data) => post('/api/auth/wx-login', data)

// 获取用户信息
export const getUserInfo = () => get('/api/auth/userinfo')
