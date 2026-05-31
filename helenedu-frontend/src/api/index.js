import { get, post, put, del } from '../utils/request'

// 作业相关
export const getStudentHomeworkList = (params) => get('/api/homework/student-list', params)
export const getTeacherHomeworkList = (params) => get('/api/homework/list', params)
export const getHomeworkDetail = (id) => get(`/api/homework/${id}`)
export const createHomework = (data) => post('/api/homework', data)
export const updateHomework = (id, data) => put(`/api/homework/${id}`, data)
export const deleteHomework = (id) => del(`/api/homework/${id}`)
export const submitHomework = (id, data) => post(`/api/homework/${id}/submit`, data)
export const getHomeworkSubmits = (id, params) => get(`/api/homework/${id}/submits`, params)
export const reviewHomework = (id, data) => put(`/api/homework/submit/${id}/review`, data)
export const getSubmitDetail = (id) => get(`/api/homework/submit/${id}`)

// 预习资料相关
export const getStudentPreviewList = (params) => get('/api/preview/student-list', params)
export const getTeacherPreviewList = (params) => get('/api/preview/list', params)
export const getPreviewDetail = (id) => get(`/api/preview/${id}`)
export const createPreview = (data) => post('/api/preview', data)
export const updatePreview = (id, data) => put(`/api/preview/${id}`, data)
export const deletePreview = (id) => del(`/api/preview/${id}`)

// 班级相关
export const getClassList = (params) => get('/api/class/list', params)
export const getMyClasses = () => get('/api/class/my-classes')
export const getMyStudentClasses = () => get('/api/class/my-student-classes')
export const getClassDetail = (id) => get(`/api/class/${id}`)
export const createClass = (data) => post('/api/class', data)
export const updateClass = (id, data) => put(`/api/class/${id}`, data)
export const deleteClass = (id) => del(`/api/class/${id}`)
export const getClassStudents = (id) => get(`/api/class/${id}/students`)
export const getClassTeachers = (id) => get(`/api/class/${id}/teachers`)
export const addStudentToClass = (id, data) => post(`/api/class/${id}/students`, data)
export const removeStudentFromClass = (id, studentId) => del(`/api/class/${id}/students/${studentId}`)
export const addTeacherToClass = (id, data) => post(`/api/class/${id}/teachers`, data)
export const removeTeacherFromClass = (id, teacherId) => del(`/api/class/${id}/teachers/${teacherId}`)

// 用户管理
export const getUserList = (params) => get('/api/user/list', params)
export const getAllTeachers = () => get('/api/user/teachers')
export const getAllStudents = () => get('/api/user/students')
export const createUser = (data) => post('/api/user', data)
export const updateUser = (id, data) => put(`/api/user/${id}`, data)
export const toggleUserStatus = (id) => put(`/api/user/${id}/toggle-status`)
export const deleteUser = (id) => del(`/api/user/${id}`)

// 数据看板
export const getDashboardOverview = () => get('/api/dashboard/overview')
export const getClassRank = () => get('/api/dashboard/class-rank')
