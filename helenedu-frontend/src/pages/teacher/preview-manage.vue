<template>
  <view class="container">
    <view v-if="list.length > 0">
      <view class="card" v-for="item in list" :key="item.id">
        <view class="item-header">
          <text class="item-title">{{ item.title }}</text>
          <text class="tag tag-info">{{ item.className }}</text>
        </view>
        <text class="item-desc" v-if="item.description">{{ item.description }}</text>
        <text class="item-date">{{ formatDate(item.createdAt) }}</text>
      </view>
    </view>
    <view v-else class="empty"><text class="empty-text">暂无预习资料</text></view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getTeacherPreviewList } from '../../api/index'

const list = ref([])
const fetchList = async () => {
  try {
    const res = await getTeacherPreviewList({ page: 1, size: 50 })
    list.value = res.records
  } catch (err) { console.error(err) }
}
const formatDate = (d) => d ? `${new Date(d).getMonth()+1}/${new Date(d).getDate()}` : ''
onMounted(() => fetchList())
</script>

<style scoped>
.item-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12rpx; }
.item-title { font-size: 32rpx; font-weight: bold; flex: 1; margin-right: 16rpx; }
.item-desc { font-size: 26rpx; color: #666; margin-bottom: 8rpx; display: block; }
.item-date { font-size: 24rpx; color: #999; }
</style>
