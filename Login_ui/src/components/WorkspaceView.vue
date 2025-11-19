<template>
  <div class="workspace-container">
    <!-- 顶部搜索栏 -->
    <div class="search-bar-container">
      <input v-model="searchQuery" class="search-input" placeholder="搜索笔记标题、空间或笔记本..." @keyup.enter="handleSearch" />
      <button class="primary search-btn" @click="handleSearch">搜索</button>
    </div>
    <div class="workspace-layout-3col">
      <!-- 侧边栏树结构 START -->
      <div class="sidebar-tree">
        <!-- 空间 -->
        <ul class="tree-root">
          <li v-for="space in spacesWithExpand" :key="space.id" class="tree-node">
            <div :class="['node-label', { active: selectedSpace?.id === space.id }]" @click="onToggleExpand('space', space)">
              <span @click.stop="selectSpace(space)">
                <span class="expand-trigger" v-if="space.expandable" @click.stop="toggleExpand('space', space)">
                  <span v-if="space.expanded">▼</span>
                  <span v-else>▶</span>
                </span>
                📁 {{ space.name }}
              </span>
              <span class="item-actions">
                <button @click.stop="editSpace(space)">✏️</button>
                <button @click.stop="deleteSpace(space.id)">🗑️</button>
                <button @click.stop="showCreateNotebookForSpace(space)">➕</button>
              </span>
            </div>
            <ul v-show="space.expanded" v-if="space.notebooks && space.notebooks.length" class="tree-children">
              <li v-for="notebook in space.notebooks" :key="notebook.id" class="tree-node">
                <div :class="['node-label', { active: selectedNotebook?.id === notebook.id }]" @click="onToggleExpand('notebook', notebook, space)">
                  <span @click.stop="selectNotebook(notebook, space)">
                    <span class="expand-trigger" v-if="notebook.expandable" @click.stop="toggleExpand('notebook', notebook, space)">
                      <span v-if="notebook.expanded">▼</span>
                      <span v-else>▶</span>
                    </span>
                    📒 {{ notebook.name }}
                  </span>
                  <span class="item-actions">
                    <button @click.stop="moveNotebook(notebook)">📤</button>
                    <button @click.stop="editNotebook(notebook)">✏️</button>
                    <button @click.stop="deleteNotebook(notebook.id)">🗑️</button>
                    <button @click.stop="showCreateNoteForNotebook(notebook, space)">➕</button>
                  </span>
                </div>
                <ul v-show="notebook.expanded" v-if="notebook.notes && notebook.notes.length" class="tree-children">
                  <li v-for="note in notebook.notes" :key="note.id" :class="['tree-node', {'active-note': selectedNote?.id === note.id}]">
                    <div class="node-label" @click="selectNote(note, notebook, space)">
                      📄 {{ note.title }}
                      <span class="item-actions">
                        <button @click.stop="moveNote(note)">📤</button>
                        <button @click.stop="editNote(note)">✏️</button>
                        <button @click.stop="deleteNote(note.id)">🗑️</button>
                      </span>
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
        <button class="sidebar-add-btn" @click="showCreateSpaceDialog = true">+ 新建空间</button>
      </div>
      <!-- 侧边栏树结构 END -->

      <!-- 中间栏：占位，可扩展用于标签、预览等 -->
      <div class="middle-panel"></div>

      <!-- 右侧：笔记详情展示 -->
      <div class="right-panel" v-if="selectedNoteContent">
        <div class="note-detail-header">
          <h3>{{ selectedNote?.title || '笔记详情' }}</h3>
        </div>
        <div class="note-detail-content" v-if="selectedNoteContent.type === 'editor'">
          <div v-html="selectedNoteContent.content"></div>
        </div>
        <div class="note-detail-content" v-else-if="selectedNoteContent.type === 'upload'">
          <div>
            <a :href="selectedNoteContent.fileUrl" target="_blank">下载附件/预览</a>
          </div>
        </div>
        <div class="note-detail-content" v-else>
          <em>无法展示此类型的内容</em>
        </div>
      </div>
    </div>

    <!-- 创建空间对话框 -->
    <div v-if="showCreateSpaceDialog" class="modal" @click.self="showCreateSpaceDialog = false">
      <div class="modal-content">
        <h3>{{ editingSpace ? '重命名空间' : '创建新空间' }}</h3>
        <input v-model="newSpaceName" type="text" placeholder="请输入空间名称" />
        <div class="modal-actions">
          <button @click="showCreateSpaceDialog = false">取消</button>
          <button class="primary" @click="handleCreateOrUpdateSpace">确定</button>
        </div>
      </div>
    </div>

    <!-- 创建笔记本对话框 -->
    <div v-if="showCreateNotebookDialog" class="modal" @click.self="showCreateNotebookDialog = false">
      <div class="modal-content">
        <h3>{{ editingNotebook ? '重命名笔记本' : '创建新笔记本' }}</h3>
        <input v-model="newNotebookName" type="text" placeholder="请输入笔记本名称" />
        <div class="modal-actions">
          <button @click="showCreateNotebookDialog = false">取消</button>
          <button class="primary" @click="handleCreateOrUpdateNotebook">确定</button>
        </div>
      </div>
    </div>

    <!-- 移动笔记本对话框 -->
    <div v-if="showMoveNotebookDialog" class="modal" @click.self="showMoveNotebookDialog = false">
      <div class="modal-content">
        <h3>移动笔记本到其他空间</h3>
        <select v-model="targetSpaceId">
          <option value="">请选择目标空间</option>
          <option v-for="space in spacesWithExpand" :key="space.id" :value="space.id">
            {{ space.name }}
          </option>
        </select>
        <div class="modal-actions">
          <button @click="showMoveNotebookDialog = false">取消</button>
          <button class="primary" @click="handleMoveNotebook">确定</button>
        </div>
      </div>
    </div>

    <!-- 移动笔记对话框 -->
    <div v-if="showMoveNoteDialog" class="modal" @click.self="showMoveNoteDialog = false">
      <div class="modal-content">
        <h3>移动笔记到其他笔记本</h3>
        <select v-model="targetNotebookId">
          <option value="">请选择目标笔记本</option>
          <option v-for="notebook in allNotebooks" :key="notebook.id" :value="notebook.id">
            {{ notebook.name }} ({{ notebook.spaceName }})
          </option>
        </select>
        <div class="modal-actions">
          <button @click="showMoveNoteDialog = false">取消</button>
          <button class="primary" @click="handleMoveNote">确定</button>
        </div>
      </div>
    </div>

    <!-- 创建/编辑笔记对话框 -->
    <div v-if="showCreateNoteDialog" class="modal large" @click.self="showCreateNoteDialog = false">
      <div class="modal-content">
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

            {formData.noteType === 'editor' && (
              <div className="editor-container">
                <textarea 
                  placeholder="请输入笔记内容"
                  rows="10"
                  value={formData.content}
                  onChange={(e) => setFormData({ ...formData, content: e.target.value })}
                />
              </div>
            )}

        <div v-if="noteType === 'upload'" class="upload-container">
          <input type="file" @change="handleFileUpload" accept=".txt,.md,.pdf,.doc,.docx" />
          <p v-if="uploadedFile" class="uploaded-file">已选择: {{ uploadedFile.name }}</p>
        </div>

        <div class="modal-actions">
          <button @click="showCreateNoteDialog = false">取消</button>
          <button class="primary" @click="handleCreateOrUpdateNote">确定</button>
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

// 初始化
refreshTree()

</script>

<style scoped>
.workspace-container {
  height: 100%;
}
.search-bar-container {
  background: #f5f5f5;
  padding: 12px 25px;
  display: flex;
  gap: 16px;
}
.search-input {
  flex: 1;
  font-size: 16px;
  padding: 7px 12px;
  border: 1px solid #e0e0e0;
  border-radius: 5px;
  transition: border .2s;
}
.search-input:focus{border-color:#00bcd4;}

.search-btn {
  padding: 8px 16px;
  font-size: 15px;
}

.workspace-layout-3col {
  display: grid;
  grid-template-columns: 320px 1fr 2fr;
  gap: 20px;
  height: calc(100% - 48px);
  min-height: 550px;
}

.sidebar-tree {
  background: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  height: 100%;
  padding: 16px 0 16px 0;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}
.tree-root {
  padding-left: 0;
  margin: 0;
  list-style: none;
}
.tree-node {
  margin: 0;
  margin-bottom: 4px;
  font-size: 15px;
}
.tree-children {
  list-style: none;
  padding-left: 26px;
  margin: 5px 0 8px 0;
}
.node-label {
  display: flex;
  align-items: center;
  padding: 7px 12px;
  border-radius: 5px;
  justify-content: space-between;
  background: none;
  cursor: pointer;
  transition: background .2s;
}
.node-label.active, .tree-node.active-note > .node-label {
  background: #e0f7fa;
}
.node-label:hover {
  background: #f5fbfe;
}
.expand-trigger {
  display: inline-block;
  width: 18px;
  text-align: center;
  margin-right: 3px;
  user-select: none;
  color: #888;
}
.item-actions {
  display: flex;
  gap: 4px;
}
.item-actions button {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 15px;
  padding: 2px 4px;
  opacity: 0.7;
  transition: opacity 0.2s;
  margin-left: 2px;
}
.item-actions button:hover {
  opacity: 1;
}
.sidebar-add-btn {
  margin: 12px auto 0 auto;
  display: block;
  background: #00bcd4;
  color: white;
  border: none;
  padding: 7px 18px;
  border-radius: 24px;
  font-size: 16px;
  cursor: pointer;
  margin-top: auto;
  margin-bottom: 0;
}
.sidebar-add-btn:hover {
  background: #0097a7;
}
.tree-empty{
  color: #aaa;
  font-size: 13px;
  padding-left: 20px;
}

.middle-panel {
  /* 用于后续拓展：标签列表或预览等 */
}
.right-panel {
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e0e0e0;
  min-height: 400px;
  padding: 30px;
  word-break: break-all;
}
.note-detail-header {
  border-bottom:1px solid #e0e0e0;
  margin-bottom: 15px;
  padding-bottom: 8px;
}
.note-detail-content {
  margin-top: 18px;
  font-size: 15px;
}
.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}
.modal-content {
  background: white;
  padding: 30px;
  border-radius: 10px;
  min-width: 400px;
  max-width: 90%;
}
.modal.large .modal-content {
  min-width: 600px;
}
.modal-content h3 {
  margin: 0 0 20px 0;
  color: #333;
}
.modal-content input[type="text"],
.modal-content select,
.modal-content textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #e0e0e0;
  border-radius: 5px;
  font-size: 14px;
  margin-bottom: 15px;
}
.note-title-input {
  font-size: 16px;
  font-weight: bold;
}
.note-type-selector {
  display: flex;
  gap: 20px;
  margin-bottom: 15px;
}
.note-type-selector label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}
.editor-container textarea {
  font-family: monospace;
  resize: vertical;
}
.upload-container {
  padding: 20px;
  border: 2px dashed #e0e0e0;
  border-radius: 5px;
  text-align: center;
  margin-bottom: 15px;
}
.uploaded-file {
  margin-top: 10px;
  color: #00bcd4;
  font-weight: bold;
}
.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}
.modal-actions button {
  padding: 8px 20px;
  border: 1px solid #e0e0e0;
  border-radius: 5px;
  cursor: pointer;
  transition: all 0.3s;
}
.modal-actions button:hover {
  background: #f5f5f5;
}
.modal-actions button.primary {
  background: #00bcd4;
  color: white;
  border-color: #00bcd4;
}
.modal-actions button.primary:hover {
  background: #00acc1;
}
</style>