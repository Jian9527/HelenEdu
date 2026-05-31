<template>
  <view class="container">
    <view class="tab-bar">
      <view :class="['tab-item', { active: activeTab === null }]" @click="switchTab(null)">全部</view>
      <view :class="['tab-item', { active: activeTab === 0 }]" @click="switchTab(0)">待批改</view>
      <view :class="['tab-item', { active: activeTab === 1 }]" @click="switchTab(1)">已批改</view>
      <view :class="['tab-item', { active: activeTab === 2 }]" @click="switchTab(2)">已退回</view>
    </view>

    <view v-if="submits.length > 0">
      <view class="card submit-item" v-for="item in submits" :key="item.id" @click="openReview(item)">
        <view class="submit-header">
          <text class="student-name">{{ item.studentName }}</text>
          <view :class="['tag', getStatusClass(item.status)]">{{ item.statusName }}</view>
        </view>
        <text class="submit-content" v-if="item.content">{{ item.content.substring(0, 50) }}...</text>
        <view class="submit-footer">
          <text class="submit-time">提交: {{ formatDateTime(item.submitTime) }}</text>
          <text class="score" v-if="item.score">得分: {{ item.score }}</text>
        </view>
      </view>
    </view>
    <view v-else class="empty">
      <text class="empty-text">暂无提交</text>
    </view>

    <!-- 批改弹窗 -->
    <view class="modal-mask" v-if="showReview" @click="showReview = false">
      <view class="modal-content" @click.stop>
        <text class="modal-title">批改作业</text>
        <view v-if="currentSubmit">
          <text class="review-student">学生: {{ currentSubmit.studentName }}</text>
          <view class="review-content" v-if="currentSubmit.content">
            <text class="section-label">提交内容:</text>
            <text class="review-text">{{ currentSubmit.content }}</text>
          </view>
          <view class="form-item">
            <text class="section-label">分数</text>
            <input class="form-input" v-model="reviewForm.score" type="digit" placeholder="输入分数" />
          </view>
          <view class="form-item">
            <text class="section-label">评语</text>
            <textarea class="form-textarea" v-model="reviewForm.comment" placeholder="输入评语（可选）" auto-height />
          </view>
          <view class="modal-actions">
            <button class="btn-reject" @click="handleReject">退回</button>
            <button class="btn-approve" @click="handleApprove">通过</button>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getHomeworkSubmits, reviewHomework } from '../../api/index'

const homeworkId = ref('')
const submits = ref([])
const activeTab = ref(null)
const showReview = ref(false)
const currentSubmit = ref(null)
const reviewForm = ref({ score: '', comment: '' })

onMounted(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  homeworkId.value = currentPage.options.id || ''
  fetchSubmits()
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

const openReview = (item) => {
  currentSubmit.value = item
  reviewForm.value = { score: item.score || '', comment: item.comment || '' }
  showReview.value = true
}

const handleApprove = async () => {
  if (!reviewForm.value.score) {
    uni.showToast({ title: '请输入分数', icon: 'none' })
    return
  }
  try {
    await reviewHomework(currentSubmit.value.id, {
      score: parseFloat(reviewForm.value.score),
      comment: reviewForm.value.comment,
      status: 1
    })
    uni.showToast({ title: '批改成功', icon: 'success' })
    showReview.value = false
    fetchSubmits()
  } catch (err) { console.error(err) }
}

const handleReject = async () => {
  try {
    await reviewHomework(currentSubmit.value.id, {
      score: parseFloat(reviewForm.value.score) || 0,
      comment: reviewForm.value.comment || '请重新完成',
      status: 2
    })
    uni.showToast({ title: '已退回', icon: 'success' })
    showReview.value = false
    fetchSubmits()
  } catch (err) { console.error(err) }
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
.submit-footer { display: flex; justify-content: space-between; font-size: 24rpx; color: #999; }
.score { color: #4A90D9; font-weight: bold; }
.modal-mask { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 999; }
.modal-content { width: 85%; background: #fff; border-radius: 20rpx; padding: 40rpx; max-height: 80vh; overflow-y: auto; }
.modal-title { font-size: 34rpx; font-weight: bold; margin-bottom: 24rpx; display: block; }
.review-student { font-size: 28rpx; color: #666; margin-bottom: 16rpx; display: block; }
.section-label { font-size: 26rpx; color: #999; margin-bottom: 8rpx; display: block; }
.review-text { font-size: 28rpx; color: #333; line-height: 1.6; display: block; margin-bottom: 20rpx; }
.form-item { margin-bottom: 20rpx; }
.form-input { width: 100%; height: 80rpx; background: #f8f9fa; border-radius: 8rpx; padding: 0 20rpx; font-size: 28rpx; box-sizing: border-box; }
.form-textarea { width: 100%; min-height: 120rpx; background: #f8f9fa; border-radius: 8rpx; padding: 20rpx; font-size: 28rpx; box-sizing: border-box; }
.modal-actions { display: flex; gap: 20rpx; margin-top: 24rpx; }
.btn-reject { flex: 1; height: 80rpx; line-height: 80rpx; background: #fff; color: #f44336; border: 1rpx solid #f44336; border-radius: 8rpx; font-size: 28rpx; }
.btn-approve { flex: 1; height: 80rpx; line-height: 80rpx; background: #4A90D9; color: #fff; border: none; border-radius: 8rpx; font-size: 28rpx; }
</style>
