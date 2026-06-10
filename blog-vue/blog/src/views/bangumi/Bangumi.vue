<template>
  <div class="bangumi-page">
    <div class="bangumi-pattern"></div>
    <section class="bangumi-shell">
      <header class="bangumi-header">
        <div class="bangumi-kicker">
          <v-icon size="18" color="#ef5da8">mdi-star-four-points</v-icon>
          <span>Anime Shelf</span>
        </div>
        <h1>追番</h1>
        <p>
          把追过的番剧收进一格光里，留下每次更新、完结和重逢的坐标。
        </p>
        <div class="bangumi-stats">
          <div>
            <strong>{{ bangumiList.length }}</strong>
            <span>全部</span>
          </div>
          <div>
            <strong>{{ watchingCount }}</strong>
            <span>连载中</span>
          </div>
          <div>
            <strong>{{ finishedCount }}</strong>
            <span>已完结</span>
          </div>
          <div>
            <strong>{{ styleItems.length }}</strong>
            <span>风格</span>
          </div>
        </div>
      </header>

      <section class="bangumi-controls">
        <div class="bangumi-toolbar">
          <div class="bangumi-tabs" role="tablist">
            <button
              v-for="item of filterItems"
              :key="item.value"
              type="button"
              :class="{ active: filterStatus === item.value }"
              @click="filterStatus = item.value"
            >
              {{ item.label }}
            </button>
          </div>
          <label class="bangumi-search">
            <v-icon size="18" color="#64748b">mdi-magnify</v-icon>
            <input
              v-model.trim="keyword"
              type="search"
              placeholder="搜索番名 / 地区 / 风格"
            />
          </label>
        </div>

        <div v-if="styleItems.length > 0" class="bangumi-style-filter">
          <button
            type="button"
            :class="{ active: selectedStyle === '' }"
            @click="selectedStyle = ''"
          >
            全部风格
          </button>
          <button
            v-for="style of styleItems"
            :key="style"
            type="button"
            :class="{ active: selectedStyle === style }"
            @click="selectedStyle = style"
          >
            {{ style }}
          </button>
        </div>
      </section>

      <div v-if="loading" class="bangumi-state">
        <v-icon size="26" color="#ef5da8">mdi-loading</v-icon>
        <span>同步星轨中</span>
      </div>

      <div v-else-if="filteredBangumiList.length === 0" class="bangumi-state">
        <v-icon size="26" color="#64748b">mdi-inbox-outline</v-icon>
        <span>暂无追番记录</span>
      </div>

      <template v-else>
        <div class="bangumi-result-bar">
          <span>
            {{ filteredBangumiList.length }} 部作品 · 第 {{ currentPage }}/{{ pageCount }} 页
          </span>
          <span>每页 {{ pageSize }} 个</span>
        </div>

        <main class="bangumi-grid">
          <article
            v-for="item of pagedBangumiList"
            :key="item.seasonId"
            class="bangumi-card"
          >
            <a :href="item.url" target="_blank" class="bangumi-cover">
              <img
                v-if="!item.coverError"
                :src="item.cover"
                :alt="item.title"
                referrerpolicy="no-referrer"
                @error="markCoverError(item)"
              />
              <div v-else class="bangumi-cover-fallback">
                <v-icon size="34" color="#ef5da8">mdi-image-off-outline</v-icon>
                <span>{{ item.title }}</span>
              </div>
              <span class="bangumi-rank">#{{ item.sortOrder }}</span>
              <span class="bangumi-status" :class="{ finished: item.isFinish }">
                {{ item.isFinish ? "已完结" : "连载中" }}
              </span>
            </a>

            <div class="bangumi-body">
              <div class="bangumi-title-row">
                <div class="bangumi-title-main">
                  <h2 :title="item.title">{{ item.title }}</h2>
                  <div
                    class="bangumi-follow-line"
                    :title="formatNumber(item.followCount) + '人追番'"
                  >
                    <v-icon size="14" color="#8b5cf6">mdi-account-heart-outline</v-icon>
                    <span>{{ formatNumber(item.followCount) }}人追番</span>
                  </div>
                </div>
                <div class="bangumi-score-card">
                  <strong>{{ item.ratingScore || "暂无" }}</strong>
                  <span>{{ formatRatingCount(item.ratingCount) }}</span>
                </div>
              </div>

              <div class="bangumi-progress">
                <v-icon size="16" color="#14b8a6">mdi-play-circle-outline</v-icon>
                <span :title="getProgressText(item)">{{ getProgressText(item) }}</span>
              </div>

              <div class="bangumi-info-grid">
                <span :title="item.releaseDate || '暂无首播日期'">
                  <v-icon size="14" color="#f59e0b">mdi-calendar-star</v-icon>
                  {{ item.releaseDate || "暂无首播日期" }}
                </span>
                <span :title="item.area || '暂无地区'">
                  <v-icon size="14" color="#0ea5e9">mdi-map-marker-outline</v-icon>
                  {{ item.area || "暂无地区" }}
                </span>
              </div>

              <div class="bangumi-renewal">
                <v-icon size="14" color="#ef5da8">mdi-calendar-sync</v-icon>
                <span :title="item.renewalTime || '暂无更新时间'">
                  {{ item.renewalTime || "暂无更新时间" }}
                </span>
              </div>

              <div class="bangumi-style-tags">
                <button
                  v-for="style of getStyleList(item)"
                  :key="item.seasonId + '-' + style"
                  type="button"
                  @click="selectedStyle = style"
                >
                  {{ style }}
                </button>
              </div>

              <p :title="item.evaluate">{{ item.evaluate }}</p>

              <div class="bangumi-footer">
                <span>{{ item.seasonTypeName || "番剧" }}</span>
                <a :href="item.url" target="_blank">
                  去 B 站看
                  <v-icon size="14" color="currentColor">mdi-arrow-right</v-icon>
                </a>
              </div>
            </div>
          </article>
        </main>

        <v-pagination
          v-if="pageCount > 1"
          class="bangumi-pagination"
          color="#ef5da8"
          v-model="currentPage"
          :length="pageCount"
          total-visible="7"
        />
      </template>
    </section>
  </div>
</template>

<script>
const PAGE_SIZE = 8;

export default {
  created() {
    this.listBangumis();
  },
  data: function() {
    return {
      loading: true,
      keyword: "",
      filterStatus: "all",
      selectedStyle: "",
      currentPage: 1,
      pageSize: PAGE_SIZE,
      bangumiList: [],
      filterItems: [
        { label: "全部", value: "all" },
        { label: "连载中", value: "watching" },
        { label: "已完结", value: "finished" }
      ]
    };
  },
  methods: {
    listBangumis() {
      this.loading = true;
      this.axios
        .get("/api/bangumis")
        .then(({ data }) => {
          this.bangumiList = data.data || [];
        })
        .finally(() => {
          this.loading = false;
        });
    },
    getProgressText(item) {
      if (item.progress) {
        return item.progress;
      }
      if (item.latestEpIndex && item.latestEpLongTitle) {
        return item.latestEpIndex + " · " + item.latestEpLongTitle;
      }
      return item.latestEpIndex || "暂无进度";
    },
    getStyleList(item) {
      if (!item || !item.styles) {
        return [];
      }
      return item.styles
        .split("/")
        .map(style => style.trim())
        .filter(Boolean);
    },
    formatRatingCount(value) {
      return value ? formatNumberValue(value) + "人评" : "暂无评分";
    },
    formatNumber(value) {
      return formatNumberValue(value);
    },
    markCoverError(item) {
      this.$set(item, "coverError", true);
    },
    resetPage() {
      this.currentPage = 1;
    }
  },
  computed: {
    watchingCount() {
      return this.bangumiList.filter(item => !item.isFinish).length;
    },
    finishedCount() {
      return this.bangumiList.filter(item => item.isFinish).length;
    },
    styleItems() {
      const styleSet = new Set();
      this.bangumiList.forEach(item => {
        this.getStyleList(item).forEach(style => styleSet.add(style));
      });
      return Array.from(styleSet).sort((a, b) => a.localeCompare(b, "zh-CN"));
    },
    filteredBangumiList() {
      const keyword = this.keyword.toLowerCase();
      return this.bangumiList.filter(item => {
        const styleList = this.getStyleList(item);
        const matchedStatus =
          this.filterStatus === "all" ||
          (this.filterStatus === "watching" && !item.isFinish) ||
          (this.filterStatus === "finished" && item.isFinish);
        const matchedStyle =
          !this.selectedStyle || styleList.includes(this.selectedStyle);
        const searchText = [
          item.title,
          item.area,
          item.seasonTypeName,
          item.styles,
          item.releaseDate
        ]
          .filter(Boolean)
          .join(" ")
          .toLowerCase();
        const matchedKeyword = !keyword || searchText.includes(keyword);
        return matchedStatus && matchedStyle && matchedKeyword;
      });
    },
    pageCount() {
      return Math.max(Math.ceil(this.filteredBangumiList.length / this.pageSize), 1);
    },
    pagedBangumiList() {
      const start = (this.currentPage - 1) * this.pageSize;
      return this.filteredBangumiList.slice(start, start + this.pageSize);
    }
  },
  watch: {
    keyword() {
      this.resetPage();
    },
    filterStatus() {
      this.resetPage();
    },
    selectedStyle() {
      this.resetPage();
    },
    pageCount(value) {
      if (this.currentPage > value) {
        this.currentPage = value;
      }
    }
  }
};

function formatNumberValue(value) {
  const number = Number(value || 0);
  if (number >= 100000000) {
    return (number / 100000000).toFixed(1) + "亿";
  }
  if (number >= 10000) {
    return (number / 10000).toFixed(1) + "万";
  }
  return String(number);
}
</script>

<style scoped>
.bangumi-page {
  position: relative;
  width: 100%;
  min-height: 100vh;
  overflow: hidden;
  padding: 112px 18px 72px;
  background:
    linear-gradient(180deg, rgba(255, 247, 250, 0.96), rgba(232, 252, 247, 0.94)),
    repeating-linear-gradient(
      135deg,
      rgba(239, 93, 168, 0.08) 0,
      rgba(239, 93, 168, 0.08) 1px,
      transparent 1px,
      transparent 18px
    );
  color: #182235;
}
.bangumi-page *,
.bangumi-page *::before,
.bangumi-page *::after {
  box-sizing: border-box;
}
.bangumi-pattern {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background:
    linear-gradient(90deg, rgba(20, 184, 166, 0.09) 1px, transparent 1px),
    linear-gradient(0deg, rgba(239, 93, 168, 0.08) 1px, transparent 1px);
  background-size: 36px 36px;
  mask-image: linear-gradient(180deg, #000 0%, transparent 80%);
}
.bangumi-shell {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 1180px;
  margin: 0 auto;
}
.bangumi-header {
  display: grid;
  gap: 14px;
  width: 100%;
  max-width: 760px;
  margin-bottom: 24px;
}
.bangumi-kicker {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  width: fit-content;
  padding: 7px 12px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(239, 93, 168, 0.18);
  border-radius: 8px;
  color: #be185d;
  font-size: 13px;
  font-weight: 800;
}
.bangumi-header h1 {
  margin: 0;
  color: #172033;
  font-size: 44px;
  font-weight: 900;
  letter-spacing: 0;
  line-height: 1.12;
}
.bangumi-header p {
  max-width: 640px;
  margin: 0;
  color: #64748b;
  font-size: 15px;
  line-height: 1.8;
  overflow-wrap: anywhere;
}
.bangumi-stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(104px, 1fr));
  gap: 10px;
  max-width: 560px;
  margin-top: 4px;
}
.bangumi-stats div {
  padding: 12px 14px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(20, 184, 166, 0.16);
  border-radius: 8px;
}
.bangumi-stats strong {
  display: block;
  color: #0f766e;
  font-size: 24px;
  line-height: 1;
}
.bangumi-stats span {
  display: block;
  margin-top: 6px;
  color: #64748b;
  font-size: 12px;
}
.bangumi-controls {
  display: grid;
  gap: 12px;
  margin-bottom: 18px;
}
.bangumi-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  width: 100%;
}
.bangumi-tabs {
  display: inline-flex;
  padding: 4px;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(239, 93, 168, 0.14);
  border-radius: 8px;
}
.bangumi-tabs button,
.bangumi-style-filter button {
  border: 0;
  border-radius: 6px;
  background: transparent;
  color: #64748b;
  cursor: pointer;
  font-weight: 800;
  transition: color 0.2s ease, background 0.2s ease, transform 0.2s ease;
}
.bangumi-tabs button {
  min-width: 76px;
  height: 34px;
  padding: 0 14px;
}
.bangumi-tabs button.active,
.bangumi-tabs button:hover,
.bangumi-style-filter button.active,
.bangumi-style-filter button:hover {
  background: #ef5da8;
  color: #fff;
  transform: translateY(-1px);
}
.bangumi-search {
  display: flex;
  align-items: center;
  gap: 8px;
  width: min(360px, 100%);
  height: 44px;
  padding: 0 14px;
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(20, 184, 166, 0.18);
  border-radius: 8px;
}
.bangumi-search input {
  width: 100%;
  border: 0;
  outline: 0;
  background: transparent;
  color: #172033;
  font-size: 14px;
}
.bangumi-style-filter {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  overflow: visible;
  padding: 2px 0 8px;
}
.bangumi-style-filter button {
  flex: 0 0 auto;
  min-height: 32px;
  padding: 0 12px;
  background: rgba(255, 255, 255, 0.68);
  border: 1px solid rgba(20, 184, 166, 0.16);
  font-size: 12px;
}
.bangumi-result-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
  color: #64748b;
  font-size: 13px;
  font-weight: 800;
}
.bangumi-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px;
}
.bangumi-card {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.88);
  border: 1px solid rgba(239, 93, 168, 0.13);
  border-radius: 8px;
  box-shadow: 0 16px 40px rgba(15, 23, 42, 0.08);
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}
.bangumi-card:hover {
  border-color: rgba(239, 93, 168, 0.38);
  box-shadow: 0 20px 50px rgba(239, 93, 168, 0.16);
  transform: translateY(-4px);
}
.bangumi-cover {
  position: relative;
  display: block;
  flex: 0 0 auto;
  aspect-ratio: 3 / 4;
  overflow: hidden;
  background: #e2e8f0;
}
.bangumi-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.35s ease;
}
.bangumi-cover-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 12px;
  width: 100%;
  height: 100%;
  padding: 22px;
  background:
    linear-gradient(150deg, rgba(255, 228, 239, 0.92), rgba(183, 248, 234, 0.88)),
    repeating-linear-gradient(
      45deg,
      rgba(239, 93, 168, 0.12) 0,
      rgba(239, 93, 168, 0.12) 1px,
      transparent 1px,
      transparent 12px
    );
  color: #be185d;
  font-size: 15px;
  font-weight: 900;
  line-height: 1.5;
  text-align: center;
}
.bangumi-card:hover .bangumi-cover img {
  transform: scale(1.05);
}
.bangumi-cover::after {
  position: absolute;
  inset: auto 0 0;
  height: 42%;
  pointer-events: none;
  background: linear-gradient(180deg, transparent, rgba(15, 23, 42, 0.72));
  content: "";
}
.bangumi-rank,
.bangumi-status {
  position: absolute;
  z-index: 2;
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  padding: 0 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 900;
}
.bangumi-rank {
  top: 10px;
  left: 10px;
  background: rgba(255, 255, 255, 0.9);
  color: #be185d;
}
.bangumi-status {
  right: 10px;
  bottom: 10px;
  background: #14b8a6;
  color: #fff;
}
.bangumi-status.finished {
  background: #f59e0b;
}
.bangumi-body {
  display: grid;
  flex: 1;
  gap: 10px;
  grid-template-rows: 72px 40px 22px 22px 58px 62px 24px;
  align-content: start;
  min-width: 0;
  padding: 14px;
}
.bangumi-title-row {
  display: grid;
  align-items: start;
  grid-template-columns: minmax(0, 1fr) 76px;
  gap: 10px;
  min-width: 0;
}
.bangumi-title-main {
  display: grid;
  gap: 6px;
  min-width: 0;
}
.bangumi-title-main h2 {
  display: -webkit-box;
  height: 44px;
  margin: 0;
  overflow: hidden;
  color: #172033;
  font-size: 16px;
  font-weight: 900;
  letter-spacing: 0;
  line-height: 1.35;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}
.bangumi-follow-line {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  min-width: 0;
  overflow: hidden;
  color: #7c3aed;
  font-size: 12px;
  font-weight: 800;
  line-height: 1;
}
.bangumi-follow-line .v-icon {
  flex: 0 0 auto;
}
.bangumi-follow-line span {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.bangumi-score-card {
  display: grid;
  align-content: center;
  width: 76px;
  height: 58px;
  padding: 6px;
  background: #fff7ed;
  border: 1px solid rgba(245, 158, 11, 0.24);
  border-radius: 8px;
  color: #ea580c;
  text-align: center;
}
.bangumi-score-card strong {
  font-size: 18px;
  line-height: 1;
}
.bangumi-score-card span {
  margin-top: 4px;
  overflow: hidden;
  color: #9a3412;
  font-size: 10px;
  line-height: 1.2;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.bangumi-progress {
  display: flex;
  align-items: flex-start;
  gap: 7px;
  height: 40px;
  overflow: hidden;
  color: #0f766e;
  font-size: 13px;
  font-weight: 800;
  line-height: 1.5;
}
.bangumi-progress span {
  display: -webkit-box;
  min-width: 0;
  overflow: hidden;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}
.bangumi-info-grid {
  display: grid;
  align-items: center;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
  height: 22px;
  color: #64748b;
  font-size: 12px;
  line-height: 1;
}
.bangumi-info-grid span {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.bangumi-info-grid .v-icon,
.bangumi-renewal .v-icon,
.bangumi-progress .v-icon {
  flex: 0 0 auto;
}
.bangumi-renewal {
  display: flex;
  align-items: center;
  gap: 5px;
  min-width: 0;
  height: 22px;
  overflow: hidden;
  color: #64748b;
  font-size: 12px;
  line-height: 1;
}
.bangumi-renewal span {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.bangumi-style-tags {
  display: flex;
  flex-wrap: wrap;
  align-content: flex-start;
  gap: 6px;
  height: 58px;
  overflow: hidden;
}
.bangumi-style-tags button {
  height: 26px;
  padding: 0 9px;
  overflow: hidden;
  border: 1px solid rgba(239, 93, 168, 0.16);
  border-radius: 6px;
  background: #fff7fb;
  color: #be185d;
  cursor: pointer;
  font-size: 12px;
  font-weight: 800;
  line-height: 24px;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.bangumi-body p {
  display: -webkit-box;
  height: 62px;
  margin: 0 !important;
  overflow: hidden;
  color: #475569;
  font-size: 13px;
  line-height: 1.55;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
}
.bangumi-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  height: 24px;
  padding-top: 2px;
  color: #94a3b8;
  font-size: 12px;
}
.bangumi-footer span,
.bangumi-footer a {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.bangumi-footer a {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  color: #0f766e !important;
  font-weight: 900;
}
.bangumi-state {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  min-height: 260px;
  color: #64748b;
  font-weight: 800;
}
.bangumi-pagination {
  margin-top: 24px;
}
@media (max-width: 1180px) {
  .bangumi-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}
@media (max-width: 860px) {
  .bangumi-page {
    padding: 92px 14px 48px;
  }
  .bangumi-header h1 {
    font-size: 34px;
  }
  .bangumi-stats {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .bangumi-toolbar {
    align-items: stretch;
    flex-direction: column;
  }
  .bangumi-tabs,
  .bangumi-search {
    width: 100%;
    max-width: 100%;
  }
  .bangumi-tabs {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
  .bangumi-tabs button {
    min-width: 0;
  }
  .bangumi-result-bar {
    align-items: flex-start;
    flex-direction: column;
  }
  .bangumi-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
@media (max-width: 560px) {
  .bangumi-grid {
    grid-template-columns: 1fr;
  }
  .bangumi-title-main h2 {
    font-size: 15px;
  }
  .bangumi-info-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .bangumi-body {
    padding: 12px;
  }
  .bangumi-body p {
    min-height: auto;
    -webkit-line-clamp: 2;
  }
  .bangumi-footer {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
