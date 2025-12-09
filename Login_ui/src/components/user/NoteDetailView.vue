<template>
  <div class="note-detail-page">
    <div v-if="loading" class="loading-state">
      <div class="loader"></div>
      <p>加载中...</p>
    </div>

    <div v-else-if="error" class="error-state">
      <p>{{ error }}</p>
      <button class="back-button" @click="goBack">返回</button>
    </div>

    <div v-else-if="noteDetail" class="detail-container">
      <!-- 返回按钮 -->
      <div class="back-header">
        <button class="back-link-button" @click="goBack">
          <svg class="back-icon" viewBox="0 0 16 16" fill="currentColor">
            <path fill-rule="evenodd" d="M15 8a.5.5 0 0 0-.5-.5H2.707l3.147-3.146a.5.5 0 1 0-.708-.708l-4 4a.5.5 0 0 0 0 .708l4 4a.5.5 0 0 0 .708-.708L2.707 8.5H14.5A.5.5 0 0 0 15 8z"/>
          </svg>
          <span>返回</span>
        </button>
      </div>
      
      <!-- 头部：标题 -->
      <header class="detail-header">
        <h1 class="note-title">{{ noteDetail.title || '无标题' }}</h1>
      </header>

      <!-- 作者和统计信息 -->
      <div class="note-meta-section">
        <div class="author-info">
          <svg class="author-icon" viewBox="0 0 16 16" fill="currentColor">
            <path d="M8 8a3 3 0 100-6 3 3 0 000 6zm2-3a2 2 0 11-4 0 2 2 0 014 0zm4 8c0 1-1 1-1 1H3s-1 0-1-1 1-4 6-4 6 3 6 4zm-1-.004c-.001-.246-.154-.986-.832-1.664C11.516 10.68 10.289 10 8 10c-2.29 0-3.516.68-4.168 1.332-.678.678-.83 1.418-.832 1.664h10z"/>
          </svg>
          <span class="author-name">{{ stats.authorName || '未知作者' }}</span>
        </div>
        <div class="stats-info">
          <span class="stat-item">
            <svg class="stat-icon" viewBox="0 0 16 16" fill="currentColor">
              <path d="M8 4a.5.5 0 01.5.5v3h3a.5.5 0 010 1h-3v3a.5.5 0 01-1 0v-3h-3a.5.5 0 010-1h3v-3A.5.5 0 018 4z"/>
            </svg>
            {{ stats.views || 0 }} 阅读
          </span>
          <span class="stat-item">
            <svg class="stat-icon" viewBox="0 0 16 16" fill="currentColor">
              <path d="M8 15A7 7 0 118 1a7 7 0 010 14zm0 1A8 8 0 108 0a8 8 0 000 16z"/>
              <path d="M8 4a.5.5 0 00-.5.5v3h-3a.5.5 0 000 1h3v3a.5.5 0 001 0v-3h3a.5.5 0 000-1h-3v-3A.5.5 0 008 4z"/>
            </svg>
            {{ stats.likes || 0 }} 点赞
          </span>
          <span class="stat-item">
            <svg class="stat-icon" viewBox="0 0 16 16" fill="currentColor">
              <path d="M2 2v13.5a.5.5 0 00.74.439L8 13.069l5.26 2.87A.5.5 0 0014 15.5V2a2 2 0 00-2-2H4a2 2 0 00-2 2z"/>
            </svg>
            {{ stats.favorites || 0 }} 收藏
          </span>
          <span class="stat-item">
            <svg class="stat-icon" viewBox="0 0 16 16" fill="currentColor">
              <path d="M2.5 1A1.5 1.5 0 001 2.5v11A1.5 1.5 0 002.5 15h6.086a1.5 1.5 0 001.06-.44l4.915-4.914A1.5 1.5 0 0015 7.586V2.5A1.5 1.5 0 0013.5 1h-11zM2 2.5a.5.5 0 01.5-.5h11a.5.5 0 01.5.5v7.086a.5.5 0 01-.146.353l-4.915 4.915a.5.5 0 01-.353.146H2.5a.5.5 0 01-.5-.5v-11z"/>
              <path d="M5.5 6a.5.5 0 000 1h5a.5.5 0 000-1h-5zM5 8.5a.5.5 0 01.5-.5h5a.5.5 0 010 1h-5a.5.5 0 01-.5-.5zm0 2a.5.5 0 01.5-.5h2a.5.5 0 010 1h-2a.5.5 0 01-.5-.5z"/>
            </svg>
            {{ stats.comments || 0 }} 评论
          </span>
        </div>
      </div>

      <!-- 主题部分：笔记内容（不可编辑） -->
      <div class="note-content-section">
        <div v-if="contentLoading" class="content-loading">
          <div class="loader"></div>
          <p>加载笔记内容...</p>
        </div>
        <div v-else-if="noteDetail.fileType === 'pdf'" class="pdf-preview-container">
          <VuePdfEmbed
            v-if="fileUrl"
            :source="fileUrl"
            class="pdf-viewer"
            :width="800"
          />
          <p v-else class="no-content">无法加载PDF文件</p>
        </div>
        <div v-else-if="noteDetail.fileType === 'md'" class="markdown-content">
          <div 
            v-if="fileUrl && markdownContent" 
            class="markdown-body"
            v-html="markdownContent"
          ></div>
          <p v-else class="no-content">无法加载笔记内容</p>
        </div>
        <div v-else class="no-content">
          <p>不支持的文件类型：{{ noteDetail.fileType }}</p>
        </div>
      </div>

      <!-- 尾部：评论部分（预留展示栏） -->
      <div class="comments-section">
        <div class="comments-header">
          <h2 class="comments-title">评论</h2>
          <span class="comments-count">({{ stats.comments || 0 }})</span>
        </div>
        <div class="comments-placeholder">
          <p class="placeholder-text">评论功能即将上线，敬请期待...</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getFileUrlByNoteId, getNoteStats } from '@/api/note'
import VuePdfEmbed from 'vue-pdf-embed'
import MarkdownIt from 'markdown-it'

const router = useRouter()
const route = useRoute()

const props = defineProps({
  noteId: {
    type: [Number, String],
    required: true
  },
  initialStats: {
    type: Object,
    default: null
  }
})

const loading = ref(true)
const contentLoading = ref(false)
const error = ref(null)
const noteDetail = ref(null)
// 初始化统计信息，优先使用从搜索结果传递过来的数据
const stats = ref(
  props.initialStats ? {
    authorName: props.initialStats.authorName || '未知作者',
    views: props.initialStats.views || 0,
    likes: props.initialStats.likes || 0,
    favorites: props.initialStats.favorites || 0,
    comments: props.initialStats.comments || 0
  } : {
    authorName: '',
    views: 0,
    likes: 0,
    favorites: 0,
    comments: 0
  }
)
const fileUrl = ref(null)
const markdownContent = ref('')

// 配置markdown-it解析器
const mdParser = new MarkdownIt({
  breaks: true,
  html: true,
  linkify: true,
  typographer: true
})

// 获取笔记详情和统计信息
const fetchNoteDetail = async () => {
  loading.value = true
  error.value = null

  try {
    // 确保noteId是有效的数字
    const noteId = typeof props.noteId === 'string' ? Number(props.noteId) : props.noteId
    if (!noteId || isNaN(noteId) || noteId <= 0) {
      throw new Error('无效的笔记ID')
    }

    console.log('获取笔记详情，noteId:', noteId)

    // 优先使用从搜索结果传递过来的统计信息
    if (props.initialStats) {
      stats.value = {
        authorName: props.initialStats.authorName || '未知作者',
        views: props.initialStats.views || 0,
        likes: props.initialStats.likes || 0,
        favorites: props.initialStats.favorites || 0,
        comments: props.initialStats.comments || 0
      }
    } else {
      // 如果没有传递统计信息，则从API获取
      try {
        const statsData = await getNoteStats(noteId)
        if (statsData) {
          stats.value = {
            authorName: statsData.authorName || '未知作者',
            views: statsData.views || 0,
            likes: statsData.likes || 0,
            favorites: statsData.favorites || 0,
            comments: statsData.comments || 0
          }
        }
      } catch (statsError) {
        console.error('获取统计信息失败:', statsError)
        // 统计信息获取失败不影响主要内容显示
        stats.value = {
          authorName: '未知作者',
          views: 0,
          likes: 0,
          favorites: 0,
          comments: 0
        }
      }
    }

    // 获取文件URL
    let url
    try {
      url = await getFileUrlByNoteId(noteId)
      if (!url) {
        throw new Error('无法获取笔记文件URL')
      }
    } catch (urlError) {
      console.error('获取文件URL失败:', urlError)
      if (urlError.response) {
        console.error('响应数据:', urlError.response.data)
        console.error('响应状态:', urlError.response.status)
      }
      throw new Error(`无法获取笔记文件URL: ${urlError.message || '未知错误'}`)
    }

    fileUrl.value = url

    // 从URL判断文件类型（根据文件扩展名）
    let fileType = 'md' // 默认类型
    if (url.toLowerCase().endsWith('.pdf')) {
      fileType = 'pdf'
    } else if (url.toLowerCase().endsWith('.md') || url.toLowerCase().endsWith('.markdown')) {
      fileType = 'md'
    }

    // 构建笔记详情对象（从路由参数或搜索结果中获取标题）
    noteDetail.value = {
      title: route.query.title || '无标题笔记',
      fileType: route.query.fileType || fileType,
      noteId: noteId
    }

    // 如果是Markdown文件，获取内容并转换为HTML
    if (noteDetail.value.fileType === 'md') {
      contentLoading.value = true
      try {
        const response = await fetch(fileUrl.value, {
          method: 'GET',
          cache: 'no-cache'
        })
        if (response.ok) {
          const text = await response.text()
          markdownContent.value = mdParser.render(text)
        } else {
          throw new Error('无法加载笔记内容')
        }
      } catch (err) {
        console.error('加载Markdown内容失败:', err)
        error.value = '无法加载笔记内容'
      } finally {
        contentLoading.value = false
      }
    }

    // 增加阅读量
    try {
      const userId = JSON.parse(localStorage.getItem('userInfo') || '{}')?.id
      if (userId) {
        // 调用增加阅读量的API（可选，如果后端支持）
        // await incrementNoteView(noteId, userId)
      }
    } catch (err) {
      console.error('增加阅读量失败:', err)
    }

  } catch (err) {
    console.error('获取笔记详情失败:', err)
    error.value = err.message || '加载笔记详情失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

// 返回上一页
const goBack = () => {
  // 如果是从搜索页面跳转过来的，返回到搜索页面
  if (route.query.tab === 'note-detail') {
    router.replace({
      path: route.path,
      query: {
        ...route.query,
        tab: 'search',
        noteId: undefined,
        title: undefined,
        fileType: undefined
      }
    })
  } else {
    router.back()
  }
}

// 监听noteId变化
watch(() => props.noteId, () => {
  if (props.noteId) {
    fetchNoteDetail()
  }
})

// 监听initialStats变化，如果统计信息更新了，也要更新显示
watch(() => props.initialStats, (newStats) => {
  if (newStats) {
    stats.value = {
      authorName: newStats.authorName || '未知作者',
      views: newStats.views || 0,
      likes: newStats.likes || 0,
      favorites: newStats.favorites || 0,
      comments: newStats.comments || 0
    }
  }
}, { immediate: true, deep: true })

onMounted(() => {
  if (props.noteId) {
    fetchNoteDetail()
  }
})
</script>

<style scoped>
:global(:root) {
  --brand-primary: #007FFF;
  --surface-base: #ffffff;
  --surface-muted: #f6f6f6;
  --line-soft: #e2e2e2;
  --text-strong: #111c17;
  --text-secondary: #666;
  --text-muted: #999;
}

.note-detail-page {
  min-height: 100vh;
  background: var(--surface-muted);
  padding: 20px;
}

.detail-container {
  max-width: 900px;
  margin: 0 auto;
  background: var(--surface-base);
  border-radius: 8px;
  padding: 40px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

/* 返回按钮区域 */
.back-header {
  margin-bottom: 20px;
}

.back-link-button {
  display: flex;
  align-items: center;
  gap: 6px;
  background: none;
  border: none;
  color: var(--brand-primary);
  font-size: 14px;
  cursor: pointer;
  padding: 8px 0;
  transition: color 0.2s;
}

.back-link-button:hover {
  color: #006EDC;
}

.back-icon {
  width: 16px;
  height: 16px;
}

/* 头部标题 */
.detail-header {
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--line-soft);
}

.note-title {
  margin: 0;
  font-size: 28px;
  font-weight: 600;
  color: var(--text-strong);
  line-height: 1.4;
}

/* 作者和统计信息 */
.note-meta-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 32px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--line-soft);
}

.author-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.author-icon {
  width: 18px;
  height: 18px;
  color: var(--text-secondary);
}

.author-name {
  font-size: 15px;
  color: var(--text-secondary);
  font-weight: 500;
}

.stats-info {
  display: flex;
  align-items: center;
  gap: 24px;
  flex-wrap: wrap;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: var(--text-muted);
}

.stat-icon {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
}

/* 笔记内容区域 */
.note-content-section {
  margin-bottom: 40px;
  min-height: 200px;
}

.content-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  gap: 16px;
}

.pdf-preview-container {
  width: 100%;
  display: flex;
  justify-content: center;
}

.pdf-viewer {
  max-width: 100%;
}

.markdown-content {
  width: 100%;
}

.markdown-body {
  font-size: 16px;
  line-height: 1.8;
  color: var(--text-strong);
  word-wrap: break-word;
}

.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3),
.markdown-body :deep(h4),
.markdown-body :deep(h5),
.markdown-body :deep(h6) {
  margin-top: 24px;
  margin-bottom: 16px;
  font-weight: 600;
  line-height: 1.25;
}

.markdown-body :deep(h1) {
  font-size: 2em;
  border-bottom: 1px solid var(--line-soft);
  padding-bottom: 8px;
}

.markdown-body :deep(h2) {
  font-size: 1.5em;
  border-bottom: 1px solid var(--line-soft);
  padding-bottom: 8px;
}

.markdown-body :deep(p) {
  margin-bottom: 16px;
}

.markdown-body :deep(code) {
  background: var(--surface-muted);
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 0.9em;
}

.markdown-body :deep(pre) {
  background: var(--surface-muted);
  padding: 16px;
  border-radius: 6px;
  overflow-x: auto;
  margin-bottom: 16px;
}

.markdown-body :deep(pre code) {
  background: none;
  padding: 0;
}

.markdown-body :deep(blockquote) {
  border-left: 4px solid var(--brand-primary);
  padding-left: 16px;
  margin: 16px 0;
  color: var(--text-secondary);
}

.markdown-body :deep(ul),
.markdown-body :deep(ol) {
  margin-bottom: 16px;
  padding-left: 24px;
}

.markdown-body :deep(li) {
  margin-bottom: 8px;
}

.markdown-body :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 6px;
  margin: 16px 0;
}

.markdown-body :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 16px;
}

.markdown-body :deep(th),
.markdown-body :deep(td) {
  border: 1px solid var(--line-soft);
  padding: 8px 12px;
  text-align: left;
}

.markdown-body :deep(th) {
  background: var(--surface-muted);
  font-weight: 600;
}

.no-content {
  text-align: center;
  padding: 60px 20px;
  color: var(--text-muted);
  font-size: 15px;
}

/* 评论区域 */
.comments-section {
  margin-top: 40px;
  padding-top: 32px;
  border-top: 1px solid var(--line-soft);
}

.comments-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 24px;
}

.comments-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: var(--text-strong);
}

.comments-count {
  font-size: 16px;
  color: var(--text-muted);
}

.comments-placeholder {
  padding: 60px 20px;
  text-align: center;
  background: var(--surface-muted);
  border-radius: 8px;
  border: 1px dashed var(--line-soft);
}

.placeholder-text {
  margin: 0;
  color: var(--text-muted);
  font-size: 15px;
}

/* 加载和错误状态 */
.loading-state,
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  gap: 16px;
}

.loader {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 4px solid var(--line-soft);
  border-top-color: var(--brand-primary);
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.error-state p {
  color: var(--text-secondary);
  font-size: 16px;
}

.back-button {
  padding: 10px 20px;
  background: var(--brand-primary);
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.back-button:hover {
  background: #006EDC;
}

@media (max-width: 768px) {
  .note-detail-page {
    padding: 12px;
  }

  .detail-container {
    padding: 24px 20px;
  }

  .note-title {
    font-size: 22px;
  }

  .note-meta-section {
    flex-direction: column;
    align-items: flex-start;
  }

  .stats-info {
    width: 100%;
    justify-content: space-between;
  }

  .stat-item {
    font-size: 13px;
  }
}
</style>

