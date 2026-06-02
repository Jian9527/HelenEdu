<template>
  <view class="page">
    <view class="card" v-if="homework">
      <view class="detail-header">
        <text class="detail-title">{{ homework.title }}</text>
        <view :class="['status-tag', homework.status === 1 ? 'published' : 'draft']">
          {{ homework.status === 1 ? '已发布' : '草稿' }}
        </view>
      </view>

      <view class="meta-section">
        <view class="meta-row">
          <text class="meta-label">班级</text>
          <text class="meta-value">{{ homework.className }}</text>
        </view>
        <view class="meta-row">
          <text class="meta-label">科目</text>
          <text class="meta-value">{{ homework.subject || '-' }}</text>
        </view>
        <view class="meta-row">
          <text class="meta-label">布置范围</text>
          <text class="meta-value">{{ homework.targetType === 1 ? '指定学生' : '全班' }}</text>
        </view>
        <view class="meta-row" v-if="homework.targetType === 1 && homework.targetStudentNames && homework.targetStudentNames.length">
          <text class="meta-label">指定学生</text>
          <text class="meta-value student-names">{{ homework.targetStudentNames.join('、') }}</text>
        </view>
        <view class="meta-row" v-if="homework.deadline">
          <text class="meta-label">截止时间</text>
          <text class="meta-value">{{ formatDeadline(homework.deadline) }}</text>
        </view>
        <view class="meta-row">
          <text class="meta-label">发布时间</text>
          <text class="meta-value">{{ formatTime(homework.createdAt) }}</text>
        </view>
      </view>

      <!-- 作业内容 -->
      <view class="content-section" v-if="homework.content">
        <text class="section-title">作业内容</text>
        <text class="content-text">{{ homework.content }}</text>
      </view>
      <view class="content-section empty-hint" v-else>
        <text class="hint-text">📝 未填写作业内容</text>
      </view>

      <!-- 附件 -->
      <view class="attachments-section" v-if="homework.attachmentUrls && homework.attachmentUrls.length">
        <text class="section-title">附件 ({{ homework.attachmentUrls.length }})</text>
        <view class="image-grid" v-if="imageAttachments.length">
          <image
            v-for="(url, idx) in imageAttachments"
            :key="'img-' + idx"
            :src="url"
            mode="aspectFill"
            class="attach-img"
            @click="previewImage(url)"
          />
        </view>
        <view class="file-list" v-if="fileAttachments.length">
          <view class="file-item" v-for="(item, idx) in fileAttachments" :key="'file-' + idx" @click="openFile(item.url)">
            <text class="file-icon">📄</text>
            <text class="file-name">{{ item.name }}</text>
          </view>
        </view>
      </view>
      <view class="attachments-section empty-hint" v-else>
        <text class="hint-text">📎 未上传附件</text>
      </view>

      <!-- 提交统计 -->
      <view class="stats-section">
        <text class="section-title">提交情况</text>
        <view class="stats-bar">
          <view class="stat-box">
            <text class="stat-num">{{ homework.totalCount }}</text>
            <text class="stat-label">总人数</text>
          </view>
          <view class="stat-box">
            <text class="stat-num highlight">{{ homework.submitCount }}</text>
            <text class="stat-label">已提交</text>
          </view>
          <view class="stat-box">
            <text class="stat-num success">{{ homework.reviewedCount }}</text>
            <text class="stat-label">已批改</text>
          </view>
          <view class="stat-box">
            <text class="stat-num warning">{{ homework.totalCount - homework.submitCount }}</text>
            <text class="stat-label">未提交</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 操作按钮 -->
    <view class="action-bar" v-if="homework">
      <button class="btn-edit" @click="goEdit">编辑作业</button>
      <button class="btn-primary" @click="goReview">查看提交</button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getHomeworkDetail } from '../../api/index'

const homework = ref(null)
const homeworkId = ref('')
const classId = ref('')

onMounted(async () => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  homeworkId.value = currentPage.options.id
  classId.value = currentPage.options.classId || ''
  await fetchDetail()
})

const fetchDetail = async () => {
  try {
    homework.value = await getHomeworkDetail(homeworkId.value)
    console.log('作业详情:', homework.value)
    console.log('附件URLs:', homework.value.attachmentUrls)
  } catch (err) {
    console.error('加载作业详情失败', err)
  }
}

/**
 * 从 URL 中提取原始文件名
 * URL 格式: http://host/uploads/2026/06/02/uuid.pdf?name=原始文件名.pdf
 */
const getFileName = (url) => {
  // 优先从 ?name= 参数获取原始文件名
  const nameMatch = url.match(/[?&]name=([^&#]+)/)
  if (nameMatch) {
    return decodeURIComponent(nameMatch[1])
  }
  // 回退：从路径最后一段获取
  const path = url.split('?')[0]
  return decodeURIComponent(path.split('/').pop() || '文件')
}

const isImage = (url) => /\.(jpg|jpeg|png|gif|webp|bmp)(\?.*)?$/i.test(url)

const imageAttachments = computed(() => {
  if (!homework.value?.attachmentUrls) return []
  return homework.value.attachmentUrls.filter(isImage)
})

const fileAttachments = computed(() => {
  if (!homework.value?.attachmentUrls) return []
  return homework.value.attachmentUrls.filter(url => !isImage(url)).map(url => ({
    url,
    name: getFileName(url)
  }))
})

const previewImage = (url) => {
  uni.previewImage({ urls: imageAttachments.value, current: url })
}

const openFile = (url) => {
  // #ifdef H5
  window.open(url, '_blank')
  // #endif
  // #ifdef MP-WEIXIN
  uni.downloadFile({ url, success: (res) => uni.openDocument({ filePath: res.tempFilePath }) })
  // #endif
}

const goEdit = () => {
  uni.navigateTo({ url: `/pages/teacher/homework-create?id=${homeworkId.value}&classId=${classId.value}` })
}

const goReview = () => {
  uni.navigateTo({ url: `/pages/teacher/homework-review?id=${homeworkId.value}` })
}

const formatDeadline = (d) => {
  if (!d) return '-'
  const date = new Date(d)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

const formatTime = (d) => {
  if (!d) return '-'
  const date = new Date(d)
  return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
}
</script>

<style scoped>
.page { min-height: 100vh; background: #f4f6f9; padding: 30rpx; box-sizing: border-box; }
.card { background: #fff; border-radius: 20rpx; padding: 36rpx; box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.06); }

.detail-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 28rpx; }
.detail-title { font-size: 36rpx; font-weight: bold; flex: 1; margin-right: 16rpx; line-height: 1.4; }
.status-tag { font-size: 22rpx; padding: 6rpx 20rpx; border-radius: 20rpx; font-weight: 600; white-space: nowrap; }
.status-tag.published { background: #e6f9ee; color: #22a357; }
.status-tag.draft { background: #fff3e0; color: #e67700; }

.meta-section { background: #f8f9fb; border-radius: 14rpx; padding: 20rpx 24rpx; margin-bottom: 32rpx; }
.meta-row { display: flex; justify-content: space-between; padding: 10rpx 0; }
.meta-label { color: #999; font-size: 26rpx; }
.meta-value { color: #333; font-size: 26rpx; font-weight: 500; }
.meta-value.student-names { max-width: 70%; text-align: right; line-height: 1.6; }

.section-title { font-size: 30rpx; font-weight: bold; margin-bottom: 16rpx; display: block; }

.content-section { margin-bottom: 32rpx; }
.content-text { font-size: 28rpx; color: #555; line-height: 1.8; white-space: pre-wrap; }
.empty-hint { padding: 20rpx; background: #f8f9fa; border-radius: 12rpx; text-align: center; }
.hint-text { font-size: 26rpx; color: #999; }

.attachments-section { margin-bottom: 32rpx; }
.image-grid { display: flex; flex-wrap: wrap; gap: 14rpx; margin-bottom: 14rpx; }
.attach-img { width: 200rpx; height: 200rpx; border-radius: 12rpx; }
.file-list { }
.file-item { display: flex; align-items: center; gap: 12rpx; padding: 16rpx 20rpx; background: #f8f9fa; border-radius: 12rpx; margin-bottom: 10rpx; }
.file-icon { font-size: 36rpx; }
.file-name { font-size: 26rpx; color: #4A90D9; flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

.stats-section { margin-bottom: 16rpx; }
.stats-bar { display: flex; gap: 0; background: #f8f9fb; border-radius: 14rpx; overflow: hidden; }
.stat-box { flex: 1; display: flex; flex-direction: column; align-items: center; padding: 24rpx 0; border-right: 1rpx solid #eef0f3; }
.stat-box:last-child { border-right: none; }
.stat-num { font-size: 36rpx; font-weight: bold; color: #333; margin-bottom: 6rpx; }
.stat-num.highlight { color: #4A90D9; }
.stat-num.success { color: #22a357; }
.stat-num.warning { color: #e67700; }
.stat-label { font-size: 22rpx; color: #aaa; }

.action-bar { display: flex; gap: 20rpx; margin-top: 30rpx; }
.action-bar .btn-primary {
  flex: 1; height: 84rpx; line-height: 84rpx; padding: 0;
  font-size: 30rpx; font-weight: 600; border-radius: 42rpx;
}
.btn-edit {
  flex: 1; height: 84rpx; line-height: 84rpx; font-size: 30rpx; font-weight: 600;
  background: #fff; color: #4A90D9; border: 2rpx solid #4A90D9; border-radius: 42rpx;
}
</style>
