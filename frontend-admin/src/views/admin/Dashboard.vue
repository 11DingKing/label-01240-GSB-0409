<template>
  <div class="admin-dashboard">
    <div class="page-header">
      <h2>数据看板</h2>
      <el-button type="primary" text :icon="Refresh" @click="fetchData" :loading="loading">
        刷新数据
      </el-button>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
          <el-icon><Document /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.todayNewBlogs }}</div>
          <div class="stat-label">今日新增博客</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
          <el-icon><User /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.todayNewUsers }}</div>
          <div class="stat-label">今日新增用户</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
          <el-icon><Timer /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.pendingReviewCount }}</div>
          <div class="stat-label">待审核博客</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);">
          <el-icon><TrendCharts /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ total7Days }}</div>
          <div class="stat-label">近7日发布总数</div>
        </div>
      </div>
    </div>

    <!-- 趋势图表 -->
    <div class="chart-card">
      <div class="chart-header">
        <h3>近7日博客发布趋势</h3>
        <el-tag type="info" size="small">数据每30秒自动刷新</el-tag>
      </div>
      <div ref="chartRef" class="chart-container"></div>
    </div>

    <!-- 快捷操作 -->
    <div class="quick-actions">
      <h3>快捷操作</h3>
      <div class="action-buttons">
        <el-button type="primary" @click="$router.push('/admin/blogs')">
          <el-icon><Document /></el-icon>
          审核博客
        </el-button>
        <el-button type="success" @click="$router.push('/admin/users')">
          <el-icon><User /></el-icon>
          用户管理
        </el-button>
        <el-button type="warning" @click="$router.push('/admin/comments')">
          <el-icon><ChatDotRound /></el-icon>
          评论管理
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, User, Timer, TrendCharts, Refresh, ChatDotRound } from '@element-plus/icons-vue'
import { adminApi } from '../../api'
import * as echarts from 'echarts'

const loading = ref(false)
const chartRef = ref(null)
let chartInstance = null
let refreshTimer = null

const stats = ref({
  todayNewBlogs: 0,
  todayNewUsers: 0,
  pendingReviewCount: 0,
  last7DaysTrend: []
})

const total7Days = computed(() => {
  return stats.value.last7DaysTrend.reduce((sum, item) => sum + (item.count || 0), 0)
})

async function fetchData() {
  loading.value = true
  try {
    const res = await adminApi.getDashboard()
    stats.value = res.data
    nextTick(() => {
      initChart()
    })
  } catch (e) {
    console.error(e)
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

function initChart() {
  if (!chartRef.value) return

  if (chartInstance) {
    chartInstance.dispose()
  }

  chartInstance = echarts.init(chartRef.value)

  const dates = stats.value.last7DaysTrend.map(item => item.date)
  const counts = stats.value.last7DaysTrend.map(item => item.count)

  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#e4e7ed',
      borderWidth: 1,
      textStyle: {
        color: '#303133'
      },
      formatter: function(params) {
        return `<div style="font-weight:600;margin-bottom:4px">${params[0].name}</div>
                <div style="color:#667eea">发布博客: <strong>${params[0].value}</strong> 篇</div>`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates,
      axisLine: {
        lineStyle: {
          color: '#dcdfe6'
        }
      },
      axisLabel: {
        color: '#606266'
      }
    },
    yAxis: {
      type: 'value',
      axisLine: {
        show: false
      },
      axisTick: {
        show: false
      },
      splitLine: {
        lineStyle: {
          color: '#ebeef5',
          type: 'dashed'
        }
      },
      axisLabel: {
        color: '#606266'
      }
    },
    series: [
      {
        name: '发布博客数',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        sampling: 'average',
        itemStyle: {
          color: '#667eea'
        },
        lineStyle: {
          width: 3,
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 1,
            y2: 0,
            colorStops: [
              { offset: 0, color: '#667eea' },
              { offset: 1, color: '#764ba2' }
            ]
          }
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(102, 126, 234, 0.3)' },
              { offset: 1, color: 'rgba(102, 126, 234, 0.05)' }
            ]
          }
        },
        data: counts,
        animationDuration: 1500,
        animationEasing: 'cubicOut'
      }
    ]
  }

  chartInstance.setOption(option)
}

function handleResize() {
  chartInstance?.resize()
}

onMounted(() => {
  fetchData()
  window.addEventListener('resize', handleResize)

  // 每30秒自动刷新数据
  refreshTimer = setInterval(() => {
    fetchData()
  }, 30000)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
  if (chartInstance) {
    chartInstance.dispose()
  }
})
</script>

<style lang="scss" scoped>
.admin-dashboard {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;

    h2 {
      font-size: 24px;
      font-weight: 600;
      color: var(--text-primary);
      margin: 0;
    }
  }
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;

  @media (max-width: 1200px) {
    grid-template-columns: repeat(2, 1fr);
  }

  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
}

.stat-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-normal);

  &:hover {
    transform: translateY(-4px);
    box-shadow: var(--shadow-md);
  }

  .stat-icon {
    width: 56px;
    height: 56px;
    border-radius: var(--radius-md);
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 24px;
    flex-shrink: 0;
  }

  .stat-info {
    flex: 1;

    .stat-value {
      font-size: 28px;
      font-weight: 700;
      color: var(--text-primary);
      line-height: 1.2;
      margin-bottom: 4px;
    }

    .stat-label {
      font-size: 14px;
      color: var(--text-secondary);
    }
  }
}

.chart-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: var(--shadow-sm);

  .chart-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h3 {
      font-size: 18px;
      font-weight: 600;
      color: var(--text-primary);
      margin: 0;
    }
  }

  .chart-container {
    height: 300px;
    width: 100%;
  }
}

.quick-actions {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 24px;
  box-shadow: var(--shadow-sm);

  h3 {
    font-size: 18px;
    font-weight: 600;
    color: var(--text-primary);
    margin: 0 0 16px 0;
  }

  .action-buttons {
    display: flex;
    gap: 12px;
    flex-wrap: wrap;

    .el-button {
      padding: 12px 24px;

      .el-icon {
        margin-right: 8px;
      }
    }
  }
}
</style>
