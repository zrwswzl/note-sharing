<template>
  <div class="search-container">
    <div class="search-header">
      <h2>查询笔记</h2>
    </div>

    <!-- 搜索框 -->
    <div class="search-box">
      <input 
        v-model="searchQuery"
        type="text" 
        class="search-input"
        placeholder="可输入笔记、笔记本、空间、标签、用户查询"
        @keyup.enter="handleSearch"
      />
      <button class="search-btn" @click="handleSearch">
        🔍 搜索
      </button>
    </div>

    <!-- 搜索结果区域 -->
    <div class="search-results">
      <div v-if="loading" class="loading">搜索中...</div>
      
      <div v-else-if="searchResults.length > 0" class="results-list">
        <div 
          v-for="result in searchResults" 
          :key="result.id"
          class="result-item"
        >
          <div class="result-type">{{ result.type }}</div>
          <div class="result-title">{{ result.title }}</div>
          <div class="result-info">{{ result.info }}</div>
        </div>
      </div>

      <div v-else-if="hasSearched" class="no-results">
        暂无搜索结果
      </div>

      <div v-else class="placeholder">
        <p>💡 在上方输入关键词开始搜索</p>
        <p class="tip">支持搜索：笔记标题、笔记本名称、空间名称、标签、用户名</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const searchQuery = ref('')
const searchResults = ref([])
const loading = ref(false)
const hasSearched = ref(false)

/**
 * API: 搜索笔记
 * POST /api/search
 * 输入: {
 *   query: string,  // 搜索关键词
 *   type: string    // 可选: 'note', 'notebook', 'space', 'tag', 'user', 'all'
 * }
 * 输出: {
 *   code: number,
 *   data: [
 *     {
 *       id: string,
 *       type: string,      // 'note', 'notebook', 'space', 'tag', 'user'
 *       title: string,     // 标题/名称
 *       info: string,      // 附加信息(如笔记所属笔记本等)
 *       content: string    // 笔记内容预览(仅笔记类型)
 *     }
 *   ]
 * }
 */
const handleSearch = async () => {
  if (!searchQuery.value.trim()) return
  
  loading.value = true
  hasSearched.value = true
  
  try {
    // 调用搜索API
    // const response = await fetch('/api/search', {
    //   method: 'POST',
    //   headers: { 'Content-Type': 'application/json' },
    //   body: JSON.stringify({ query: searchQuery.value, type: 'all' })
    // })
    // const result = await response.json()
    // searchResults.value = result.data
    
    // 模拟数据
    setTimeout(() => {
      searchResults.value = [
        { id: '1', type: '笔记', title: '示例笔记', info: '所属: 示例笔记本 > 示例空间' },
        { id: '2', type: '笔记本', title: '示例笔记本', info: '所属空间: 示例空间' }
      ]
      loading.value = false
    }, 500)
  } catch (error) {
    console.error('搜索失败:', error)
    loading.value = false
  }
}
</script>

<style scoped>
.search-container {
  max-width: 1000px;
  margin: 0 auto;
}

.search-header h2 {
  color: #333;
  margin-bottom: 20px;
}

.search-box {
  display: flex;
  gap: 10px;
  margin-bottom: 30px;
}

.search-input {
  flex: 1;
  padding: 12px 20px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 16px;
  transition: border-color 0.3s;
}

.search-input:focus {
  outline: none;
  border-color: #00bcd4;
}

.search-btn {
  padding: 12px 30px;
  background: #00bcd4;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
  transition: background 0.3s;
}

.search-btn:hover {
  background: #00acc1;
}

.search-results {
  min-height: 400px;
}

.loading {
  text-align: center;
  padding: 50px;
  color: #666;
}

.results-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.result-item {
  padding: 20px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.result-item:hover {
  border-color: #00bcd4;
  box-shadow: 0 2px 8px rgba(0, 188, 212, 0.2);
}

.result-type {
  display: inline-block;
  padding: 4px 12px;
  background: #00bcd4;
  color: white;
  border-radius: 12px;
  font-size: 12px;
  margin-bottom: 8px;
}

.result-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.result-info {
  color: #666;
  font-size: 14px;
}

.no-results {
  text-align: center;
  padding: 50px;
  color: #999;
  font-size: 16px;
}

.placeholder {
  text-align: center;
  padding: 80px 20px;
  color: #999;
}

.placeholder p {
  font-size: 18px;
  margin: 10px 0;
}

.tip {
  font-size: 14px;
  color: #bbb;
}
</style>