<template>
  <view class="container">
    <!-- 基本信息表单 -->
    <view class="card">
      <view class="form-item">
        <text class="form-label">班级名称</text>
        <input class="form-input" v-model="form.name" placeholder="请输入班级名称" />
      </view>
      <view class="form-item">
        <text class="form-label">年级</text>
        <input class="form-input" v-model="form.grade" placeholder="请输入年级（可选）" />
      </view>
      <view class="form-item">
        <text class="form-label">班主任</text>
        <picker :range="teacherNames" @change="onTeacherChange">
          <view class="form-input picker-display">
            {{ selectedTeacherName || '请选择班主任（可选）' }}
          </view>
        </picker>
      </view>
    </view>
    <button class="btn-primary submit-btn" @click="handleSubmit" :disabled="submitting">
      {{ submitting ? '保存中...' : (isEdit ? '保存' : '创建') }}
    </button>

    <!-- 编辑模式：成员管理 -->
    <view v-if="isEdit">
      <!-- 教师管理 -->
      <view class="card section-card">
        <view class="section-header">
          <text class="section-title">班级教师</text>
          <text class="add-link" @click="showAddTeacher">+ 添加</text>
        </view>
        <view v-if="classTeachers.length > 0">
          <view class="member-item" v-for="t in classTeachers" :key="t.id">
            <text class="member-name">{{ t.name }}</text>
            <text class="member-sub">{{ t.subject || '' }}</text>
            <text class="remove-link" @click="handleRemoveTeacher(t.id)">移除</text>
          </view>
        </view>
        <text v-else class="empty-hint">暂无教师</text>
      </view>

      <!-- 学生管理 -->
      <view class="card section-card">
        <view class="section-header">
          <text class="section-title">班级学生</text>
          <text class="add-link" @click="showAddStudent">+ 添加</text>
        </view>
        <view v-if="classStudents.length > 0">
          <view class="member-item" v-for="s in classStudents" :key="s.id">
            <text class="member-name">{{ s.name }}</text>
            <text class="remove-link" @click="handleRemoveStudent(s.id)">移除</text>
          </view>
        </view>
        <text v-else class="empty-hint">暂无学生</text>
      </view>
    </view>

    <!-- 添加教师弹窗 -->
    <view v-if="showTeacherPicker" class="modal-mask" @click="showTeacherPicker = false">
      <view class="modal-content" @click.stop>
        <text class="modal-title">选择教师</text>
        <view class="picker-list">
          <view class="picker-item" v-for="t in availableTeachers" :key="t.id" @click="handleAddTeacher(t.id)">
            {{ t.name }}
          </view>
          <text v-if="availableTeachers.length === 0" class="empty-hint">无可选教师</text>
        </view>
      </view>
    </view>

    <!-- 添加学生弹窗 -->
    <view v-if="showStudentPicker" class="modal-mask" @click="showStudentPicker = false">
      <view class="modal-content" @click.stop>
        <text class="modal-title">选择学生</text>
        <view class="picker-list">
          <view class="picker-item" v-for="s in availableStudents" :key="s.id" @click="handleAddStudent(s.id)">
            {{ s.name }}
          </view>
          <text v-if="availableStudents.length === 0" class="empty-hint">无可选学生</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import {
  createClass, updateClass,
  getAllTeachers, getAllStudents,
  getClassStudents, getClassTeachers,
  addStudentToClass, removeStudentFromClass,
  addTeacherToClass, removeTeacherFromClass
} from '../../api/index'

const isEdit = ref(false)
const classId = ref('')
const submitting = ref(false)
const form = ref({ name: '', grade: '', teacherId: null })

// 教师选择器
const allTeachers = ref([])
const teacherNames = computed(() => allTeachers.value.map(t => t.name))
const selectedTeacherName = computed(() => {
  const t = allTeachers.value.find(t => t.id === form.value.teacherId)
  return t ? t.name : ''
})
const onTeacherChange = (e) => {
  form.value.teacherId = allTeachers.value[e.detail.value]?.id || null
}

// 成员管理
const classTeachers = ref([])
const classStudents = ref([])
const showTeacherPicker = ref(false)
const showStudentPicker = ref(false)
const allStudents = ref([])

const availableTeachers = computed(() => {
  const ids = new Set(classTeachers.value.map(t => t.id))
  return allTeachers.value.filter(t => !ids.has(t.id))
})
const availableStudents = computed(() => {
  const ids = new Set(classStudents.value.map(s => s.id))
  return allStudents.value.filter(s => !ids.has(s.id))
})

const fetchMembers = async () => {
  if (!classId.value) return
  const [teachers, students] = await Promise.all([
    getClassTeachers(classId.value),
    getClassStudents(classId.value)
  ])
  classTeachers.value = teachers || []
  classStudents.value = students || []
}

const showAddTeacher = async () => {
  if (allTeachers.value.length === 0) {
    allTeachers.value = await getAllTeachers() || []
  }
  showTeacherPicker.value = true
}
const showAddStudent = async () => {
  if (allStudents.value.length === 0) {
    allStudents.value = await getAllStudents() || []
  }
  showStudentPicker.value = true
}

const handleAddTeacher = async (teacherId) => {
  try {
    await addTeacherToClass(classId.value, { teacherId })
    showTeacherPicker.value = false
    fetchMembers()
    uni.showToast({ title: '已添加', icon: 'success' })
  } catch (err) { console.error(err) }
}
const handleRemoveTeacher = async (teacherId) => {
  uni.showModal({ title: '确认', content: '确定移除该教师？', success: async (res) => {
    if (res.confirm) {
      try {
        await removeTeacherFromClass(classId.value, teacherId)
        fetchMembers()
        uni.showToast({ title: '已移除', icon: 'success' })
      } catch (err) { console.error(err) }
    }
  }})
}
const handleAddStudent = async (studentId) => {
  try {
    await addStudentToClass(classId.value, { studentId })
    showStudentPicker.value = false
    fetchMembers()
    uni.showToast({ title: '已添加', icon: 'success' })
  } catch (err) { console.error(err) }
}
const handleRemoveStudent = async (studentId) => {
  uni.showModal({ title: '确认', content: '确定移除该学生？', success: async (res) => {
    if (res.confirm) {
      try {
        await removeStudentFromClass(classId.value, studentId)
        fetchMembers()
        uni.showToast({ title: '已移除', icon: 'success' })
      } catch (err) { console.error(err) }
    }
  }})
}

onMounted(async () => {
  allTeachers.value = await getAllTeachers() || []
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options
  if (options.id) {
    isEdit.value = true
    classId.value = options.id
    form.value = {
      name: options.name || '',
      grade: options.grade || '',
      teacherId: options.teacherId ? Number(options.teacherId) : null
    }
    uni.setNavigationBarTitle({ title: '编辑班级' })
    fetchMembers()
  }
})

const handleSubmit = async () => {
  if (!form.value.name.trim()) {
    uni.showToast({ title: '请输入班级名称', icon: 'none' })
    return
  }
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateClass(classId.value, form.value)
    } else {
      await createClass(form.value)
    }
    uni.showToast({ title: isEdit.value ? '已保存' : '已创建', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1500)
  } catch (err) { console.error(err) }
  finally { submitting.value = false }
}
</script>

<style scoped>
.form-item { margin-bottom: 30rpx; }
.form-label { display: block; font-size: 28rpx; font-weight: bold; margin-bottom: 12rpx; }
.form-input { width: 100%; height: 80rpx; background: #f8f9fa; border-radius: 12rpx; padding: 0 24rpx; font-size: 28rpx; box-sizing: border-box; }
.picker-display { display: flex; align-items: center; color: #666; line-height: 80rpx; }
.submit-btn { margin-top: 20rpx; width: 100%; }
.section-card { margin-top: 30rpx; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20rpx; }
.section-title { font-size: 30rpx; font-weight: bold; }
.add-link { color: #4A90D9; font-size: 26rpx; }
.member-item { display: flex; align-items: center; gap: 16rpx; padding: 16rpx 0; border-bottom: 1rpx solid #f5f5f5; }
.member-item:last-child { border-bottom: none; }
.member-name { font-size: 28rpx; flex: 1; }
.member-sub { font-size: 24rpx; color: #999; }
.remove-link { color: #f44336; font-size: 24rpx; }
.empty-hint { font-size: 26rpx; color: #ccc; display: block; text-align: center; padding: 20rpx 0; }
.modal-mask { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 999; }
.modal-content { background: #fff; border-radius: 16rpx; padding: 30rpx; width: 80%; max-height: 70vh; overflow-y: auto; }
.modal-title { font-size: 32rpx; font-weight: bold; display: block; margin-bottom: 24rpx; }
.picker-list { }
.picker-item { padding: 24rpx 0; border-bottom: 1rpx solid #f5f5f5; font-size: 28rpx; }
.picker-item:last-child { border-bottom: none; }
</style>
