<template>
  <div class="qa-page">
    <section class="qa-hero">
      <div>
        <p class="section-label">问答社区</p>
        <h2>像知乎一样提问、回答、追踪讨论</h2>
        <p class="hero-desc">
          直接对接后端 /qa 接口：支持提问、回答、评论、回复、点赞与收藏，便于快速验证链路。
        </p>
        <div class="hero-actions">
          <button class="primary-btn" type="button" @click="openAskDialog">发起提问</button>
          <div class="fetch-line">
            <input
              v-model="fetchId"
              type="text"
              placeholder="输入问题 ID，回车拉取详情"
              @keyup.enter="handleFetchById"
            />
            <button class="ghost-btn" type="button" @click="handleFetchById">获取</button>
          </div>
        </div>
      </div>
      <div class="hero-stats">
        <div class="stat-card">
          <p>已加载问题</p>
          <strong>{{ questionList.length }}</strong>
        </div>
        <div class="stat-card">
          <p>当前查看</p>
          <strong>{{ activeQuestionId || '未选' }}</strong>
        </div>
        <div class="stat-card">
          <p>登录用户</p>
          <strong>{{ userStore.userInfo.id ?? '未登录' }}</strong>
        </div>
      </div>
    </section>

    <div class="qa-layout">
      <section class="question-list">
        <header class="section-header">
          <div>
            <p class="section-label">问题流</p>
            <h3>最新/手动载入的问题</h3>
          </div>
          <span class="muted">点击卡片可在右侧查看详情</span>
        </header>

        <div v-if="!questionList.length" class="empty">
          <p>暂无问题数据。</p>
          <p>可点击“发起提问”创建，或输入已有问题 ID 拉取。</p>
        </div>

        <article
          v-for="question in questionList"
          :key="question.questionId"
          class="question-card"
          :class="{ active: question.questionId === activeQuestionId }"
        >
          <div class="card-title" @click="setActive(question.questionId)">
            {{ question.title }}
          </div>
          <p class="card-excerpt">{{ question.content }}</p>
          <div class="tag-list">
            <span v-for="tag in question.tags" :key="tag" class="tag-chip"># {{ tag }}</span>
            <span v-if="!question.tags || !question.tags.length" class="tag-chip muted">未设置标签</span>
          </div>
          <div class="card-meta">
            <span>{{ formatTime(question.createdAt) }}</span>
            <span>{{ question.answers?.length || 0 }} 个回答</span>
            <div class="meta-actions">
              <button type="button" class="text-btn" @click="handleLikeQuestion(question)">
                赞同 {{ question.likeCount ?? 0 }}
              </button>
              <button type="button" class="text-btn" @click="handleFavoriteQuestion(question)">
                收藏 {{ question.favoriteCount ?? 0 }}
              </button>
              <button type="button" class="text-btn" @click="setActive(question.questionId)">查看</button>
              <button
                type="button"
                class="text-btn danger"
                @click="handleDeleteQuestion(question)"
              >
                删除
              </button>
            </div>
          </div>
        </article>
      </section>

      <section class="question-detail">
        <header class="section-header">
          <div>
            <p class="section-label">问题详情</p>
            <h3>{{ activeQuestion?.title || '请选择左侧问题或新建一个' }}</h3>
          </div>
          <span class="muted">
            {{ detailLoading ? '加载中...' : (activeQuestionId ? `ID: ${activeQuestionId}` : '') }}
          </span>
        </header>

        <div v-if="activeQuestion">
          <div class="detail-head">
            <p class="detail-content">{{ activeQuestion.content }}</p>
            <div class="tag-list">
              <span v-for="tag in activeQuestion.tags" :key="tag" class="tag-chip"># {{ tag }}</span>
              <span v-if="!activeQuestion.tags?.length" class="tag-chip muted">无标签</span>
            </div>
            <div class="detail-meta">
              <span>作者 ID：{{ activeQuestion.authorId }}</span>
              <span>创建时间：{{ formatTime(activeQuestion.createdAt) }}</span>
              <div class="meta-actions">
                <button class="text-btn" type="button" @click="handleLikeQuestion(activeQuestion)">
                  赞同 {{ activeQuestion.likeCount ?? 0 }}
                </button>
                <button class="text-btn" type="button" @click="handleFavoriteQuestion(activeQuestion)">
                  收藏 {{ activeQuestion.favoriteCount ?? 0 }}
                </button>
                <button class="text-btn danger" type="button" @click="handleDeleteQuestion(activeQuestion)">
                  删除
                </button>
              </div>
            </div>
          </div>

          <div class="answer-editor">
            <textarea
              v-model="answerInput"
              rows="3"
              placeholder="写下你的回答..."
            ></textarea>
            <div class="editor-actions">
              <div class="muted">当前用户：{{ userStore.userInfo.id ?? '未登录' }}</div>
              <button class="primary-btn" type="button" @click="handleCreateAnswer">发布回答</button>
            </div>
          </div>

          <div class="answer-list" v-if="activeQuestion.answers?.length">
            <div v-for="answer in activeQuestion.answers" :key="answer.answerId" class="answer-card">
              <div class="answer-meta">
                <div class="meta-left">
                  <strong>回答者 #{{ answer.authorId }}</strong>
                  <span>{{ formatTime(answer.createdAt) }}</span>
                </div>
                <div class="meta-actions">
                  <button class="text-btn" type="button" @click="handleLikeAnswer(answer)">
                    赞同 {{ answer.likeCount ?? 0 }}
                  </button>
                  <button class="text-btn danger" type="button" @click="handleDeleteAnswer(answer)">
                    删除
                  </button>
                </div>
              </div>
              <p class="answer-content">{{ answer.content }}</p>

              <div class="comment-editor">
                <input
                  v-model="commentDrafts[answer.answerId]"
                  type="text"
                  placeholder="写评论..."
                />
                <button class="ghost-btn" type="button" @click="handleCreateComment(answer.answerId)">
                  评论
                </button>
              </div>

              <div class="comment-list" v-if="answer.comments?.length">
                <div v-for="comment in answer.comments" :key="comment.commentId" class="comment-row">
                  <div class="comment-meta">
                    <div class="meta-left">
                      <strong>评论者 #{{ comment.authorId }}</strong>
                      <span>{{ formatTime(comment.createdAt) }}</span>
                    </div>
                    <div class="meta-actions">
                      <button class="text-btn" type="button" @click="handleLikeComment(answer, comment)">
                        赞 {{ comment.likeCount ?? 0 }}
                      </button>
                      <button
                        class="text-btn danger"
                        type="button"
                        @click="handleDeleteComment(answer, comment)"
                      >
                        删
                      </button>
                    </div>
                  </div>
                  <p class="comment-content">{{ comment.content }}</p>

                  <div class="reply-editor">
                    <input
                      v-model="replyDrafts[`${answer.answerId}-${comment.commentId}`]"
                      type="text"
                      placeholder="写回复..."
                    />
                    <button
                      class="ghost-btn"
                      type="button"
                      @click="handleCreateReply(answer.answerId, comment.commentId)"
                    >
                      回复
                    </button>
                  </div>

                  <div class="reply-list" v-if="comment.replies?.length">
                    <div v-for="reply in comment.replies" :key="reply.replyId" class="reply-chip">
                      <div class="meta-left">
                        <span>#{{ reply.authorId }}</span>
                        <span>{{ formatTime(reply.createdAt) }}</span>
                      </div>
                      <p>{{ reply.content }}</p>
                      <div class="meta-actions">
                        <button class="text-btn" type="button" @click="handleLikeReply(answer, comment, reply)">
                          赞 {{ reply.likeCount ?? 0 }}
                        </button>
                        <button
                          class="text-btn danger"
                          type="button"
                          @click="handleDeleteReply(answer, comment, reply)"
                        >
                          删
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              <div v-else class="muted small">暂无评论</div>
            </div>
          </div>
          <div v-else class="empty">还没有回答，快来写下第一条回答吧。</div>
        </div>
        <div v-else class="empty">
          请选择左侧问题或点击“发起提问”。
        </div>
      </section>
    </div>

    <div v-if="askVisible" class="dialog-mask">
      <div class="dialog">
        <header class="dialog-header">
          <h4>发起提问</h4>
        </header>
        <div class="dialog-body">
          <input v-model="askForm.title" type="text" placeholder="一句话描述你的问题" />
          <textarea
            v-model="askForm.content"
            rows="4"
            placeholder="补充细节，描述场景、遇到的问题等"
          ></textarea>
          <input v-model="askForm.tagsText" type="text" placeholder="标签，使用逗号分隔，例如：Java, Vue3" />
        </div>
        <footer class="dialog-footer">
          <button class="ghost-btn" type="button" @click="closeAskDialog">取消</button>
          <button class="primary-btn" type="button" @click="handleCreateQuestion">提交</button>
        </footer>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useUserStore } from '@/stores/user'
import {
  createAnswer,
  createComment,
  createQuestion,
  createReply,
  deleteAnswer,
  deleteComment,
  deleteQuestion,
  deleteReply,
  favoriteQuestion,
  getQuestionDetail,
  likeAnswer,
  likeComment,
  likeQuestion,
  likeReply
} from '@/api/qa'

const userStore = useUserStore()

const questionList = ref([])
const activeQuestionId = ref(null)
const detailLoading = ref(false)

const askVisible = ref(false)
const askForm = reactive({
  title: '',
  content: '',
  tagsText: ''
})

const fetchId = ref('')
const answerInput = ref('')
const commentDrafts = reactive({})
const replyDrafts = reactive({})

const activeQuestion = computed(() =>
  questionList.value.find(q => q.questionId === activeQuestionId.value) || null
)

const normalizeQuestion = (vo) => ({
  ...vo,
  answers: vo?.answers || [],
  tags: vo?.tags || []
})

const setActive = (id) => {
  activeQuestionId.value = id
}

const upsertQuestion = (vo) => {
  const data = normalizeQuestion(vo)
  const idx = questionList.value.findIndex(item => item.questionId === data.questionId)
  if (idx >= 0) {
    questionList.value[idx] = data
  } else {
    questionList.value.unshift(data)
  }
}

const formatTime = (val) => {
  if (!val) return '刚刚'
  try {
    return new Date(val).toLocaleString()
  } catch (e) {
    return String(val)
  }
}

const ensureLogin = () => {
  if (!userStore.userInfo.id) {
    window.alert('请先登录后再进行此操作')
    return false
  }
  return true
}

const fetchDetail = async (id, setActiveTab = true) => {
  if (!id) return
  detailLoading.value = true
  try {
    const data = await getQuestionDetail(id)
    if (data) {
      upsertQuestion(data)
      if (setActiveTab) {
        activeQuestionId.value = data.questionId
      }
    } else {
      window.alert('未找到对应问题')
    }
  } catch (err) {
    console.error('获取问题详情失败', err)
    window.alert('获取问题详情失败，请检查 ID 或后端服务')
  } finally {
    detailLoading.value = false
  }
}

const handleFetchById = async () => {
  const id = fetchId.value.trim()
  if (!id) return
  await fetchDetail(id, true)
}

const openAskDialog = () => {
  askVisible.value = true
}

const closeAskDialog = () => {
  askVisible.value = false
}

const handleCreateQuestion = async () => {
  if (!ensureLogin()) return
  const title = askForm.title.trim()
  const content = askForm.content.trim()
  if (!title || !content) {
    window.alert('标题和内容不能为空')
    return
  }
  const tags = askForm.tagsText
    .split(/[,，]/)
    .map(t => t.trim())
    .filter(Boolean)

  try {
    const data = await createQuestion({
      authorId: userStore.userInfo.id,
      title,
      content,
      tags
    })
    if (data) {
      upsertQuestion(data)
      activeQuestionId.value = data.questionId
      fetchId.value = data.questionId
      askForm.title = ''
      askForm.content = ''
      askForm.tagsText = ''
      closeAskDialog()
      await fetchDetail(data.questionId, true)
    }
  } catch (err) {
    console.error('创建问题失败', err)
    window.alert('创建问题失败，请检查后端接口或输入内容')
  }
}

const handleDeleteQuestion = async (question) => {
  if (!question?.questionId) return
  if (!window.confirm('确认删除该问题吗？')) return
  try {
    await deleteQuestion(question.questionId)
    questionList.value = questionList.value.filter(q => q.questionId !== question.questionId)
    if (activeQuestionId.value === question.questionId) {
      activeQuestionId.value = questionList.value[0]?.questionId || null
    }
  } catch (err) {
    console.error('删除问题失败', err)
    window.alert('删除问题失败')
  }
}

const handleLikeQuestion = async (question) => {
  if (!ensureLogin()) return
  if (!question?.questionId) return
  try {
    await likeQuestion(userStore.userInfo.id, question.questionId)
    await fetchDetail(question.questionId, question.questionId === activeQuestionId.value)
  } catch (err) {
    console.error('点赞问题失败', err)
    window.alert('点赞失败')
  }
}

const handleFavoriteQuestion = async (question) => {
  if (!ensureLogin()) return
  if (!question?.questionId) return
  try {
    await favoriteQuestion(userStore.userInfo.id, question.questionId)
    await fetchDetail(question.questionId, question.questionId === activeQuestionId.value)
  } catch (err) {
    console.error('收藏问题失败', err)
    window.alert('收藏失败')
  }
}

const handleCreateAnswer = async () => {
  if (!ensureLogin()) return
  if (!activeQuestionId.value) {
    window.alert('请先选择一个问题')
    return
  }
  const content = answerInput.value.trim()
  if (!content) {
    window.alert('回答内容不能为空')
    return
  }
  try {
    await createAnswer({
      questionId: activeQuestionId.value,
      authorId: userStore.userInfo.id,
      content
    })
    answerInput.value = ''
    await fetchDetail(activeQuestionId.value, true)
  } catch (err) {
    console.error('创建回答失败', err)
    window.alert('创建回答失败')
  }
}

const handleDeleteAnswer = async (answer) => {
  if (!activeQuestionId.value || !answer?.answerId) return
  if (!window.confirm('确认删除该回答吗？')) return
  try {
    await deleteAnswer(activeQuestionId.value, answer.answerId)
    await fetchDetail(activeQuestionId.value, true)
  } catch (err) {
    console.error('删除回答失败', err)
    window.alert('删除回答失败')
  }
}

const handleLikeAnswer = async (answer) => {
  if (!ensureLogin()) return
  if (!activeQuestionId.value || !answer?.answerId) return
  try {
    await likeAnswer(userStore.userInfo.id, activeQuestionId.value, answer.answerId)
    await fetchDetail(activeQuestionId.value, true)
  } catch (err) {
    console.error('点赞回答失败', err)
    window.alert('点赞失败')
  }
}

const handleCreateComment = async (answerId) => {
  if (!ensureLogin()) return
  if (!activeQuestionId.value || !answerId) return
  const content = (commentDrafts[answerId] || '').trim()
  if (!content) {
    window.alert('评论内容不能为空')
    return
  }
  try {
    await createComment({
      questionId: activeQuestionId.value,
      answerId,
      authorId: userStore.userInfo.id,
      content
    })
    commentDrafts[answerId] = ''
    await fetchDetail(activeQuestionId.value, true)
  } catch (err) {
    console.error('创建评论失败', err)
    window.alert('创建评论失败')
  }
}

const handleDeleteComment = async (answer, comment) => {
  if (!activeQuestionId.value || !answer?.answerId || !comment?.commentId) return
  if (!window.confirm('确认删除该评论吗？')) return
  try {
    await deleteComment(activeQuestionId.value, answer.answerId, comment.commentId)
    await fetchDetail(activeQuestionId.value, true)
  } catch (err) {
    console.error('删除评论失败', err)
    window.alert('删除评论失败')
  }
}

const handleLikeComment = async (answer, comment) => {
  if (!ensureLogin()) return
  if (!activeQuestionId.value || !answer?.answerId || !comment?.commentId) return
  try {
    await likeComment(userStore.userInfo.id, activeQuestionId.value, answer.answerId, comment.commentId)
    await fetchDetail(activeQuestionId.value, true)
  } catch (err) {
    console.error('点赞评论失败', err)
    window.alert('点赞失败')
  }
}

const handleCreateReply = async (answerId, commentId) => {
  if (!ensureLogin()) return
  if (!activeQuestionId.value || !answerId || !commentId) return
  const key = `${answerId}-${commentId}`
  const content = (replyDrafts[key] || '').trim()
  if (!content) {
    window.alert('回复内容不能为空')
    return
  }
  try {
    await createReply({
      questionId: activeQuestionId.value,
      answerId,
      commentId,
      authorId: userStore.userInfo.id,
      content
    })
    replyDrafts[key] = ''
    await fetchDetail(activeQuestionId.value, true)
  } catch (err) {
    console.error('创建回复失败', err)
    window.alert('创建回复失败')
  }
}

const handleDeleteReply = async (answer, comment, reply) => {
  if (!activeQuestionId.value || !answer?.answerId || !comment?.commentId || !reply?.replyId) return
  if (!window.confirm('确认删除该回复吗？')) return
  try {
    await deleteReply(activeQuestionId.value, answer.answerId, comment.commentId, reply.replyId)
    await fetchDetail(activeQuestionId.value, true)
  } catch (err) {
    console.error('删除回复失败', err)
    window.alert('删除回复失败')
  }
}

const handleLikeReply = async (answer, comment, reply) => {
  if (!ensureLogin()) return
  if (!activeQuestionId.value || !answer?.answerId || !comment?.commentId || !reply?.replyId) return
  try {
    await likeReply(
      userStore.userInfo.id,
      activeQuestionId.value,
      answer.answerId,
      comment.commentId,
      reply.replyId
    )
    await fetchDetail(activeQuestionId.value, true)
  } catch (err) {
    console.error('点赞回复失败', err)
    window.alert('点赞失败')
  }
}

defineExpose({ openAskDialog })
</script>

<style scoped>
.qa-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.qa-hero {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  padding: 20px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  background: #fff;
}

.hero-desc {
  color: #6b7280;
  margin: 8px 0 12px;
}

.hero-actions {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.hero-stats {
  display: grid;
  grid-template-columns: repeat(1, minmax(140px, 1fr));
  gap: 10px;
  align-items: start;
}

.stat-card {
  padding: 12px 14px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  background: #f9fafb;
}

.qa-layout {
  display: grid;
  grid-template-columns: 1.1fr 1.3fr;
  gap: 16px;
}

.question-list,
.question-detail {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 16px;
  min-height: 400px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.section-label {
  font-size: 12px;
  color: #9ca3af;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  margin: 0;
}

.muted {
  color: #9ca3af;
}

.small {
  font-size: 12px;
}

.question-card {
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 12px;
  margin-bottom: 10px;
  background: #fafafa;
}

.question-card.active {
  border-color: #2563eb;
  box-shadow: 0 0 0 2px rgba(37, 99, 235, 0.1);
}

.card-title {
  font-weight: 600;
  font-size: 16px;
  cursor: pointer;
}

.card-excerpt {
  margin: 6px 0 10px;
  color: #4b5563;
  line-height: 1.5;
}

.tag-list {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.tag-chip {
  background: #eef2ff;
  color: #4338ca;
  padding: 4px 8px;
  border-radius: 8px;
  font-size: 12px;
}

.tag-chip.muted {
  background: #f3f4f6;
  color: #9ca3af;
}

.card-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
  color: #6b7280;
}

.meta-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.detail-head {
  border-bottom: 1px solid #e5e7eb;
  padding-bottom: 12px;
  margin-bottom: 14px;
}

.detail-content {
  color: #374151;
  line-height: 1.6;
}

.detail-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 8px;
  align-items: center;
  color: #6b7280;
}

.answer-editor textarea {
  width: 100%;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 10px;
  resize: vertical;
}

.editor-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}

.answer-card {
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 12px;
  margin-top: 12px;
  background: #f9fafb;
}

.answer-meta,
.comment-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #6b7280;
}

.answer-content,
.comment-content {
  margin: 8px 0;
  color: #374151;
  line-height: 1.5;
}

.comment-editor,
.reply-editor {
  display: flex;
  gap: 8px;
  margin: 8px 0;
}

.comment-editor input,
.reply-editor input {
  flex: 1;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 8px 10px;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.comment-row {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 10px;
}

.reply-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 6px;
}

.reply-chip {
  border: 1px dashed #e5e7eb;
  border-radius: 8px;
  padding: 8px;
  background: #fdfefe;
}

.empty {
  padding: 24px 0;
  text-align: center;
  color: #9ca3af;
}

.primary-btn {
  background: #2563eb;
  color: #fff;
  border: none;
  padding: 8px 14px;
  border-radius: 8px;
  cursor: pointer;
}

.ghost-btn {
  border: 1px solid #e5e7eb;
  background: #fff;
  color: #374151;
  padding: 8px 12px;
  border-radius: 8px;
  cursor: pointer;
}

.text-btn {
  background: none;
  border: none;
  color: #2563eb;
  cursor: pointer;
  padding: 2px 6px;
}

.text-btn.danger {
  color: #dc2626;
}

.ghost-btn:hover,
.primary-btn:hover,
.text-btn:hover {
  opacity: 0.9;
}

.fetch-line {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #f3f4f6;
  padding: 6px 8px;
  border-radius: 10px;
}

.fetch-line input {
  border: none;
  background: transparent;
  outline: none;
  padding: 6px 8px;
  min-width: 220px;
}

.dialog-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 50;
}

.dialog {
  width: min(680px, 92vw);
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.dialog-body input,
.dialog-body textarea {
  width: 100%;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 10px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

@media (max-width: 1100px) {
  .qa-layout {
    grid-template-columns: 1fr;
  }
}
</style>

