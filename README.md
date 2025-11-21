## USER

### CommentView

* GET用户评论
  * 用户收到的评论
  * 用户发出的评论
* SENT评论
* DELETE评论

```
/**
 * API 占位信息（仅限展示）
 * - GET /api/comments/received
 *   输入: 无
 *   输出: { code: number, data: Comment[] }
 *   返回码: 200 成功 / 401 未登录
 * - GET /api/comments/sent
 *   输入: 无
 *   输出: { code: number, data: Comment[] }
 *   返回码: 200 成功 / 401 未登录
 * - DELETE /api/comments/{id}
 *   输入: 无
 *   输出: { code: number, message: string }
 *   返回码: 200 删除成功 / 404 评论不存在
 */
```

### FavoritesView

* GET收藏夹信息
* PATCH更新收藏夹信息
* DELETE移出收藏夹

```
/**
 * API 占位信息（仅前端展示）
 * - GET /api/favorites
 *   输入: 无
 *   输出: { code: number, data: Favorite[] }
 *   返回码: 200 成功 / 401 未登录 / 500 服务器异常
 * - PATCH /api/favorites/{id}
 *   输入: { folderId: string }
 *   输出: { code: number, message: string }
 *   返回码: 200 更新成功 / 400 参数错误 / 404 收藏不存在
 * - DELETE /api/favorites/{id}
 *   输入: 无
 *   输出: { code: number, message: string }
 *   返回码: 200 删除成功 / 404 收藏不存在
 */
```
### ProfileView.vue

* GET获取用户信息
* POST退出登录
* POST修改密码

```
/**
 * API: 获取用户信息
 * GET /api/user/profile
 * 输出: {
 *   code: number,
 *   data: {
 *     username: string,
 *     studentId: string,
 *     email: string,
 *     createdAt: string
 *   }
 * }
 */
```

### SearchView.vue

* POST搜索笔记

```
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
```

# WorkspaceView

## 1. 空间管理 API

### 获取所有空间
- **API**: `GET /api/spaces`
- **描述**: 获取所有的空间数据。

### 创建/更新空间
- **API**: `POST /api/spaces` 或 `PUT /api/spaces/:id`
- **描述**: 创建或更新一个空间。

### 删除空间
- **API**: `DELETE /api/spaces/:id`
- **描述**: 删除指定 ID 的空间。

## 2. 笔记本管理 API

### 获取某个空间下的笔记本
- **API**: `GET /api/spaces/:spaceId/notebooks`
- **描述**: 获取指定空间下的笔记本列表。

### 创建/更新笔记本
- **API**: `POST /api/notebooks` 或 `PUT /api/notebooks/:id`
- **描述**: 创建或更新笔记本。

### 删除笔记本
- **API**: `DELETE /api/notebooks/:id`
- **描述**: 删除指定 ID 的笔记本。

### 移动笔记本
- **API**: `PUT /api/notebooks/:id/move`
- **描述**: 将笔记本从一个空间移动到另一个空间。

## 3. 笔记管理 API

### 获取某个笔记本下的笔记
- **API**: `GET /api/notebooks/:notebookId/notes`
- **描述**: 获取指定笔记本下的笔记列表。

### 获取笔记详细内容
- **API**: `GET /api/notes/:noteId`
- **描述**: 获取指定笔记的详细内容。

### 创建/更新笔记
- **API**: `POST /api/notes` 或 `PUT /api/notes/:id`
- **描述**: 创建或更新笔记。

### 上传笔记
- **API**: `POST /api/notes/upload`
- **描述**: 上传笔记文件。

### 删除笔记
- **API**: `DELETE /api/notes/:id`
- **描述**: 删除指定 ID 的笔记。

### 移动笔记
- **API**: `PUT /api/notes/:id/move`
- **描述**: 将笔记从一个笔记本移动到另一个笔记本。

## 4. 收藏功能 API

### 收藏笔记
- **API**: `POST /api/notes/{noteId}/favorite`
- **输入**: `{ folderId: string }` （目标文件夹 ID）
- **输出**: `{ code: number, message: string }`
- **描述**: 将笔记加入收藏。

### 取消收藏笔记
- **API**: `DELETE /api/notes/{noteId}/favorite`
- **描述**: 取消收藏笔记。

## 5. 评论功能 API

### 获取笔记的评论
- **API**: `GET /api/notes/{noteId}/comments`
- **描述**: 获取指定笔记的所有评论。

### 创建评论
- **API**: `POST /api/notes/{noteId}/comments`
- **输入**: `{ content: string }` （评论内容）
- **输出**: `{ code: number, data: { commentId: string } }`
- **描述**: 为指定笔记创建评论。

## 6. 搜索功能 API

### 搜索笔记
- **API**: `GET /api/search/notes?query={searchQuery}`
- **描述**: 根据搜索查询获取相关的笔记列表。

## 7. 文件上传 API

### 上传文件
- **API**: `POST /api/notes/upload`
- **描述**: 上传文件类型的笔记。

