<template>
  <div class="reading-page">
    <div class="reading-shell">
      <main v-if="articleList.length" class="card-grid">
        <article
          v-for="(article, index) of articleList"
          :key="article.articleId"
          class="article-card"
          :style="{ animationDelay: index * 50 + 'ms' }"
        >
          <router-link
            :to="getArticleLink(article)"
            class="card-title"
          >
            {{ article.articleTitle }}
          </router-link>

          <div v-if="article.history" class="card-progress">
            <div class="progress-track">
              <div
                class="progress-fill"
                :style="{ '--target-width': Math.round(article.history.percent || 0) + '%' }"
              ></div>
            </div>
            <span class="progress-label">{{ Math.round(article.history.percent || 0) }}%</span>
          </div>

          <div v-if="article.history" class="record-section">
            <div class="section-label">最近阅读</div>
            <router-link :to="getHistoryLink(article)" class="record-item">
              <i class="record-dot is-history"></i>
              <span class="record-text">{{ article.history.anchorText || '文章开头' }}</span>
              <span class="record-time">{{ formatDate(article.history.updatedAt) }}</span>
            </router-link>
          </div>

          <div v-if="article.positions.length" class="record-section">
            <div class="section-label">
              书签
              <span class="bookmark-count">{{ article.positions.length }}</span>
            </div>
            <div v-for="pos of article.positions" :key="pos.positionId" class="record-item-wrap">
              <router-link :to="getBookmarkLink(article, pos)" class="record-item">
                <i class="record-dot is-bookmark"></i>
                <span class="record-text">{{ pos.anchorText || '书签' }}</span>
              </router-link>
              <button
                class="remove-btn"
                title="删除书签"
                @click="handleRemoveBookmark(article.articleId, pos.positionId)"
              >
                <v-icon size="16">mdi-close</v-icon>
              </button>
            </div>
          </div>
        </article>
      </main>

      <section v-else class="reading-empty">
        <span class="empty-leaf" aria-hidden="true"></span>
        <h2>还没有星签</h2>
        <p>打开文章会自动记录阅读位置，目录旁的小绿叶可以保存书签</p>
        <router-link to="/archives">去归档里翻一篇</router-link>
      </section>
    </div>

    <v-dialog v-model="confirmDialog.show" max-width="340">
      <v-card class="confirm-card">
        <v-card-title class="confirm-title">确认删除</v-card-title>
        <v-card-text class="confirm-text">{{ confirmDialog.message }}</v-card-text>
        <v-card-actions class="confirm-actions">
          <v-btn text @click="confirmDialog.show = false">取消</v-btn>
          <v-btn class="confirm-delete-btn" dark @click="confirmDialog.onConfirm">删除</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script>
import {
  listReadingTree,
  removeArticleBookmark
} from "../../utils/readingStore";

function formatDate(value) {
  if (!value) return "刚刚";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "刚刚";
  return date.toLocaleString("zh-CN", {
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit"
  });
}

export default {
  data() {
    return {
      treeList: [],
      confirmDialog: {
        show: false,
        message: "",
        onConfirm: null
      }
    };
  },
  mounted() {
    document.body.classList.add("reading-page-active");
    this.treeList = listReadingTree();
  },
  beforeDestroy() {
    document.body.classList.remove("reading-page-active");
  },
  methods: {
    formatDate,
    getArticleLink(article) {
      const link = { path: "/articles/" + article.articleId };
      if (article.history && article.history.positionId) {
        link.query = { bookmark: article.history.positionId };
      }
      return link;
    },
    getHistoryLink(article) {
      return {
        path: "/articles/" + article.articleId,
        query: { bookmark: article.history.positionId }
      };
    },
    getBookmarkLink(article, pos) {
      return {
        path: "/articles/" + article.articleId,
        query: { bookmark: pos.positionId }
      };
    },
    handleRemoveBookmark(articleId, positionId) {
      this.confirmDialog = {
        show: true,
        message: "删除后无法恢复此书签，确定继续？",
        onConfirm: () => {
          this.treeList = removeArticleBookmark(articleId, positionId);
          this.confirmDialog.show = false;
        }
      };
    }
  },
  computed: {
    articleList() {
      return this.treeList;
    }
  }
};
</script>

<style scoped>
.reading-page {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 92px 24px 0;
  box-sizing: border-box;
  background-image: url("https://pic.caiguoyu.cn/typora/202605271022662.png");
  background-position: center 30%;
  background-size: cover;
  background-attachment: fixed;
  background-repeat: no-repeat;
}
.reading-shell {
  max-width: 1080px;
  margin: 0 auto;
  height: 100%;
  overflow-y: auto;
  padding-bottom: 180px;
  scrollbar-width: none;
  -ms-overflow-style: none;
}
.reading-shell::-webkit-scrollbar {
  display: none;
}

/* 三列网格 */
.card-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}
.article-card {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 18px;
  border: 1px solid rgba(226, 232, 240, 0.8);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 2px 12px rgba(31, 41, 55, 0.04);
  opacity: 0;
  transform: translateY(10px);
  animation: cardEnter 0.4s ease forwards;
  transition: box-shadow 0.22s, transform 0.22s, border-color 0.22s;
}
.article-card:hover {
  border-color: rgba(112, 193, 179, 0.45);
  box-shadow: 0 8px 24px rgba(31, 41, 55, 0.08);
  transform: translateY(-2px);
}

/* 标题 */
.card-title {
  display: block;
  color: #1e293b;
  font-size: 15px;
  font-weight: 700;
  line-height: 1.5;
  text-decoration: none;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color 0.2s;
}
.card-title:hover {
  color: #059669;
}

/* 进度条 */
.card-progress {
  display: flex;
  align-items: center;
  gap: 8px;
}
.progress-track {
  flex: 1;
  height: 6px;
  border-radius: 3px;
  background: rgba(226, 232, 240, 0.7);
  overflow: hidden;
}
.progress-fill {
  height: 100%;
  border-radius: 3px;
  background: linear-gradient(90deg, #fbbf24, #f59e0b);
  width: var(--target-width);
  animation: progressGrow 0.8s ease-out forwards;
}
.progress-label {
  flex-shrink: 0;
  color: #92400e;
  font-size: 12px;
  font-weight: 800;
  min-width: 32px;
  text-align: right;
}

/* 记录区 */
.record-section {
  padding-top: 6px;
}
.record-section + .record-section {
  border-top: 1px dashed rgba(226, 232, 240, 0.6);
  padding-top: 10px;
}
.section-label {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 6px;
  color: #94a3b8;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.05em;
}
.bookmark-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 9px;
  background: linear-gradient(135deg, #86efac, #22c55e);
  color: #fff;
  font-size: 10px;
  font-weight: 800;
}
.record-item-wrap {
  display: flex;
  align-items: center;
  gap: 4px;
}
.record-item {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
  padding: 6px 8px;
  border-radius: 6px;
  text-decoration: none;
  color: inherit;
  transition: background 0.15s;
}
.record-item:hover {
  background: rgba(112, 193, 179, 0.08);
}
.record-dot {
  flex-shrink: 0;
  width: 8px;
  height: 10px;
  border-radius: 80% 0 80% 0;
  transform: rotate(-26deg);
}
.record-dot.is-history {
  background: linear-gradient(135deg, #fbbf24, #f59e0b);
}
.record-dot.is-bookmark {
  background: linear-gradient(135deg, #86efac, #22c55e);
}
.record-text {
  flex: 1;
  min-width: 0;
  color: #334155;
  font-size: 13px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.record-time {
  flex-shrink: 0;
  color: #94a3b8;
  font-size: 11px;
  font-weight: 500;
  white-space: nowrap;
}

/* 删除按钮 - 危险风格 */
.remove-btn {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  padding: 0;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: #cbd5e1;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.15s, color 0.15s, background 0.15s, transform 0.15s;
}
.record-item-wrap:hover .remove-btn {
  opacity: 1;
}
.remove-btn:hover {
  background: #fef2f2;
  color: #dc2626;
  transform: scale(1.15);
  animation: dangerPulse 0.6s ease-in-out;
}

/* 确认弹窗 */
.confirm-card {
  border-radius: 12px !important;
}
.confirm-title {
  color: #991b1b !important;
  font-size: 17px !important;
  font-weight: 800 !important;
}
.confirm-text {
  color: #64748b !important;
  font-size: 14px !important;
  line-height: 1.6 !important;
}
.confirm-actions {
  padding: 8px 16px 16px !important;
  justify-content: flex-end !important;
  gap: 8px;
}
.confirm-delete-btn {
  background: #dc2626 !important;
  border-radius: 8px !important;
  font-weight: 700 !important;
  letter-spacing: 0.02em;
}
.confirm-delete-btn:hover {
  background: #b91c1c !important;
}

/* 空状态 */
.reading-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 320px;
  text-align: center;
  animation: cardEnter 0.4s ease both;
}
.empty-leaf {
  width: 64px;
  height: 42px;
  margin-bottom: 20px;
  border-radius: 100% 0 100% 0;
  background: linear-gradient(135deg, #86efac, #22c55e);
  transform: rotate(-18deg);
}
.reading-empty h2 {
  margin: 0 0 10px;
  color: #2f3a4a;
  font-size: 24px;
}
.reading-empty p {
  max-width: 480px;
  margin: 0 0 18px;
  color: #64748b;
  line-height: 1.8;
}
.reading-empty a {
  display: inline-flex;
  align-items: center;
  min-height: 38px;
  padding: 0 16px;
  border-radius: 8px;
  background: #31425f;
  color: #fff;
  font-weight: 800;
  text-decoration: none;
  transition: background 0.2s, transform 0.2s;
}
.reading-empty a:hover {
  background: #253248;
  transform: translateY(-1px);
}

@keyframes cardEnter {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
@keyframes progressGrow {
  from {
    width: 0;
  }
  to {
    width: var(--target-width);
  }
}
@keyframes dangerPulse {
  0%, 100% {
    box-shadow: 0 0 0 0 rgba(220, 38, 38, 0);
  }
  50% {
    box-shadow: 0 0 0 4px rgba(220, 38, 38, 0.15);
  }
}

@media (max-width: 900px) {
  .card-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
@media (max-width: 600px) {
  .reading-page {
    padding: 76px 16px 0;
  }
  .reading-shell {
    padding-bottom: 150px;
  }
  .card-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  .record-time {
    display: none;
  }
  .remove-btn {
    opacity: 1;
  }
}
</style>

<style>
body.reading-page-active {
  background: url("https://pic.caiguoyu.cn/typora/202605271022662.png") center 30% / cover fixed no-repeat;
}
body.reading-page-active .theme--light.v-application {
  background: transparent;
}
body.reading-page-active .nav-theme-reading.nav,
body.reading-page-active .nav-theme-reading.nav-fixed {
  background: rgba(255, 255, 255, 0.16) !important;
}
body.reading-page-active .theme--light.v-footer {
  background: transparent !important;
}
body.reading-page-active .footer-wrap {
  padding: 24px 20px;
  background: transparent;
  box-shadow: none;
  text-shadow: 0 2px 10px rgba(15, 23, 42, 0.58);
  backdrop-filter: none;
  -webkit-backdrop-filter: none;
  animation: none;
}
</style>
