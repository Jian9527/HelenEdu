<template>
  <view class="page">
    <view class="card">
      <!-- 角色选择 -->
      <view class="form-item">
        <text class="form-label">角色</text>
        <picker :range="roleOptions" range-key="label" :value="roleIndex" @change="onRoleChange">
          <view class="form-input picker-value">{{ roleOptions[roleIndex].label }}</view>
        </picker>
      </view>

      <!-- 通用: 姓名 -->
      <view class="form-item">
        <text class="form-label">姓名 <text class="required">*</text></text>
        <input class="form-input" v-model="form.name" placeholder="请输入姓名" />
      </view>

      <!-- 教师: 科目 + 联系方式 -->
      <template v-if="form.role === 2">
        <view class="form-item">
          <text class="form-label">教授科目 <text class="required">*</text></text>
          <input class="form-input" v-model="form.subject" placeholder="如：语文、数学、英语" />
        </view>
        <view class="form-item">
          <text class="form-label">联系方式 <text class="required">*</text></text>
          <input class="form-input" v-model="form.phone" placeholder="手机号或微信号" />
        </view>
      </template>

      <!-- 学生: 家长微信 + 年级 + 性别 + 班级 -->
      <template v-if="form.role === 1">
        <view class="form-item">
          <text class="form-label">家长联系方式 <text class="required">*</text></text>
          <input class="form-input" v-model="form.parentContact" placeholder="家长微信号或手机号" />
          <text class="form-hint">微信号或手机号均可</text>
        </view>
        <view class="form-item">
          <text class="form-label">年级 <text class="required">*</text></text>
          <picker :range="gradeOptions" :value="gradeIndex" @change="onGradeChange">
            <view class="form-input picker-value">{{ form.grade || '请选择年级' }}</view>
          </picker>
        </view>
        <view class="form-item">
          <text class="form-label">性别 <text class="required">*</text></text>
          <view class="gender-group">
            <view :class="['gender-btn', form.gender === 1 ? 'active' : '']" @click="form.gender = 1">男</view>
            <view :class="['gender-btn', form.gender === 2 ? 'active' : '']" @click="form.gender = 2">女</view>
          </view>
        </view>
        <view class="form-item">
          <text class="form-label">分配班级</text>
          <picker :range="classList" range-key="name" :value="classIndex" @change="onClassChange">
            <view class="form-input picker-value">{{ selectedClassName || '选择班级（可选）' }}</view>
          </picker>
        </view>
      </template>
    </view>

    <button class="btn-primary submit-btn" @click="handleSubmit" :disabled="submitting">
      {{ submitting ? '保存中...' : (isEdit ? '保存' : '创建') }}
    </button>
  </view>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { createUser, updateUser, getClassList } from '../../api/index'

const roleOptions = [
  { label: '学生', value: 1 },
  { label: '教师', value: 2 }
]
const gradeOptions = ['一年级', '二年级', '三年级', '四年级', '五年级', '六年级']

const isEdit = ref(false)
const userId = ref('')
const submitting = ref(false)
const roleIndex = ref(0)
const gradeIndex = ref(0)
const classIndex = ref(0)
const classList = ref([])

const form = ref({
  name: '',
  phone: '',
  role: 1,
  subject: '',
  gender: null,
  parentContact: '',
  grade: '',
  classId: null
})

const selectedClassName = computed(() => {
  if (!form.value.classId) return ''
  const cls = classList.value.find(c => c.id === form.value.classId)
  return cls ? cls.name : ''
})

const onRoleChange = (e) => {
  roleIndex.value = e.detail.value
  form.value.role = roleOptions[roleIndex.value].value
}

const onGradeChange = (e) => {
  gradeIndex.value = e.detail.value
  form.value.grade = gradeOptions[gradeIndex.value]
}

const onClassChange = (e) => {
  classIndex.value = e.detail.value
  form.value.classId = classList.value[classIndex.value]?.id || null
}

const loadClasses = async () => {
  try {
    const res = await getClassList({ page: 1, size: 100 })
    classList.value = res.records || []
  } catch (err) { console.error(err) }
}

onMounted(() => {
  loadClasses()
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options
  if (options.id) {
    isEdit.value = true
    userId.value = options.id
    form.value.name = decodeURIComponent(options.name || '')
    form.value.phone = decodeURIComponent(options.phone || '')
    form.value.role = parseInt(options.role) || 1
    form.value.subject = decodeURIComponent(options.subject || '')
    form.value.gender = options.gender ? parseInt(options.gender) : null
    form.value.parentContact = decodeURIComponent(options.parentContact || '')
    form.value.grade = decodeURIComponent(options.grade || '')
    form.value.classId = options.classId ? parseInt(options.classId) : null

    roleIndex.value = roleOptions.findIndex(r => r.value === form.value.role)
    if (roleIndex.value < 0) roleIndex.value = 0
    if (form.value.grade) {
      gradeIndex.value = gradeOptions.indexOf(form.value.grade)
      if (gradeIndex.value < 0) gradeIndex.value = 0
    }
    uni.setNavigationBarTitle({ title: '编辑人员' })
  }
})

const handleSubmit = async () => {
  const f = form.value
  if (!f.name.trim()) { uni.showToast({ title: '请输入姓名', icon: 'none' }); return }

  if (f.role === 2) {
    if (!f.subject.trim()) { uni.showToast({ title: '请输入教授科目', icon: 'none' }); return }
    if (!f.phone.trim()) { uni.showToast({ title: '请输入联系方式', icon: 'none' }); return }
  }

  if (f.role === 1) {
    if (!f.parentContact.trim()) { uni.showToast({ title: '请输入家长联系方式', icon: 'none' }); return }
    if (!f.grade) { uni.showToast({ title: '请选择年级', icon: 'none' }); return }
    if (!f.gender) { uni.showToast({ title: '请选择性别', icon: 'none' }); return }
  }

  submitting.value = true
  try {
    const data = { ...f }
    // 教师不需要学生字段
    if (data.role === 2) {
      data.gender = null
      data.parentContact = ''
      data.grade = ''
      data.classId = null
    }
    // 学生不需要手机号和科目
    if (data.role === 1) {
      data.phone = ''
      data.subject = ''
    }

    if (isEdit.value) {
      await updateUser(userId.value, data)
    } else {
      await createUser(data)
    }
    uni.showToast({ title: isEdit.value ? '已保存' : '已创建', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1500)
  } catch (err) {
    console.error(err)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.page { min-height: 100vh; background: #f4f6f9; padding: 30rpx; box-sizing: border-box; }
.form-item { margin-bottom: 30rpx; }
.form-label { display: block; font-size: 28rpx; font-weight: bold; margin-bottom: 12rpx; }
.required { color: #f44336; }
.form-hint { display: block; font-size: 24rpx; color: #aaa; margin-top: 8rpx; }
.form-input { width: 100%; height: 80rpx; background: #f8f9fa; border-radius: 12rpx; padding: 0 24rpx; font-size: 28rpx; box-sizing: border-box; line-height: 80rpx; }
.picker-value { display: flex; align-items: center; color: #333; }
.gender-group { display: flex; gap: 20rpx; }
.gender-btn { flex: 1; text-align: center; padding: 18rpx 0; border-radius: 12rpx; font-size: 28rpx; font-weight: 600; background: #f0f4ff; color: #4A90D9; }
.gender-btn.active { background: #4A90D9; color: #fff; }
.submit-btn { margin-top: 20rpx; width: 100%; }
</style>
