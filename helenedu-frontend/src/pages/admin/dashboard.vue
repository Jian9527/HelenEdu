<template>
  <view class="container">
    <!-- 总览卡片 -->
    <view class="card overview" v-if="overview">
      <text class="card-title">数据总览</text>
      <view class="stats-grid">
        <view class="stat-item">
          <text class="stat-value">{{ overview.totalClasses }}</text>
          <text class="stat-label">班级数</text>
        </view>
        <view class="stat-item">
          <text class="stat-value">{{ overview.totalStudents }}</text>
          <text class="stat-label">学生数</text>
        </view>
        <view class="stat-item">
          <text class="stat-value">{{ overview.totalTeachers }}</text>
          <text class="stat-label">教师数</text>
        </view>
        <view class="stat-item">
          <text class="stat-value">{{ overview.weekHomeworkCount }}</text>
          <text class="stat-label">本周作业</text>
        </view>
      </view>
      <view class="stats-row">
        <view class="stat-item">
          <text class="stat-value highlight">{{ overview.submitRate }}%</text>
          <text class="stat-label">提交率</text>
        </view>
        <view class="stat-item">
          <text class="stat-value highlight">{{ overview.avgScore }}</text>
          <text class="stat-label">平均分</text>
        </view>
      </view>
    </view>

    <!-- 快捷入口 -->
    <view class="card">
      <text class="card-title">快捷操作</text>
      <view class="quick-actions">
        <view class="action-item" @click="goClassManage">
          <text class="action-icon">🏫</text>
          <text class="action-text">班级管理</text>
        </view>
        <view class="action-item" @click="goUserManage">
          <text class="action-icon">👥</text>
          <text class="action-text">人员管理</text>
        </view>
      </view>
    </view>

    <!-- 班级排行 -->
    <view class="card" v-if="rankList.length > 0">
      <text class="card-title">班级提交率排行</text>
      <view class="rank-list">
        <view class="rank-item" v-for="(item, idx) in rankList" :key="item.classId">
          <view class="rank-left">
            <text :class="['rank-no', idx < 3 ? 'top3' : '']">{{ idx + 1 }}</text>
            <text class="rank-name">{{ item.className }}</text>
          </view>
          <view class="rank-right">
            <view class="progress-bar">
              <view class="progress-fill" :style="{ width: item.submitRate + '%' }"></view>
            </view>
            <text class="rank-rate">{{ item.submitRate }}%</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getDashboardOverview, getClassRank } from '../../api/index'

const overview = ref(null)
const rankList = ref([])

const fetchData = async () => {
  try {
    const [overviewData, rankData] = await Promise.all([
      getDashboardOverview(),
      getClassRank()
    ])
    overview.value = overviewData
    rankList.value = rankData
  } catch (err) {
    console.error(err)
  }
}

const goClassManage = () => {
  uni.navigateTo({
    url: '/pages/admin/class-manage',
    fail: (err) => {
      console.error('跳转失败', err)
      uni.redirectTo({ url: '/pages/admin/class-manage' })
    }
  })
}
const goUserManage = () => {
  uni.navigateTo({
    url: '/pages/admin/user-manage',
    fail: (err) => {
      console.error('跳转失败', err)
      uni.redirectTo({ url: '/pages/admin/user-manage' })
    }
  })
}

onMounted(() => fetchData())
</script>

<style scoped>
.card-title { font-size: 32rpx; font-weight: bold; margin-bottom: 24rpx; display: block; }
.stats-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 24rpx; margin-bottom: 24rpx; }
.stat-item { text-align: center; }
.stat-value { display: block; font-size: 44rpx; font-weight: bold; color: #333; }
.stat-value.highlight { color: #4A90D9; }
.stat-label { display: block; font-size: 24rpx; color: #999; margin-top: 8rpx; }
.stats-row { display: flex; justify-content: space-around; padding-top: 24rpx; border-top: 1rpx solid #eee; }
.quick-actions { display: flex; gap: 30rpx; }
.action-item { flex: 1; display: flex; flex-direction: column; align-items: center; padding: 30rpx; background: #f8f9fa; border-radius: 12rpx; }
.action-icon { font-size: 48rpx; margin-bottom: 12rpx; }
.action-text { font-size: 26rpx; color: #666; }
.rank-list { }
.rank-item { display: flex; justify-content: space-between; align-items: center; padding: 16rpx 0; border-bottom: 1rpx solid #f5f5f5; }
.rank-item:last-child { border-bottom: none; }
.rank-left { display: flex; align-items: center; flex: 1; }
.rank-no { width: 48rpx; height: 48rpx; line-height: 48rpx; text-align: center; border-radius: 50%; background: #eee; font-size: 24rpx; margin-right: 16rpx; }
.rank-no.top3 { background: #4A90D9; color: #fff; }
.rank-name { font-size: 28rpx; }
.rank-right { display: flex; align-items: center; gap: 12rpx; }
.progress-bar { width: 120rpx; height: 16rpx; background: #eee; border-radius: 8rpx; overflow: hidden; }
.progress-fill { height: 100%; background: #4A90D9; border-radius: 8rpx; transition: width 0.3s; }
.rank-rate { font-size: 24rpx; color: #4A90D9; font-weight: bold; width: 80rpx; text-align: right; }
</style>
