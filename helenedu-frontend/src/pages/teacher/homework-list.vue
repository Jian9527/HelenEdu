<template>
  <view class="page">
    <!-- 顶部操作栏 -->
    <view class="action-bar">
      <view class="action-card students-card" @click="goStudents">
        <text class="action-icon">👨‍🎓</text>
        <text class="action-label">管理学生</text>
      </view>
      <view class="action-card create-card" @click="goCreate">
        <text class="action-icon">📝</text>
        <text class="action-label">布置作业</text>
      </view>
    </view>

    <!-- 作业列表 -->
    <view v-if="homeworkList.length > 0" class="hw-list">
      <view class="hw-card" v-for="item in homeworkList" :key="item.id" @click="goDetail(item.id)">
        <view class="hw-top">
          <text class="hw-title">{{ item.title }}</text>
          <view :class="['status-tag', item.status === 1 ? 'published' : 'draft']">
            {{ item.status === 1 ? '已发布' : '草稿' }}
          </view>
        </view>
        <view class="hw-stats">
          <view class="stat-item">
            <text class="stat-num">{{ item.submitCount }}/{{ item.totalCount }}</text>
            <text class="stat-label">已提交</text>
          </view>
          <view class="stat-item">
            <text class="stat-num">{{ item.reviewedCount }}</text>
            <text class="stat-label">已批改</text>
          </view>
          <view class="stat-item">
            <text class="stat-num">{{ formatDate(item.createdAt) }}</text>
            <text class="stat-label">发布时间</text>
          </view>
        </view>
        <view class="hw-bottom">
          <button class="btn-detail" @click.stop="goDetail(item.id)">作业详情</button>
          <button class="btn-review" @click.stop="goReview(item.id)">查看提交</button>
        </view>
      </view>
    </view>
    <view v-else class="empty-state">
      <text class="empty-icon">📋</text>
      <text class="empty-title">还没有布置作业</text>
      <text class="empty-desc">点击上方「布置作业」开始</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getTeacherHomeworkList } from '../../api/index'

const classId = ref('')
const className = ref('')
const homeworkList = ref([])

onMounted(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  classId.value = currentPage.options.classId || currentPage.options.classid || ''
  className.value = currentPage.options.className || ''
  if (className.value) {
    uni.setNavigationBarTitle({ title: className.value + ' - 作业管理' })
  }
  fetchList()
})

const fetchList = async () => {
  try {
    const res = await getTeacherHomeworkList({ classId: classId.value, page: 1, size: 50 })
    homeworkList.value = res.records
  } catch (err) {
    console.error(err)
  }
}

const goStudents = () => {
  uni.navigateTo({ url: `/pages/teacher/class-students?classId=${classId.value}&className=${encodeURIComponent(className.value)}` })
}
const goDetail = (id) => {
  uni.navigateTo({ url: `/pages/teacher/homework-detail?id=${id}&classId=${classId.value}` })
}
const goCreate = () => {
  uni.navigateTo({ url: `/pages/teacher/homework-create?classId=${classId.value}` })
}
const goEdit = (id) => {
  uni.navigateTo({ url: `/pages/teacher/homework-create?id=${id}&classId=${classId.value}` })
}
const goReview = (id) => {
  uni.navigateTo({ url: `/pages/teacher/homework-review?id=${id}` })
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  return `${d.getMonth() + 1}/${d.getDate()} ${d.getHours()}:${String(d.getMinutes()).padStart(2, '0')}`
}
</script>

<style scoped>
.page { min-height: 100vh; background: #f4f6f9; padding: 30rpx; box-sizing: border-box; }

/* 顶部操作栏 */
.action-bar { display: grid; grid-template-columns: 1fr 1fr; gap: 24rpx; margin-bottom: 36rpx; }
.action-card {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  padding: 36rpx 0; border-radius: 20rpx; box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.06);
}
.students-card { background: linear-gradient(135deg, #e8f0fe 0%, #d4e4fc 100%); }
.create-card { background: linear-gradient(135deg, #4A90D9 0%, #6a7fdb 100%); }
.create-card .action-label { color: #fff; }
.create-card .action-icon { filter: brightness(10); }
.action-icon { font-size: 52rpx; margin-bottom: 12rpx; }
.action-label { font-size: 28rpx; font-weight: 600; color: #4A90D9; }

/* 作业卡片 */
.hw-list { }
.hw-card {
  background: #fff; border-radius: 20rpx; padding: 32rpx;
  margin-bottom: 24rpx; box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.06);
}
.hw-top { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 24rpx; }
.hw-title { font-size: 32rpx; font-weight: bold; color: #1a1a1a; flex: 1; line-height: 1.4; }
.status-tag {
  font-size: 22rpx; padding: 6rpx 16rpx; border-radius: 20rpx; font-weight: 600;
  white-space: nowrap; margin-left: 16rpx;
}
.status-tag.published { background: #e6f9ee; color: #22a357; }
.status-tag.draft { background: #fff3e0; color: #e67700; }

.hw-stats {
  display: flex; gap: 0; margin-bottom: 24rpx;
  background: #f8f9fb; border-radius: 12rpx; padding: 20rpx 0;
}
.stat-item {
  flex: 1; display: flex; flex-direction: column; align-items: center;
  border-right: 1rpx solid #eef0f3;
}
.stat-item:last-child { border-right: none; }
.stat-num { font-size: 28rpx; font-weight: bold; color: #333; margin-bottom: 6rpx; }
.stat-label { font-size: 22rpx; color: #aaa; }

.hw-bottom { display: flex; justify-content: flex-end; gap: 16rpx; }
.btn-detail {
  background: #fff; color: #4A90D9; font-size: 26rpx; font-weight: 600;
  padding: 14rpx 36rpx; border-radius: 30rpx; border: 2rpx solid #4A90D9;
}
.btn-review {
  background: #4A90D9; color: #fff; font-size: 26rpx; font-weight: 600;
  padding: 14rpx 36rpx; border-radius: 30rpx; border: none;
}

/* 空状态 */
.empty-state {
  display: flex; flex-direction: column; align-items: center;
  padding: 100rpx 0 60rpx;
}
.empty-icon { font-size: 80rpx; margin-bottom: 20rpx; }
.empty-title { font-size: 32rpx; font-weight: bold; color: #999; margin-bottom: 10rpx; }
.empty-desc { font-size: 26rpx; color: #bbb; }
</style>
