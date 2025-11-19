
import { useState } from 'react';

export default function NoteWorkspace() {
  // 示例数据
  const [spaces, setSpaces] = useState([
    { id: '1', name: '工作空间', createdAt: '2024-01-01' },
    { id: '2', name: '个人学习', createdAt: '2024-01-02' }
  ]);

  const [notebooks, setNotebooks] = useState([
    { id: 'n1', name: '项目文档', spaceId: '1', createdAt: '2024-01-03' },
    { id: 'n2', name: '会议记录', spaceId: '1', createdAt: '2024-01-04' },
    { id: 'n3', name: 'JavaScript', spaceId: '2', createdAt: '2024-01-05' },
    { id: 'n4', name: 'React学习', spaceId: '2', createdAt: '2024-01-06' }
  ]);

  const [notes, setNotes] = useState([
    { id: 'note1', title: 'API设计文档', notebookId: 'n1', content: '这是API设计的详细内容，包含了RESTful接口规范、数据格式定义、错误码说明等。\n\n主要接口：\n1. 用户认证接口\n2. 数据查询接口\n3. 数据更新接口', createdAt: '2024-01-06' },
    { id: 'note2', title: '数据库设计', notebookId: 'n1', content: '数据库表结构设计方案\n\n用户表：\n- id: 主键\n- username: 用户名\n- email: 邮箱\n- created_at: 创建时间', createdAt: '2024-01-07' },
    { id: 'note3', title: '周会纪要', notebookId: 'n2', content: '本周工作总结：\n1. 完成了用户模块开发\n2. 修复了3个bug\n3. 下周计划开始支付模块', createdAt: '2024-01-08' },
    { id: 'note4', title: 'Vue3基础', notebookId: 'n3', content: 'Vue3响应式原理学习笔记\n\nProxy vs Object.defineProperty\n- Proxy可以监听数组变化\n- 性能更好', createdAt: '2024-01-09' },
    { id: 'note5', title: 'Hooks详解', notebookId: 'n4', content: 'React Hooks使用指南\n\nuseState: 状态管理\nuseEffect: 副作用处理\nuseContext: 跨组件通信', createdAt: '2024-01-10' }
  ]);

  // 展开状态
  const [expandedSpaces, setExpandedSpaces] = useState(new Set(['1', '2']));
  const [expandedNotebooks, setExpandedNotebooks] = useState(new Set(['n1', 'n3']));

  // 选中状态
  const [selectedNote, setSelectedNote] = useState(null);

  // 对话框状态
  const [showCreateSpaceDialog, setShowCreateSpaceDialog] = useState(false);
  const [showCreateNotebookDialog, setShowCreateNotebookDialog] = useState(false);
  const [showCreateNoteDialog, setShowCreateNoteDialog] = useState(false);
  const [showMoveNotebookDialog, setShowMoveNotebookDialog] = useState(false);
  const [showMoveNoteDialog, setShowMoveNoteDialog] = useState(false);

  // 编辑状态
  const [editingItem, setEditingItem] = useState(null);
  const [editingType, setEditingType] = useState(null);

  // 表单数据
  const [formData, setFormData] = useState({
    name: '',
    title: '',
    content: '',
    targetSpaceId: '',
    targetNotebookId: '',
    noteType: 'editor'
  });
  const [uploadedFile, setUploadedFile] = useState(null);
  const [currentSpaceId, setCurrentSpaceId] = useState(null);
  const [currentNotebookId, setCurrentNotebookId] = useState(null);

  // 树形结构操作
  const toggleSpace = (spaceId) => {
    const newExpanded = new Set(expandedSpaces);
    if (newExpanded.has(spaceId)) {
      newExpanded.delete(spaceId);
    } else {
      newExpanded.add(spaceId);
    }
    setExpandedSpaces(newExpanded);
  };

  const toggleNotebook = (notebookId) => {
    const newExpanded = new Set(expandedNotebooks);
    if (newExpanded.has(notebookId)) {
      newExpanded.delete(notebookId);
    } else {
      newExpanded.add(notebookId);
    }
    setExpandedNotebooks(newExpanded);
  };

  const getNotebooksInSpace = (spaceId) => {
    return notebooks.filter(nb => nb.spaceId === spaceId);
  };

  const getNotesInNotebook = (notebookId) => {
    return notes.filter(n => n.notebookId === notebookId);
  };

  // 空间操作
  const handleCreateSpace = () => {
    setEditingItem(null);
    setEditingType('space');
    setFormData({ ...formData, name: '' });
    setShowCreateSpaceDialog(true);
  };

  const handleEditSpace = (space, e) => {
    e.stopPropagation();
    setEditingItem(space);
    setEditingType('space');
    setFormData({ ...formData, name: space.name });
    setShowCreateSpaceDialog(true);
  };

  const handleDeleteSpace = (spaceId, e) => {
    e.stopPropagation();
    if (window.confirm('确定要删除此空间吗？将同时删除其下所有笔记本和笔记。')) {
      setSpaces(spaces.filter(s => s.id !== spaceId));
      setNotebooks(notebooks.filter(nb => nb.spaceId !== spaceId));
      console.log('删除空间:', spaceId);
    }
  };

  const handleSaveSpace = () => {
    if (!formData.name.trim()) {
      alert('请输入空间名称');
      return;
    }

    if (editingItem) {
      setSpaces(spaces.map(s => s.id === editingItem.id ? { ...s, name: formData.name } : s));
      console.log('更新空间:', editingItem.id, formData.name);
    } else {
      const newSpace = {
        id: 'space_' + Date.now(),
        name: formData.name,
        createdAt: new Date().toISOString()
      };
      setSpaces([...spaces, newSpace]);
      console.log('创建空间:', formData.name);
    }

    setShowCreateSpaceDialog(false);
    setFormData({ ...formData, name: '' });
  };

  // 笔记本操作
  const handleCreateNotebook = (space, e) => {
    e.stopPropagation();
    setCurrentSpaceId(space.id);
    setEditingItem(null);
    setEditingType('notebook');
    setFormData({ ...formData, name: '' });
    setShowCreateNotebookDialog(true);
  };

  const handleEditNotebook = (notebook, e) => {
    e.stopPropagation();
    setEditingItem(notebook);
    setEditingType('notebook');
    setFormData({ ...formData, name: notebook.name });
    setShowCreateNotebookDialog(true);
  };

  const handleDeleteNotebook = (notebookId, e) => {
    e.stopPropagation();
    if (window.confirm('确定要删除此笔记本吗？将同时删除其下所有笔记。')) {
      setNotebooks(notebooks.filter(nb => nb.id !== notebookId));
      setNotes(notes.filter(n => n.notebookId !== notebookId));
      console.log('删除笔记本:', notebookId);
    }
  };

  const handleMoveNotebook = (notebook, e) => {
    e.stopPropagation();
    setEditingItem(notebook);
    setFormData({ ...formData, targetSpaceId: '' });
    setShowMoveNotebookDialog(true);
  };

  const handleSaveNotebook = () => {
    if (!formData.name.trim()) {
      alert('请输入笔记本名称');
      return;
    }

    if (editingItem) {
      setNotebooks(notebooks.map(nb => nb.id === editingItem.id ? { ...nb, name: formData.name } : nb));
      console.log('更新笔记本:', editingItem.id, formData.name);
    } else {
      const newNotebook = {
        id: 'notebook_' + Date.now(),
        name: formData.name,
        spaceId: currentSpaceId,
        createdAt: new Date().toISOString()
      };
      setNotebooks([...notebooks, newNotebook]);
      console.log('创建笔记本:', formData.name, '在空间:', currentSpaceId);
    }

    setShowCreateNotebookDialog(false);
    setFormData({ ...formData, name: '' });
  };

  const handleConfirmMoveNotebook = () => {
    if (!formData.targetSpaceId) {
      alert('请选择目标空间');
      return;
    }

    setNotebooks(notebooks.map(nb => 
      nb.id === editingItem.id ? { ...nb, spaceId: formData.targetSpaceId } : nb
    ));
    console.log('移动笔记本:', editingItem.id, '到空间:', formData.targetSpaceId);

    setShowMoveNotebookDialog(false);
    setFormData({ ...formData, targetSpaceId: '' });
  };

  // 笔记操作
  const handleCreateNote = (notebook, e) => {
    e.stopPropagation();
    setCurrentNotebookId(notebook.id);
    setEditingItem(null);
    setEditingType('note');
    setFormData({ ...formData, title: '', content: '', noteType: 'editor' });
    setUploadedFile(null);
    setShowCreateNoteDialog(true);
  };

  const handleEditNote = (note, e) => {
    e.stopPropagation();
    setEditingItem(note);
    setEditingType('note');
    setFormData({ ...formData, title: note.title, content: note.content || '', noteType: 'editor' });
    setShowCreateNoteDialog(true);
  };

  const handleDeleteNote = (noteId, e) => {
    e.stopPropagation();
    if (window.confirm('确定要删除此笔记吗？')) {
      setNotes(notes.filter(n => n.id !== noteId));
      if (selectedNote?.id === noteId) {
        setSelectedNote(null);
      }
      console.log('删除笔记:', noteId);
    }
  };

  const handleMoveNote = (note, e) => {
    e.stopPropagation();
    setEditingItem(note);
    setFormData({ ...formData, targetNotebookId: '' });
    setShowMoveNoteDialog(true);
  };

  const handleSaveNote = () => {
    if (!formData.title.trim()) {
      alert('请输入笔记标题');
      return;
    }

    if (editingItem) {
      setNotes(notes.map(n => 
        n.id === editingItem.id 
          ? { ...n, title: formData.title, content: formData.content, updatedAt: new Date().toISOString() }
          : n
      ));
      if (selectedNote?.id === editingItem.id) {
        setSelectedNote({ ...selectedNote, title: formData.title, content: formData.content });
      }
      console.log('更新笔记:', editingItem.id);
    } else {
      if (formData.noteType === 'editor') {
        const newNote = {
          id: 'note_' + Date.now(),
          title: formData.title,
          content: formData.content,
          notebookId: currentNotebookId,
          createdAt: new Date().toISOString()
        };
        setNotes([...notes, newNote]);
        console.log('创建笔记(编辑):', formData.title);
      } else {
        console.log('创建笔记(上传):', formData.title, uploadedFile);
        const newNote = {
          id: 'note_' + Date.now(),
          title: formData.title,
          content: `已上传文件: ${uploadedFile?.name}`,
          notebookId: currentNotebookId,
          createdAt: new Date().toISOString()
        };
        setNotes([...notes, newNote]);
      }
    }

    setShowCreateNoteDialog(false);
    setFormData({ ...formData, title: '', content: '' });
    setUploadedFile(null);
  };

  const handleConfirmMoveNote = () => {
    if (!formData.targetNotebookId) {
      alert('请选择目标笔记本');
      return;
    }

    setNotes(notes.map(n => 
      n.id === editingItem.id ? { ...n, notebookId: formData.targetNotebookId } : n
    ));
    console.log('移动笔记:', editingItem.id, '到笔记本:', formData.targetNotebookId);

    setShowMoveNoteDialog(false);
    setFormData({ ...formData, targetNotebookId: '' });
  };

  const selectNote = (note) => {
    setSelectedNote(note);
  };

  const handleFileUpload = (e) => {
    setUploadedFile(e.target.files[0]);
  };

  const getAllNotebooksWithSpaceName = () => {
    return notebooks.map(nb => {
      const space = spaces.find(s => s.id === nb.spaceId);
      return {
        ...nb,
        spaceName: space?.name || '未知空间'
      };
    });
  };

  return (
    <div className="workspace-container">
      <div className="workspace-layout">
        <div className="sidebar">
          <div className="sidebar-header">
            <h2>我的笔记</h2>
            <button className="add-btn" onClick={handleCreateSpace}>+ 新建空间</button>
          </div>
          
          <div className="tree-container">
            {spaces.map(space => (
              <div key={space.id} className="tree-node">
                <div className="tree-item space-item">
                  <button className="expand-btn" onClick={() => toggleSpace(space.id)}>
                    {expandedSpaces.has(space.id) ? '▼' : '▶'}
                  </button>
                  <span className="item-icon">📁</span>
                  <span className="item-name">{space.name}</span>
                  <div className="item-actions">
                    <button onClick={(e) => handleCreateNotebook(space, e)} title="新建笔记本">➕</button>
                    <button onClick={(e) => handleEditSpace(space, e)} title="重命名">✏️</button>
                    <button onClick={(e) => handleDeleteSpace(space.id, e)} title="删除">🗑️</button>
                  </div>
                </div>

                {expandedSpaces.has(space.id) && (
                  <div className="tree-children">
                    {getNotebooksInSpace(space.id).map(notebook => (
                      <div key={notebook.id} className="tree-node">
                        <div className="tree-item notebook-item">
                          <button className="expand-btn" onClick={() => toggleNotebook(notebook.id)}>
                            {expandedNotebooks.has(notebook.id) ? '▼' : '▶'}
                          </button>
                          <span className="item-icon">📒</span>
                          <span className="item-name">{notebook.name}</span>
                          <div className="item-actions">
                            <button onClick={(e) => handleCreateNote(notebook, e)} title="新建笔记">➕</button>
                            <button onClick={(e) => handleMoveNotebook(notebook, e)} title="移动">📤</button>
                            <button onClick={(e) => handleEditNotebook(notebook, e)} title="重命名">✏️</button>
                            <button onClick={(e) => handleDeleteNotebook(notebook.id, e)} title="删除">🗑️</button>
                          </div>
                        </div>

                        {expandedNotebooks.has(notebook.id) && (
                          <div className="tree-children">
                            {getNotesInNotebook(notebook.id).map(note => (
                              <div key={note.id} className="tree-node">
                                <div 
                                  className={`tree-item note-item ${selectedNote?.id === note.id ? 'active' : ''}`}
                                  onClick={() => selectNote(note)}
                                >
                                  <span className="expand-placeholder"></span>
                                  <span className="item-icon">📄</span>
                                  <span className="item-name">{note.title}</span>
                                  <div className="item-actions">
                                    <button onClick={(e) => handleMoveNote(note, e)} title="移动">📤</button>
                                    <button onClick={(e) => handleEditNote(note, e)} title="编辑">✏️</button>
                                    <button onClick={(e) => handleDeleteNote(note.id, e)} title="删除">🗑️</button>
                                  </div>
                                </div>
                              </div>
                            ))}
                          </div>
                        )}
                      </div>
                    ))}
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>

        <div className="content-area">
          {!selectedNote ? (
            <div className="empty-state">
              <div className="empty-icon">📝</div>
              <p>选择一个笔记开始查看</p>
            </div>
          ) : (
            <div className="note-viewer">
              <div className="note-header">
                <h2>{selectedNote.title}</h2>
                <button className="edit-btn" onClick={(e) => handleEditNote(selectedNote, e)}>编辑笔记</button>
              </div>
              <div className="note-content">
                {selectedNote.content || '暂无内容'}
              </div>
            </div>
          )}
        </div>
      </div>

      {showCreateSpaceDialog && (
        <div className="modal" onClick={() => setShowCreateSpaceDialog(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h3>{editingItem ? '重命名空间' : '创建新空间'}</h3>
            <input 
              type="text" 
              placeholder="请输入空间名称" 
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              onKeyPress={(e) => e.key === 'Enter' && handleSaveSpace()}
              autoFocus
            />
            <div className="modal-actions">
              <button onClick={() => setShowCreateSpaceDialog(false)}>取消</button>
              <button className="primary" onClick={handleSaveSpace}>确定</button>
            </div>
          </div>
        </div>
      )}

      {showCreateNotebookDialog && (
        <div className="modal" onClick={() => setShowCreateNotebookDialog(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h3>{editingItem ? '重命名笔记本' : '创建新笔记本'}</h3>
            <input 
              type="text" 
              placeholder="请输入笔记本名称" 
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              onKeyPress={(e) => e.key === 'Enter' && handleSaveNotebook()}
              autoFocus
            />
            <div className="modal-actions">
              <button onClick={() => setShowCreateNotebookDialog(false)}>取消</button>
              <button className="primary" onClick={handleSaveNotebook}>确定</button>
            </div>
          </div>
        </div>
      )}

      {showMoveNotebookDialog && (
        <div className="modal" onClick={() => setShowMoveNotebookDialog(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h3>移动笔记本到其他空间</h3>
            <select 
              value={formData.targetSpaceId}
              onChange={(e) => setFormData({ ...formData, targetSpaceId: e.target.value })}
            >
              <option value="">请选择目标空间</option>
              {spaces.filter(s => s.id !== editingItem?.spaceId).map(space => (
                <option key={space.id} value={space.id}>{space.name}</option>
              ))}
            </select>
            <div className="modal-actions">
              <button onClick={() => setShowMoveNotebookDialog(false)}>取消</button>
              <button className="primary" onClick={handleConfirmMoveNotebook}>确定</button>
            </div>
          </div>
        </div>
      )}

      {showMoveNoteDialog && (
        <div className="modal" onClick={() => setShowMoveNoteDialog(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h3>移动笔记到其他笔记本</h3>
            <select 
              value={formData.targetNotebookId}
              onChange={(e) => setFormData({ ...formData, targetNotebookId: e.target.value })}
            >
              <option value="">请选择目标笔记本</option>
              {getAllNotebooksWithSpaceName().filter(nb => nb.id !== editingItem?.notebookId).map(notebook => (
                <option key={notebook.id} value={notebook.id}>
                  {notebook.name} ({notebook.spaceName})
                </option>
              ))}
            </select>
            <div className="modal-actions">
              <button onClick={() => setShowMoveNoteDialog(false)}>取消</button>
              <button className="primary" onClick={handleConfirmMoveNote}>确定</button>
            </div>
          </div>
        </div>
      )}

      {showCreateNoteDialog && (
        <div className="modal large" onClick={() => setShowCreateNoteDialog(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h3>{editingItem ? '编辑笔记' : '创建新笔记'}</h3>
            <input 
              type="text" 
              placeholder="请输入笔记标题" 
              className="note-title-input"
              value={formData.title}
              onChange={(e) => setFormData({ ...formData, title: e.target.value })}
              autoFocus
            />
            
            {!editingItem && (
              <div className="note-type-selector">
                <label>
                  <input 
                    type="radio" 
                    value="editor" 
                    checked={formData.noteType === 'editor'}
                    onChange={(e) => setFormData({ ...formData, noteType: e.target.value })}
                  />
                  在线编辑
                </label>
                <label>
                  <input 
                    type="radio" 
                    value="upload" 
                    checked={formData.noteType === 'upload'}
                    onChange={(e) => setFormData({ ...formData, noteType: e.target.value })}
                  />
                  上传文件
                </label>
              </div>
            )}

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

            {formData.noteType === 'upload' && !editingItem && (
              <div className="upload-container">
                <input 
                  type="file" 
                  onChange={handleFileUpload} 
                  accept=".txt,.md,.pdf,.doc,.docx" 
                />
                {uploadedFile && <p className="uploaded-file">已选择: {uploadedFile.name}</p>}
              </div>
            )}

            <div className="modal-actions">
              <button onClick={() => setShowCreateNoteDialog(false)}>取消</button>
              <button className="primary" onClick={handleSaveNote}>确定</button>
            </div>
          </div>
        </div>
      )}

      <style jsx>{`
        .workspace-container {
          height: 100vh;
          background: #f5f5f5;
          font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
        }

        .workspace-layout {
          display: grid;
          grid-template-columns: 350px 1fr;
          height: 100%;
        }

        .sidebar {
          background: white;
          border-right: 1px solid #e0e0e0;
          display: flex;
          flex-direction: column;
          overflow: hidden;
        }

        .sidebar-header {
          background: linear-gradient(135deg, #00bcd4 0%, #0097a7 100%);
          color: white;
          padding: 20px;
          display: flex;
          justify-content: space-between;
          align-items: center;
          box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }

        .sidebar-header h2 {
          margin: 0;
          font-size: 20px;
          font-weight: 600;
        }

        .add-btn {
          background: rgba(255, 255, 255, 0.2);
          color: white;
          border: none;
          padding: 8px 16px;
          border-radius: 6px;
          cursor: pointer;
          font-size: 14px;
          transition: all 0.3s;
          font-weight: 500;
        }

        .add-btn:hover {
          background: rgba(255, 255, 255, 0.3);
          transform: translateY(-1px);
        }

        .tree-container {
          flex: 1;
          overflow-y: auto;
          padding: 10px;
        }

        .tree-node {
          user-select: none;
        }

        .tree-item {
          display: flex;
          align-items: center;
          padding: 10px 8px;
          margin: 2px 0;
          border-radius: 6px;
          cursor: pointer;
          transition: all 0.2s;
          position: relative;
        }

        .tree-item:hover {
          background: #f5f5f5;
        }

        .tree-item.active {
          background: #e0f7fa;
          border-left: 3px solid #00bcd4;
        }

        .expand-btn {
          background: none;
          border: none;
          cursor: pointer;
          font-size: 12px;
          width: 20px;
          height: 20px;
          display: flex;
          align-items: center;
          justify-content: center;
          color: #666;
          transition: transform 0.2s;
          flex-shrink: 0;
        }

        .expand-btn:hover {
          color: #00bcd4;
        }

        .expand-placeholder {
          width: 20px;
          flex-shrink: 0;
        }

        .item-icon {
          font-size: 18px;
          margin: 0 8px;
          flex-shrink: 0;
        }

        .item-name {
          flex: 1;
          font-size: 14px;
          color: #333;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .item-actions {
          display: none;
          gap: 4px;
          margin-left: 8px;
        }

        .tree-item:hover .item-actions {
          display: flex;
        }

        .item-actions button {
          background: rgba(0, 0, 0, 0.05);
          border: none;
          cursor: pointer;
          font-size: 14px;
          width: 24px;
          height: 24px;
          border-radius: 4px;
          display: flex;
          align-items: center;
          justify-content: center;
          transition: all 0.2s;
        }

        .item-actions button:hover {
          background: rgba(0, 188, 212, 0.1);
          transform: scale(1.1);
        }

        .tree-children {
          margin-left: 20px;
        }

        .space-item {
          font-weight: 600;
        }

        .notebook-item {
          font-weight: 500;
        }

        .note-item {
          font-weight: normal;
          font-size: 13px;
        }

        .content-area {
          background: white;
          overflow-y: auto;
        }

        .empty-state {
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          height: 100%;
          color: #999;
        }

        .empty-icon {
          font-size: 80px;
          margin-bottom: 20px;
          opacity: 0.3;
        }

        .empty-state p {
          font-size: 16px;
        }

        .note-viewer {
          padding: 40px;
          max-width: 900px;
          margin: 0 auto;
        }

        .note-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 30px;
          padding-bottom: 20px;
          border-bottom: 2px solid #e0e0e0;
        }

        .note-header h2 {
          margin: 0;
          color: #333;
          font-size: 28px;
        }

        .edit-btn {
          background: #00bcd4;
          color: white;
          border: none;
          padding: 10px 24px;
          border-radius: 6px;
          cursor: pointer;
          font-size: 14px;
          font-weight: 500;
          transition: all 0.3s;
        }

        .edit-btn:hover {
          background: #00acc1;
          transform: translateY(-2px);
          box-shadow: 0 4px 8px rgba(0,188,212,0.3);
        }

        .note-content {
          font-size: 15px;
          line-height: 1.8;
          color: #555;
          white-space: pre-wrap;
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
          box-shadow: 0 10px 40px rgba(0,0,0,0.2);
        }

        .modal.large .modal-content {
          min-width: 600px;
        }

        .modal-content h3 {
          margin: 0 0 20px 0;
          color: #333;
          font-size: 20px;
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
          box-sizing: border-box;
        }

        .modal-content input[type="text"]:focus,
        .modal-content select:focus,
        .modal-content textarea:focus {
          outline: none;
          border-color: #00bcd4;
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
          font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
          resize: vertical;
          min-height: 200px;
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
          font-size: 14px;
          background: white;
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

        .tree-container::-webkit-scrollbar {
          width: 8px;
        }

        .tree-container::-webkit-scrollbar-track {
          background: #f1f1f1;
        }

        .tree-container::-webkit-scrollbar-thumb {
          background: #888;
          border-radius: 4px;
        }

        .tree-container::-webkit-scrollbar-thumb:hover {
          background: #555;
        }
      `}</style>
    </div>
  );
}