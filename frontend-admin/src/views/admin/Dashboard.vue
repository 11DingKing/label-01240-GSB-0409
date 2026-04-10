<template>
  <div class="dashboard">
    <h2>数据看板</h2>
    
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card today-blogs">
          <div class="stat-icon">
            <el-icon><Document /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.todayNewBlogs || 0 }}</div>
            <div class="stat-label">今日新增博客</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card today-users">
          <div class="stat-icon">
            <el-icon><User /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.todayNewUsers || 0 }}</div>
            <div class="stat-label">今日新增用户</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card pending-review">
          <div class="stat-icon">
            <el-icon><Clock /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.pendingReviewBlogs || 0 }}</div>
            <div class="stat-label">待审核博客</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card total-published">
          <div class="stat-icon">
            <el-icon><CheckCircle /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">-</div>
            <div class="stat-label">已发布博客(总计)</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-card class="chart-card" style="margin-top: 20px;">
      <template #header>
        <span>近7日每日发布博客趋势</span>
        <el-button 
          type="text" 
          style="float: right; padding: 3px 0; color: #1890ff"
          @click="refreshData"
          :loading="loading"
        >
          刷新数据
        </el-button>
      </template>
      <v-chart 
        class="chart" 
        :option="chartOption" 
        autoresize
        v-loading="loading"
        element-loading-text="数据加载中..."
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components'
import { adminApi } from '../../api'
import { ElMessage } from 'element-plus'

use([
  CanvasRenderer,
  LineChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
])

const stats = ref({})
const loading = ref(false)
let refreshTimer = null

const chartOption = ref({
  tooltip: {
    trigger: 'axis',
    formatter: '{b}：{c} 篇'
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
    data: [],
    axisLabel: {
      rotate: 45,
      fontSize: 12
    }
  },
  yAxis: {
    type: 'value',
    name: '发布数量',
    minInterval: 1
  },
  series: [
    {
      name: '发布博客数',
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      lineStyle: {
        width: 3,
        color: '#1890ff'
      },
      itemStyle: {
        color: '#1890ff',
        borderWidth: 2
      },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(24, 144, 255, 0.3)' },
            { offset: 1, color: 'rgba(24, 144, 255, 0.05)' }
          ]
        }
      },
      data: []
    }
  ]
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await adminApi.getDashboardStats()
    stats.value = res.data
    
    const xAxisData = res.data.last7DaysTrend?.map(item => item.date?.slice(5) || item.date) || []
    const seriesData = res.data.last7DaysTrend?.map(item => item.count) || []
    
    chartOption.value.xAxis.data = xAxisData
    chartOption.value.series[0].data = seriesData
    
  } catch (err) {
    console.error('获取看板数据失败:', err)
  } finally {
    loading.value = false
  }
}

const refreshData = () => {
  fetchData()
  ElMessage.success('数据已刷新')
}

onMounted(() => {
  fetchData()
  refreshTimer = setInterval(fetchData, 30000)
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<style lang="scss" scoped>
.dashboard {
  padding: 20px;
  
  h2 {
    margin-bottom: 20px;
    color: #333;
    font-size: 22px;
  }
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
  }
  
  :deep(.el-card__body) {
    display: flex;
    align-items: center;
    padding: 20px;
  }
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  
  .el-icon {
    font-size: 28px;
    color: #fff;
  }
}

.today-blogs {
  .stat-icon {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  }
}

.today-users {
  .stat-icon {
    background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  }
}

.pending-review {
  .stat-icon {
    background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  }
}

.total-published {
  .stat-icon {
    background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  }
}

.stat-content {
  flex: 1;
  
  .stat-value {
    font-size: 32px;
    font-weight: 700;
    color: #1f2937;
    line-height: 1.2;
    margin-bottom: 4px;
  }
  
  .stat-label {
    font-size: 14px;
    color: #6b7280;
  }
}

.chart-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border: none;
  
  :deep(.el-card__header) {
    border-bottom: 1px solid #f0f0f0;
    font-weight: 600;
    color: #333;
  }
}

.chart {
  height: 400px;
  width: 100%;
}
</style>