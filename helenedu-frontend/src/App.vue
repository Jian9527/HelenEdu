<script>
export default {
  onLaunch() {
    console.log('App Launch')
  },
  onShow() {
    console.log('App Show')
  },
  onHide() {
    console.log('App Hide')
  }
}
</script>

<style>
/* 全局样式 */
page {
  background-color: #f5f5f5;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  font-size: 28rpx;
  color: #333;
}

.container {
  padding: 30rpx;
}

/* 通用卡片样式 */
.card {
  background-color: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
}

/* 按钮样式 */
.btn-primary {
  background-color: #4A90D9;
  color: #fff;
  border: none;
  border-radius: 12rpx;
  padding: 20rpx 40rpx;
  font-size: 30rpx;
  text-align: center;
}

.btn-primary::after {
  border: none;
}

/* 状态标签 */
.tag {
  display: inline-block;
  padding: 4rpx 16rpx;
  border-radius: 8rpx;
  font-size: 22rpx;
}

.tag-success {
  background-color: #e8f5e9;
  color: #4caf50;
}

.tag-warning {
  background-color: #fff3e0;
  color: #ff9800;
}

.tag-info {
  background-color: #e3f2fd;
  color: #2196f3;
}

.tag-error {
  background-color: #fce4ec;
  color: #f44336;
}

/* 列表项 */
.list-item {
  display: flex;
  align-items: center;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #eee;
}

.list-item:last-child {
  border-bottom: none;
}

/* 空状态 */
.empty {
  text-align: center;
  padding: 100rpx 0;
  color: #999;
}

.empty-text {
  font-size: 28rpx;
  margin-top: 20rpx;
}
</style>
