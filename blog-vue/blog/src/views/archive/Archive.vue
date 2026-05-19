<template>
  <div class="archive-page" :style="pageStyle">
    <div class="archive-grid-bg"></div>
    <div class="archive-top-glow"></div>

    <div class="archive-shell">
      <header class="archive-header">
        <div class="archive-command">
          <v-icon size="18" color="#10b981">mdi-console</v-icon>
          <span>{{ displayedCommand }}</span>
          <span
            v-if="typingLine === 'command'"
            :class="['archive-inline-cursor', { hidden: !cursorBlink }]"
          >
            _
          </span>
        </div>
        <h1 class="archive-title">
          {{ displayedTitle
          }}<span
            v-if="displayedTitle || typingLine === 'title'"
            :class="['archive-cursor', { hidden: !cursorBlink }]"
          >
            _
          </span>
        </h1>
        <div v-show="resultVisible" class="archive-meta">
          <span class="archive-status">
            <v-icon size="12" color="#94a3b8">mdi-cpu-64-bit</v-icon>
            STATUS: ONLINE
          </span>
          <span>
            TOTAL_RECORDS:
            <strong>{{ count }}</strong>
          </span>
          <span>
            PAGE:
            <strong>{{ current }}/{{ pageCount }}</strong>
          </span>
          <span>
            RESULT:
            <strong>{{ archiveList.length }}</strong>
            RECORDS_LOADED
          </span>
        </div>
      </header>

      <main :class="['archive-timeline', { ready: articlesVisible }]">
        <div class="archive-line"></div>

        <article
          v-for="(article, index) of decoratedArchiveList"
          :key="article.id"
          :class="[
            'archive-row',
            { reverse: index % 2 === 0, active: hoveredId === article.id }
          ]"
          @mouseenter="hoveredId = article.id"
          @mouseleave="hoveredId = null"
        >
          <div class="archive-spacer"></div>
          <div class="archive-node">
            <span></span>
          </div>

          <div class="archive-card-wrap">
            <div class="archive-connector"></div>
            <router-link
              :to="'/articles/' + article.id"
              class="archive-card"
              :style="{ '--accent': article.accent }"
            >
              <div class="archive-cover">
                <div class="archive-scanline"></div>
                <div class="archive-color-mask"></div>
                <div class="archive-dark-mask"></div>
                <img :src="article.image" :alt="article.articleTitle" />
                <span class="archive-img-id">
                  IMG_{{ formatImageId(article.id) }}
                </span>
                <span class="archive-decoded">&gt; DATA_DECODED</span>
              </div>

              <div class="archive-card-meta">
                <span class="archive-icon">
                  <v-icon size="16" :color="article.accent">
                    {{ article.icon }}
                  </v-icon>
                </span>
                <time>{{ article.createTime | date }}</time>
                <span class="archive-index">
                  #{{ String(article.id).padStart(3, "0") }}
                </span>
              </div>

              <h2>{{ article.articleTitle }}</h2>

              <div class="archive-read">
                <span>&gt;</span>
                READ_ARTICLE
                <v-icon size="14" color="#34d399">mdi-chevron-right</v-icon>
              </div>
            </router-link>
          </div>
        </article>

        <div class="archive-eof">[EOF]</div>
      </main>

      <v-pagination
        v-show="articlesVisible"
        class="archive-pagination"
        color="#10b981"
        dark
        v-model="current"
        :length="pageCount"
        total-visible="7"
      />
    </div>
  </div>
</template>

<script>
const archiveBackground = "https://pic.caiguoyu.cn/20260519195307471.png";

const ARCHIVE_TITLE_TEXT = "System.Logs";
const ARCHIVE_PAGE_SIZE = 10;

const ARTICLE_META_LIST = [
  { accent: "#34d399", icon: "mdi-database-outline" },
  { accent: "#22d3ee", icon: "mdi-server-network" },
  { accent: "#fb923c", icon: "mdi-console-line" },
  { accent: "#60a5fa", icon: "mdi-harddisk" },
  { accent: "#c084fc", icon: "mdi-server" },
  { accent: "#fb7185", icon: "mdi-shield-lock-outline" },
  { accent: "#22d3ee", icon: "mdi-docker" },
  { accent: "#facc15", icon: "mdi-flash-outline" },
  { accent: "#818cf8", icon: "mdi-database-search-outline" }
];

export default {
  mounted() {
    document.body.classList.add("archive-route-bg");
    this.cursorTimer = window.setInterval(() => {
      this.cursorBlink = !this.cursorBlink;
    }, 530);
    this.runArchiveSequence(false);
  },
  beforeDestroy() {
    document.body.classList.remove("archive-route-bg");
    window.clearInterval(this.cursorTimer);
    this.clearHeaderTyping();
  },
  data: function() {
    return {
      current: 1,
      count: 0,
      archiveList: [],
      hoveredId: null,
      cursorBlink: true,
      cursorTimer: null,
      displayedCommand: "",
      displayedTitle: "",
      typingLine: "",
      typingTimers: [],
      resultVisible: false,
      articlesVisible: false,
      sequenceId: 0
    };
  },
  methods: {
    listArchives() {
      return this.axios
        .get("/api/articles/archives", {
          params: { current: this.current }
        })
        .then(({ data }) => {
          this.archiveList = data.data.recordList;
          this.count = data.data.count;
        });
    },
    getArticleMeta(article, index) {
      const title = article.articleTitle || "";
      if (/kafka|mysql|mongo|aggregation|数据库/i.test(title)) {
        return ARTICLE_META_LIST[0];
      }
      if (/nginx|服务器|server/i.test(title)) {
        return ARTICLE_META_LIST[1];
      }
      if (/docker|jenkins/i.test(title)) {
        return ARTICLE_META_LIST[6];
      }
      if (/linux|debian|硬盘|虚拟机/i.test(title)) {
        return ARTICLE_META_LIST[3];
      }
      if (/python|脚本|script/i.test(title)) {
        return ARTICLE_META_LIST[7];
      }
      return ARTICLE_META_LIST[index % ARTICLE_META_LIST.length];
    },
    formatImageId(id) {
      return (Number(id) * 1024).toString(16).toUpperCase();
    },
    getArticleImage(article) {
      return article.articleCover || archiveBackground;
    },
    runArchiveSequence(shouldScrollTop) {
      const sequenceId = this.sequenceId + 1;
      this.sequenceId = sequenceId;
      this.resultVisible = false;
      this.articlesVisible = false;
      this.hoveredId = null;
      if (shouldScrollTop) {
        window.scrollTo({ top: 0, behavior: "smooth" });
      }
      const archivesPromise = this.listArchives();
      const typingPromise = this.startHeaderTyping();
      Promise.all([archivesPromise, typingPromise]).then(() => {
        if (sequenceId !== this.sequenceId) {
          return;
        }
        this.resultVisible = true;
        const timer = window.setTimeout(() => {
          if (sequenceId === this.sequenceId) {
            this.articlesVisible = true;
          }
        }, 320);
        this.typingTimers.push(timer);
      });
    },
    startHeaderTyping() {
      this.clearHeaderTyping();
      this.displayedCommand = "";
      this.displayedTitle = "";
      return new Promise(resolve => {
        this.typeText("command", this.archiveCommand, 28, () => {
          const timer = window.setTimeout(() => {
            this.typeText("title", ARCHIVE_TITLE_TEXT, 72, resolve);
          }, 220);
          this.typingTimers.push(timer);
        });
      });
    },
    typeText(line, text, speed, done) {
      this.typingLine = line;
      let index = 0;
      const writeNext = () => {
        index++;
        if (line === "command") {
          this.displayedCommand = text.slice(0, index);
        } else {
          this.displayedTitle = text.slice(0, index);
        }
        if (index < text.length) {
          const timer = window.setTimeout(writeNext, speed);
          this.typingTimers.push(timer);
          return;
        }
        this.typingLine = "";
        if (done) {
          done();
        }
      };
      writeNext();
    },
    clearHeaderTyping() {
      this.typingTimers.forEach(timer => {
        window.clearTimeout(timer);
      });
      this.typingTimers = [];
      this.typingLine = "";
    }
  },
  computed: {
    archiveCommand() {
      return (
        "root@server:~/blog/timeline $ ./fetch_logs.sh --page=" +
        this.current +
        " --size=" +
        ARCHIVE_PAGE_SIZE
      );
    },
    decoratedArchiveList() {
      return this.archiveList.map((article, index) => {
        return Object.assign({}, article, this.getArticleMeta(article, index), {
          image: this.getArticleImage(article)
        });
      });
    },
    pageStyle() {
      return (
        "background-image: linear-gradient(rgba(3, 7, 18, 0.58), rgba(3, 7, 18, 0.76)), url(" +
        archiveBackground +
        ")"
      );
    },
    pageCount() {
      return Math.max(Math.ceil(this.count / ARCHIVE_PAGE_SIZE), 1);
    }
  },
  watch: {
    current() {
      this.runArchiveSequence(true);
    }
  }
};
</script>

<style scoped>
.archive-page {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  padding: 112px 16px 72px;
  background: #0a0a0a;
  background-position: center top;
  background-size: cover;
  background-attachment: fixed;
  color: #cbd5e1;
}
.archive-grid-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background-image: linear-gradient(
      to right,
      rgba(128, 128, 128, 0.07) 1px,
      transparent 1px
    ),
    linear-gradient(to bottom, rgba(128, 128, 128, 0.07) 1px, transparent 1px);
  background-size: 24px 24px;
}
.archive-top-glow {
  position: absolute;
  top: -180px;
  left: 50%;
  width: min(800px, 90vw);
  height: 400px;
  pointer-events: none;
  background: rgba(6, 78, 59, 0.22);
  border-radius: 50%;
  filter: blur(120px);
  transform: translateX(-50%);
}
.archive-shell {
  position: relative;
  z-index: 1;
  max-width: 896px;
  margin: 0 auto;
}
.archive-header {
  margin: 32px 0 80px;
  padding-bottom: 32px;
  border-bottom: 1px solid #1e293b;
}
.archive-command {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  color: #10b981;
  font-family: "SFMono-Regular", Consolas, "Liberation Mono", monospace !important;
  font-size: 14px;
}
.archive-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 16px;
  color: #fff;
  font-size: clamp(32px, 7vw, 56px);
  font-weight: 900;
  letter-spacing: 0;
  line-height: 1.08;
}
.archive-cursor {
  color: #10b981;
  transition: opacity 0.15s ease;
}
.archive-inline-cursor {
  color: #10b981;
  transition: opacity 0.15s ease;
}
.archive-cursor.hidden {
  opacity: 0;
}
.archive-inline-cursor.hidden {
  opacity: 0;
}
.archive-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  color: #64748b;
  font-family: "SFMono-Regular", Consolas, "Liberation Mono", monospace !important;
  font-size: 12px;
}
.archive-meta strong {
  color: #34d399;
}
.archive-status {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  background: #0f172a;
  border: 1px solid #1e293b;
  border-radius: 4px;
}
.archive-timeline {
  position: relative;
  padding-left: 4px;
  max-height: 0;
  overflow: hidden;
  opacity: 0;
  transform: translateY(18px);
  transition: max-height 0.9s ease, opacity 0.45s ease, transform 0.45s ease;
}
.archive-timeline.ready {
  max-height: 6000px;
  opacity: 1;
  transform: translateY(0);
}
.archive-line {
  position: absolute;
  top: 0;
  bottom: 0;
  left: 16px;
  border-left: 2px dashed rgba(16, 185, 129, 0.28);
}
.archive-row {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  margin-bottom: 40px;
}
.archive-spacer {
  display: none;
}
.archive-node {
  position: absolute;
  left: 16px;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  transform: translateX(-50%);
}
.archive-node span {
  width: 10px;
  height: 10px;
  background: #0a0a0a;
  border: 1px solid rgba(16, 185, 129, 0.55);
  transform: rotate(45deg);
  transition: all 0.3s ease;
}
.archive-row.active .archive-node span {
  background: #34d399;
  border-color: #6ee7b7;
  box-shadow: 0 0 12px rgba(52, 211, 153, 0.8);
  transform: rotate(45deg) scale(1.5);
}
.archive-card-wrap {
  position: relative;
  width: calc(100% - 40px);
  margin-left: 40px;
}
.archive-connector {
  display: none;
}
.archive-card {
  display: block;
  padding: 20px;
  overflow: hidden;
  background: #111;
  border: 1px solid rgba(30, 41, 59, 0.8);
  border-radius: 8px;
  color: #e2e8f0 !important;
  cursor: pointer;
  text-decoration: none;
  transition: all 0.3s ease;
}
.archive-card:hover {
  background: #161616;
  border-color: color-mix(in srgb, var(--accent) 45%, transparent);
  box-shadow: 0 0 15px color-mix(in srgb, var(--accent) 15%, transparent);
  transform: translateY(-2px);
}
.archive-cover {
  position: relative;
  width: 100%;
  height: 160px;
  margin-bottom: 20px;
  overflow: hidden;
  background: #0f172a;
  border: 1px solid rgba(30, 41, 59, 0.8);
  border-radius: 4px;
  transition: border-color 0.5s ease;
}
.archive-card:hover .archive-cover {
  border-color: rgba(16, 185, 129, 0.4);
}
.archive-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  filter: grayscale(1) contrast(1.25);
  transform: scale(1);
  transition: all 0.7s ease;
}
.archive-card:hover .archive-cover img {
  filter: grayscale(0.2) contrast(1.1);
  transform: scale(1.1);
}
.archive-scanline,
.archive-color-mask,
.archive-dark-mask {
  position: absolute;
  inset: 0;
  z-index: 2;
  pointer-events: none;
}
.archive-scanline {
  z-index: 4;
  opacity: 0.6;
  background-image: linear-gradient(
      rgba(0, 0, 0, 0) 50%,
      rgba(0, 0, 0, 0.25) 50%
    ),
    linear-gradient(
      90deg,
      rgba(255, 0, 0, 0.06),
      rgba(0, 255, 0, 0.02),
      rgba(0, 0, 255, 0.06)
    );
  background-size: 100% 4px, 3px 100%;
  transition: opacity 0.5s ease;
}
.archive-card:hover .archive-scanline {
  opacity: 0.2;
}
.archive-color-mask {
  background: rgba(5, 150, 105, 0.4);
  mix-blend-mode: color;
  transition: opacity 0.5s ease;
}
.archive-dark-mask {
  background: rgba(10, 10, 10, 0.7);
  transition: opacity 0.5s ease;
}
.archive-card:hover .archive-color-mask,
.archive-card:hover .archive-dark-mask {
  opacity: 0;
}
.archive-img-id,
.archive-decoded {
  position: absolute;
  z-index: 5;
  font-family: "SFMono-Regular", Consolas, "Liberation Mono", monospace !important;
  font-size: 9px;
}
.archive-img-id {
  top: 8px;
  left: 8px;
  padding: 2px 6px;
  color: #64748b;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 2px;
}
.archive-decoded {
  right: 8px;
  bottom: 8px;
  padding: 2px 8px;
  color: #34d399;
  background: rgba(0, 0, 0, 0.9);
  border: 1px solid rgba(16, 185, 129, 0.3);
  opacity: 0;
  transform: translateY(8px);
  transition: all 0.3s ease 0.1s;
}
.archive-card:hover .archive-decoded {
  opacity: 1;
  transform: translateY(0);
}
.archive-card-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  color: #94a3b8;
  font-family: "SFMono-Regular", Consolas, "Liberation Mono", monospace !important;
  font-size: 13px;
  letter-spacing: 0.04em;
}
.archive-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  background: rgba(15, 23, 42, 0.5);
  border: 1px solid #1e293b;
  border-radius: 6px;
}
.archive-index {
  margin-left: auto;
  color: #334155;
  font-size: 10px;
  font-weight: 700;
}
.archive-card h2 {
  margin: 0;
  color: #e2e8f0;
  font-size: 16px;
  font-weight: 500;
  line-height: 1.7;
  transition: color 0.3s ease;
}
.archive-card:hover h2 {
  color: #fff;
}
.archive-read {
  display: flex;
  align-items: center;
  max-height: 0;
  margin-top: 0;
  overflow: hidden;
  color: rgba(52, 211, 153, 0.8);
  border-top: 1px solid rgba(30, 41, 59, 0.5);
  opacity: 0;
  font-family: "SFMono-Regular", Consolas, "Liberation Mono", monospace !important;
  font-size: 12px;
  transition: all 0.3s ease;
}
.archive-read span {
  margin-right: 8px;
}
.archive-card:hover .archive-read {
  max-height: 40px;
  margin-top: 16px;
  padding-top: 8px;
  opacity: 1;
}
.archive-eof {
  position: relative;
  z-index: 2;
  width: fit-content;
  margin: 48px 0 32px 16px;
  color: #475569;
  font-family: "SFMono-Regular", Consolas, "Liberation Mono", monospace !important;
  font-size: 14px;
  transform: translateX(-50%);
}
.archive-pagination {
  margin-top: 16px;
}

@media (min-width: 760px) {
  .archive-page {
    padding-top: 128px;
  }
  .archive-timeline {
    padding-left: 0;
  }
  .archive-line {
    left: 50%;
    transform: translateX(-50%);
  }
  .archive-row.reverse {
    flex-direction: row-reverse;
  }
  .archive-spacer {
    display: block;
    width: 41.666667%;
  }
  .archive-node {
    left: 50%;
  }
  .archive-card-wrap {
    width: 41.666667%;
    margin-left: 0;
  }
  .archive-connector {
    position: absolute;
    top: 50%;
    display: block;
    width: 24px;
    border-bottom: 1px dashed #334155;
    transform: translateY(-50%);
    transition: border-color 0.3s ease;
  }
  .archive-row:not(.reverse) .archive-connector {
    right: -24px;
  }
  .archive-row.reverse .archive-connector {
    left: -24px;
  }
  .archive-row.active .archive-connector {
    border-color: rgba(16, 185, 129, 0.55);
  }
  .archive-eof {
    margin-left: auto;
    margin-right: auto;
    transform: none;
  }
}

@media (max-width: 759px) {
  .archive-page {
    padding: 92px 12px 48px;
  }
  .archive-header {
    margin: 16px 0 56px;
  }
  .archive-command {
    align-items: flex-start;
    font-size: 12px;
    line-height: 1.6;
  }
  .archive-cover {
    height: 132px;
  }
  .archive-card {
    padding: 16px;
  }
  .archive-card-meta {
    gap: 8px;
    font-size: 12px;
  }
}
</style>

<style>
body.archive-route-bg .theme--light.v-application,
body.archive-route-bg .theme--dark.v-application {
  background-color: #0a0a0a;
  background-image: linear-gradient(rgba(3, 7, 18, 0.62), rgba(3, 7, 18, 0.82)),
    url("https://pic.caiguoyu.cn/20260519195307471.png");
  background-position: center top;
  background-size: cover;
  background-attachment: fixed;
}
</style>
