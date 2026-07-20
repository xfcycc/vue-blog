<template>
  <div class="game-detail-page">
    <div v-if="loading" class="detail-state">
      <v-icon class="loading-icon" size="28" color="#0f7c75"
        >mdi-loading</v-icon
      >
      <span>翻开游戏档案中</span>
    </div>

    <div v-else-if="!game" class="detail-state">
      <v-icon size="30" color="#687386">mdi-controller-classic-outline</v-icon>
      <span>这份游戏档案暂时找不到了</span>
      <router-link to="/games">返回游戏列表</router-link>
    </div>

    <main v-else class="detail-shell">
      <router-link to="/games" class="back-link">
        <v-icon size="17" color="currentColor">mdi-arrow-left</v-icon>
        返回游戏列表
      </router-link>

      <section class="detail-hero">
        <div class="hero-media">
          <img
            v-if="game.cover"
            :src="game.cover"
            :alt="game.gameName"
            referrerpolicy="no-referrer"
          />
          <div v-else class="hero-fallback">
            <v-icon size="56" color="#0f7c75"
              >mdi-controller-classic-outline</v-icon
            >
            <span>{{ game.gameName }}</span>
          </div>
          <div class="hero-platforms">
            <span v-for="platform in game.platformList" :key="platform">
              {{ platformName(platform) }}
            </span>
          </div>
        </div>

        <div class="hero-content">
          <h1>{{ game.gameName }}</h1>
          <p>{{ game.gameIntro || "这份游戏档案还没有填写简介。" }}</p>

          <div v-if="game.tagList && game.tagList.length" class="hero-tags">
            <span
              v-for="(tag, index) in game.tagList"
              :key="tag"
              :class="'tag-tone-' + (index % 5)"
            >
              {{ tag }}
            </span>
          </div>

          <div class="hero-data-grid">
            <div class="hero-data-item">
              <span>游玩状态</span>
              <strong
                class="hero-status-value"
                :class="'status-' + String(game.playStatus).toLowerCase()"
              >
                {{ statusName(game.playStatus) }}
              </strong>
            </div>
            <div class="hero-data-item">
              <span>累计游玩</span>
              <strong>{{ formatPlaytime(game.playtimeForever) }}</strong>
            </div>
            <div class="hero-data-item">
              <span>评分</span>
              <div
                class="hero-score-stars"
                :aria-label="'评分 ' + scoreStars + ' 星'"
                :title="scoreStars + ' 星'"
              >
                <v-icon
                  v-for="star in scoreStars"
                  :key="star"
                  size="20"
                  color="#f59e0b"
                >
                  mdi-star
                </v-icon>
              </div>
            </div>
            <div
              v-for="field in fieldList"
              :key="field.id || field.fieldName"
              class="hero-data-item custom-data-item"
            >
              <span>{{ field.fieldName }}</span>
              <a
                v-if="field.fieldType === 'LINK' && safeLink(field.fieldValue)"
                :href="safeLink(field.fieldValue)"
                target="_blank"
                rel="noopener noreferrer"
              >
                {{ field.fieldValue }}
                <v-icon size="14" color="currentColor">mdi-open-in-new</v-icon>
              </a>
              <strong v-else>{{ formatFieldValue(field) }}</strong>
            </div>
          </div>
        </div>
      </section>

      <section v-if="hasScreenshots" class="detail-section screenshot-section">
        <game-screenshot-gallery
          :items="game.screenshotItemList || []"
          :images="game.screenshotList"
          :game-name="game.gameName"
        />
      </section>

      <section v-if="game.reviewContent" class="detail-section review-section">
        <article class="game-review markdown-body" v-html="reviewHtml" />
      </section>
    </main>
  </div>
</template>

<script>
import Clipboard from "clipboard";
import GameScreenshotGallery from "../../components/game/GameScreenshotGallery";
import markdownToHtml from "../../utils/markdown";

export default {
  components: {
    GameScreenshotGallery
  },
  created() {
    this.getGame();
  },
  beforeDestroy() {
    if (this.clipboard) this.clipboard.destroy();
  },
  data() {
    return {
      loading: true,
      game: null,
      clipboard: null,
      platformNames: {
        STEAM: "Steam",
        PSN: "PSN",
        PC: "PC",
        MOBILE: "手游",
        SWITCH: "Switch"
      },
      statusNames: {
        WANT: "想玩",
        PLAYING: "在玩",
        PLAYED: "玩过"
      }
    };
  },
  computed: {
    reviewHtml() {
      return this.game && this.game.reviewContent
        ? markdownToHtml(this.game.reviewContent)
        : "";
    },
    fieldList() {
      return (this.game && this.game.fieldList) || [];
    },
    hasScreenshots() {
      return Boolean(
        this.game &&
          ((this.game.screenshotItemList &&
            this.game.screenshotItemList.length) ||
            (this.game.screenshotList && this.game.screenshotList.length))
      );
    },
    scoreStars() {
      if (!this.game || this.game.personalScore == null) return 0;
      return Math.max(
        0,
        Math.min(5, Math.round(Number(this.game.personalScore) / 2))
      );
    }
  },
  methods: {
    getGame() {
      this.loading = true;
      this.axios
        .post("/api/games/detail", { id: Number(this.$route.params.gameId) })
        .then(({ data }) => {
          this.game = data.flag ? data.data : null;
          if (this.game && this.game.reviewContent) {
            this.$nextTick(() => {
              this.clipboard = new Clipboard(".game-review .copy-btn");
            });
          }
        })
        .catch(() => {
          this.game = null;
        })
        .finally(() => {
          this.loading = false;
        });
    },
    platformName(value) {
      return this.platformNames[value] || value;
    },
    statusName(value) {
      return this.statusNames[value] || "未设置";
    },
    formatPlaytime(minutes) {
      if (!minutes) return "0 小时";
      return Math.round(minutes / 60) + " 小时";
    },
    formatFieldValue(field) {
      const value = field && field.fieldValue ? field.fieldValue : "—";
      const unit =
        field && field.fieldUnit
          ? field.fieldUnit
          : field && field.fieldType === "PERCENT"
          ? "%"
          : "";
      return value + unit;
    },
    safeLink(value) {
      return /^https?:\/\//i.test(value || "") ? value : "";
    }
  }
};
</script>

<style scoped>
.game-detail-page {
  --detail-ink: #111827;
  --detail-muted: #687386;
  --detail-line: #dce6e7;
  --detail-teal: #0f7c75;
  --detail-amber: #f59e0b;
  position: relative;
  box-sizing: border-box;
  width: 100%;
  min-height: 100vh;
  overflow: hidden;
  padding: 108px 24px 88px;
  background: linear-gradient(180deg, #fdfdff 0%, #f8fbfb 56%, #eef9f7 100%);
  color: var(--detail-ink);
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC",
    "Microsoft YaHei", "Noto Sans SC", sans-serif;
}
.game-detail-page *,
.game-detail-page *::before,
.game-detail-page *::after {
  box-sizing: border-box;
}
.detail-shell {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 1280px;
  margin: 0 auto;
}
.back-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 20px;
  color: var(--detail-teal) !important;
  font-size: 14px;
  font-weight: 700;
  line-height: 1;
  text-decoration: none;
  transition: color 0.2s ease, transform 0.2s ease;
}
.back-link:hover {
  color: #0b625d !important;
  transform: translateX(-2px);
}
.detail-hero {
  display: grid;
  min-height: 520px;
  overflow: hidden;
  grid-template-columns: minmax(0, 47%) minmax(0, 53%);
  border: 1px solid #e6ecee;
  border-radius: 14px;
  background: #fff;
  box-shadow: 0 22px 56px rgba(15, 23, 42, 0.1);
}
.hero-media {
  position: relative;
  min-height: 520px;
  overflow: hidden;
  background: #e6ecee;
}
.hero-media img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.hero-fallback {
  display: flex;
  width: 100%;
  height: 100%;
  min-height: 520px;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 14px;
  padding: 32px;
  background: #eef7f5;
  color: var(--detail-teal);
  font-size: 18px;
  font-weight: 800;
  text-align: center;
}
.hero-platforms {
  position: absolute;
  z-index: 2;
  top: 20px;
  left: 18px;
  display: flex;
  max-width: calc(100% - 36px);
  flex-wrap: wrap;
  gap: 8px;
}
.hero-platforms span {
  min-height: 32px;
  padding: 0 13px;
  border: 1px solid rgba(255, 255, 255, 0.86);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 7px 20px rgba(15, 23, 42, 0.14);
  color: var(--detail-ink);
  font-size: 12px;
  font-weight: 800;
  line-height: 30px;
}
.hero-content {
  display: flex;
  min-width: 0;
  justify-content: center;
  flex-direction: column;
  padding: 48px 52px 44px;
}
.hero-content h1 {
  margin: 0;
  color: var(--detail-ink);
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC",
    "Microsoft YaHei", sans-serif;
  font-size: clamp(48px, 5vw, 68px);
  font-weight: 900;
  letter-spacing: -0.055em;
  line-height: 0.98;
  overflow-wrap: anywhere;
}
.hero-content > p {
  display: -webkit-box;
  max-height: 48px;
  margin: 14px 0 0;
  overflow: hidden;
  color: var(--detail-muted);
  font-size: 13px;
  font-weight: 400;
  line-height: 1.7;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}
.hero-tags {
  display: flex;
  margin-top: 20px;
  align-content: flex-start;
  flex-wrap: wrap;
  gap: 7px;
}
.hero-tags span {
  max-width: 100%;
  height: 28px;
  padding: 0 11px;
  overflow: hidden;
  border: 1px solid transparent;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 800;
  line-height: 26px;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.hero-tags .tag-tone-0 {
  border-color: rgba(239, 93, 168, 0.18);
  background: #fff7fb;
  color: #be185d;
}
.hero-tags .tag-tone-1 {
  border-color: rgba(20, 184, 166, 0.18);
  background: #f0fdfa;
  color: #0f766e;
}
.hero-tags .tag-tone-2 {
  border-color: rgba(139, 92, 246, 0.18);
  background: #f5f3ff;
  color: #7c3aed;
}
.hero-tags .tag-tone-3 {
  border-color: rgba(245, 158, 11, 0.2);
  background: #fffbeb;
  color: #b45309;
}
.hero-tags .tag-tone-4 {
  border-color: rgba(14, 165, 233, 0.18);
  background: #f0f9ff;
  color: #0369a1;
}
.hero-tags + .hero-data-grid {
  margin-top: 22px;
}
.hero-data-grid {
  display: grid;
  width: 100%;
  min-width: 0;
  margin-top: 28px;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 20px 12px;
}
.hero-data-item {
  display: flex;
  min-width: 0;
  min-height: 80px;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 8px;
  padding: 16px 12px;
  text-align: center;
}
.hero-data-item:not(:nth-child(3n + 1)) {
  border-left: 1px solid var(--detail-line);
}
.hero-data-item:nth-child(n + 4) {
  border-top: 1px solid var(--detail-line);
}
.hero-data-item > span {
  width: 100%;
  overflow: hidden;
  color: var(--detail-muted);
  font-size: 13px;
  font-weight: 500;
  line-height: 1.2;
  text-align: center;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.hero-data-item strong,
.hero-data-item a {
  display: flex;
  width: 100%;
  min-width: 0;
  align-items: center;
  justify-content: center;
  gap: 4px;
  overflow: hidden;
  color: var(--detail-ink);
  font-size: 20px;
  font-weight: 800;
  line-height: 1.2;
  text-align: center;
  text-decoration: none;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.custom-data-item > span,
.custom-data-item strong,
.custom-data-item a {
  overflow: visible;
  overflow-wrap: anywhere;
  text-overflow: clip;
  white-space: normal;
}
.hero-data-item .hero-status-value,
.hero-data-item .hero-status-value.status-playing,
.hero-data-item .hero-status-value.status-played,
.hero-data-item a {
  color: var(--detail-teal);
}
.hero-score-stars {
  display: flex;
  width: 100%;
  min-height: 19px;
  align-items: center;
  justify-content: center;
  gap: 2px;
}
.detail-section {
  margin-top: 32px;
}
.game-review {
  min-height: 176px;
  padding: 44px 48px;
  border: 1px solid #e6ecee;
  border-radius: 14px;
  background: #fff;
  box-shadow: 0 16px 42px rgba(15, 23, 42, 0.07);
  color: var(--detail-ink);
  font-size: 15px;
  line-height: 1.9;
}
.detail-state {
  position: relative;
  z-index: 1;
  display: flex;
  min-height: calc(100vh - 180px);
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 12px;
  color: var(--detail-muted);
  font-weight: 700;
}
.detail-state a {
  color: var(--detail-teal);
  font-weight: 800;
  text-decoration: none;
}
.loading-icon {
  animation: spin 1s linear infinite;
}
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
@media (prefers-reduced-motion: reduce) {
  .back-link {
    transition: none;
  }
}
@media (max-width: 1100px) {
  .detail-hero,
  .hero-media,
  .hero-fallback {
    min-height: 480px;
  }
  .hero-content {
    padding: 40px 38px;
  }
  .hero-content h1 {
    font-size: 56px;
  }
  .hero-data-item {
    padding-right: 18px;
    padding-left: 18px;
  }
}
@media (max-width: 900px) {
  .game-detail-page {
    padding: 94px 14px 64px;
  }
  .detail-hero {
    min-height: 0;
    grid-template-columns: 1fr;
  }
  .hero-media {
    min-height: 0;
    aspect-ratio: 16 / 9;
  }
  .hero-fallback {
    min-height: 0;
  }
  .hero-content {
    padding: 34px 32px 38px;
  }
  .hero-content h1 {
    font-size: 46px;
  }
}
@media (max-width: 560px) {
  .game-detail-page {
    padding: 88px 14px 48px;
  }
  .back-link {
    margin-bottom: 14px;
    font-size: 13px;
  }
  .detail-hero,
  .game-review {
    border-radius: 12px;
  }
  .hero-platforms {
    top: 14px;
    left: 14px;
  }
  .hero-platforms span {
    min-height: 30px;
    padding: 0 12px;
    line-height: 28px;
  }
  .hero-content {
    padding: 28px 20px 30px;
  }
  .hero-content h1 {
    font-size: 36px;
    line-height: 1;
  }
  .hero-content > p {
    max-height: 44px;
    margin-top: 10px;
    font-size: 12px;
  }
  .hero-data-grid {
    margin-top: 20px;
    gap: 16px 6px;
  }
  .hero-data-item {
    min-height: 72px;
    padding: 14px 10px;
  }
  .hero-data-item > span {
    font-size: 11px;
  }
  .hero-data-item strong,
  .hero-data-item a {
    font-size: 17px;
  }
  .detail-section {
    margin-top: 24px;
  }
  .game-review {
    min-height: 150px;
    padding: 28px 22px;
    font-size: 15px;
  }
}
</style>
