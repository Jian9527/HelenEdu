<template>
  <view class="page">
    <!-- 提交内容 -->
    <view class="card">
      <text class="section-title">提交内容</text>
      <textarea 
        class="content-input" 
        v-model="content" 
        placeholder="请输入作业内容（可选）..."
        :maxlength="-1"
        auto-height
      />
    </view>

    <!-- 图片附件 -->
    <view class="card">
      <text class="section-title">图片附件</text>
      <text class="section-hint">支持拍照或从相册选择</text>
      <view class="file-grid">
        <view class="file-item image-item" v-for="(img, idx) in imageList" :key="idx">
          <image :src="img" mode="aspectFill" class="upload-img" @click="previewImage(idx)" />
          <view class="remove-btn" @click="removeImage(idx)">x</view>
        </view>
        <view class="add-btn" @click="chooseImages" v-if="imageList.length < 9">
          <text class="add-icon">+</text>
          <text class="add-label">添加图片</text>
        </view>
      </view>
    </view>

    <!-- 文件附件 -->
    <view class="card">
      <text class="section-title">文件附件</text>
      <text class="section-hint">支持Word、PDF等文档</text>
      <view class="file-list">
        <view class="file-row" v-for="(file, idx) in fileList" :key="idx">
          <view class="file-icon">📄</view>
          <view class="file-info">
            <text class="file-name">{{ file.name || '文件' }}</text>
            <text class="file-size">{{ formatSize(file.size) }}</text>
          </view>
          <view class="remove-btn-sm" @click="removeFile(idx)">删除</view>
        </view>
        <view class="add-file-btn" @click="chooseFiles" v-if="fileList.length < 5">
          <text class="add-icon">📎</text>
          <text class="add-label">选择文件</text>
        </view>
      </view>
    </view>

    <view class="action-buttons">
      <button class="btn-save" @click="handleSave" :disabled="submitting">
        {{ submitting ? '保存中...' : '保存作业' }}
      </button>
      <button class="btn-primary btn-submit" @click="handleSubmit" :disabled="submitting">
        {{ submitting ? '提交中...' : '提交作业' }}
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getHomeworkDetail, submitHomework } from '../../api/index'
import { uploadFile, chooseFile } from '../../utils/request'

const homeworkId = ref('')
const content = ref('')
const imageList = ref([])
const fileList = ref([]) // {name, size, path}
const submitting = ref(false)

const isImageUrl = (url) => /\.(jpg|jpeg|png|gif|webp|bmp)(\?.*)?$/i.test(url)

onMounted(async () => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  homeworkId.value = currentPage.options.id

  // 加载已有草稿
  try {
    const hw = await getHomeworkDetail(homeworkId.value)
    if (hw.mySubmitStatus === -1 && (hw.myDraftContent || hw.myDraftAttachmentUrls?.length)) {
      content.value = hw.myDraftContent || ''
      if (hw.myDraftAttachmentUrls && hw.myDraftAttachmentUrls.length) {
        for (const url of hw.myDraftAttachmentUrls) {
          if (isImageUrl(url)) {
            imageList.value.push(url)
          } else {
            const nameMatch = url.match(/[?&]name=([^&#]+)/)
            const name = nameMatch ? decodeURIComponent(nameMatch[1]) : decodeURIComponent(url.split('?')[0].split('/').pop() || '文件')
            fileList.value.push({ name, size: 0, path: url })
          }
        }
      }
      console.log('草稿已加载')
    }
  } catch (err) {
    console.error('加载草稿失败', err)
  }
})

// 图片操作
const chooseImages = () => {
  uni.chooseImage({
    count: 9 - imageList.value.length,
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
  uni.previewImage({
    urls: imageList.value,
    current: imageList.value[idx]
  })
}

// 文件操作
const chooseFiles = async () => {
  try {
    const files = await chooseFile({ type: 'all', count: 5 - fileList.value.length })
    for (const file of files) {
      fileList.value.push({ name: file.name, size: 0, path: file.path })
    }
  } catch (err) {
    console.log('选择文件取消', err)
  }
}

const removeFile = (idx) => {
  fileList.value.splice(idx, 1)
}

const formatSize = (size) => {
  if (!size) return ''
  if (size < 1024) return size + 'B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(1) + 'KB'
  return (size / 1024 / 1024).toFixed(1) + 'MB'
}

// 上传所有附件
const uploadAllFiles = async () => {
  const urls = []
  // 上传图片
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
  // 上传文件
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

const handleSave = async () => {
  submitting.value = true
  try {
    const urls = await uploadAllFiles()
    await submitHomework(homeworkId.value, {
      content: content.value,
      attachmentUrls: urls,
      draft: true
    })
    uni.showToast({ title: '已保存草稿', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1000)
  } catch (err) {
    console.error('保存草稿失败', err)
  } finally {
    submitting.value = false
  }
}

const handleSubmit = async () => {
  if (!content.value.trim() && imageList.value.length === 0 && fileList.value.length === 0) {
    uni.showToast({ title: '请输入内容或上传附件', icon: 'none' })
    return
  }

  submitting.value = true
  try {
    const urls = await uploadAllFiles()
    await submitHomework(homeworkId.value, {
      content: content.value,
      attachmentUrls: urls,
      draft: false
    })
    uni.showToast({ title: '提交成功', icon: 'success' })
    setTimeout(() => uni.switchTab({ url: '/pages/student/homework-list' }), 1500)
  } catch (err) {
    console.error('提交失败', err)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.page { min-height: 100vh; background: #f4f6f9; padding: 30rpx; box-sizing: border-box; }
.card { background: #fff; border-radius: 16rpx; padding: 30rpx; margin-bottom: 24rpx; box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04); }
.section-title { font-size: 30rpx; font-weight: bold; margin-bottom: 8rpx; display: block; }
.section-hint { font-size: 24rpx; color: #aaa; margin-bottom: 20rpx; display: block; }

.content-input {
  width: 100%; min-height: 180rpx; padding: 20rpx;
  background: #f8f9fa; border-radius: 12rpx; font-size: 28rpx; box-sizing: border-box;
}

/* 图片网格 */
.file-grid { display: flex; flex-wrap: wrap; gap: 16rpx; }
.file-item { position: relative; }
.image-item { width: 180rpx; height: 180rpx; }
.upload-img { width: 100%; height: 100%; border-radius: 12rpx; }
.remove-btn { position: absolute; top: -10rpx; right: -10rpx; width: 40rpx; height: 40rpx; background: #f44336; color: #fff; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 24rpx; }

.add-btn {
  width: 180rpx; height: 180rpx; background: #f8f9fa; border: 2rpx dashed #ccc;
  border-radius: 12rpx; display: flex; flex-direction: column; align-items: center; justify-content: center;
}
.add-btn .add-icon { font-size: 48rpx; color: #ccc; }
.add-btn .add-label { font-size: 22rpx; color: #aaa; margin-top: 6rpx; }

/* 文件列表 */
.file-list { margin-top: 16rpx; }
.file-row {
  display: flex; align-items: center; gap: 16rpx; padding: 20rpx 16rpx;
  background: #f8f9fa; border-radius: 12rpx; margin-bottom: 12rpx;
}
.file-icon { font-size: 40rpx; }
.file-info { flex: 1; }
.file-name { display: block; font-size: 28rpx; color: #333; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.file-size { display: block; font-size: 22rpx; color: #aaa; margin-top: 4rpx; }
.remove-btn-sm { font-size: 24rpx; color: #f44336; padding: 8rpx 16rpx; }

.add-file-btn {
  display: flex; align-items: center; justify-content: center; gap: 12rpx;
  padding: 24rpx; background: #f8f9fa; border: 2rpx dashed #ccc; border-radius: 12rpx;
}
.add-file-btn .add-icon { font-size: 36rpx; }
.add-file-btn .add-label { font-size: 26rpx; color: #999; }

.submit-btn { margin-top: 20rpx; width: 100%; }

/* 操作按钮组 */
.action-buttons { display: flex; gap: 20rpx; margin-top: 24rpx; }
.btn-save {
  flex: 1; height: 84rpx; line-height: 84rpx; font-size: 30rpx; font-weight: 600;
  background: #fff; color: #4A90D9; border: 2rpx solid #4A90D9; border-radius: 42rpx; padding: 0;
}
.btn-submit {
  flex: 1; height: 84rpx; line-height: 84rpx; font-size: 30rpx; font-weight: 600;
  border-radius: 42rpx; padding: 0;
}
</style>
