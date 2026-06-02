<template>
  <view class="container">
    <view class="card" v-if="homework">
      <view class="detail-header">
        <text class="detail-title">{{ homework.title }}</text>
        <view :class="['tag', getStatusClass(homework.mySubmitStatus)]">
          {{ getStatusText(homework.mySubmitStatus) }}
        </view>
      </view>

      <view class="detail-meta">
        <view class="meta-item">
          <text class="meta-label">班级</text>
          <text class="meta-value">{{ homework.className }}</text>
        </view>
        <view class="meta-item">
          <text class="meta-label">科目</text>
          <text class="meta-value">{{ homework.subject || '-' }}</text>
        </view>
        <view class="meta-item">
          <text class="meta-label">教师</text>
          <text class="meta-value">{{ homework.teacherName }}</text>
        </view>
        <view class="meta-item" v-if="homework.deadline">
          <text class="meta-label">截止时间</text>
          <text class="meta-value">{{ homework.deadline }}</text>
        </view>
      </view>

      <view class="detail-content" v-if="homework.content">
        <text class="section-title">作业要求</text>
        <text class="content-text">{{ homework.content }}</text>
      </view>

      <!-- 附件 -->
      <view class="detail-attachments" v-if="homework.attachmentUrls && homework.attachmentUrls.length">
        <text class="section-title">附件</text>
        <!-- 图片附件 -->
        <view class="attachment-list" v-if="imageAttachments.length">
          <image 
            v-for="(url, idx) in imageAttachments" 
            :key="'img-' + idx" 
            :src="url" 
            mode="widthFix"
            class="attachment-img"
            @click="previewImage(url)"
          />
        </view>
        <!-- 文件附件 -->
        <view class="file-attachments" v-if="fileAttachments.length">
          <view class="file-attach-item" v-for="(item, idx) in fileAttachments" :key="'file-' + idx" @click="openFile(item.url)">
            <text class="file-icon">📄</text>
            <text class="file-name">{{ item.name }}</text>
          </view>
        </view>
      </view>

      <!-- 批改结果 -->
      <view class="review-result" v-if="homework.mySubmitStatus === 2 && submitDetail">
        <text class="section-title">批改结果</text>
        <view class="score-area">
          <text class="score-label">分数</text>
          <text class="score-value">{{ submitDetail.score }}</text>
        </view>
        <view class="comment-area" v-if="submitDetail.comment">
          <text class="comment-label">评语</text>
          <text class="comment-text">{{ submitDetail.comment }}</text>
        </view>
      </view>
    </view>

    <!-- 操作按钮 -->
    <view class="action-bar" v-if="homework">
      <!-- 未提交 / 草稿 / 已退回：可进入做作业页面 -->
      <button 
        v-if="homework.mySubmitStatus === 0" 
        class="btn-primary action-btn"
        @click="goSubmit"
      >
        开始做作业
      </button>
      <button 
        v-else-if="homework.mySubmitStatus === -1" 
        class="btn-primary action-btn"
        @click="goSubmit"
      >
        继续做作业
      </button>
      <button 
        v-else-if="homework.mySubmitStatus === 3" 
        class="btn-primary action-btn"
        @click="goSubmit"
      >
        重新提交
      </button>
      <!-- 已提交 / 已批改 -->
      <view v-else class="submitted-tip">
        <text>{{ homework.mySubmitStatus === 1 ? '已提交，等待批改' : '已批改' }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getHomeworkDetail, getSubmitDetail } from '../../api/index'
import { onShow } from '@dcloudio/uni-app'

const homework = ref(null)
const submitDetail = ref(null)
const homeworkId = ref('')

onMounted(async () => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  homeworkId.value = currentPage.options.id
  
  await fetchDetail()
})

// 每次页面显示时刷新数据（从提交页返回时更新状态）
onShow(() => {
  if (homeworkId.value) {
    fetchDetail()
  }
})

const fetchDetail = async () => {
  try {
    homework.value = await getHomeworkDetail(homeworkId.value)
    console.log('作业详情:', JSON.stringify(homework.value))
    console.log('附件列表:', homework.value.attachmentUrls)
    // 如果已批改，获取批改详情
    if (homework.value.mySubmitStatus === 2 && homework.value.mySubmitId) {
      submitDetail.value = await getSubmitDetail(homework.value.mySubmitId)
    }
  } catch (err) {
    console.error(err)
  }
}

const goSubmit = () => {
  uni.navigateTo({ url: `/pages/student/homework-submit?id=${homeworkId.value}` })
}

const isImage = (url) => /\.(jpg|jpeg|png|gif|webp|bmp)(\?.*)?$/i.test(url)

const getFileName = (url) => {
  const nameMatch = url.match(/[?&]name=([^&#]+)/)
  if (nameMatch) return decodeURIComponent(nameMatch[1])
  const path = url.split('?')[0]
  return decodeURIComponent(path.split('/').pop() || '文件')
}

const imageAttachments = computed(() => {
  if (!homework.value?.attachmentUrls) return []
  return homework.value.attachmentUrls.filter(isImage)
})

const fileAttachments = computed(() => {
  if (!homework.value?.attachmentUrls) return []
  return homework.value.attachmentUrls.filter(url => !isImage(url)).map(url => ({
    url,
    name: getFileName(url)
  }))
})

const openFile = (url) => {
  // #ifdef H5
  window.open(url, '_blank')
  // #endif
  // #ifdef MP-WEIXIN
  uni.downloadFile({
    url,
    success: (res) => {
      uni.openDocument({ filePath: res.tempFilePath })
    }
  })
  // #endif
}

const previewImage = (url) => {
  uni.previewImage({
    urls: imageAttachments.value,
    current: url
  })
}

const getStatusText = (status) => {
  const map = { '-1': '草稿', 0: '未提交', 1: '已提交', 2: '已批改', 3: '已退回' }
  return map[status] || '未提交'
}

const getStatusClass = (status) => {
  const map = { 0: 'tag-warning', 1: 'tag-info', 2: 'tag-success', 3: 'tag-error' }
  return map[status] || 'tag-warning'
}
</script>

<style scoped>
.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 30rpx;
}

.detail-title {
  font-size: 36rpx;
  font-weight: bold;
  flex: 1;
  margin-right: 20rpx;
}

.detail-meta {
  background: #f8f9fa;
  border-radius: 12rpx;
  padding: 24rpx;
  margin-bottom: 30rpx;
}

.meta-item {
  display: flex;
  justify-content: space-between;
  padding: 10rpx 0;
}

.meta-label {
  color: #999;
  font-size: 26rpx;
}

.meta-value {
  color: #333;
  font-size: 26rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: bold;
  margin-bottom: 16rpx;
  display: block;
}

.content-text {
  font-size: 28rpx;
  color: #555;
  line-height: 1.8;
}

.detail-content {
  margin-bottom: 30rpx;
}

.attachment-img {
  width: 200rpx;
  border-radius: 8rpx;
  margin-right: 16rpx;
  margin-bottom: 16rpx;
}

.attachment-list {
  display: flex;
  flex-wrap: wrap;
}

.file-attachments { margin-top: 16rpx; }
.file-attach-item {
  display: flex; align-items: center; gap: 12rpx; padding: 16rpx 20rpx;
  background: #f8f9fa; border-radius: 12rpx; margin-bottom: 10rpx;
}
.file-attach-item .file-icon { font-size: 36rpx; }
.file-attach-item .file-name { font-size: 26rpx; color: #4A90D9; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; flex: 1; }

.review-result {
  margin-top: 30rpx;
  padding-top: 30rpx;
  border-top: 1rpx solid #eee;
}

.score-area {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
}

.score-label {
  font-size: 28rpx;
  color: #666;
  margin-right: 20rpx;
}

.score-value {
  font-size: 48rpx;
  font-weight: bold;
  color: #4A90D9;
}

.comment-text {
  font-size: 28rpx;
  color: #555;
  line-height: 1.6;
}

.action-bar {
  margin-top: 40rpx;
}

.action-btn {
  width: 100%;
}

.submitted-tip {
  text-align: center;
  padding: 30rpx;
  color: #999;
}
</style>
