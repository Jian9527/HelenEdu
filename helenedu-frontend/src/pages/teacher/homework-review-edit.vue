<template>
  <view class="container">
    <!-- 学生信息 -->
    <view class="card" v-if="submit">
      <view class="student-info">
        <text class="student-name">{{ submit.studentName }}</text>
        <view :class="['tag', getStatusClass(submit.status)]">{{ submit.statusName }}</view>
      </view>
      <text class="submit-time">提交时间: {{ formatDateTime(submit.submitTime) }}</text>
    </view>

    <!-- 提交内容 -->
    <view class="card" v-if="submit">
      <text class="section-title">提交内容</text>
      <text class="content-text" v-if="submit.content">{{ submit.content }}</text>
      <text class="content-empty" v-else>（无文字内容）</text>

      <!-- 附件 -->
      <view class="attachments" v-if="submit.attachmentUrls && submit.attachmentUrls.length">
        <text class="section-title">附件</text>
        <!-- 图片附件 -->
        <view class="image-list" v-if="imageAttachments.length">
          <image
            v-for="(url, idx) in imageAttachments"
            :key="'img-' + idx"
            :src="url"
            mode="widthFix"
            class="attach-img"
            @click="previewImage(url)"
          />
        </view>
        <!-- 文件附件 -->
        <view class="file-list" v-if="fileAttachments.length">
          <view class="file-item" v-for="(item, idx) in fileAttachments" :key="'file-' + idx" @click="openFile(item.url)">
            <text class="file-icon">📄</text>
            <text class="file-name">{{ item.name }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 批改表单 -->
    <view class="card" v-if="showForm">
      <text class="section-title">{{ submit && submit.status === 1 ? '重新批改' : '批改' }}</text>
      <view class="form-item">
        <text class="form-label">分数 <text class="required">*</text></text>
        <input class="form-input" v-model="form.score" type="digit" placeholder="输入分数" />
      </view>
      <view class="form-item">
        <text class="form-label">评语</text>
        <textarea class="form-textarea" v-model="form.comment" placeholder="输入评语（可选）" auto-height />
      </view>
    </view>

    <!-- 已批改查看模式：显示当前批改结果 -->
    <view class="card" v-if="submit && submit.status === 1 && !showForm">
      <text class="section-title">批改结果</text>
      <view class="review-info">
        <text class="review-label">分数</text>
        <text class="review-value">{{ submit.score }}</text>
      </view>
      <view class="review-info" v-if="submit.comment">
        <text class="review-label">评语</text>
        <text class="review-value">{{ submit.comment }}</text>
      </view>
    </view>

    <!-- 操作按钮 -->
    <!-- 已批改查看模式：显示“重新批改”按钮 -->
    <view class="action-buttons" v-if="submit && submit.status === 1 && !showForm">
      <button class="btn-regrade" @click="showForm = true">重新批改</button>
    </view>
    <!-- 批改模式：显示“退回”和“通过”按钮 -->
    <view class="action-buttons" v-else-if="showForm">
      <button class="btn-reject" @click="handleReject" :disabled="submitting">
        {{ submitting ? '处理中...' : '退回' }}
      </button>
      <button class="btn-approve" @click="handleApprove" :disabled="submitting">
        {{ submitting ? '处理中...' : '通过' }}
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getSubmitDetail, reviewHomework } from '../../api/index'

const submitId = ref('')
const submit = ref(null)
const form = ref({ score: '', comment: '' })
const submitting = ref(false)
const showForm = ref(true) // 默认显示表单

onMounted(async () => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  submitId.value = currentPage.options.submitId || ''
  
  if (submitId.value) {
    try {
      const data = await getSubmitDetail(submitId.value)
      submit.value = data
      // 加载已有的分数和评语
      form.value.score = data.score ? String(data.score) : ''
      form.value.comment = data.comment || ''
      // 如果是已批改状态，默认显示详情，点击“重新批改”才显示表单
      if (data.status === 1) {
        showForm.value = false
      }
    } catch (err) {
      console.error('加载提交详情失败', err)
      uni.showToast({ title: '加载失败', icon: 'none' })
    }
  }
})

const isImage = (url) => /\.(jpg|jpeg|png|gif|webp|bmp)(\?.*)?$/i.test(url)

const getFileName = (url) => {
  const nameMatch = url.match(/[?&]name=([^&#]+)/)
  if (nameMatch) return decodeURIComponent(nameMatch[1])
  const path = url.split('?')[0]
  return decodeURIComponent(path.split('/').pop() || '文件')
}

const imageAttachments = computed(() => {
  if (!submit.value?.attachmentUrls) return []
  return submit.value.attachmentUrls.filter(isImage)
})

const fileAttachments = computed(() => {
  if (!submit.value?.attachmentUrls) return []
  return submit.value.attachmentUrls.filter(url => !isImage(url)).map(url => ({
    url,
    name: getFileName(url)
  }))
})

const previewImage = (url) => {
  uni.previewImage({ urls: imageAttachments.value, current: url })
}

const openFile = (url) => {
  // #ifdef H5
  window.open(url, '_blank')
  // #endif
  // #ifdef MP-WEIXIN
  uni.downloadFile({
    url,
    success: (res) => { uni.openDocument({ filePath: res.tempFilePath }) }
  })
  // #endif
}

const handleApprove = async () => {
  if (!form.value.score) {
    uni.showToast({ title: '请输入分数', icon: 'none' })
    return
  }
  submitting.value = true
  try {
    await reviewHomework(submitId.value, {
      score: parseFloat(form.value.score),
      comment: form.value.comment,
      status: 1
    })
    uni.showToast({ title: '批改成功', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1000)
  } catch (err) {
    console.error('批改失败', err)
  } finally {
    submitting.value = false
  }
}

const handleReject = async () => {
  submitting.value = true
  try {
    await reviewHomework(submitId.value, {
      score: parseFloat(form.value.score) || 0,
      comment: form.value.comment || '请重新完成',
      status: 2
    })
    uni.showToast({ title: '已退回', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1000)
  } catch (err) {
    console.error('退回失败', err)
  } finally {
    submitting.value = false
  }
}

const getStatusClass = (status) => {
  return { 0: 'tag-warning', 1: 'tag-success', 2: 'tag-error' }[status] || 'tag-info'
}

const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getMonth()+1}/${d.getDate()} ${d.getHours()}:${String(d.getMinutes()).padStart(2,'0')}`
}
</script>

<style scoped>
.student-info { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12rpx; }
.student-name { font-size: 32rpx; font-weight: bold; }
.submit-time { font-size: 24rpx; color: #999; }

.section-title { font-size: 30rpx; font-weight: bold; margin-bottom: 12rpx; display: block; }
.content-text { font-size: 28rpx; color: #333; line-height: 1.8; display: block; }
.content-empty { font-size: 28rpx; color: #ccc; display: block; }

.attachments { margin-top: 24rpx; padding-top: 24rpx; border-top: 1rpx solid #eee; }
.image-list { display: flex; flex-wrap: wrap; gap: 16rpx; margin-bottom: 16rpx; }
.attach-img { width: 200rpx; border-radius: 12rpx; }
.file-list { margin-top: 12rpx; }
.file-item {
  display: flex; align-items: center; gap: 12rpx; padding: 20rpx 24rpx;
  background: #f8f9fa; border-radius: 12rpx; margin-bottom: 10rpx;
}
.file-icon { font-size: 36rpx; }
.file-name { font-size: 26rpx; color: #4A90D9; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; flex: 1; }

.form-item { margin-bottom: 24rpx; }
.form-label { font-size: 26rpx; color: #666; margin-bottom: 8rpx; display: block; }
.required { color: #f44336; }
.form-input { width: 100%; height: 80rpx; background: #f8f9fa; border-radius: 12rpx; padding: 0 24rpx; font-size: 28rpx; box-sizing: border-box; }
.form-textarea { width: 100%; min-height: 160rpx; background: #f8f9fa; border-radius: 12rpx; padding: 24rpx; font-size: 28rpx; box-sizing: border-box; }

.action-buttons { display: flex; gap: 24rpx; margin-top: 30rpx; }
.btn-reject {
  flex: 1; height: 84rpx; line-height: 84rpx; font-size: 30rpx; font-weight: 600;
  background: #fff; color: #f44336; border: 2rpx solid #f44336; border-radius: 42rpx; padding: 0;
}
.btn-approve {
  flex: 1; height: 84rpx; line-height: 84rpx; font-size: 30rpx; font-weight: 600;
  background: #4A90D9; color: #fff; border: none; border-radius: 42rpx; padding: 0;
}
.btn-regrade {
  width: 100%; height: 84rpx; line-height: 84rpx; font-size: 30rpx; font-weight: 600;
  background: #4A90D9; color: #fff; border: none; border-radius: 42rpx; padding: 0;
}
.btn-reject::after, .btn-approve::after, .btn-regrade::after { border: none; }

.review-info { display: flex; justify-content: space-between; padding: 12rpx 0; border-bottom: 1rpx solid #f0f0f0; }
.review-info:last-child { border-bottom: none; }
.review-label { font-size: 26rpx; color: #999; }
.review-value { font-size: 28rpx; color: #333; font-weight: 500; max-width: 60%; text-align: right; }
</style>
