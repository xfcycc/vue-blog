<template>
  <div :class="{ 'category-article-page': isCategoryMode }" :style="pageStyle">
    <!-- 标签或分类名 -->
    <div
      class="banner article-list-banner"
      :class="{ 'category-article-hero': isCategoryMode }"
      :style="cover"
    >
      <div :class="heroInnerClass">
        <div v-if="isCategoryMode" class="category-article-kicker">
          <v-icon color="#000" size="16">mdi-broadcast</v-icon>
          CATEGORY CHANNEL
        </div>
        <h1 :class="heroTitleClass">{{ heroTitle }}</h1>
        <div v-if="isCategoryMode" class="category-article-subtitle">
          {{ name || "加载分类中" }} · {{ articleList.length }} 篇文章
        </div>
      </div>
      <div v-if="isCategoryMode" class="category-article-marquee">
        <div class="category-article-marquee-track">
          <span v-for="item in articleTitleMarquee" :key="item">
            {{ item }}
          </span>
          <span v-for="item in articleTitleMarquee" :key="item + '-copy'">
            {{ item }}
          </span>
        </div>
      </div>
    </div>
    <div class="article-list-wrapper">
      <router-link
        v-if="isCategoryMode"
        to="/categories"
        class="category-back-link"
        aria-label="返回分类"
      >
        <v-icon color="#000" size="22">mdi-arrow-left</v-icon>
      </router-link>
      <div v-if="isCategoryMode" class="category-article-grid">
        <v-card
          v-for="(item, index) of articleList"
          :key="item.id"
          class="category-item-card"
          :class="getBentoClass(index)"
          :style="getArticleCardStyle(item)"
        >
          <span class="category-bg-id">{{ getArticleId(index) }}</span>
          <router-link
            :to="'/articles/' + item.id"
            class="category-card-link article-card-link"
          >
            <div class="category-card-top">
              <span class="category-action">
                <v-icon size="20">mdi-arrow-top-right</v-icon>
              </span>
            </div>
            <div class="category-item-info article-card-info">
              <div class="category-name article-card-title">
                {{ item.articleTitle }}
              </div>
              <div class="category-count article-card-date">
                {{ item.createTime | date }}
              </div>
            </div>
          </router-link>
        </v-card>
      </div>
      <v-row v-else>
        <v-col md="4" cols="12" v-for="item of articleList" :key="item.id">
          <!-- 文章 -->
          <v-card class="animated zoomIn article-item-card">
            <div class="article-item-cover">
              <router-link :to="'/articles/' + item.id">
                <!-- 缩略图 -->
                <v-img
                  class="on-hover"
                  width="100%"
                  height="100%"
                  :src="item.articleCover"
                />
              </router-link>
            </div>
            <div class="article-item-info">
              <!-- 文章标题 -->
              <div class="article-item-title">
                <router-link
                  :to="'/articles/' + item.id"
                  class="article-title-link"
                >
                  {{ item.articleTitle }}
                </router-link>
              </div>
              <div class="article-item-meta">
                <!-- 发表时间 -->
                <v-icon size="20">mdi-clock-outline</v-icon>
                {{ item.createTime | date }}
                <!-- 文章分类 -->
                <router-link
                  :to="'/categories/' + item.categoryId"
                  class="float-right"
                >
                  <v-icon>mdi-bookmark</v-icon>{{ item.categoryName }}
                </router-link>
              </div>
            </div>
            <!-- 分割线 -->
            <v-divider></v-divider>
            <!-- 文章标签 -->
            <div class="tag-wrapper">
              <router-link
                :to="'/tags/' + tag.id"
                class="tag-btn"
                v-for="tag of item.tagDTOList"
                :key="tag.id"
              >
                {{ tag.tagName }}
              </router-link>
            </div>
          </v-card>
        </v-col>
      </v-row>
      <!-- 无限加载 -->
      <infinite-loading @infinite="infiniteHandler">
        <div slot="no-results" />
        <div slot="no-more" />
      </infinite-loading>
    </div>
  </div>
</template>

<script>
const CATEGORY_COVER = "https://pic.caiguoyu.cn/20260518204951097.jpg";

export default {
  created() {
    const path = this.$route.path;
    if (path.indexOf("/categories") != -1) {
      this.title = "分类";
    } else {
      this.title = "标签";
    }
  },
  data: function() {
    return {
      current: 1,
      size: 10,
      articleList: [],
      name: "",
      title: "",
      marqueeList: [
        "// 暂无文章",
        "// 等待信号",
        "// CATEGORY CHANNEL",
        "// 文章加载中"
      ]
    };
  },
  methods: {
    infiniteHandler($state) {
      this.axios
        .get("/api/articles/condition", {
          params: {
            categoryId: this.$route.params.categoryId,
            tagId: this.$route.params.tagId,
            current: this.current,
            size: this.isCategoryMode ? 200 : this.size
          }
        })
        .then(({ data }) => {
          if (data.data.name) {
            this.name = data.data.name;
            document.title = this.title + " - " + this.name;
          }
          if (data.data.articlePreviewDTOList.length) {
            this.current++;
            this.articleList.push(...data.data.articlePreviewDTOList);
            $state.loaded();
          } else {
            $state.complete();
          }
        });
    },
    getArticleId(index) {
      return String(index + 1).padStart(2, "0");
    },
    getBentoClass(index) {
      const classList = [
        "bento-large",
        "bento-small",
        "bento-tall",
        "bento-small",
        "bento-wide",
        "bento-small",
        "bento-small",
        "bento-wide"
      ];
      return classList[index % classList.length];
    },
    getArticleCardStyle(item) {
      return {
        backgroundImage:
          "linear-gradient(135deg, rgba(2, 6, 23, 0.28), rgba(2, 6, 23, 0.82)), url(" +
          item.articleCover +
          ")",
        backgroundPosition: "center",
        backgroundSize: "cover"
      };
    }
  },
  computed: {
    isCategoryMode() {
      return this.$route.path.indexOf("/categories") != -1;
    },
    pageStyle() {
      if (!this.isCategoryMode) {
        return "";
      }
      return (
        "background: linear-gradient(rgba(3, 6, 20, 0.76), rgba(6, 0, 18, 0.88)), url(" +
        CATEGORY_COVER +
        ") center top / cover fixed no-repeat"
      );
    },
    heroTitle() {
      if (this.isCategoryMode) {
        return this.name || "Categories";
      }
      return this.title + " - " + this.name;
    },
    heroInnerClass() {
      return this.isCategoryMode
        ? "category-article-hero-inner"
        : "article-list-hero-inner";
    },
    heroTitleClass() {
      return this.isCategoryMode
        ? "category-article-title"
        : "banner-title animated fadeInDown";
    },
    articleTitleMarquee() {
      if (!this.articleList.length) {
        return this.marqueeList;
      }
      return this.articleList.map(item => "// " + item.articleTitle);
    },
    cover() {
      if (this.isCategoryMode) {
        return (
          "background: linear-gradient(115deg, rgba(8, 3, 30, 0.38), rgba(250, 0, 130, 0.2) 45%, rgba(0, 245, 255, 0.22)), url(" +
          CATEGORY_COVER +
          ") center 46% / cover no-repeat"
        );
      }
      var cover = "";
      this.$store.state.blogInfo.pageList.forEach(item => {
        if (item.pageLabel == "articleList") {
          cover = item.pageCover;
        }
      });
      return "background: url(" + cover + ") center center / cover no-repeat";
    }
  }
};
</script>

<style scoped>
@media (min-width: 760px) {
  .article-list-wrapper {
    max-width: 1106px;
    margin: 370px auto 1rem auto;
  }
  .category-article-page .article-list-wrapper {
    max-width: none;
    width: 100%;
    margin: 350px 0 0;
    padding: 58px 36px 88px;
    overflow: hidden;
    position: relative;
    z-index: 1;
    background: linear-gradient(
      180deg,
      rgba(3, 6, 20, 0.94) 0%,
      rgba(8, 3, 30, 0.78) 42%,
      rgba(6, 0, 18, 0.86) 100%
    );
    box-shadow: inset 0 38px 46px rgba(0, 245, 255, 0.12);
  }
  .category-article-page .article-list-wrapper:before,
  .category-article-page .article-list-wrapper:after {
    content: "";
    position: absolute;
    inset: 0;
    pointer-events: none;
  }
  .category-article-page .article-list-wrapper:before {
    opacity: 0.72;
    background-image: radial-gradient(
        circle at 12% 16%,
        rgba(255, 43, 214, 0.34),
        transparent 24%
      ),
      radial-gradient(
        circle at 86% 22%,
        rgba(0, 245, 255, 0.32),
        transparent 22%
      ),
      linear-gradient(rgba(0, 245, 255, 0.12) 1px, transparent 1px),
      linear-gradient(90deg, rgba(255, 43, 214, 0.1) 1px, transparent 1px);
    background-size: auto, auto, 54px 54px, 54px 54px;
  }
  .category-article-page .article-list-wrapper:after {
    opacity: 0.28;
    background: repeating-linear-gradient(
      180deg,
      rgba(255, 255, 255, 0.12) 0,
      rgba(255, 255, 255, 0.12) 1px,
      transparent 1px,
      transparent 8px
    );
    mix-blend-mode: screen;
    animation: cyber-scan 8s linear infinite;
  }
  .category-article-grid {
    display: grid;
    grid-auto-flow: dense;
    grid-auto-rows: 260px;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 28px;
    position: relative;
    z-index: 1;
  }
  .bento-large {
    grid-column: span 2;
    grid-row: span 2;
  }
  .bento-wide {
    grid-column: span 1;
  }
  .bento-tall {
    grid-row: span 2;
  }
  .category-item-card:hover {
    box-shadow: 4px 4px 0 #000 !important;
    transform: translate(4px, 4px);
  }
  .category-item-card:not(:hover) {
    transform: translate(0, 0);
  }
  .article-item-card:hover {
    transition: all 0.3s;
    box-shadow: 0 4px 12px 12px rgba(7, 17, 27, 0.15);
  }
  .article-item-card:not(:hover) {
    transition: all 0.3s;
  }
  .article-item-card:hover .on-hover {
    transition: all 0.6s;
    transform: scale(1.1);
  }
  .article-item-card:not(:hover) .on-hover {
    transition: all 0.6s;
  }
  .article-item-info {
    line-height: 1.7;
    padding: 15px 15px 12px 18px;
    font-size: 15px;
  }
  .category-article-page .article-item-info {
    min-height: 134px;
    padding: 18px 18px 16px;
  }
}
@media (max-width: 759px) {
  .article-list-wrapper {
    margin-top: 230px;
    padding: 0 12px;
  }
  .category-article-hero-inner {
    padding: 98px 14px 48px;
  }
  .category-article-title {
    font-size: 46px;
  }
  .category-article-subtitle {
    font-size: 16px;
  }
  .category-article-page .article-list-wrapper {
    margin-top: 220px;
    padding: 28px 14px 64px;
    overflow: hidden;
    position: relative;
    z-index: 1;
    background: linear-gradient(
      180deg,
      rgba(3, 6, 20, 0.94),
      rgba(6, 0, 18, 0.84)
    );
  }
  .category-article-page .article-list-wrapper:before {
    content: "";
    position: absolute;
    inset: 0;
    opacity: 0.62;
    pointer-events: none;
    background-image: radial-gradient(
        circle at 20% 10%,
        rgba(255, 43, 214, 0.28),
        transparent 28%
      ),
      radial-gradient(
        circle at 82% 24%,
        rgba(0, 245, 255, 0.26),
        transparent 26%
      ),
      linear-gradient(rgba(0, 245, 255, 0.12) 1px, transparent 1px),
      linear-gradient(90deg, rgba(255, 43, 214, 0.1) 1px, transparent 1px);
    background-size: auto, auto, 42px 42px, 42px 42px;
  }
  .category-article-grid {
    display: grid;
    gap: 18px;
    position: relative;
    z-index: 1;
  }
  .category-back-link {
    left: 14px;
    top: 20px;
  }
  .article-card-link {
    min-height: 260px;
  }
  .article-card-title {
    font-size: 30px;
  }
  .article-item-info {
    line-height: 1.7;
    padding: 15px 15px 12px 18px;
  }
}
.category-article-page {
  min-height: 100vh;
  padding-bottom: 80px;
  background-color: #080315;
}
.article-list-banner {
  overflow: hidden;
}
.article-list-hero-inner {
  text-align: center;
}
.category-article-hero {
  z-index: 1;
  box-shadow: inset 0 -80px 90px rgba(3, 6, 20, 0.62);
}
.category-back-link {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 72px;
  height: 34px;
  position: absolute;
  top: 10px;
  left: 36px;
  border: 2px solid #0ff;
  border-radius: 8px;
  background: #ffc900;
  box-shadow: 4px 4px 0 #000, 0 0 22px rgba(255, 201, 0, 0.62);
  color: #000 !important;
  z-index: 2;
}
.category-back-link:hover {
  color: #000 !important;
  transform: translate(3px, 3px);
  box-shadow: 1px 1px 0 #000, 0 0 24px rgba(0, 245, 255, 0.72);
}
.category-article-hero-inner {
  max-width: 1106px;
  margin: 0 auto;
  padding: 120px 16px 76px;
  text-align: center;
}
.category-article-kicker {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 24px;
  padding: 8px 16px;
  border: 2px solid #0ff;
  border-radius: 999px;
  background: #ff2bd6;
  box-shadow: 4px 4px 0 #000, 0 0 24px rgba(255, 43, 214, 0.8);
  color: #000;
  font-size: 14px;
  font-weight: 900;
  cursor: default;
}
.category-article-title {
  margin: 0;
  color: #fff;
  font-size: 82px;
  font-weight: 900;
  letter-spacing: 0;
  line-height: 0.9;
  text-shadow: 4px 4px 0 #000, -4px 0 0 #ff2bd6, 4px 0 0 #00f5ff,
    0 0 28px rgba(0, 245, 255, 0.68);
  text-transform: uppercase;
  cursor: default;
}
.category-article-subtitle {
  display: inline-block;
  margin-top: 24px;
  padding: 4px 16px;
  background: #000;
  color: #fff;
  font-size: 22px;
  font-weight: 900;
  transform: rotate(-1deg);
  box-shadow: 5px 5px 0 #ff2bd6, -5px -5px 0 #00f5ff;
  cursor: default;
}
.category-article-kicker:hover,
.category-article-title:hover,
.category-article-subtitle:hover,
.category-article-hero-inner:hover .category-article-kicker,
.category-article-hero-inner:hover .category-article-title,
.category-article-hero-inner:hover .category-article-subtitle {
  animation: cyber-title-shake 0.38s steps(2, end) infinite;
}
.category-article-marquee {
  position: absolute;
  right: 0;
  bottom: 0;
  left: 0;
  overflow: hidden;
  border-top: 4px solid #00f5ff;
  border-bottom: 4px solid #ff2bd6;
  background: #030614;
  white-space: nowrap;
  box-shadow: 0 0 26px rgba(0, 245, 255, 0.42);
}
.category-article-marquee-track {
  display: inline-flex;
  width: max-content;
  animation: category-marquee 36s linear infinite;
}
.category-article-marquee-track span {
  display: inline-flex;
  align-items: center;
  padding: 8px 0;
  margin-right: 26px;
  color: #00f5ff;
  font-size: 20px;
  font-weight: 900;
  text-shadow: 2px 0 #ff2bd6, 0 0 14px rgba(0, 245, 255, 0.76);
}
.article-item-card {
  border-radius: 8px !important;
  box-shadow: 0 4px 8px 6px rgba(7, 17, 27, 0.06);
}
.category-item-card {
  border: 4px solid #000;
  border-radius: 8px !important;
  box-shadow: 8px 8px 0 #000, 0 0 22px rgba(0, 245, 255, 0.28) !important;
  overflow: hidden;
  position: relative;
  transition: all 0.2s ease-out;
}
.category-item-card:before {
  content: "";
  position: absolute;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  background: radial-gradient(
      circle at 82% 20%,
      rgba(255, 43, 214, 0.34),
      transparent 28%
    ),
    linear-gradient(180deg, rgba(2, 6, 23, 0.18), rgba(2, 6, 23, 0.86));
}
.category-item-card:after {
  content: "";
  position: absolute;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  opacity: 0.22;
  background: repeating-linear-gradient(
    180deg,
    rgba(255, 255, 255, 0.18) 0,
    rgba(255, 255, 255, 0.18) 1px,
    transparent 1px,
    transparent 7px
  );
}
.category-item-card:nth-child(6n + 1) {
  background-color: #ff90e8;
}
.category-item-card:nth-child(6n + 2) {
  background-color: #ffc900;
}
.category-item-card:nth-child(6n + 3) {
  background-color: #00e5ff;
}
.category-item-card:nth-child(6n + 4) {
  background-color: #8a2be2;
}
.category-item-card:nth-child(6n + 5) {
  background-color: #ff4d4d;
}
.category-item-card:nth-child(6n) {
  background-color: #00ff00;
}
.category-card-link {
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  height: 100%;
  justify-content: flex-end;
  padding: 28px;
  position: relative;
  z-index: 1;
}
.category-bg-id {
  position: absolute;
  right: -8px;
  bottom: -26px;
  z-index: 1;
  color: rgba(255, 255, 255, 0.16);
  font-size: 150px;
  font-weight: 900;
  letter-spacing: 0;
  line-height: 0.8;
  pointer-events: none;
  text-shadow: 3px 3px 0 rgba(0, 0, 0, 0.46);
}
.category-card-top {
  display: flex;
  align-items: flex-start;
  justify-content: flex-end;
  position: absolute;
  top: 24px;
  right: 24px;
  z-index: 2;
}
.category-action {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  flex: 0 0 auto;
  border: 2px solid #000;
  border-radius: 50%;
  background: #000;
  color: #fff;
  transition: transform 0.3s;
}
.category-item-card:hover .category-action {
  transform: rotate(45deg);
}
.article-card-link {
  color: inherit !important;
}
.category-item-info {
  line-height: 1.7;
  position: relative;
  z-index: 2;
}
.category-name {
  color: #fff;
  font-family: "Arial Black", "PingFang SC", "Microsoft YaHei", sans-serif;
  font-size: clamp(30px, 3vw, 48px);
  font-weight: 900;
  line-height: 1.02;
  transition: transform 0.3s;
  text-shadow: 4px 4px 0 #000, -2px 0 0 #ff2bd6, 2px 0 0 #00f5ff,
    0 0 18px rgba(0, 245, 255, 0.72);
}
.category-item-card:hover .category-name {
  transform: scale(1.02);
  transform-origin: left center;
}
.category-count {
  display: inline-block;
  margin-top: 16px;
  padding: 3px 10px;
  border: 2px solid #000;
  background: #fff;
  box-shadow: 2px 2px 0 #000;
  color: #000;
  font-size: 14px;
  font-weight: 900;
}
.category-eng {
  margin-bottom: 4px;
  color: #000;
  font-size: 13px;
  font-weight: 900;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}
.article-card-title {
  display: -webkit-box;
  overflow: hidden;
  max-width: 100%;
  word-break: break-word;
  overflow-wrap: anywhere;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 4;
}
.bento-large .article-card-title {
  font-size: clamp(42px, 5vw, 68px);
  -webkit-line-clamp: 5;
}
.bento-tall .article-card-title {
  -webkit-line-clamp: 6;
}
.bento-small .article-card-title,
.bento-wide .article-card-title {
  font-size: clamp(28px, 2.3vw, 40px);
  -webkit-line-clamp: 3;
}
.article-card-date {
  margin-top: 18px;
}
.article-item-card a {
  transition: all 0.3s;
}
.article-item-cover {
  height: 220px;
  overflow: hidden;
}
.article-item-card a:hover {
  color: #8e8cd8;
}
.article-item-title {
  min-height: 52px;
}
.article-title-link {
  font-weight: 700;
}
.article-item-meta {
  margin-top: 0.375rem;
}
.tag-wrapper {
  padding: 10px 15px 10px 18px;
}
.tag-wrapper a {
  color: #fff !important;
}
.tag-btn {
  display: inline-block;
  font-size: 0.725rem;
  line-height: 22px;
  height: 22px;
  border-radius: 10px;
  padding: 0 12px !important;
  background: linear-gradient(to right, #bf4643 0%, #6c9d8f 100%);
  opacity: 0.6;
  margin-right: 0.5rem;
}
@keyframes category-marquee {
  0% {
    transform: translateX(0);
  }
  100% {
    transform: translateX(-50%);
  }
}
@keyframes cyber-title-shake {
  0% {
    transform: translate(0, 0) skewX(0deg);
  }
  20% {
    transform: translate(-2px, 1px) skewX(-3deg);
  }
  40% {
    transform: translate(3px, -1px) skewX(2deg);
  }
  60% {
    transform: translate(-1px, 2px) skewX(4deg);
  }
  80% {
    transform: translate(2px, -2px) skewX(-2deg);
  }
  100% {
    transform: translate(0, 0) skewX(0deg);
  }
}
@keyframes cyber-scan {
  0% {
    transform: translateY(-40px);
  }
  100% {
    transform: translateY(40px);
  }
}
</style>
