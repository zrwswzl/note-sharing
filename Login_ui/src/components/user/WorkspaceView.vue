<template>
  <div class="workspace-page">
    <section class="workspace-hero">
      <div class="hero-text">
        <p class="section-label">知识空间</p>
        <h1>工作台</h1>
        <p class="hero-subtext">浏览空间树、查看笔记详情并快速管理内容，所有操作保持当前逻辑。</p>
      </div>
      <div class="hero-search">
        <input
          v-model="searchQuery"
          type="text"
          placeholder="搜索笔记标题、空间或笔记本..."
          @keyup.enter="handleSearch"
        />
        <button class="text-action primary" type="button" @click="handleSearch">
          <span>搜索</span>
          <span aria-hidden="true">↗</span>
        </button>
      </div>
    </section>

    <div class="workspace-grid">
      <aside class="sidebar-tree">
        <div class="sidebar-head">
          <div>
            <p class="label">结构树</p>
            <h3>空间与笔记本</h3>
          </div>
          <button class="ghost-btn" type="button" @click="showCreateSpaceDialog = true">+ 新建空间</button>
        </div>

        <ul class="tree-root">
          <li v-for="space in spacesWithExpand" :key="space.id" class="tree-node">
            <div :class="['node-label', { active: selectedSpace?.id === space.id }]" @click="onToggleExpand('space', space)">
              <div class="node-main">
                <button
                  class="expand-trigger"
                  type="button"
                  v-if="space.expandable"
                  @click.stop="toggleExpand('space', space)"
                >
                  <span :class="['chevron', { open: space.expanded }]"></span>
                </button>
                <button class="node-focus" type="button" @click.stop="selectSpace(space)">
                  <span class="icon icon-folder" aria-hidden="true"></span>
                  <span>{{ space.name }}</span>
                </button>
              </div>
              <div class="item-actions">
                <button class="ghost-btn" type="button" @click.stop="editSpace(space)">重命名</button>
                <button class="ghost-btn" type="button" @click.stop="deleteSpace(space.id)">删除</button>
                <button class="ghost-btn" type="button" @click.stop="showCreateNotebookForSpace(space)">新建笔记本</button>
              </div>
            </div>

            <ul v-show="space.expanded" v-if="space.notebooks && space.notebooks.length" class="tree-children">
              <li v-for="notebook in space.notebooks" :key="notebook.id" class="tree-node">
                <div :class="['node-label', { active: selectedNotebook?.id === notebook.id }]" @click="onToggleExpand('notebook', notebook, space)">
                  <div class="node-main">
                    <button
                      class="expand-trigger"
                      type="button"
                      v-if="notebook.expandable"
                      @click.stop="toggleExpand('notebook', notebook, space)"
                    >
                      <span :class="['chevron', { open: notebook.expanded }]"></span>
                    </button>
                    <button class="node-focus" type="button" @click.stop="selectNotebook(notebook, space)">
                      <span class="icon icon-notebook" aria-hidden="true"></span>
                      <span>{{ notebook.name }}</span>
                    </button>
                  </div>
                  <div class="item-actions">
                    <button class="ghost-btn" type="button" @click.stop="moveNotebook(notebook)">移动</button>
                    <button class="ghost-btn" type="button" @click.stop="editNotebook(notebook)">重命名</button>
                    <button class="ghost-btn" type="button" @click.stop="deleteNotebook(notebook.id)">删除</button>
                    <button class="ghost-btn" type="button" @click.stop="showCreateNoteForNotebook(notebook, space)">新建笔记</button>
                  </div>
                </div>

                <ul v-show="notebook.expanded" v-if="notebook.notes && notebook.notes.length" class="tree-children">
                  <li
                    v-for="note in notebook.notes"
                    :key="note.id"
                    :class="['tree-node', { 'active-note': selectedNote?.id === note.id }]"
                  >
                    <div class="node-label note-node" @click="selectNote(note, notebook, space)">
                      <div class="node-main">
                        <span class="icon icon-note" aria-hidden="true"></span>
                        <span>{{ note.title }}</span>
                      </div>
                      <div class="item-actions">
                        <button class="ghost-btn" type="button" @click.stop="moveNote(note)">移动</button>
                        <button class="ghost-btn" type="button" @click.stop="editNote(note)">编辑</button>
                        <button class="ghost-btn" type="button" @click.stop="deleteNote(note.id)">删除</button>
                      </div>
                    </div>
                  </li>
                  <li v-if="notebook.notes.length === 0">
                    <em class="tree-empty">暂无笔记</em>
                  </li>
                </ul>
                <ul v-if="notebook.expanded && (!notebook.notes || notebook.notes.length === 0)">
                  <li>
                    <em class="tree-empty">暂无笔记</em>
                  </li>
                </ul>
              </li>
              <li v-if="space.notebooks.length === 0">
                <em class="tree-empty">暂无笔记本</em>
              </li>
            </ul>
            <ul v-if="space.expanded && (!space.notebooks || space.notebooks.length === 0)">
              <li>
                <em class="tree-empty">暂无笔记本</em>
              </li>
            </ul>
          </li>
        </ul>
      </aside>

      <section class="middle-panel">
        <div class="info-card">
          <p class="label">当前空间</p>
          <h3>{{ selectedSpace?.name || '未选择空间' }}</h3>
          <p class="muted">选择左侧空间以加载对应笔记本。</p>
        </div>
        <div class="info-card">
          <p class="label">当前笔记本</p>
          <h3>{{ selectedNotebook?.name || '未选择笔记本' }}</h3>
          <p class="muted">在笔记本下可创建在线笔记或上传文件。</p>
        </div>
      </section>

      <section class="right-panel" v-if="selectedNoteContent">
        <div class="note-detail-header">
          <p class="label">笔记详情</p>
          <h3>{{ selectedNote?.title || '笔记详情' }}</h3>
        </div>

        <div class="note-detail-actions">
          <div class="action-cluster">
            <button
              type="button"
              class="pill-btn"
              :class="{ active: noteEngagement.isFavorite }"
              @click="toggleFavoriteNote"
            >
              {{ noteEngagement.isFavorite ? '已收藏' : '收藏笔记' }}
            </button>
            <label class="folder-select">
              <span>收藏夹</span>
              <select v-model="noteEngagement.folder">
                <option v-for="folder in favoriteFolderOptions" :key="folder">{{ folder }}</option>
              </select>
            </label>
          </div>
          <div class="action-cluster">
            <button type="button" class="pill-btn ghost" @click="toggleInlineComments">
              查看评论 ({{ noteEngagement.commentCount }})
            </button>
            <button type="button" class="pill-btn primary" @click="addQuickComment">
              写评论
            </button>
          </div>
        </div>

        <div class="note-detail-content" v-if="selectedNoteContent.type === 'editor'">
          <div v-html="selectedNoteContent.content"></div>
        </div>
        <div class="note-detail-content" v-else-if="selectedNoteContent.type === 'upload'">
          <a :href="selectedNoteContent.fileUrl" target="_blank" class="text-link">下载附件 / 预览</a>
        </div>
        <div class="note-detail-content" v-else>
          <em>无法展示此类型的内容</em>
        </div>

        <div v-if="showInlineComments" class="note-inline-comments">
          <div class="comment-headline">
            <p>最新评论 ({{ inlineComments.length }})</p>
            <button type="button" class="text-link" @click="showInlineComments = false">收起</button>
          </div>
          <article v-for="comment in inlineComments" :key="comment.id" class="inline-comment">
            <header>
              <strong>{{ comment.author }}</strong>
              <span>{{ comment.createdAt }}</span>
            </header>
            <p>{{ comment.content }}</p>
          </article>
        </div>
      </section>
      <section class="right-panel empty" v-else>
        <p>选择需要查看的笔记</p>
        <small>点击左侧树状结构中的笔记即可在此处预览内容。</small>
      </section>
    </div>

    <div v-if="showCreateSpaceDialog" class="modal" @click.self="showCreateSpaceDialog = false">
      <div class="modal-card">
        <h3>{{ editingSpace ? '重命名空间' : '创建新空间' }}</h3>
        <input v-model="newSpaceName" type="text" placeholder="请输入空间名称" />
        <div class="modal-actions">
          <button type="button" class="ghost-btn" @click="showCreateSpaceDialog = false">取消</button>
          <button type="button" class="text-action primary" @click="handleCreateOrUpdateSpace">确定</button>
        </div>
      </div>
    </div>

    <div v-if="showCreateNotebookDialog" class="modal" @click.self="showCreateNotebookDialog = false">
      <div class="modal-card">
        <h3>{{ editingNotebook ? '重命名笔记本' : '创建新笔记本' }}</h3>
        <input v-model="newNotebookName" type="text" placeholder="请输入笔记本名称" />
        <div class="modal-actions">
          <button type="button" class="ghost-btn" @click="showCreateNotebookDialog = false">取消</button>
          <button type="button" class="text-action primary" @click="handleCreateOrUpdateNotebook">确定</button>
        </div>
      </div>
    </div>

    <div v-if="showMoveNotebookDialog" class="modal" @click.self="showMoveNotebookDialog = false">
      <div class="modal-card">
        <h3>移动笔记本到其他空间</h3>
        <select v-model="targetSpaceId">
          <option value="">请选择目标空间</option>
          <option v-for="space in spacesWithExpand" :key="space.id" :value="space.id">
            {{ space.name }}
          </option>
        </select>
        <div class="modal-actions">
          <button type="button" class="ghost-btn" @click="showMoveNotebookDialog = false">取消</button>
          <button type="button" class="text-action primary" @click="handleMoveNotebook">确定</button>
        </div>
      </div>
    </div>

    <div v-if="showMoveNoteDialog" class="modal" @click.self="showMoveNoteDialog = false">
      <div class="modal-card">
        <h3>移动笔记到其他笔记本</h3>
        <select v-model="targetNotebookId">
          <option value="">请选择目标笔记本</option>
          <option v-for="notebook in allNotebooks" :key="notebook.id" :value="notebook.id">
            {{ notebook.name }} ({{ notebook.spaceName }})
          </option>
        </select>
        <div class="modal-actions">
          <button type="button" class="ghost-btn" @click="showMoveNoteDialog = false">取消</button>
          <button type="button" class="text-action primary" @click="handleMoveNote">确定</button>
        </div>
      </div>
    </div>

    <div v-if="showCreateNoteDialog" class="modal large" @click.self="showCreateNoteDialog = false">
      <div class="modal-card">
        <h3>{{ editingNote ? '编辑笔记' : '创建新笔记' }}</h3>
        <input v-model="newNoteTitle" type="text" placeholder="请输入笔记标题" class="note-title-input" />

        <div class="note-type-selector">
          <label>
            <input type="radio" v-model="noteType" value="editor" /> 在线编辑
          </label>
          <label>
            <input type="radio" v-model="noteType" value="upload" /> 上传文件
          </label>
        </div>

        <div v-if="noteType === 'editor'" class="editor-container">
          <textarea
            v-model="noteContent"
            placeholder="请输入笔记内容"
            rows="10"
          ></textarea>
        </div>

        <div v-if="noteType === 'upload'" class="upload-container">
          <input type="file" @change="handleFileUpload" accept=".txt,.md,.pdf,.doc,.docx" />
          <p v-if="uploadedFile" class="uploaded-file">已选择: {{ uploadedFile.name }}</p>
        </div>

        <div class="modal-actions">
          <button type="button" class="ghost-btn" @click="showCreateNoteDialog = false">取消</button>
          <button type="button" class="text-action primary" @click="handleCreateOrUpdateNote">确定</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'

/** 顶部搜索栏 */
const searchQuery = ref('')
const searchResults = ref([])

const spacesRaw = ref([])
const spacesWithExpand = ref([])
// spacesWithExpand结构: [{ id, name, expanded, notebooks: [ { id, name, expanded, notes: [{...}] } ] }]

const allNotebooks = ref([]) // 移动功能用
const selectedSpace = ref(null)
const selectedNotebook = ref(null)
const selectedNote = ref(null)
const selectedNoteContent = ref(null)

/**
 * API 占位（收藏与评论，仅用于描述接口格式）
 * - POST /api/notes/{noteId}/favorite
 *   输入: { folderId: string }
 *   输出: { code: number, message: string }
 *   返回码: 200 已收藏 / 400 参数错误 / 401 未登录
 * - DELETE /api/notes/{noteId}/favorite
 *   输入: 无
 *   输出: { code: number, message: string }
 *   返回码: 200 已取消 / 404 未找到收藏
 * - GET /api/notes/{noteId}/comments
 *   输入: 无
 *   输出: { code: number, data: Comment[] }
 *   返回码: 200 成功
 * - POST /api/notes/{noteId}/comments
 *   输入: { content: string }
 *   输出: { code: number, data: { commentId: string } }
 *   返回码: 200 创建成功 / 400 参数错误
 */
const favoriteFolderOptions = ['默认收藏', '学习沉淀', '项目灵感', '灵感闪现']
const noteEngagement = ref({
  isFavorite: false,
  folder: favoriteFolderOptions[0],
  commentCount: 0
})
const showInlineComments = ref(false)
const inlineComments = ref([])
const commentPresetMap = {
  '1a1': [
    { id: 'c-101', author: '林梓萱', content: '这段拆解很清晰，已收藏！', createdAt: '2025-11-16 14:20' },
    { id: 'c-102', author: '马凡', content: '是否能补充 pinia 的示例？', createdAt: '2025-11-15 09:31' }
  ],
  '2a1': [
    { id: 'c-201', author: '项目组A', content: '附件很实用，感谢分享。', createdAt: '2025-11-10 20:05' }
  ]
}

// 对话框及编辑态
const showCreateSpaceDialog = ref(false)
const showCreateNotebookDialog = ref(false)
const showCreateNoteDialog = ref(false)
const showMoveNotebookDialog = ref(false)
const showMoveNoteDialog = ref(false)
const editingSpace = ref(null)
const editingNotebook = ref(null)
const editingNote = ref(null)

const newSpaceName = ref('')
const newNotebookName = ref('')
const newNoteTitle = ref('')
const noteContent = ref('')
const noteType = ref('editor')
const uploadedFile = ref(null)
const targetSpaceId = ref('')
const targetNotebookId = ref('')

/**
 * API: 获取所有空间 GET /api/spaces
 */
const loadSpaces = async () => {
  // const res = await fetch('/api/spaces'); const data = await res.json()
  // spacesRaw.value = data.data
  // 模拟: Demo结构
  // 实际应替换为后端接口
  spacesRaw.value = [{ id: '1', name: '工作', createdAt: '' }, { id: '2', name: '学习', createdAt: '' }]
  // 初始化 spacesWithExpand
  spacesWithExpand.value = spacesRaw.value.map(s => ({
    ...s, expanded: false, notebooks: [], expandable: true
  }))
}
/**
 * API: 获取某空间下的笔记本 GET /api/spaces/:spaceId/notebooks
 */
const loadNotebooksLazy = async (space) => {
  // const res = await fetch(`/api/spaces/${space.id}/notebooks`); const data = await res.json()
  // return data.data
  // 模拟
  if (space.id === '1') {
    return [{ id: '1a', name: '前端', spaceId: '1', createdAt: '' }, { id: '1b', name: '后端', spaceId: '1', createdAt: '' }]
  } else {
    return [{ id: '2a', name: '大学', spaceId: '2', createdAt: '' }]
  }
}
/**
 * API: 获取某笔记本下的笔记 GET /api/notebooks/:notebookId/notes
 */
const loadNotesLazy = async (notebook) => {
  // const res = await fetch(`/api/notebooks/${notebook.id}/notes`); const data = await res.json()
  // return data.data
  // 模拟
  if (notebook.id === '1a') return [
    { id: '1a1', title: 'Vue3基础', notebookId: '1a', createdAt: '', updatedAt: '', type: 'editor', content: '<p>内容A</p>' },
    { id: '1a2', title: 'Vue3进阶', notebookId: '1a', createdAt: '', updatedAt: '', type: 'editor', content: '<p>内容B</p>' }
  ]
  if (notebook.id === '1b') return []
  if (notebook.id === '2a') return [
    { id: '2a1', title: '数学笔记', notebookId: '2a', type: 'upload', fileUrl: '/note.pdf' }
  ]
  return []
}

/**
 * API: 获取笔记内容 GET /api/notes/:noteId
 * @param noteId
 * 返回的 data 里包含 type, content/fileUrl
 */
const fetchNoteDetail = async (noteId) => {
  // const res = await fetch(`/api/notes/${noteId}`); const data = await res.json()
  // return data.data
  // 模拟
  if (noteId === '1a1') return { id: '1a1', title: 'Vue3基础', type: 'editor', content: '<p>内容A</p>' }
  if (noteId === '2a1') return { id: '2a1', title: '数学笔记', type: 'upload', fileUrl: '/note.pdf' }
  return { id: noteId, title: '未知笔记', type: 'editor', content: '' }
}

const refreshTree = async () => {
  await loadSpaces()
}

// 树结构操作
const toggleExpand = async (type, node, parent = null) => {
  node.expanded = !node.expanded
  if (type === 'space' && node.expanded && (!node.notebooks || node.notebooks.length === 0)) {
    const notebooks = await loadNotebooksLazy(node)
    node.notebooks = notebooks.map(nb => ({
      ...nb, expanded: false, notes: [], expandable: true
    }))
  }
  if (type === 'notebook' && node.expanded && (!node.notes || node.notes.length === 0)) {
    const notes = await loadNotesLazy(node)
    node.notes = notes
  }
}

// 工具方法：点击非展开箭头时也自动选择节点
const onToggleExpand = async (type, node, parent = null) => {
  selectTreeNode(type, node, parent)
}

// 选中操作
const selectTreeNode = (type, node, parent = null) => {
  if (type === 'space') {
    selectedSpace.value = node
    selectedNotebook.value = null
    selectedNote.value = null
    selectedNoteContent.value = null
  }
  if (type === 'notebook') {
    selectedNotebook.value = node
    selectedSpace.value = parent
    selectedNote.value = null
    selectedNoteContent.value = null
  }
}

const selectSpace = (space) => {
  selectedSpace.value = space
  selectedNotebook.value = null
  selectedNote.value = null
  selectedNoteContent.value = null
  // 如果未展开，自动展开
  if (!space.expanded) toggleExpand('space', space)
}
const selectNotebook = (notebook, space) => {
  selectedSpace.value = space
  selectedNotebook.value = notebook
  selectedNote.value = null
  selectedNoteContent.value = null
  // 如果未展开，自动展开
  if (!notebook.expanded) toggleExpand('notebook', notebook, space)
}
const selectNote = async (note, notebook, space) => {
  selectedSpace.value = space
  selectedNotebook.value = notebook
  selectedNote.value = note
  // 展示笔记内容
  selectedNoteContent.value = await fetchNoteDetail(note.id)
  const preset = commentPresetMap[note.id] || []
  inlineComments.value = [...preset]
  noteEngagement.value = {
    isFavorite: note.id === '1a1',
    folder: favoriteFolderOptions[0],
    commentCount: preset.length
  }
  showInlineComments.value = false
}

// 工具栏新建
const showCreateNotebookForSpace = (space) => {
  editingNotebook.value = null
  newNotebookName.value = ''
  selectedSpace.value = space
  showCreateNotebookDialog.value = true
}
const showCreateNoteForNotebook = (notebook, space) => {
  editingNote.value = null
  newNoteTitle.value = ''
  noteContent.value = ''
  noteType.value = 'editor'
  selectedNotebook.value = notebook
  selectedSpace.value = space
  showCreateNoteDialog.value = true
}

// -----------------------------CRUB逻辑
const handleCreateOrUpdateSpace = async () => {
  if (!newSpaceName.value.trim()) return
  // POST /api/spaces  或 PUT /api/spaces/:id
  if (editingSpace.value) {
    // await fetch(`/api/spaces/${editingSpace.value.id}`, {...})
    // 后端实现: 更新空间
    // ...
  } else {
    // await fetch(`/api/spaces`, {...})
    // 后端实现: 新建
    // ...
  }
  newSpaceName.value = ''
  editingSpace.value = null
  showCreateSpaceDialog.value = false
  await refreshTree()
}
const editSpace = (space) => {
  editingSpace.value = space
  newSpaceName.value = space.name
  showCreateSpaceDialog.value = true
}
const deleteSpace = async (id) => {
  if (confirm('确定要删除此空间吗?')) {
    // await fetch(`/api/spaces/${id}`, {method:'DELETE'})
    // 后端实现: 删除空间
    await refreshTree()
  }
}

const handleCreateOrUpdateNotebook = async () => {
  if (!newNotebookName.value.trim()) return
  // POST /api/notebooks 或 PUT /api/notebooks/:id
  const parent = findSpaceById(selectedSpace.value?.id)
  if (editingNotebook.value) {
    // await fetch(`/api/notebooks/${editingNotebook.value.id}`, {...})
    // 后端实现: 更新笔记本
    // ...
  } else {
    // await fetch(`/api/notebooks`, {...}) 传{ name, spaceId }
    // 后端实现: 新建
    // ...
  }
  newNotebookName.value = ''
  editingNotebook.value = null
  showCreateNotebookDialog.value = false
  // 刷新该空间下的笔记本
  if (parent) await toggleExpand('space', parent)
}
const editNotebook = (notebook) => {
  editingNotebook.value = notebook
  newNotebookName.value = notebook.name
  showCreateNotebookDialog.value = true
}
const deleteNotebook = async (id) => {
  if (confirm('确定要删除此笔记本吗?')) {
    // await fetch(`/api/notebooks/${id}`, {method:'DELETE'})
    // 后端实现: 删除笔记本
    // ...
    // 查找该笔记本属于哪个空间
    const parent = spacesWithExpand.value.find(sp =>
      sp.notebooks.find(nb => nb.id === id)
    )
    if (parent) await toggleExpand('space', parent)
  }
}

const handleCreateOrUpdateNote = async () => {
  if (!newNoteTitle.value.trim()) return
  // POST /api/notes、/api/notes/upload 或 PUT /api/notes/:id
  if (editingNote.value) {
    // await fetch(`/api/notes/${editingNote.value.id}`, ...)
    // 后端实现: 更新
    // ...
  } else {
    if (noteType.value === 'editor') {
      // await fetch(`/api/notes`, {...})
      // 后端实现: 创建
    } else {
      // await fetch(`/api/notes/upload`, {...})
      // 后端实现: 上传
    }
  }
  newNoteTitle.value = ''
  noteContent.value = ''
  uploadedFile.value = null
  editingNote.value = null
  showCreateNoteDialog.value = false
  // 刷新当前笔记本下的笔记
  const parentNB = findNotebookInTree(selectedNotebook.value?.id)
  if (parentNB) await toggleExpand('notebook', parentNB)
}
const editNote = (note) => {
  editingNote.value = note
  newNoteTitle.value = note.title
  noteContent.value = note.content || ''
  noteType.value = note.type || 'editor'
  showCreateNoteDialog.value = true
}
const deleteNote = async (id) => {
  if (confirm('确定要删除此笔记吗?')) {
    // await fetch(`/api/notes/${id}`, {method:'DELETE'})
    // 后端实现: 删除
    // ...
    // 查找该笔记属于哪个notebook
    const parentNB = findNotebookByNoteId(id)
    if (parentNB) await toggleExpand('notebook', parentNB)
  }
}

const moveNotebook = (notebook) => {
  editingNotebook.value = notebook
  targetSpaceId.value = ''
  showMoveNotebookDialog.value = true
}
const handleMoveNotebook = async () => {
  if (!targetSpaceId.value) return
  // PUT /api/notebooks/:id/move
  // await fetch(`/api/notebooks/${editingNotebook.value.id}/move`, { targetSpaceId: targetSpaceId.value })
  showMoveNotebookDialog.value = false
  editingNotebook.value = null
  targetSpaceId.value = ''
  await refreshTree()
}
const moveNote = (note) => {
  editingNote.value = note
  targetNotebookId.value = ''
  showMoveNoteDialog.value = true
  loadAllNotebooksData()
}
const handleMoveNote = async () => {
  if (!targetNotebookId.value) return
  // PUT /api/notes/:id/move
  // await fetch(`/api/notes/${editingNote.value.id}/move`, { targetNotebookId: targetNotebookId.value })
  showMoveNoteDialog.value = false
  editingNote.value = null
  targetNotebookId.value = ''
  await refreshTree()
}

// 搜索功能
const handleSearch = async () => {
  // 
  // 这里可以结合后端:
  // GET /api/search/notes?query=searchQuery
  // 返回 {notes: [{id, title, notebookId, spaceId}]}
  // 示例: search 'Vue'
  // searchResults.value = [{id:'1a1', title:'Vue3基础', notebookId:'1a', spaceId:'1'}]
  // 这里只模拟下跳转到第一个匹配笔记
  if (searchQuery.value.trim() === '') return
  // 假设搜'Vue'能搜到第一个空间第一个笔记本第一个笔记
  if (searchQuery.value.includes('Vue')) {
    // 自动展开空间->笔记本->笔记，选中该笔记
    const space = spacesWithExpand.value.find(s => s.id === '1')
    if (!space.expanded) await toggleExpand('space', space)
    const notebook = space.notebooks.find(nb => nb.id === '1a')
    if (!notebook.expanded) await toggleExpand('notebook', notebook, space)
    const note = notebook.notes.find(n => n.id === '1a1')
    await selectNote(note, notebook, space)
  }
}

// ---- 辅助找树节点
function findSpaceById(id) {
  return spacesWithExpand.value.find(s => s.id === id)
}
function findNotebookInTree(id) {
  for (let sp of spacesWithExpand.value) {
    const nb = sp.notebooks?.find(n => n.id === id)
    if (nb) return nb
  }
  return null
}
function findNotebookByNoteId(noteId) {
  for (let sp of spacesWithExpand.value) {
    for (let nb of sp.notebooks || []) {
      if (nb.notes?.some(n => n.id === noteId)) {
        return nb
      }
    }
  }
  return null
}

// ----- 文件上传
const handleFileUpload = (event) => {
  uploadedFile.value = event.target.files[0]
}

// 所有笔记本（为移动功能用）
// GET /api/notebooks
const loadAllNotebooksData = async () => {
  // 应从后端API获取
  allNotebooks.value = [
    { id: '1a', name: '前端', spaceName: '工作' },
    { id: '1b', name: '后端', spaceName: '工作' },
    { id: '2a', name: '大学', spaceName: '学习' }
  ]
}

const toggleFavoriteNote = () => {
  // TODO: 调用 POST/DELETE /api/notes/{noteId}/favorite
  noteEngagement.value.isFavorite = !noteEngagement.value.isFavorite
}

const toggleInlineComments = () => {
  showInlineComments.value = !showInlineComments.value
}

const addQuickComment = () => {
  // TODO: 调用 POST /api/notes/{noteId}/comments
  const newComment = {
    id: `temp-${Date.now()}`,
    author: '我',
    content: '已添加示例评论，仅为前端展示占位。',
    createdAt: new Date().toLocaleString()
  }
  inlineComments.value = [newComment, ...inlineComments.value]
  noteEngagement.value.commentCount = inlineComments.value.length
  showInlineComments.value = true
}

// 初始化
refreshTree()

</script>

<style scoped>
:global(:root) {
  --brand-primary: #22ee99;
  --surface-base: #ffffff;
  --surface-muted: #f7faf8;
  --line-soft: #e6ece8;
  --text-strong: #111c17;
  --text-secondary: #4b5a53;
  --text-muted: #90a19b;
}

.workspace-page {
  min-height: 100vh;
  padding: 48px 32px 96px;
  background: var(--surface-muted);
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.workspace-hero {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  background: var(--surface-base);
  border: 1px solid var(--line-soft);
  border-radius: 32px;
  padding: 32px 40px;
  box-shadow: 0 25px 80px rgba(17, 28, 23, 0.08);
}

.hero-text h1 {
  margin: 8px 0 6px;
  font-size: 40px;
  color: var(--text-strong);
}

.section-label {
  font-size: 14px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--text-muted);
  margin: 0;
}

.hero-subtext {
  margin: 0;
  color: var(--text-secondary);
  max-width: 520px;
}

.hero-search {
  flex-shrink: 0;
  width: min(420px, 100%);
  display: flex;
  gap: 12px;
  align-items: center;
  border: 1px solid var(--line-soft);
  border-radius: 999px;
  padding: 10px 12px 10px 20px;
  background: var(--surface-muted);
}

.hero-search input {
  flex: 1;
  border: none;
  background: transparent;
  font-size: 16px;
  color: var(--text-strong);
}

.hero-search input:focus {
  outline: none;
}

.text-action {
  appearance: none;
  border: 1px solid transparent;
  background: transparent;
  border-radius: 999px;
  padding: 10px 18px;
  display: inline-flex;
  gap: 8px;
  align-items: center;
  font-size: 15px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: color 0.2s ease, border-color 0.2s ease;
}

.text-action.primary {
  background: var(--surface-base);
  border-color: var(--line-soft);
}

.text-action:hover,
.text-action:focus-visible {
  color: var(--brand-primary);
  border-color: var(--brand-primary);
}

.workspace-grid {
  display: grid;
  grid-template-columns: 320px minmax(0, 300px) 1fr;
  gap: 24px;
}

.sidebar-tree,
.middle-panel,
.right-panel {
  background: var(--surface-base);
  border: 1px solid var(--line-soft);
  border-radius: 32px;
  padding: 28px;
  box-shadow: 0 18px 60px rgba(17, 28, 23, 0.05);
}

.sidebar-tree {
  display: flex;
  flex-direction: column;
  max-height: 75vh;
}

.sidebar-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  gap: 12px;
}

.sidebar-head h3 {
  margin: 4px 0 0;
  font-size: 20px;
  color: var(--text-strong);
}

.label {
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--text-muted);
  margin: 0;
}

.ghost-btn {
  appearance: none;
  border: 1px solid var(--line-soft);
  border-radius: 999px;
  padding: 6px 14px;
  background: transparent;
  font-size: 13px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: color 0.2s ease, border-color 0.2s ease;
}

.ghost-btn:hover,
.ghost-btn:focus-visible {
  color: var(--brand-primary);
  border-color: var(--brand-primary);
}

.tree-root {
  list-style: none;
  margin: 0;
  padding-left: 0;
  overflow-y: auto;
}

.tree-node {
  margin-bottom: 6px;
  font-size: 15px;
}

.tree-children {
  list-style: none;
  padding-left: 26px;
  margin: 8px 0;
}

.node-label {
  border-radius: 18px;
  padding: 8px 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: background 0.2s ease;
}

.node-label:hover {
  background: rgba(34, 238, 153, 0.07);
}

.node-label.active,
.tree-node.active-note > .node-label {
  background: rgba(34, 238, 153, 0.15);
}

.node-main {
  display: flex;
  align-items: center;
  gap: 8px;
}

.node-focus {
  border: none;
  background: transparent;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  font-size: 15px;
  color: var(--text-strong);
  cursor: pointer;
}

.expand-trigger {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: 1px solid var(--line-soft);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  cursor: pointer;
}

.chevron {
  width: 8px;
  height: 8px;
  border-right: 1px solid var(--text-muted);
  border-bottom: 1px solid var(--text-muted);
  transform: rotate(-45deg);
  transition: transform 0.2s ease;
}

.chevron.open {
  transform: rotate(45deg);
}

.icon {
  width: 26px;
  height: 26px;
  border-radius: 8px;
  border: 1px solid var(--brand-primary);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.icon-folder::after,
.icon-notebook::after,
.icon-note::after {
  content: '';
  display: block;
  width: 12px;
  height: 8px;
  border: 1px solid var(--brand-primary);
  border-radius: 2px;
}

.icon-folder::after {
  width: 14px;
  height: 10px;
}

.icon-notebook::after {
  border-left-width: 2px;
}

.icon-note::after {
  border-top: none;
}

.note-node .node-main {
  gap: 12px;
}

.item-actions {
  display: flex;
  gap: 6px;
  flex-shrink: 0;
}

.tree-empty {
  color: var(--text-muted);
  font-size: 13px;
  padding: 6px 0 6px 24px;
}

.middle-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-card {
  padding: 18px;
  border-radius: 24px;
  border: 1px solid var(--line-soft);
  background: var(--surface-muted);
}

.info-card h3 {
  margin: 4px 0;
  font-size: 18px;
  color: var(--text-strong);
}

.info-card .muted {
  margin: 0;
  font-size: 14px;
  color: var(--text-secondary);
}

.right-panel {
  min-height: 420px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.right-panel.empty {
  align-items: flex-start;
  justify-content: center;
  color: var(--text-muted);
}

.right-panel.empty p {
  margin: 0 0 6px;
  font-size: 18px;
  color: var(--text-strong);
}

.note-detail-header {
  border-bottom: 1px solid var(--line-soft);
  padding-bottom: 12px;
}

.note-detail-header h3 {
  margin: 4px 0 0;
  font-size: 24px;
  color: var(--text-strong);
}

.note-detail-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
  padding: 16px 0 6px;
}

.action-cluster {
  display: inline-flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

.pill-btn {
  border-radius: 999px;
  border: 1px solid var(--line-soft);
  padding: 8px 18px;
  background: var(--surface-soft);
  color: var(--text-secondary);
  font-size: 14px;
  transition: all 0.2s ease;
}

.pill-btn.active,
.pill-btn.primary {
  background: var(--brand-primary);
  border-color: var(--brand-primary);
  color: #0b1f14;
  font-weight: 600;
}

.pill-btn.ghost {
  background: transparent;
}

.folder-select {
  display: inline-flex;
  gap: 8px;
  align-items: center;
  font-size: 14px;
  color: var(--text-secondary);
}

.folder-select select {
  border-radius: 999px;
  border: 1px solid var(--line-soft);
  padding: 6px 12px;
  background: var(--surface-soft);
}

.note-detail-content {
  font-size: 15px;
  color: var(--text-secondary);
  line-height: 1.7;
}

.note-inline-comments {
  margin-top: 18px;
  border-radius: 18px;
  border: 1px solid var(--line-soft);
  background: var(--surface-muted);
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.comment-headline {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  color: var(--text-secondary);
}

.inline-comment {
  border-radius: 16px;
  border: 1px dashed var(--line-soft);
  padding: 12px 16px;
  background: #fff;
}

.inline-comment header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: var(--text-muted);
  margin-bottom: 6px;
}

.text-link {
  color: var(--brand-primary);
  font-weight: 600;
}

.modal {
  position: fixed;
  inset: 0;
  background: rgba(17, 28, 23, 0.45);
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 24px;
  z-index: 1000;
}

.modal-card {
  width: min(520px, 100%);
  background: var(--surface-base);
  border-radius: 28px;
  border: 1px solid var(--line-soft);
  padding: 32px;
  box-shadow: 0 30px 70px rgba(17, 28, 23, 0.2);
}

.modal.large .modal-card {
  width: min(720px, 100%);
}

.modal-card h3 {
  margin: 0 0 18px;
  font-size: 22px;
  color: var(--text-strong);
}

.modal-card input[type='text'],
.modal-card select,
.modal-card textarea {
  width: 100%;
  padding: 12px 14px;
  border-radius: 16px;
  border: 1px solid var(--line-soft);
  background: var(--surface-muted);
  font-size: 15px;
  color: var(--text-strong);
  margin-bottom: 16px;
  transition: border-color 0.2s ease, background 0.2s ease;
}

.modal-card input:focus,
.modal-card select:focus,
.modal-card textarea:focus {
  outline: none;
  border-color: var(--brand-primary);
  background: #fff;
}

.note-title-input {
  font-weight: 600;
}

.note-type-selector {
  display: flex;
  gap: 20px;
  margin-bottom: 16px;
  color: var(--text-secondary);
}

.editor-container textarea {
  font-family: Menlo, Consolas, monospace;
  resize: vertical;
  min-height: 200px;
}

.upload-container {
  padding: 20px;
  border: 1px dashed var(--line-soft);
  border-radius: 18px;
  text-align: center;
  margin-bottom: 16px;
  background: var(--surface-muted);
}

.uploaded-file {
  margin-top: 10px;
  color: var(--text-secondary);
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 10px;
}

@media (max-width: 1100px) {
  .workspace-grid {
    grid-template-columns: 1fr;
  }

  .sidebar-tree,
  .middle-panel,
  .right-panel {
    max-height: none;
  }

  .hero-search {
    width: 100%;
  }

  .workspace-hero {
    flex-direction: column;
  }
}

@media (max-width: 640px) {
  .workspace-page {
    padding: 32px 20px 80px;
  }

  .workspace-hero,
  .sidebar-tree,
  .middle-panel,
  .right-panel {
    border-radius: 24px;
    padding: 24px;
  }

  .node-label {
    flex-direction: column;
    align-items: flex-start;
  }

  .item-actions {
    width: 100%;
    flex-wrap: wrap;
  }
}
</style>