<template>
  <view class="page">
    <view v-if="classList.length > 0" class="class-grid">
      <view class="class-card" v-for="item in classList" :key="item.id" @click="goClassDetail(item)">
        <view class="class-icon">🏫</view>
        <view class="class-info">
          <text class="class-name">{{ item.name }}</text>
          <text class="class-grade" v-if="item.grade">{{ item.grade }}</text>
        </view>
        <view class="class-stats">
          <view class="stat-chip">
            <text class="stat-chip-num">{{ item.studentCount }}</text>
            <text class="stat-chip-label">学生</text>
          </view>
          <view class="stat-chip">
            <text class="stat-chip-num">{{ item.teacherCount }}</text>
            <text class="stat-chip-label">教师</text>
          </view>
        </view>
        <view class="class-arrow">›</view>
      </view>
    </view>
    <view v-else class="empty-state">
      <text class="empty-icon">🏫</text>
      <text class="empty-title">暂无班级</text>
      <text class="empty-desc">管理员分配班级后将在此显示</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMyClasses } from '../../api/index'

const classList = ref([])

const fetchClasses = async () => {
  try {
    classList.value = await getMyClasses()
  } catch (err) {
    console.error(err)
  }
}

const goClassDetail = (item) => {
  uni.navigateTo({ url: `/pages/teacher/homework-list?classId=${item.id}&className=${item.name}` })
}

onMounted(() => fetchClasses())
</script>

<style scoped>
.page { min-height: 100vh; background: #f4f6f9; padding: 30rpx; box-sizing: border-box; }
.class-grid { display: flex; flex-direction: column; gap: 24rpx; }
.class-card {
  background: #fff; border-radius: 20rpx; padding: 32rpx;
  display: flex; align-items: center; gap: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.06);
}
.class-icon { font-size: 48rpx; }
.class-info { flex: 1; }
.class-name { font-size: 32rpx; font-weight: bold; color: #1a1a1a; display: block; margin-bottom: 6rpx; }
.class-grade { font-size: 24rpx; color: #999; }
.class-stats { display: flex; gap: 16rpx; }
.stat-chip {
  display: flex; flex-direction: column; align-items: center;
  background: #f4f6f9; border-radius: 12rpx; padding: 12rpx 20rpx;
}
.stat-chip-num { font-size: 28rpx; font-weight: bold; color: #4A90D9; }
.stat-chip-label { font-size: 20rpx; color: #aaa; }
.class-arrow { font-size: 36rpx; color: #ccc; }

.empty-state {
  display: flex; flex-direction: column; align-items: center;
  padding: 120rpx 0 60rpx;
}
.empty-icon { font-size: 80rpx; margin-bottom: 20rpx; }
.empty-title { font-size: 32rpx; font-weight: bold; color: #999; margin-bottom: 10rpx; }
.empty-desc { font-size: 26rpx; color: #bbb; }
</style>
