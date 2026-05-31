<template>
  <view class="container">
    <view class="card">
      <view class="section-header">
        <text class="section-title">班级学生</text>
        <text class="add-link" @click="showAddStudent">+ 添加学生</text>
      </view>
      <view v-if="students.length > 0">
        <view class="member-item" v-for="s in students" :key="s.id">
          <text class="member-name">{{ s.name }}</text>
          <text class="member-sub">{{ s.phone || '' }}</text>
          <text class="remove-link" @click="handleRemove(s.id, s.name)">移除</text>
        </view>
      </view>
      <text v-else class="empty-hint">暂无学生，点击右上角添加</text>
    </view>

    <!-- 添加学生弹窗 -->
    <view v-if="showPicker" class="modal-mask" @click="showPicker = false">
      <view class="modal-content" @click.stop>
        <text class="modal-title">选择学生</text>
        <view class="picker-list">
          <view class="picker-item" v-for="s in availableStudents" :key="s.id" @click="handleAdd(s.id)">
            <text>{{ s.name }}</text>
            <text class="picker-sub">{{ s.phone || '' }}</text>
          </view>
          <text v-if="availableStudents.length === 0" class="empty-hint">无可选学生（已全部加入）</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getClassStudents, addStudentToClass, removeStudentFromClass, getAllStudents } from '../../api/index'

const classId = ref('')
const className = ref('')
const students = ref([])
const allStudents = ref([])
const showPicker = ref(false)

const availableStudents = computed(() => {
  const ids = new Set(students.value.map(s => s.id))
  return allStudents.value.filter(s => !ids.has(s.id))
})

const fetchStudents = async () => {
  if (!classId.value) return
  students.value = await getClassStudents(classId.value) || []
}

const showAddStudent = async () => {
  if (allStudents.value.length === 0) {
    allStudents.value = await getAllStudents() || []
  }
  showPicker.value = true
}

const handleAdd = async (studentId) => {
  try {
    await addStudentToClass(classId.value, { userId: studentId })
    showPicker.value = false
    fetchStudents()
    uni.showToast({ title: '已添加', icon: 'success' })
  } catch (err) { console.error(err) }
}

const handleRemove = (studentId, name) => {
  uni.showModal({
    title: '确认移除',
    content: `确定将"${name}"从班级移除？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          await removeStudentFromClass(classId.value, studentId)
          fetchStudents()
          uni.showToast({ title: '已移除', icon: 'success' })
        } catch (err) { console.error(err) }
      }
    }
  })
}

onMounted(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  classId.value = currentPage.options.classId || ''
  className.value = decodeURIComponent(currentPage.options.className || '')
  if (className.value) {
    uni.setNavigationBarTitle({ title: className.value + ' - 学生管理' })
  }
  fetchStudents()
})
</script>

<style scoped>
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20rpx; }
.section-title { font-size: 30rpx; font-weight: bold; }
.add-link { color: #4A90D9; font-size: 26rpx; }
.member-item { display: flex; align-items: center; gap: 16rpx; padding: 20rpx 0; border-bottom: 1rpx solid #f5f5f5; }
.member-item:last-child { border-bottom: none; }
.member-name { font-size: 28rpx; flex: 1; }
.member-sub { font-size: 24rpx; color: #999; }
.remove-link { color: #f44336; font-size: 24rpx; }
.empty-hint { font-size: 26rpx; color: #ccc; display: block; text-align: center; padding: 40rpx 0; }
.modal-mask { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 999; }
.modal-content { background: #fff; border-radius: 16rpx; padding: 30rpx; width: 80%; max-height: 70vh; overflow-y: auto; }
.modal-title { font-size: 32rpx; font-weight: bold; display: block; margin-bottom: 24rpx; }
.picker-item { display: flex; justify-content: space-between; align-items: center; padding: 24rpx 0; border-bottom: 1rpx solid #f5f5f5; font-size: 28rpx; }
.picker-item:last-child { border-bottom: none; }
.picker-sub { font-size: 24rpx; color: #999; }
</style>
