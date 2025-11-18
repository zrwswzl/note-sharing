<template>
  <div class="workspace-container">
    <div class="workspace-layout">
      <!-- 空间列表 -->
      <div class="panel spaces-panel">
        <div class="panel-header">
          <h3>空间</h3>
          <button class="add-btn" @click="showCreateSpaceDialog = true">+ 新建</button>
        </div>
        <div class="list">
          <div 
            v-for="space in spaces" 
            :key="space.id"
            :class="['list-item', { active: selectedSpace?.id === space.id }]"
            @click="selectSpace(space)"
          >
            <span>📁 {{ space.name }}</span>
            <div class="item-actions">
              <button @click.stop="editSpace(space)">✏️</button>
              <button @click.stop="deleteSpace(space.id)">🗑️</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 笔记本列表 -->
      <div class="panel notebooks-panel">
        <div class="panel-header">
          <h3>笔记本</h3>
          <button 
            class="add-btn" 
            :disabled="!selectedSpace"
            @click="showCreateNotebookDialog = true"
          >
            + 新建
          </button>
        </div>
        <div class="list">
          <div 
            v-for="notebook in notebooks" 
            :key="notebook.id"
            :class="['list-item', { active: selectedNotebook?.id === notebook.id }]"
            @click="selectNotebook(notebook)"
          >
            <span>📒 {{ notebook.name }}</span>
            <div class="item-actions">
              <button @click.stop="moveNotebook(notebook)">📤</button>
              <button @click.stop="editNotebook(notebook)">✏️</button>
              <button @click.stop="deleteNotebook(notebook.id)">🗑️</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 笔记列表 -->
      <div class="panel notes-panel">
        <div class="panel-header">
          <h3>笔记</h3>
          <button 
            class="add-btn" 
            :disabled="!selectedNotebook"
            @click="showCreateNoteDialog = true"
          >
            + 新建
          </button>
        </div>
        <div class="list">
          <div 
            v-for="note in notes" 
            :key="note.id"
            :class="['list-item', { active: selectedNote?.id === note.id }]"
            @click="selectNote(note)"
          >
            <span>📄 {{ note.title }}</span>
            <div class="item-actions">
              <button @click.stop="moveNote(note)">📤</button>
              <button @click.stop="editNote(note)">✏️</button>
              <button @click.stop="deleteNote(note.id)">🗑️</button>
            </div>
          </div>
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
          <option v-for="space in spaces" :key="space.id" :value="space.id">
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

        <div v-if="noteType === 'editor'" class="editor-container">
          <textarea 
            v-model="noteContent" 
            placeholder="请输入笔记内容（支持富文本）"
            rows="10"
          ></textarea>
        </div>

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
import { ref, computed } from 'vue'

// 数据
const spaces = ref([])
const notebooks = ref([])
const notes = ref([])
const allNotebooks = ref([]) // 用于移动笔记时显示所有笔记本

// 选中的项
const selectedSpace = ref(null)
const selectedNotebook = ref(null)
const selectedNote = ref(null)

// 对话框显示状态
const showCreateSpaceDialog = ref(false)
const showCreateNotebookDialog = ref(false)
const showCreateNoteDialog = ref(false)
const showMoveNotebookDialog = ref(false)
const showMoveNoteDialog = ref(false)

// 编辑状态
const editingSpace = ref(null)
const editingNotebook = ref(null)
const editingNote = ref(null)

// 表单数据
const newSpaceName = ref('')
const newNotebookName = ref('')
const newNoteTitle = ref('')
const noteContent = ref('')
const noteType = ref('editor')
const uploadedFile = ref(null)
const targetSpaceId = ref('')
const targetNotebookId = ref('')

/**
 * API: 获取所有空间
 * GET /api/spaces
 * 输出: {
 *   code: number,
 *   data: [{ id: string, name: string, createdAt: string }]
 * }
 */
const loadSpaces = async () => {
  // 调用API加载空间列表
}

/**
 * API: 创建空间
 * POST /api/spaces
 * 输入: { name: string }
 * 输出: { code: number, data: { id: string, name: string } }
 */
/**
 * API: 更新空间
 * PUT /api/spaces/:id
 * 输入: { name: string }
 * 输出: { code: number, message: string }
 */
const handleCreateOrUpdateSpace = async () => {
  if (!newSpaceName.value.trim()) return
  
  if (editingSpace.value) {
    // 更新空间
    console.log('更新空间:', editingSpace.value.id, newSpaceName.value)
  } else {
    // 创建空间
    console.log('创建空间:', newSpaceName.value)
  }
  
  newSpaceName.value = ''
  editingSpace.value = null
  showCreateSpaceDialog.value = false
  loadSpaces()
}

/**
 * API: 删除空间
 * DELETE /api/spaces/:id
 * 输出: { code: number, message: string }
 */
const deleteSpace = async (id) => {
  if (confirm('确定要删除此空间吗?')) {
    console.log('删除空间:', id)
    loadSpaces()
  }
}

const editSpace = (space) => {
  editingSpace.value = space
  newSpaceName.value = space.name
  showCreateSpaceDialog.value = true
}

const selectSpace = (space) => {
  selectedSpace.value = space
  selectedNotebook.value = null
  selectedNote.value = null
  loadNotebooks(space.id)
}

/**
 * API: 获取空间下的笔记本
 * GET /api/spaces/:spaceId/notebooks
 * 输出: {
 *   code: number,
 *   data: [{ id: string, name: string, spaceId: string, createdAt: string }]
 * }
 */
const loadNotebooks = async (spaceId) => {
  // 调用API加载笔记本列表
}

/**
 * API: 创建笔记本
 * POST /api/notebooks
 * 输入: { name: string, spaceId: string }
 * 输出: { code: number, data: { id: string, name: string } }
 */
/**
 * API: 更新笔记本
 * PUT /api/notebooks/:id
 * 输入: { name: string }
 * 输出: { code: number, message: string }
 */
const handleCreateOrUpdateNotebook = async () => {
  if (!newNotebookName.value.trim()) return
  
  if (editingNotebook.value) {
    console.log('更新笔记本:', editingNotebook.value.id, newNotebookName.value)
  } else {
    console.log('创建笔记本:', newNotebookName.value, selectedSpace.value.id)
  }
  
  newNotebookName.value = ''
  editingNotebook.value = null
  showCreateNotebookDialog.value = false
  loadNotebooks(selectedSpace.value.id)
}

/**
 * API: 删除笔记本
 * DELETE /api/notebooks/:id
 * 输出: { code: number, message: string }
 */
const deleteNotebook = async (id) => {
  if (confirm('确定要删除此笔记本吗?')) {
    console.log('删除笔记本:', id)
    loadNotebooks(selectedSpace.value.id)
  }
}

const editNotebook = (notebook) => {
  editingNotebook.value = notebook
  newNotebookName.value = notebook.name
  showCreateNotebookDialog.value = true
}

/**
 * API: 移动笔记本到其他空间
 * PUT /api/notebooks/:id/move
 * 输入: { targetSpaceId: string }
 * 输出: { code: number, message: string }
 */
const moveNotebook = (notebook) => {
  editingNotebook.value = notebook
  targetSpaceId.value = ''
  showMoveNotebookDialog.value = true
}

const handleMoveNotebook = async () => {
  if (!targetSpaceId.value) return
  
  console.log('移动笔记本:', editingNotebook.value.id, '到空间:', targetSpaceId.value)
  
  showMoveNotebookDialog.value = false
  editingNotebook.value = null
  targetSpaceId.value = ''
}

const selectNotebook = (notebook) => {
  selectedNotebook.value = notebook
  selectedNote.value = null
  loadNotes(notebook.id)
}

/**
 * API: 获取笔记本下的笔记
 * GET /api/notebooks/:notebookId/notes
 * 输出: {
 *   code: number,
 *   data: [{ id: string, title: string, notebookId: string, createdAt: string, updatedAt: string }]
 * }
 */
const loadNotes = async (notebookId) => {
  // 调用API加载笔记列表
}

/**
 * API: 创建笔记(在线编辑)
 * POST /api/notes
 * 输入: { title: string, content: string, notebookId: string, type: 'editor' }
 * 输出: { code: number, data: { id: string, title: string } }
 */
/**
 * API: 创建笔记(上传文件)
 * POST /api/notes/upload
 * 输入: FormData { title: string, file: File, notebookId: string, type: 'upload' }
 * 输出: { code: number, data: { id: string, title: string, fileUrl: string } }
 */
/**
 * API: 更新笔记
 * PUT /api/notes/:id
 * 输入: { title: string, content: string }
 * 输出: { code: number, message: string }
 */
const handleCreateOrUpdateNote = async () => {
  if (!newNoteTitle.value.trim()) return
  
  if (editingNote.value) {
    console.log('更新笔记:', editingNote.value.id)
  } else {
    if (noteType.value === 'editor') {
      console.log('创建笔记(编辑):', newNoteTitle.value, noteContent.value)
    } else {
      console.log('创建笔记(上传):', newNoteTitle.value, uploadedFile.value)
    }
  }
  
  newNoteTitle.value = ''
  noteContent.value = ''
  uploadedFile.value = null
  editingNote.value = null
  showCreateNoteDialog.value = false
  loadNotes(selectedNotebook.value.id)
}

/**
 * API: 删除笔记
 * DELETE /api/notes/:id
 * 输出: { code: number, message: string }
 */
const deleteNote = async (id) => {
  if (confirm('确定要删除此笔记吗?')) {
    console.log('删除笔记:', id)
    loadNotes(selectedNotebook.value.id)
  }
}

const editNote = (note) => {
  editingNote.value = note
  newNoteTitle.value = note.title
  noteContent.value = note.content || ''
  noteType.value = 'editor'
  showCreateNoteDialog.value = true
}

/**
 * API: 移动笔记到其他笔记本
 * PUT /api/notes/:id/move
 * 输入: { targetNotebookId: string }
 * 输出: { code: number, message: string }
 */
const moveNote = (note) => {
  editingNote.value = note
  targetNotebookId.value = ''
  showMoveNoteDialog.value = true
  loadAllNotebooks()
}

/**
 * API: 获取所有笔记本(跨空间)
 * GET /api/notebooks
 * 输出: {
 *   code: number,
 *   data: [{ id: string, name: string, spaceName: string }]
 * }
 */
const loadAllNotebooks = async () => {
  // 调用API加载所有笔记本
}

const handleMoveNote = async () => {
  if (!targetNotebookId.value) return
  
  console.log('移动笔记:', editingNote.value.id, '到笔记本:', targetNotebookId.value)
  
  showMoveNoteDialog.value = false
  editingNote.value = null
  targetNotebookId.value = ''
}

const selectNote = (note) => {
  selectedNote.value = note
  // 这里可以显示笔记详情或打开编辑器
}
//编辑器
const handleFileUpload = (event) => {
  uploadedFile.value = event.target.files[0]
}
</script>

<style scoped>
.workspace-container {
  height: 100%;
}

.workspace-layout {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 20px;
  height: 100%;
}

.panel {
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.panel-header {
  background: #00bcd4;
  color: white;
  padding: 15px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.panel-header h3 {
  margin: 0;
  font-size: 18px;
}

.add-btn {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: none;
  padding: 6px 15px;
  border-radius: 5px;
  cursor: pointer;
  transition: background 0.3s;
}

.add-btn:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.3);
}

.add-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.list {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
}

.list-item {
  padding: 12px 15px;
  margin-bottom: 8px;
  border: 1px solid #e0e0e0;
  border-radius: 5px;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.3s;
}

.list-item:hover {
  background: #f5f5f5;
}

.list-item.active {
  background: #e0f7fa;
  border-color: #00bcd4;
}

.item-actions {
  display: flex;
  gap: 5px;
}

.item-actions button {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 16px;
  padding: 4px;
  opacity: 0.6;
  transition: opacity 0.3s;
}

.item-actions button:hover {
  opacity: 1;
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