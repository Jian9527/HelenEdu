<template>
  <view class="container">
    <view class="tab-bar">
      <view :class="['tab-item', { active: activeTab === null }]" @click="switchTab(null)">全部</view>
      <view :class="['tab-item', { active: activeTab === 0 }]" @click="switchTab(0)">待批改</view>
      <view :class="['tab-item', { active: activeTab === 1 }]" @click="switchTab(1)">已批改</view>
      <view :class="['tab-item', { active: activeTab === 2 }]" @click="switchTab(2)">已退回</view>
    </view>

    <view v-if="submits.length > 0">
      <view class="card submit-item" v-for="item in submits" :key="item.id" @click="goReview(item)">
        <view class="submit-header">
          <text class="student-name">{{ item.studentName }}</text>
          <view :class="['tag', getStatusClass(item.status)]">{{ item.statusName }}</view>
        </view>
        <text class="submit-content" v-if="item.content">{{ item.content.substring(0, 50) }}...</text>
        <view class="attach-hint" v-if="item.attachmentUrls && item.attachmentUrls.length">
          <text class="attach-count">📎 {{ item.attachmentUrls.length }}个附件</text>
        </view>
        <view class="submit-footer">
          <text class="submit-time">提交: {{ formatDateTime(item.submitTime) }}</text>
          <text class="score" v-if="item.score">得分: {{ item.score }}</text>
        </view>
      </view>
    </view>
    <view v-else class="empty">
      <text class="empty-text">暂无提交</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getHomeworkSubmits } from '../../api/index'
import { onShow } from '@dcloudio/uni-app'

const homeworkId = ref('')
const submits = ref([])
const activeTab = ref(null)

onMounted(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  homeworkId.value = currentPage.options.id || ''
  fetchSubmits()
})

// 从批改页返回时刷新列表
onShow(() => {
  if (submits.value.length > 0) {
    fetchSubmits()
  }
})

const fetchSubmits = async () => {
  try {
    const params = {}
    if (activeTab.value !== null) params.status = activeTab.value
    submits.value = await getHomeworkSubmits(homeworkId.value, params)
  } catch (err) {
    console.error(err)
  }
}

const switchTab = (tab) => {
  activeTab.value = tab
  fetchSubmits()
}

const goReview = (item) => {
  uni.navigateTo({ url: `/pages/teacher/homework-review-edit?submitId=${item.id}` })
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
.tab-bar { display: flex; background: #fff; border-radius: 12rpx; margin-bottom: 20rpx; padding: 10rpx; }
.tab-item { flex: 1; text-align: center; padding: 16rpx 0; font-size: 26rpx; color: #666; border-radius: 8rpx; }
.tab-item.active { background: #4A90D9; color: #fff; font-weight: bold; }
.submit-item { margin-bottom: 16rpx; }
.submit-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8rpx; }
.student-name { font-size: 30rpx; font-weight: bold; }
.submit-content { font-size: 26rpx; color: #666; margin-bottom: 8rpx; display: block; }
.attach-hint { font-size: 24rpx; color: #4A90D9; margin-bottom: 8rpx; }
.submit-footer { display: flex; justify-content: space-between; font-size: 24rpx; color: #999; }
.score { color: #4A90D9; font-weight: bold; }
</style>
