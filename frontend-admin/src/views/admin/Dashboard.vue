<template>
  <div class="admin-dashboard">
    <div class="page-header">
      <h2>数据看板</h2>
      <p class="header-desc">实时统计：今日新增、待审核博客、近7日发布趋势</p>
    </div>

    <div class="stats-cards">
      <div class="stat-card today-blogs">
        <div class="card-icon">
          <el-icon><Document /></el-icon>
        </div>
        <div class="card-content">
          <div class="stat-value">{{ stats.todayNewBlogs || 0 }}</div>
          <div class="stat-label">今日新增博客</div>
        </div>
      </div>
      <div class="stat-card today-users">
        <div class="card-icon">
          <el-icon><User /></el-icon>
        </div>
        <div class="card-content">
          <div class="stat-value">{{ stats.todayNewUsers || 0 }}</div>
          <div class="stat-label">今日新增用户</div>
        </div>
      </div>
      <div class="stat-card pending">
        <div class="card-icon">
          <el-icon><Clock /></el-icon>
        </div>
        <div class="card-content">
          <div class="stat-value">{{ stats.pendingReviewBlogs || 0 }}</div>
          <div class="stat-label">待审核博客</div>
        </div>
      </div>
    </div>

    <div class="chart-card card">
      <div class="card-header">
        <h3>近7日博客发布趋势</h3>
      </div>
      <div ref="chartRef" class="chart-container"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { adminApi } from '../../api'
import * as echarts from 'echarts'

const stats = ref({})
const chartRef = ref(null)
let chartInstance = null

function initChart() {
  if (!chartRef.value) return
  chartInstance = echarts.init(chartRef.value)
  const weeklyData = stats.value.weeklyBlogTrends || []
  const dates = weeklyData.map(item => item.date)
  const counts = weeklyData.map(item => item.count || 0)
  
  chartInstance.setOption({
    tooltip: {
      trigger: 'axis'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates
    },
    yAxis: {
      type: 'value',
      minInterval: 1
    },
    series: [
      {
        name: '发布数量',
        type: 'line',
        smooth: true,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(102, 126, 234, 0.3)' },
            { offset: 1, color: 'rgba(102, 126, 234, 0.05)' }
          ])
        },
        lineStyle: {
          color: '#667eea',
          width: 3
        },
        itemStyle: {
          color: '#667eea'
        },
        data: counts
      }
    ]
  })
}

async function fetchStats() {
  try {
    const res = await adminApi.getDashboardStats()
    stats.value = res.data
    initChart()
  } catch (e) {
    console.error('获取统计数据失败:', e)
  }
}

function handleResize() {
  chartInstance?.resize()
}

onMounted(() => {
  fetchStats()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
})
</script>

<style lang="scss" scoped>
.admin-dashboard {
  .page-header {
    display: flex;
    flex-direction: column;
    gap: 8px;
    margin-bottom: 24px;
    
    h2 {
      font-size: 28px;
      font-weight: 700;
      color: var(--text-primary);
      margin: 0;
    }
    
    .header-desc {
      color: var(--text-secondary);
      margin: 0;
      font-size: 14px;
    }
  }
  
  .stats-cards {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
    gap: 20px;
    margin-bottom: 24px;
    
    .stat-card {
      display: flex;
      align-items: center;
      gap: 16px;
      padding: 24px;
      border-radius: var(--radius-lg);
      color: white;
      
      &.today-blogs {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      }
      
      &.today-users {
        background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
      }
      
      &.pending {
        background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
      }
      
      .card-icon {
        width: 56px;
        height: 56px;
        background: rgba(255, 255, 255, 0.2);
        border-radius: var(--radius-md);
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 28px;
      }
      
      .card-content {
        flex: 1;
        
        .stat-value {
          font-size: 36px;
          font-weight: 700;
          line-height: 1.2;
        }
        
        .stat-label {
          font-size: 14px;
          opacity: 0.9;
          margin-top: 4px;
        }
      }
    }
  }
  
  .chart-card {
    .card-header {
      padding: 20px 24px;
      border-bottom: 1px solid var(--border-light);
      
      h3 {
        margin: 0;
        font-size: 18px;
        font-weight: 600;
        color: var(--text-primary);
      }
    }
  }
  
  .chart-container {
    height: 400px;
    width: 100%;
  }
}
</style>