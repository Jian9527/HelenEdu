<template>
  <view class="page">
    <view class="card">
      <view class="form-item">
        <text class="form-label">作业标题</text>
        <input class="form-input" v-model="form.title" placeholder="请输入作业标题" />
      </view>
      <view class="form-item">
        <text class="form-label">作业内容</text>
        <textarea class="form-textarea" v-model="form.content" placeholder="请输入作业要求" auto-height />
      </view>
      <view class="form-item">
        <text class="form-label">科目</text>
        <input class="form-input" v-model="form.subject" placeholder="请输入科目（可选）" />
      </view>
      <view class="form-item">
        <text class="form-label">截止时间</text>
        <picker mode="date" :value="form.deadlineDate" @change="onDateChange">
          <view class="form-input picker-value">{{ form.deadlineDate || '选择日期' }}</view>
        </picker>
      </view>
    </view>

    <!-- 附件 -->
    <view class="card">
      <text class="form-label">附件</text>
      <text class="form-hint">支持图片、Word、PDF等文件</text>
      <!-- 图片预览 -->
      <view class="image-list" v-if="imageList.length">
        <view class="image-item" v-for="(img, idx) in imageList" :key="'img-'+idx">
          <image :src="img" mode="aspectFill" class="upload-img" @click="previewImage(idx)" />
          <view class="remove-btn" @click="removeImage(idx)">x</view>
        </view>
      </view>
      <!-- 文件列表 -->
      <view class="file-list" v-if="fileList.length">
        <view class="file-row" v-for="(file, idx) in fileList" :key="'file-'+idx">
          <text class="file-icon">📄</text>
          <text class="file-name">{{ file.name }}</text>
          <text class="file-remove" @click="removeFile(idx)">删除</text>
        </view>
      </view>
      <!-- 操作按钮 -->
      <view class="upload-actions">
        <view class="upload-btn" @click="chooseImages" v-if="imageList.length + fileList.length < 9">
          <text class="add-icon">🖼</text>
          <text class="add-label">添加图片</text>
        </view>
        <view class="upload-btn" @click="chooseFiles" v-if="imageList.length + fileList.length < 9">
          <text class="add-icon">📎</text>
          <text class="add-label">添加文件</text>
        </view>
      </view>
    </view>

    <!-- 布置范围 -->
    <view class="card">
      <text class="form-label">布置范围</text>
      <view class="target-tabs">
        <view :class="['tab-item', targetType === 0 ? 'active' : '']" @click="targetType = 0">全班</view>
        <view :class="['tab-item', targetType === 1 ? 'active' : '']" @click="switchToStudents">指定学生</view>
      </view>

      <view v-if="targetType === 1" class="student-picker">
        <view v-if="selectedIds.length > 0" class="selected-summary">
          已选 <text class="highlight">{{ selectedIds.length }}</text> 名学生
          <text class="clear-link" @click="selectedIds = []">清空</text>
        </view>
        <view class="student-list">
          <view class="student-row" v-for="s in classStudents" :key="s.id" @click="toggleStudent(s.id)">
            <view :class="['checkbox', selectedIds.includes(s.id) ? 'checked' : '']">
              <text v-if="selectedIds.includes(s.id)">✓</text>
            </view>
            <text class="student-name">{{ s.name }}</text>
          </view>
          <text v-if="classStudents.length === 0" class="empty-hint">该班级暂无学生</text>
        </view>
      </view>
    </view>

    <button class="btn-primary submit-btn" @click="handleSubmit" :disabled="submitting">
      {{ submitting ? '保存中...' : (isEdit ? '保存修改' : '发布作业') }}
    </button>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { createHomework, updateHomework, getHomeworkDetail, getClassStudents } from '../../api/index'
import { uploadFile, chooseFile } from '../../utils/request'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()
const isEdit = ref(false)
const homeworkId = ref('')
const classId = ref('')
const submitting = ref(false)
const targetType = ref(0)
const classStudents = ref([])
const selectedIds = ref([])
const imageList = ref([])
const fileList = ref([]) // {name, size, path}

const form = ref({
  title: '',
  content: '',
  subject: '',
  deadlineDate: '',
  deadline: null
})

onMounted(async () => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options
  // 兼容大小写参数
  classId.value = options.classId || options.classid || ''

  if (options.id) {
    // 编辑模式
    isEdit.value = true
    homeworkId.value = options.id
    uni.setNavigationBarTitle({ title: '编辑作业' })
    await loadHomework(options.id)
  } else {
    // 默认填入教师的科目
    if (userStore.userInfo.subject) {
      form.value.subject = userStore.userInfo.subject
    }
  }
})

const isImageUrl = (url) => /\.(jpg|jpeg|png|gif|webp|bmp)(\?.*)?$/i.test(url)

const loadHomework = async (id) => {
  try {
    const hw = await getHomeworkDetail(id)
    console.log('加载作业详情:', hw)
    console.log('附件URLs:', hw.attachmentUrls)
    form.value.title = hw.title || ''
    form.value.content = hw.content || ''
    form.value.subject = hw.subject || ''
    if (hw.deadline) {
      const d = new Date(hw.deadline)
      form.value.deadlineDate = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
    }
    targetType.value = hw.targetType || 0
    // 加载已有附件
    if (hw.attachmentUrls && hw.attachmentUrls.length) {
      console.log('附件数量:', hw.attachmentUrls.length)
      for (const url of hw.attachmentUrls) {
        if (isImageUrl(url)) {
          imageList.value.push(url)
        } else {
          const name = decodeURIComponent(url.split('/').pop() || '文件')
          fileList.value.push({ name, path: url })
        }
      }
      console.log('图片列表:', imageList.value)
      console.log('文件列表:', fileList.value)
    } else {
      console.log('没有附件')
    }
    // 加载指定学生
    if (hw.targetType === 1) {
      await switchToStudents()
      // 回填已选学生
      if (hw.targetStudentIds && hw.targetStudentIds.length) {
        selectedIds.value = [...hw.targetStudentIds]
      }
    }
  } catch (err) {
    console.error('加载作业失败', err)
  }
}

const onDateChange = (e) => {
  form.value.deadlineDate = e.detail.value
}

// 图片相关
const chooseImages = () => {
  const remaining = 9 - imageList.value.length - fileList.value.length
  uni.chooseImage({
    count: remaining,
    sizeType: ['compressed'],
    success: (res) => {
      imageList.value = [...imageList.value, ...res.tempFilePaths]
    }
  })
}

const removeImage = (idx) => {
  imageList.value.splice(idx, 1)
}

const previewImage = (idx) => {
  uni.previewImage({ urls: imageList.value, current: imageList.value[idx] })
}

// 文件相关
const chooseFiles = async () => {
  const remaining = 9 - imageList.value.length - fileList.value.length
  try {
    const files = await chooseFile({ type: 'all', count: remaining })
    for (const file of files) {
      fileList.value.push({ name: file.name, path: file.path })
    }
  } catch (err) {
    console.log('选择文件取消', err)
  }
}

const removeFile = (idx) => {
  fileList.value.splice(idx, 1)
}

// 上传所有附件
const uploadAllAttachments = async () => {
  const urls = []
  for (let i = 0; i < imageList.value.length; i++) {
    const img = imageList.value[i]
    if (img.startsWith('http')) {
      urls.push(img)
    } else {
      try {
        const url = await uploadFile(img)
        urls.push(url)
      } catch (err) { console.error('图片上传失败', err) }
    }
  }
  for (let i = 0; i < fileList.value.length; i++) {
    const file = fileList.value[i]
    if (file.path.startsWith('http')) {
      urls.push(file.path)
    } else {
      try {
        const url = await uploadFile(file.path)
        urls.push(url)
      } catch (err) { console.error('文件上传失败', err) }
    }
  }
  return urls
}

// 布置范围
const switchToStudents = async () => {
  targetType.value = 1
  if (classStudents.value.length === 0 && classId.value) {
    try {
      classStudents.value = await getClassStudents(classId.value) || []
    } catch (err) { console.error(err) }
  }
}

const toggleStudent = (id) => {
  const idx = selectedIds.value.indexOf(id)
  if (idx >= 0) {
    selectedIds.value.splice(idx, 1)
  } else {
    selectedIds.value.push(id)
  }
}

const handleSubmit = async () => {
  if (!form.value.title.trim()) {
    uni.showToast({ title: '请输入标题', icon: 'none' })
    return
  }
  if (targetType.value === 1 && selectedIds.value.length === 0) {
    uni.showToast({ title: '请至少选择一名学生', icon: 'none' })
    return
  }

  submitting.value = true
  try {
    const attachmentUrls = await uploadAllAttachments()
    console.log('上传后的附件URLs:', attachmentUrls)
    const data = {
      title: form.value.title,
      content: form.value.content,
      classId: parseInt(classId.value),
      subject: form.value.subject,
      deadline: form.value.deadlineDate ? form.value.deadlineDate + 'T23:59:59' : null,
      status: 1,
      targetType: targetType.value,
      studentIds: targetType.value === 1 ? selectedIds.value : [],
      attachmentUrls: attachmentUrls
    }
    console.log('提交数据:', data)
    if (isEdit.value) {
      await updateHomework(homeworkId.value, data)
    } else {
      await createHomework(data)
    }
    uni.showToast({ title: isEdit.value ? '已保存' : '发布成功', icon: 'success' })
    // 直接跳转回作业列表，不使用 navigateBack（H5模式下可能不生效）
    setTimeout(() => {
      uni.redirectTo({ url: `/pages/teacher/homework-list?classId=${classId.value}` })
    }, 1000)
  } catch (err) {
    console.error('发布作业失败', err)
    uni.showToast({ title: err?.message || '发布失败，请重试', icon: 'none' })
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.page { min-height: 100vh; background: #f4f6f9; padding: 30rpx; box-sizing: border-box; }
.form-item { margin-bottom: 30rpx; }
.form-label { display: block; font-size: 28rpx; font-weight: bold; margin-bottom: 12rpx; }
.form-hint { display: block; font-size: 24rpx; color: #aaa; margin-bottom: 16rpx; }
.form-input { width: 100%; height: 80rpx; background: #f8f9fa; border-radius: 12rpx; padding: 0 24rpx; font-size: 28rpx; box-sizing: border-box; }
.form-textarea { width: 100%; min-height: 200rpx; background: #f8f9fa; border-radius: 12rpx; padding: 24rpx; font-size: 28rpx; box-sizing: border-box; }
.picker-value { display: flex; align-items: center; color: #333; }
.submit-btn { margin-top: 20rpx; width: 100%; }

/* 附件上传 */
.image-list { display: flex; flex-wrap: wrap; gap: 16rpx; margin-bottom: 16rpx; }
.image-item { position: relative; width: 180rpx; height: 180rpx; }
.upload-img { width: 100%; height: 100%; border-radius: 12rpx; }
.remove-btn { position: absolute; top: -10rpx; right: -10rpx; width: 40rpx; height: 40rpx; background: #f44336; color: #fff; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 24rpx; }
.file-list { margin-bottom: 16rpx; }
.file-row { display: flex; align-items: center; gap: 12rpx; padding: 16rpx 20rpx; background: #f8f9fa; border-radius: 12rpx; margin-bottom: 10rpx; }
.file-icon { font-size: 36rpx; }
.file-name { flex: 1; font-size: 26rpx; color: #333; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.file-remove { font-size: 24rpx; color: #f44336; padding: 4rpx 12rpx; }
.upload-actions { display: flex; gap: 16rpx; }
.upload-btn { flex: 1; display: flex; align-items: center; justify-content: center; gap: 8rpx; padding: 20rpx 0; background: #f8f9fa; border: 2rpx dashed #ccc; border-radius: 12rpx; }
.upload-btn .add-icon { font-size: 32rpx; }
.upload-btn .add-label { font-size: 24rpx; color: #999; }

/* 布置范围 */
.target-tabs { display: flex; gap: 20rpx; margin-top: 12rpx; }
.tab-item { flex: 1; text-align: center; padding: 18rpx 0; border-radius: 12rpx; font-size: 28rpx; font-weight: 600; background: #f0f4ff; color: #4A90D9; }
.tab-item.active { background: #4A90D9; color: #fff; }
.student-picker { margin-top: 24rpx; }
.selected-summary { font-size: 26rpx; color: #666; margin-bottom: 16rpx; }
.highlight { color: #4A90D9; font-weight: bold; }
.clear-link { color: #f44336; font-size: 24rpx; margin-left: 16rpx; }
.student-list { background: #f8f9fa; border-radius: 12rpx; padding: 0 24rpx; max-height: 500rpx; overflow-y: auto; }
.student-row { display: flex; align-items: center; gap: 20rpx; padding: 20rpx 0; border-bottom: 1rpx solid #eef0f3; }
.student-row:last-child { border-bottom: none; }
.checkbox { width: 40rpx; height: 40rpx; border-radius: 8rpx; border: 2rpx solid #ddd; display: flex; align-items: center; justify-content: center; font-size: 24rpx; color: #fff; }
.checkbox.checked { background: #4A90D9; border-color: #4A90D9; }
.student-name { font-size: 28rpx; }
.empty-hint { font-size: 26rpx; color: #ccc; display: block; text-align: center; padding: 40rpx 0; }
</style>
