<template>
  <div class="main-shell">
    <header class="main-header">
      <div class="brand-block">
        <span class="brand-icon" aria-hidden="true">✦</span>
        <div>
          <p class="brand-label">NOTE SHARING</p>
          <h1>在线笔记中台</h1>
        </div>
      </div>
      <nav class="nav-tabs" aria-label="页面导航">
        <button
          v-for="tab in tabs"
          :key="tab.value"
          type="button"
          :class="['nav-tab', { active: currentTab === tab.value }]"
          @click="currentTab = tab.value"
        >
          <span>{{ tab.label }}</span>
          <small>{{ tab.desc }}</small>
        </button>
      </nav>
      <div class="header-meta">
        <div class="meta-item">
          <p>上次登录</p>
          <strong>今日 10:26</strong>
        </div>
        <div class="meta-item accent">
          <p>账号状态</p>
          <strong>正常</strong>
        </div>
      </div>
    </header>

    <main class="main-content">
      <section v-if="currentTab === 'search'">
        <SearchView />
      </section>
      <section v-else-if="currentTab === 'workspace'">
        <WorkspaceView />
      </section>
      <section v-else-if="currentTab === 'favorites'">
        <FavoritesView />
      </section>
      <section v-else-if="currentTab === 'comments'">
        <CommentsView />
      </section>
      <section v-else>
        <ProfileView />
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import SearchView from '../components/SearchView.vue'
import WorkspaceView from '../components/WorkspaceView.vue'
import ProfileView from '../components/ProfileView.vue'
import FavoritesView from '../components/user/FavoritesView.vue'
import CommentsView from '../components/user/CommentsView.vue'

const tabs = [
  { value: 'search', label: '全局检索', desc: 'Search' },
  { value: 'workspace', label: '创作空间', desc: 'Workspace' },
  { value: 'favorites', label: '我的收藏', desc: 'Favorites' },
  { value: 'comments', label: '我的评论', desc: 'Comments' },
  { value: 'profile', label: '个人信息', desc: 'Profile' }
]

const currentTab = ref('search')
</script>

<style scoped>
.main-shell {
  min-height: 100vh;
  background: var(--surface-muted);
  padding: 32px clamp(20px, 4vw, 48px) 60px;
  display: flex;
  flex-direction: column;
  gap: 28px;
}

.main-header {
  background: var(--surface-base);
  border-radius: 36px;
  border: 1px solid var(--line-soft);
  padding: 28px clamp(20px, 3vw, 40px);
  box-shadow: var(--shadow-card);
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 24px;
}

.brand-block {
  display: flex;
  align-items: center;
  gap: 16px;
}

.brand-icon {
  width: 52px;
  height: 52px;
  border-radius: 16px;
  background: var(--surface-soft);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: var(--brand-primary);
}

.brand-label {
  font-size: 12px;
  letter-spacing: 0.4em;
  color: var(--text-muted);
  margin: 0;
}

.main-header h1 {
  font-size: 28px;
  margin-top: 6px;
}

.nav-tabs {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 12px;
}

.nav-tab {
  background: var(--surface-soft);
  border: 1px solid transparent;
  border-radius: 20px;
  padding: 14px 18px;
  text-align: left;
  color: var(--text-secondary);
  font-size: 15px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  transition: border-color 0.2s ease, transform 0.2s ease, box-shadow 0.2s ease;
}

.nav-tab small {
  color: var(--text-muted);
  font-size: 12px;
  letter-spacing: 0.2em;
  text-transform: uppercase;
}

.nav-tab.active {
  background: var(--surface-base);
  border-color: var(--brand-primary);
  box-shadow: 0 12px 30px rgba(34, 191, 163, 0.16);
  color: var(--text-strong);
  transform: translateY(-2px);
}

.nav-tab:hover {
  border-color: var(--brand-primary);
}

.header-meta {
  display: flex;
  gap: 16px;
  align-items: stretch;
}

.meta-item {
  flex: 1;
  background: var(--surface-soft);
  border-radius: 18px;
  padding: 14px 18px;
  border: 1px solid var(--line-soft);
}

.meta-item p {
  font-size: 12px;
  letter-spacing: 0.2em;
  color: var(--text-muted);
  margin-bottom: 6px;
}

.meta-item strong {
  font-size: 18px;
  color: var(--text-strong);
}

.meta-item.accent {
  background: rgba(34, 191, 163, 0.1);
  border-color: rgba(34, 191, 163, 0.4);
}

.main-content {
  flex: 1;
}

@media (max-width: 960px) {
  .main-header {
    grid-template-columns: 1fr;
  }

  .header-meta {
    flex-direction: column;
  }

  .nav-tab {
    text-align: center;
    align-items: center;
  }
}
</style>