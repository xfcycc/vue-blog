<template>
  <div class="game-page">
    <div class="game-pattern"></div>
    <section class="game-shell">
      <header class="game-header">
        <div class="game-kicker">
          <v-icon size="18" color="#ef5da8"
            >mdi-controller-classic-outline</v-icon
          >
          <span>Game Shelf</span>
        </div>
        <h1>游戏</h1>
        <p>把玩过的世界收进一格光里，留下每次启程、沉浸和通关的坐标。</p>
        <div class="game-stats">
          <div>
            <strong>{{ total }}</strong>
            <span>全部</span>
          </div>
          <div>
            <strong>{{ statusCount("PLAYING") }}</strong>
            <span>在玩</span>
          </div>
          <div>
            <strong>{{ statusCount("PLAYED") }}</strong>
            <span>玩过</span>
          </div>
          <div>
            <strong>{{ platformItems.length }}</strong>
            <span>平台</span>
          </div>
        </div>
      </header>

      <section class="game-controls">
        <div class="game-toolbar">
          <div class="game-tabs" role="tablist">
            <button
              v-for="item in statusItems"
              :key="item.value"
              type="button"
              :class="{ active: query.playStatus === item.value }"
              @click="selectStatus(item.value)"
            >
              {{ item.label }}
            </button>
          </div>
          <label class="game-search">
            <v-icon size="18" color="#64748b">mdi-magnify</v-icon>
            <input
              v-model.trim="query.keywords"
              type="search"
              placeholder="搜索游戏名称"
              @change="searchGames"
              @keyup.enter="searchGames"
            />
          </label>
        </div>

        <div v-if="platformItems.length" class="game-platform-filter">
          <button
            type="button"
            :class="{ active: !query.platform }"
            @click="selectPlatform('')"
          >
            全部平台
          </button>
          <button
            v-for="platform in platformItems"
            :key="platform"
            type="button"
            :class="{ active: query.platform === platform }"
            @click="selectPlatform(platform)"
          >
            {{ platformName(platform) }}
          </button>
        </div>
      </section>

      <div v-if="loading" class="game-state">
        <v-icon class="loading-icon" size="26" color="#ef5da8"
          >mdi-loading</v-icon
        >
        <span>同步游戏星轨中</span>
      </div>

      <div v-else-if="gameList.length === 0" class="game-state">
        <v-icon size="28" color="#64748b"
          >mdi-controller-classic-outline</v-icon
        >
        <span>暂无游戏记录</span>
      </div>

      <template v-else>
        <div class="game-result-bar">
          <span>
            {{ total }} 个游戏 · 第 {{ query.current }}/{{ pageCount }} 页
          </span>
          <span>每页 {{ query.size }} 个</span>
        </div>

        <main class="game-grid">
          <article
            v-for="game in gameList"
            :key="game.id"
            class="game-card"
            role="button"
            tabindex="0"
            @click="openGame(game.id)"
            @keyup.enter="openGame(game.id)"
          >
            <div class="game-cover">
              <img
                v-if="game.cover && !game.coverError"
                :src="game.cover"
                :alt="game.gameName"
                referrerpolicy="no-referrer"
                @error="markCoverError(game)"
              />
              <div v-else class="game-cover-fallback">
                <v-icon size="34" color="#ef5da8">mdi-image-off-outline</v-icon>
                <span>{{ game.gameName }}</span>
              </div>
              <div class="game-platforms">
                <span
                  v-for="platform in game.platformList"
                  :key="game.id + '-' + platform"
                >
                  {{ platformName(platform) }}
                </span>
              </div>
              <span class="game-playtime">
                <v-icon size="13" color="currentColor"
                  >mdi-clock-outline</v-icon
                >
                {{ formatPlaytime(game.playtimeForever) }}
              </span>
              <span
                class="game-status"
                :class="'status-' + String(game.playStatus).toLowerCase()"
              >
                {{ statusName(game.playStatus) }}
              </span>
            </div>

            <div
              class="game-body"
              :class="{
                'without-metrics':
                  !game.cardFieldList || game.cardFieldList.length === 0,
                'without-tags': !game.tagList || game.tagList.length === 0
              }"
            >
              <div class="game-title-row">
                <div class="game-title-main">
                  <h2 :title="game.gameName">{{ game.gameName }}</h2>
                </div>
                <div class="game-score-card">
                  <strong>{{
                    game.personalScore == null ? "暂无" : game.personalScore
                  }}</strong>
                </div>
              </div>

              <div v-if="game.tagList && game.tagList.length" class="game-tags">
                <span
                  v-for="(tag, index) in game.tagList"
                  :key="game.id + '-' + tag"
                  :class="'tag-tone-' + (index % 5)"
                  :title="tag"
                >
                  {{ tag }}
                </span>
              </div>

              <div
                v-if="game.cardFieldList && game.cardFieldList.length"
                class="game-metrics"
              >
                <span
                  v-for="field in game.cardFieldList"
                  :key="field.id || field.fieldName"
                  :title="field.fieldName + ' ' + formatFieldValue(field)"
                >
                  <small>{{ field.fieldName }}</small>
                  <strong>{{ formatFieldValue(field) }}</strong>
                </span>
              </div>
              <p :title="game.gameIntro">
                {{ game.gameIntro || "这份游戏档案还没有填写简介。" }}
              </p>
            </div>
          </article>
        </main>

        <v-pagination
          v-if="pageCount > 1"
          v-model="query.current"
          class="game-pagination"
          color="#ef5da8"
          :length="pageCount"
          total-visible="7"
          @input="listGames"
        />
      </template>
    </section>
  </div>
</template>

<script>
const PAGE_SIZE = 8;

export default {
  created() {
    this.listGames();
  },
  data() {
    return {
      loading: false,
      total: 0,
      gameList: [],
      platformItems: [],
      statusCountMap: {},
      query: {
        current: 1,
        size: PAGE_SIZE,
        keywords: "",
        playStatus: "",
        platform: ""
      },
      statusItems: [
        { label: "全部", value: "" },
        { label: "想玩", value: "WANT" },
        { label: "在玩", value: "PLAYING" },
        { label: "玩过", value: "PLAYED" }
      ],
      platformNames: {
        STEAM: "Steam",
        PSN: "PSN",
        PC: "PC",
        MOBILE: "手游",
        SWITCH: "Switch"
      }
    };
  },
  computed: {
    pageCount() {
      return Math.max(Math.ceil(this.total / this.query.size), 1);
    }
  },
  methods: {
    listGames() {
      this.loading = true;
      this.axios
        .post("/api/games/list", this.query)
        .then(({ data }) => {
          const result = data.data || {};
          this.gameList = result.recordList || [];
          this.total = result.count || 0;
          this.statusCountMap = result.statusCountMap || {};
          this.platformItems = result.platformList || [];
        })
        .finally(() => {
          this.loading = false;
        });
    },
    searchGames() {
      this.query.current = 1;
      this.listGames();
    },
    selectStatus(status) {
      this.query.playStatus = status;
      this.searchGames();
    },
    selectPlatform(platform) {
      this.query.platform = platform;
      this.searchGames();
    },
    openGame(id) {
      this.$router.push({ path: "/games/" + id });
    },
    markCoverError(game) {
      this.$set(game, "coverError", true);
    },
    statusCount(status) {
      return this.statusCountMap[status] || 0;
    },
    statusName(value) {
      const item = this.statusItems.find(status => status.value === value);
      return item ? item.label : "未设置";
    },
    platformName(value) {
      return this.platformNames[value] || value;
    },
    formatPlaytime(minutes) {
      if (!minutes) return "0 小时";
      return Math.round(minutes / 60) + " 小时";
    },
    formatFieldValue(field) {
      if (!field) return "—";
      const unit =
        field.fieldUnit || (field.fieldType === "PERCENT" ? "%" : "");
      return (field.fieldValue || "—") + unit;
    }
  }
};
</script>

<style scoped>
.game-page {
  position: relative;
  width: 100%;
  min-height: 100vh;
  overflow: hidden;
  padding: 112px 18px 72px;
  background: linear-gradient(
      180deg,
      rgba(255, 247, 250, 0.96),
      rgba(232, 252, 247, 0.94)
    ),
    repeating-linear-gradient(
      135deg,
      rgba(239, 93, 168, 0.08) 0,
      rgba(239, 93, 168, 0.08) 1px,
      transparent 1px,
      transparent 18px
    );
  color: #182235;
}
.game-page *,
.game-page *::before,
.game-page *::after {
  box-sizing: border-box;
}
.game-pattern {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background: linear-gradient(
      90deg,
      rgba(20, 184, 166, 0.09) 1px,
      transparent 1px
    ),
    linear-gradient(0deg, rgba(239, 93, 168, 0.08) 1px, transparent 1px);
  background-size: 36px 36px;
  mask-image: linear-gradient(180deg, #000 0%, transparent 80%);
}
.game-shell {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 1180px;
  margin: 0 auto;
}
.game-header {
  display: grid;
  gap: 14px;
  width: 100%;
  max-width: 760px;
  margin-bottom: 24px;
}
.game-kicker {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  width: fit-content;
  padding: 7px 12px;
  border: 1px solid rgba(239, 93, 168, 0.18);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.72);
  color: #be185d;
  font-size: 13px;
  font-weight: 800;
}
.game-header h1 {
  margin: 0;
  color: #172033;
  font-size: 44px;
  font-weight: 900;
  line-height: 1.12;
}
.game-header p {
  max-width: 640px;
  margin: 0;
  color: #64748b;
  font-size: 15px;
  line-height: 1.8;
}
.game-stats {
  display: grid;
  max-width: 560px;
  margin-top: 4px;
  grid-template-columns: repeat(4, minmax(104px, 1fr));
  gap: 10px;
}
.game-stats div {
  padding: 12px 14px;
  border: 1px solid rgba(20, 184, 166, 0.16);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.72);
}
.game-stats strong {
  display: block;
  color: #0f766e;
  font-size: 24px;
  line-height: 1;
}
.game-stats span {
  display: block;
  margin-top: 6px;
  color: #64748b;
  font-size: 12px;
}
.game-controls {
  display: grid;
  gap: 12px;
  margin-bottom: 18px;
}
.game-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}
.game-tabs {
  display: inline-flex;
  padding: 4px;
  border: 1px solid rgba(239, 93, 168, 0.14);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.78);
}
.game-tabs button,
.game-platform-filter button {
  border: 0;
  border-radius: 6px;
  background: transparent;
  color: #64748b;
  cursor: pointer;
  font-weight: 800;
  transition: color 0.2s ease, background 0.2s ease, transform 0.2s ease;
}
.game-tabs button {
  min-width: 76px;
  height: 34px;
  padding: 0 14px;
}
.game-tabs button.active,
.game-tabs button:hover,
.game-platform-filter button.active,
.game-platform-filter button:hover {
  background: #ef5da8;
  color: #fff;
  transform: translateY(-1px);
}
.game-search {
  display: flex;
  width: min(360px, 100%);
  height: 44px;
  align-items: center;
  gap: 8px;
  padding: 0 14px;
  border: 1px solid rgba(20, 184, 166, 0.18);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.82);
}
.game-search input {
  width: 100%;
  border: 0;
  outline: 0;
  background: transparent;
  color: #172033;
  font-size: 14px;
}
.game-platform-filter {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 2px 0 8px;
}
.game-platform-filter button {
  min-height: 32px;
  padding: 0 12px;
  border: 1px solid rgba(20, 184, 166, 0.16);
  background: rgba(255, 255, 255, 0.68);
  font-size: 12px;
}
.game-result-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
  color: #64748b;
  font-size: 13px;
  font-weight: 800;
}
.game-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px;
}
.game-card {
  display: flex;
  overflow: hidden;
  flex-direction: column;
  border: 1px solid rgba(239, 93, 168, 0.13);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.88);
  box-shadow: 0 16px 40px rgba(15, 23, 42, 0.08);
  cursor: pointer;
  outline: 0;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}
.game-card:hover,
.game-card:focus-visible {
  border-color: rgba(239, 93, 168, 0.38);
  box-shadow: 0 20px 50px rgba(239, 93, 168, 0.16);
  transform: translateY(-4px);
}
.game-cover {
  position: relative;
  display: block;
  aspect-ratio: 460 / 215;
  overflow: hidden;
  flex: 0 0 auto;
  background: #e2e8f0;
}
.game-cover::after {
  position: absolute;
  inset: auto 0 0;
  height: 56%;
  pointer-events: none;
  background: linear-gradient(180deg, transparent, rgba(15, 23, 42, 0.72));
  content: "";
}
.game-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.35s ease;
}
.game-card:hover .game-cover img {
  transform: scale(1.05);
}
.game-cover-fallback {
  display: flex;
  width: 100%;
  height: 100%;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 10px;
  padding: 20px;
  background: linear-gradient(
      150deg,
      rgba(255, 228, 239, 0.92),
      rgba(183, 248, 234, 0.88)
    ),
    repeating-linear-gradient(
      45deg,
      rgba(239, 93, 168, 0.12) 0,
      rgba(239, 93, 168, 0.12) 1px,
      transparent 1px,
      transparent 12px
    );
  color: #be185d;
  font-size: 14px;
  font-weight: 900;
  text-align: center;
}
.game-platforms,
.game-status,
.game-playtime {
  position: absolute;
  z-index: 2;
  display: flex;
  align-items: center;
  gap: 5px;
}
.game-platforms {
  top: 10px;
  left: 10px;
}
.game-platforms span,
.game-status,
.game-playtime {
  min-height: 26px;
  padding: 0 9px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 900;
}
.game-platforms span {
  display: inline-flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.9);
  color: #be185d;
}
.game-playtime {
  top: 10px;
  right: 10px;
  background: rgba(240, 253, 250, 0.94);
  box-shadow: 0 4px 12px rgba(15, 118, 110, 0.16);
  color: #0f766e;
}
.game-status {
  right: 10px;
  bottom: 10px;
  background: #8b5cf6;
  color: #fff;
}
.game-status.status-playing {
  background: #14b8a6;
}
.game-status.status-played {
  background: #f59e0b;
}
.game-body {
  display: grid;
  min-width: 0;
  min-height: 238px;
  flex: 1;
  grid-template-rows: 32px 58px minmax(64px, auto) minmax(62px, 1fr);
  gap: 10px;
  padding: 14px;
}
.game-body.without-tags {
  grid-template-rows: 32px minmax(64px, auto) minmax(62px, 1fr);
}
.game-body.without-metrics {
  grid-template-rows: 32px 58px minmax(62px, 1fr);
}
.game-body.without-tags.without-metrics {
  grid-template-rows: 32px minmax(130px, 1fr);
}
.game-title-row {
  display: grid;
  min-width: 0;
  align-items: center;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 10px;
  font-size: 16px;
}
.game-title-main {
  min-width: 0;
}
.game-title-main h2 {
  margin: 0;
  overflow: hidden;
  color: #172033;
  font-size: inherit;
  font-weight: 900;
  line-height: 1.25;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.game-score-card {
  color: #ea580c;
  line-height: 1.25;
}
.game-score-card strong {
  font-size: inherit;
  font-weight: 900;
}
.game-tags {
  display: flex;
  height: 58px;
  align-content: flex-start;
  flex-wrap: wrap;
  gap: 6px;
  overflow: hidden;
}
.game-tags span {
  max-width: 100%;
  height: 26px;
  padding: 0 9px;
  overflow: hidden;
  border: 1px solid transparent;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 800;
  line-height: 24px;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.game-tags .tag-tone-0 {
  border-color: rgba(239, 93, 168, 0.18);
  background: #fff7fb;
  color: #be185d;
}
.game-tags .tag-tone-1 {
  border-color: rgba(20, 184, 166, 0.18);
  background: #f0fdfa;
  color: #0f766e;
}
.game-tags .tag-tone-2 {
  border-color: rgba(139, 92, 246, 0.18);
  background: #f5f3ff;
  color: #7c3aed;
}
.game-tags .tag-tone-3 {
  border-color: rgba(245, 158, 11, 0.2);
  background: #fffbeb;
  color: #b45309;
}
.game-tags .tag-tone-4 {
  border-color: rgba(14, 165, 233, 0.18);
  background: #f0f9ff;
  color: #0369a1;
}
.game-metrics {
  display: grid;
  min-height: 64px;
  grid-template-columns: repeat(3, minmax(0, 1fr));
}
.game-metrics span {
  display: flex;
  box-sizing: border-box;
  width: 100%;
  max-width: 100%;
  min-height: 64px;
  min-width: 0;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 3px;
  padding: 10px 8px;
  font-weight: 800;
  text-align: center;
}
.game-metrics span + span {
  border-left: 1px solid #e2e8f0;
}
.game-metrics small,
.game-metrics strong {
  display: block;
  width: 100%;
  line-height: 1.35;
  overflow-wrap: anywhere;
  white-space: normal;
}
.game-metrics small {
  color: #64748b;
  font-size: 11px;
  font-weight: 700;
}
.game-metrics strong {
  color: #172033;
  font-size: 14px;
  font-weight: 900;
}
.game-body > p {
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
.game-state {
  display: flex;
  min-height: 260px;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #64748b;
  font-weight: 800;
}
.loading-icon {
  animation: spin 1s linear infinite;
}
.game-pagination {
  margin-top: 24px;
}
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
@media (prefers-reduced-motion: reduce) {
  .game-card,
  .game-cover img,
  .game-tabs button,
  .game-platform-filter button {
    transition: none;
  }
}
@media (max-width: 1180px) {
  .game-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}
@media (max-width: 860px) {
  .game-page {
    padding: 92px 14px 48px;
  }
  .game-header h1 {
    font-size: 34px;
  }
  .game-stats {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .game-toolbar {
    align-items: stretch;
    flex-direction: column;
  }
  .game-tabs,
  .game-search {
    width: 100%;
    max-width: 100%;
  }
  .game-tabs {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
  .game-tabs button {
    min-width: 0;
  }
  .game-result-bar {
    align-items: flex-start;
    flex-direction: column;
  }
  .game-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
@media (max-width: 560px) {
  .game-tabs {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .game-grid {
    grid-template-columns: 1fr;
  }
  .game-title-row {
    font-size: 15px;
  }
  .game-body {
    padding: 12px;
  }
}
</style>
