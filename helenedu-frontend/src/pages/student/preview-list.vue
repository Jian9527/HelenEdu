<template>
  <view class="container">
    <view v-if="list.length > 0">
      <view class="card material-item" v-for="item in list" :key="item.id">
        <view class="material-header">
          <text class="material-title">{{ item.title }}</text>
          <text class="tag tag-info">{{ item.subject || '通用' }}</text>
        </view>
        <text class="material-desc" v-if="item.description">{{ item.description }}</text>
        <view class="material-footer">
          <text class="material-meta">{{ item.className }} - {{ item.teacherName }}</text>
          <text class="material-date">{{ formatDate(item.createdAt) }}</text>
        </view>
        <view class="file-list" v-if="item.fileUrls && item.fileUrls.length">
          <view class="file-item" v-for="(url, idx) in item.fileUrls" :key="idx" @click="openFile(url)">
            <text class="file-icon">📄</text>
            <text class="file-name">文件{{ idx + 1 }}</text>
          </view>
        </view>
      </view>
    </view>
    <view v-else class="empty">
      <text class="empty-text">暂无预习资料</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getStudentPreviewList } from '../../api/index'

const list = ref([])

const fetchList = async () => {
  try {
    const res = await getStudentPreviewList({ page: 1, size: 50 })
    list.value = res.records
  } catch (err) {
    console.error(err)
  }
}

const openFile = (url) => {
  if (url.match(/\.(jpg|jpeg|png|gif|webp)$/i)) {
    uni.previewImage({ urls: [url], current: url })
  } else {
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
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${d.getMonth() + 1}-${d.getDate()}`
}

onMounted(() => fetchList())
</script>

<style scoped>
.material-item { margin-bottom: 20rpx; }
.material-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12rpx; }
.material-title { font-size: 32rpx; font-weight: bold; flex: 1; margin-right: 16rpx; }
.material-desc { font-size: 26rpx; color: #666; margin-bottom: 16rpx; display: block; line-height: 1.6; }
.material-footer { display: flex; justify-content: space-between; font-size: 24rpx; color: #999; }
.file-list { display: flex; flex-wrap: wrap; gap: 16rpx; margin-top: 16rpx; }
.file-item { display: flex; align-items: center; background: #f0f5ff; padding: 12rpx 20rpx; border-radius: 8rpx; }
.file-icon { margin-right: 8rpx; }
.file-name { font-size: 24rpx; color: #4A90D9; }
</style>
