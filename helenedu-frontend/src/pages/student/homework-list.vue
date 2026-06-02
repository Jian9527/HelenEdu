<template>
  <view class="container">
    <!-- Tab 切换 -->
    <view class="tab-bar">
      <view 
        v-for="tab in tabs" 
        :key="tab.value" 
        :class="['tab-item', { active: activeTab === tab.value }]"
        @click="switchTab(tab.value)"
      >
        {{ tab.label }}
      </view>
    </view>

    <!-- 作业列表 -->
    <view v-if="homeworkList.length > 0">
      <view 
        class="card homework-item" 
        v-for="item in homeworkList" 
        :key="item.id"
        @click="goDetail(item.id)"
      >
        <view class="hw-header">
          <text class="hw-title">{{ item.title }}</text>
          <view :class="['tag', getStatusClass(item.mySubmitStatus)]">
            {{ getStatusText(item.mySubmitStatus) }}
          </view>
        </view>
        <view class="hw-info">
          <text class="hw-class">{{ item.className }}</text>
          <text class="hw-subject" v-if="item.subject">{{ item.subject }}</text>
        </view>
        <view class="hw-footer">
          <text class="hw-deadline" v-if="item.deadline">
            截止: {{ formatDate(item.deadline) }}
          </text>
          <text class="hw-teacher">教师: {{ item.teacherName }}</text>
        </view>
      </view>
    </view>

    <view v-else class="empty">
      <text class="empty-text">暂无作业</text>
    </view>

    <!-- 加载更多 -->
    <view v-if="hasMore" class="load-more" @click="loadMore">
      <text>{{ loading ? '加载中...' : '加载更多' }}</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getStudentHomeworkList } from '../../api/index'
import { onShow } from '@dcloudio/uni-app'

const tabs = [
  { label: '全部', value: null },
  { label: '待完成', value: 0 },
  { label: '已提交', value: 1 },
  { label: '已批改', value: 2 }
]

const activeTab = ref(null)
const homeworkList = ref([])
const page = ref(1)
const hasMore = ref(true)
const loading = ref(false)

const switchTab = (value) => {
  activeTab.value = value
  page.value = 1
  homeworkList.value = []
  hasMore.value = true
  fetchList()
}

const fetchList = async () => {
  if (loading.value) return
  loading.value = true
  try {
    const res = await getStudentHomeworkList({ 
      page: page.value, 
      size: 10,
      status: activeTab.value 
    })
    if (page.value === 1) {
      homeworkList.value = res.records
    } else {
      homeworkList.value = [...homeworkList.value, ...res.records]
    }
    hasMore.value = homeworkList.value.length < res.total
  } catch (err) {
    console.error(err)
  } finally {
    loading.value = false
  }
}

const loadMore = () => {
  page.value++
  fetchList()
}

const goDetail = (id) => {
  uni.navigateTo({ url: `/pages/student/homework-detail?id=${id}` })
}

const getStatusText = (status) => {
  const map = { '-1': '草稿', 0: '未提交', 1: '已提交', 2: '已批改', 3: '已退回' }
  return map[status] || '未提交'
}

const getStatusClass = (status) => {
  const map = { 0: 'tag-warning', 1: 'tag-info', 2: 'tag-success', 3: 'tag-error' }
  return map[status] || 'tag-warning'
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getMonth() + 1}/${d.getDate()} ${d.getHours()}:${String(d.getMinutes()).padStart(2, '0')}`
}

onMounted(() => {
  fetchList()
})

// 每次页面显示时刷新（从其他页面返回时更新状态）
onShow(() => {
  if (homeworkList.value.length > 0) {
    page.value = 1
    fetchList()
  }
})
</script>

<style scoped>
.tab-bar {
  display: flex;
  background: #fff;
  border-radius: 12rpx;
  margin-bottom: 20rpx;
  padding: 10rpx;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 16rpx 0;
  font-size: 28rpx;
  color: #666;
  border-radius: 8rpx;
}

.tab-item.active {
  background: #4A90D9;
  color: #fff;
  font-weight: bold;
}

.homework-item {
  margin-bottom: 20rpx;
}

.hw-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.hw-title {
  font-size: 32rpx;
  font-weight: bold;
  flex: 1;
  margin-right: 16rpx;
}

.hw-info {
  display: flex;
  gap: 20rpx;
  margin-bottom: 12rpx;
}

.hw-class, .hw-subject {
  font-size: 24rpx;
  color: #999;
}

.hw-footer {
  display: flex;
  justify-content: space-between;
  font-size: 24rpx;
  color: #999;
}

.load-more {
  text-align: center;
  padding: 30rpx;
  color: #999;
  font-size: 26rpx;
}
</style>
