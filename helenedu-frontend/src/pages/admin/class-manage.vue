<template>
  <view class="container">
    <view class="action-bar">
      <button class="btn-primary" @click="goAdd">+ 新建班级</button>
    </view>
    <view v-if="list.length > 0">
      <view class="card" v-for="item in list" :key="item.id" @click="goEdit(item)">
        <view class="item-header">
          <text class="item-title">{{ item.name }}</text>
          <text class="tag tag-info" v-if="item.grade">{{ item.grade }}</text>
        </view>
        <view class="item-info">
          <text>学生{{ item.studentCount }}人 / 教师{{ item.teacherCount }}人</text>
          <text v-if="item.teacherName">班主任: {{ item.teacherName }}</text>
        </view>
        <view class="item-actions">
          <text class="action-link" @click.stop="goEdit(item)">编辑</text>
          <text class="action-link danger" @click.stop="handleDelete(item)">删除</text>
        </view>
      </view>
    </view>
    <view v-else class="empty"><text class="empty-text">暂无班级</text></view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getClassList, deleteClass } from '../../api/index'

const list = ref([])
const fetchList = async () => {
  try {
    const res = await getClassList({ page: 1, size: 100 })
    list.value = res.records
  } catch (err) { console.error(err) }
}
const goAdd = () => uni.navigateTo({ url: '/pages/admin/class-form' })
const goEdit = (item) => uni.navigateTo({ url: `/pages/admin/class-form?id=${item.id}&name=${item.name}&grade=${item.grade || ''}&teacherId=${item.teacherId || ''}` })
const handleDelete = (item) => {
  uni.showModal({
    title: '确认删除',
    content: `确定删除班级"${item.name}"吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          await deleteClass(item.id)
          uni.showToast({ title: '已删除', icon: 'success' })
          fetchList()
        } catch (err) { console.error(err) }
      }
    }
  })
}
onMounted(() => fetchList())
</script>

<style scoped>
.action-bar { margin-bottom: 20rpx; }
.item-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12rpx; }
.item-title { font-size: 32rpx; font-weight: bold; }
.item-info { display: flex; justify-content: space-between; font-size: 26rpx; color: #999; margin-bottom: 16rpx; }
.item-actions { display: flex; gap: 30rpx; }
.action-link { color: #4A90D9; font-size: 26rpx; }
.action-link.danger { color: #f44336; }
</style>
