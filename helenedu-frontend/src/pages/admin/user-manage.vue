<template>
  <view class="container">
    <view class="tab-bar">
      <view :class="['tab-item', { active: activeRole === null }]" @click="switchRole(null)">全部</view>
      <view :class="['tab-item', { active: activeRole === 1 }]" @click="switchRole(1)">学生</view>
      <view :class="['tab-item', { active: activeRole === 2 }]" @click="switchRole(2)">教师</view>
    </view>

    <view class="action-bar">
      <button class="btn-primary" @click="goAdd">+ 添加人员</button>
    </view>

    <view v-if="list.length > 0">
      <view class="card user-item" v-for="item in list" :key="item.id">
        <view class="user-header">
          <view class="user-info-left">
            <view class="user-avatar"><text>{{ item.name ? item.name.charAt(0) : '?' }}</text></view>
            <view>
              <text class="user-name">{{ item.name }}</text>
              <text class="user-phone">
                <template v-if="item.role === 1">
                  {{ item.grade || '' }}{{ item.className ? ' · ' + item.className : '' }}
                </template>
                <template v-else>
                  {{ item.phone || '未绑定手机' }}
                  <text v-if="item.subject"> · {{ item.subject }}</text>
                </template>
              </text>
            </view>
          </view>
          <view :class="['tag', item.role === 1 ? 'tag-info' : 'tag-success']">{{ item.roleName }}</view>
        </view>
        <view class="user-actions">
          <text class="action-link" @click="goEdit(item)">编辑</text>
          <text class="action-link" @click="toggleStatus(item)">{{ item.status === 1 ? '禁用' : '启用' }}</text>
          <text class="action-link danger" @click="handleDelete(item)">删除</text>
        </view>
      </view>
    </view>
    <view v-else class="empty"><text class="empty-text">暂无人员</text></view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getUserList, toggleUserStatus, deleteUser } from '../../api/index'

const list = ref([])
const activeRole = ref(null)

const fetchList = async () => {
  try {
    const res = await getUserList({ page: 1, size: 100, role: activeRole.value })
    list.value = res.records
  } catch (err) { console.error(err) }
}

const switchRole = (role) => { activeRole.value = role; fetchList() }
const goAdd = () => uni.navigateTo({ url: '/pages/admin/user-form' })
const goEdit = (item) => {
  const params = new URLSearchParams()
  params.set('id', item.id)
  params.set('name', item.name || '')
  params.set('phone', item.phone || '')
  params.set('role', item.role)
  params.set('subject', item.subject || '')
  params.set('gender', item.gender || '')
  params.set('parentContact', item.parentContact || '')
  params.set('grade', item.grade || '')
  params.set('classId', item.classId || '')
  uni.navigateTo({ url: `/pages/admin/user-form?${params.toString()}` })
}

const toggleStatus = async (item) => {
  try {
    await toggleUserStatus(item.id)
    uni.showToast({ title: '操作成功', icon: 'success' })
    fetchList()
  } catch (err) { console.error(err) }
}

const handleDelete = (item) => {
  uni.showModal({
    title: '确认删除', content: `确定删除"${item.name}"吗？`,
    success: async (res) => {
      if (res.confirm) {
        try { await deleteUser(item.id); uni.showToast({ title: '已删除', icon: 'success' }); fetchList() }
        catch (err) { console.error(err) }
      }
    }
  })
}

onMounted(() => fetchList())
</script>

<style scoped>
.tab-bar { display: flex; background: #fff; border-radius: 12rpx; margin-bottom: 20rpx; padding: 10rpx; }
.tab-item { flex: 1; text-align: center; padding: 16rpx 0; font-size: 28rpx; color: #666; border-radius: 8rpx; }
.tab-item.active { background: #4A90D9; color: #fff; font-weight: bold; }
.action-bar { margin-bottom: 20rpx; }
.user-item { margin-bottom: 16rpx; }
.user-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16rpx; }
.user-info-left { display: flex; align-items: center; gap: 16rpx; }
.user-avatar { width: 72rpx; height: 72rpx; border-radius: 50%; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); display: flex; align-items: center; justify-content: center; color: #fff; font-size: 30rpx; font-weight: bold; }
.user-name { display: block; font-size: 30rpx; font-weight: bold; }
.user-phone { display: block; font-size: 24rpx; color: #999; }
.user-actions { display: flex; gap: 30rpx; }
.action-link { color: #4A90D9; font-size: 26rpx; }
.action-link.danger { color: #f44336; }
</style>
