<template>
  <div class="main-container">
    <!-- 顶部导航栏 -->
    <header class="top-nav">
      <div class="logo">📝 在线笔记系统</div>
      <nav class="nav-tabs">
        <button 
          :class="['nav-tab', { active: currentTab === 'search' }]"
          @click="currentTab = 'search'"
        >
          查询笔记
        </button>
        <button 
          :class="['nav-tab', { active: currentTab === 'workspace' }]"
          @click="currentTab = 'workspace'"
        >
          创作空间
        </button>
        <button 
          :class="['nav-tab', { active: currentTab === 'profile' }]"
          @click="currentTab = 'profile'"
        >
          个人信息
        </button>
      </nav>
    </header>

    <!-- 内容区域 -->
    <main class="content-area">
      <SearchView v-if="currentTab === 'search'" />
      <WorkspaceView v-if="currentTab === 'workspace'" />
      <ProfileView v-if="currentTab === 'profile'" />
    </main>
  </div>
</template>

//搜索功能模块

<script setup>
import { ref } from 'vue'
import SearchView from '../components/SearchView.vue'
import WorkspaceView from '../components/WorkspaceView.vue'
import ProfileView from '../components/ProfileView.vue'

const currentTab = ref('search')
</script>

<style scoped>
.main-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: white;
}

.top-nav {
  background: #00bcd4;
  color: white;
  padding: 0 30px;
  display: flex;
  align-items: center;
  height: 60px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.logo {
  font-size: 20px;
  font-weight: bold;
  margin-right: 50px;
}

.nav-tabs {
  display: flex;
  gap: 10px;
}

.nav-tab {
  background: transparent;
  border: none;
  color: white;
  padding: 10px 20px;
  font-size: 16px;
  cursor: pointer;
  border-radius: 5px;
  transition: background 0.3s;
}

.nav-tab:hover {
  background: rgba(255, 255, 255, 0.1);
}

.nav-tab.active {
  background: rgba(255, 255, 255, 0.2);
  font-weight: bold;
}

.content-area {
  flex: 1;
  overflow: auto;
  padding: 20px;
}
</style>