<template>
  <div class="category-page" :style="pageStyle">
    <div class="banner category-hero" :style="cover">
      <div class="category-hero-inner">
        <div class="category-kicker">
          <v-icon color="#000" size="16">mdi-sparkles</v-icon>
          EXPLORE THE KNOWLEDGE
        </div>
        <h1 class="category-hero-title">Categories</h1>
        <div class="category-hero-subtitle">
          发现 {{ articleTotal }} 篇硬核文章
        </div>
      </div>
      <div class="category-marquee">
        <div class="category-marquee-track">
          <span v-for="item in marqueeList" :key="item">
            {{ item }}
          </span>
          <span v-for="item in marqueeList" :key="item + '-copy'">
            {{ item }}
          </span>
        </div>
      </div>
    </div>

    <!-- 分类列表 -->
    <div class="category-list-wrapper">
      <div class="category-bento-grid">
        <v-card
          v-for="(item, index) of categoryList"
          :key="item.id"
          class="category-item-card"
          :class="getBentoClass(index)"
        >
          <span class="category-bg-id">{{ getCategoryId(index) }}</span>
          <router-link
            :to="'/categories/' + item.id"
            class="category-card-link"
          >
            <div class="category-card-top">
              <span class="category-icon">
                <v-icon color="#000" size="32">
                  {{ getCategoryIcon(item.categoryName, index) }}
                </v-icon>
              </span>
              <span class="category-action">
                <v-icon size="20">mdi-arrow-top-right</v-icon>
              </span>
            </div>
            <div class="category-item-info">
              <div>
                <div class="category-eng">
                  {{ getCategoryEng(item.categoryName, index) }}
                </div>
                <div class="category-name">{{ item.categoryName }}</div>
                <div class="category-count">
                  {{ item.articleCount }} ARTICLES
                </div>
              </div>
            </div>
          </router-link>
        </v-card>
      </div>
    </div>
  </div>
</template>

<script>
const CATEGORY_COVER =
  "https://pic.caiguoyu.cn/20260518204951097.jpg";

export default {
  created() {
    this.listCategories();
  },
  data: function() {
    return {
      categoryList: [],
      count: 0,
      marqueeList: [
        "// CATEGORIES",
        "// 分类",
        "// EXPLORE",
        "// 探索",
        "// READ MORE",
        "// 沉淀",
        "// NO LIMITS",
        "// 极客"
      ],
      categoryMetaMap: {
        总结: {
          eng: "SUMMARY",
          icon: "mdi-book-open-page-variant-outline"
        },
        计算机综合: {
          eng: "COMPUTER SCI",
          icon: "mdi-monitor"
        },
        Hexo: {
          eng: "BLOGGING",
          icon: "mdi-hexagon-outline"
        },
        云服务: {
          eng: "CLOUD COMPUTING",
          icon: "mdi-cloud-outline"
        },
        Linux: {
          eng: "SYSTEM",
          icon: "mdi-console"
        },
        开发工具: {
          eng: "DEV TOOLS",
          icon: "mdi-pencil-ruler"
        }
      },
      defaultMetaList: [
        { eng: "SUMMARY", icon: "mdi-book-open-page-variant-outline" },
        { eng: "COMPUTER SCI", icon: "mdi-monitor" },
        { eng: "BLOGGING", icon: "mdi-hexagon-outline" },
        { eng: "CLOUD COMPUTING", icon: "mdi-cloud-outline" },
        { eng: "SYSTEM", icon: "mdi-console" },
        { eng: "DEV TOOLS", icon: "mdi-pencil-ruler" }
      ]
    };
  },
  methods: {
    listCategories() {
      this.axios.get("/api/categories").then(({ data }) => {
        this.categoryList = data.data.recordList;
        this.count = data.data.count;
      });
    },
    getCategoryId(index) {
      return String(index + 1).padStart(2, "0");
    },
    getCategoryMeta(categoryName, index) {
      return (
        this.categoryMetaMap[categoryName] ||
        this.defaultMetaList[index % this.defaultMetaList.length]
      );
    },
    getCategoryEng(categoryName, index) {
      return this.getCategoryMeta(categoryName, index).eng;
    },
    getCategoryIcon(categoryName, index) {
      return this.getCategoryMeta(categoryName, index).icon;
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
    }
  },
  computed: {
    pageStyle() {
      return (
        "background: linear-gradient(rgba(3, 6, 20, 0.74), rgba(6, 0, 18, 0.86)), url(" +
        CATEGORY_COVER +
        ") center top / cover fixed no-repeat"
      );
    },
    cover() {
      return (
        "background: linear-gradient(115deg, rgba(8, 3, 30, 0.38), rgba(250, 0, 130, 0.2) 45%, rgba(0, 245, 255, 0.22)), url(" +
        CATEGORY_COVER +
        ") center 46% / cover no-repeat"
      );
    },
    articleTotal() {
      return this.categoryList.reduce((total, item) => {
        return total + item.articleCount;
      }, 0);
    }
  }
};
</script>

<style scoped>
.category-page {
  min-height: 100vh;
  padding-bottom: 80px;
  background-color: #080315;
}
.category-hero {
  overflow: hidden;
  z-index: 1;
  box-shadow: inset 0 -80px 90px rgba(3, 6, 20, 0.62);
}
.category-hero-inner {
  max-width: 1106px;
  margin: 0 auto;
  padding: 120px 16px 76px;
  text-align: center;
}
.category-kicker {
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
.category-kicker:hover,
.category-hero-title:hover,
.category-hero-subtitle:hover,
.category-hero-inner:hover .category-kicker,
.category-hero-inner:hover .category-hero-title,
.category-hero-inner:hover .category-hero-subtitle {
  animation: cyber-title-shake 0.38s steps(2, end) infinite;
}
.category-hero-title {
  margin: 0;
  color: #fff;
  font-size: 92px;
  font-weight: 900;
  letter-spacing: 0;
  line-height: 0.9;
  text-shadow: 4px 4px 0 #000, -4px 0 0 #ff2bd6, 4px 0 0 #00f5ff,
    0 0 28px rgba(0, 245, 255, 0.68);
  text-transform: uppercase;
  cursor: default;
}
.category-hero-subtitle {
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
.category-marquee {
  position: absolute;
  right: 0;
  bottom: 0;
  left: 0;
  overflow: hidden;
  border-top: 4px solid #00f5ff;
  border-bottom: 4px solid #ff2bd6;
  background: #030614;
  color: #000;
  white-space: nowrap;
  box-shadow: 0 0 26px rgba(0, 245, 255, 0.42);
}
.category-marquee-track {
  display: inline-flex;
  width: max-content;
  animation: category-marquee 18s linear infinite;
}
.category-marquee-track span {
  display: inline-flex;
  align-items: center;
  padding: 8px 0;
  margin-right: 26px;
  color: #00f5ff;
  font-size: 20px;
  font-weight: 900;
  text-shadow: 2px 0 #ff2bd6, 0 0 14px rgba(0, 245, 255, 0.76);
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
@media (min-width: 760px) {
  .category-list-wrapper {
    width: 100%;
    margin: 350px 0 0;
    padding: 44px 36px 88px;
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
  .category-list-wrapper:before,
  .category-list-wrapper:after {
    content: "";
    position: absolute;
    inset: 0;
    pointer-events: none;
  }
  .category-list-wrapper:before {
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
      radial-gradient(
        circle at 48% 88%,
        rgba(255, 201, 0, 0.16),
        transparent 30%
      ),
      linear-gradient(rgba(0, 245, 255, 0.12) 1px, transparent 1px),
      linear-gradient(90deg, rgba(255, 43, 214, 0.1) 1px, transparent 1px);
    background-size: auto, auto, auto, 54px 54px, 54px 54px;
  }
  .category-list-wrapper:after {
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
  .category-bento-grid {
    display: grid;
    grid-auto-flow: dense;
    grid-auto-rows: 230px;
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
}
@media (max-width: 759px) {
  .category-hero-inner {
    padding: 98px 14px 48px;
  }
  .category-hero-title {
    font-size: 48px;
  }
  .category-hero-subtitle {
    font-size: 17px;
  }
  .category-list-wrapper {
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
  .category-list-wrapper:before {
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
  .category-bento-grid {
    display: grid;
    gap: 18px;
    position: relative;
    z-index: 1;
  }
  .category-card-link {
    min-height: 220px;
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
.category-item-card {
  border: 4px solid #000;
  border-radius: 8px !important;
  box-shadow: 8px 8px 0 #000, 0 0 22px rgba(0, 245, 255, 0.28) !important;
  overflow: hidden;
  transition: all 0.2s ease-out;
}
.category-item-card:nth-child(6n + 1) {
  background: #ff90e8;
}
.category-item-card:nth-child(6n + 2) {
  background: #ffc900;
}
.category-item-card:nth-child(6n + 3) {
  background: #00e5ff;
}
.category-item-card:nth-child(6n + 4) {
  background: #8a2be2;
}
.category-item-card:nth-child(6n + 5) {
  background: #ff4d4d;
}
.category-item-card:nth-child(6n) {
  background: #00ff00;
}
.category-item-card:nth-child(6n + 4) .category-name,
.category-item-card:nth-child(6n + 4) .category-eng {
  color: #fff;
}
.category-item-card:nth-child(6n + 4) .category-bg-id {
  color: rgba(255, 255, 255, 0.2);
}
.category-card-link {
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  height: 100%;
  justify-content: space-between;
  padding: 28px;
  position: relative;
}
.category-bg-id {
  position: absolute;
  right: -8px;
  bottom: -26px;
  color: rgba(0, 0, 0, 0.18);
  font-size: 150px;
  font-weight: 900;
  letter-spacing: 0;
  line-height: 0.8;
  pointer-events: none;
}
.category-card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
  z-index: 1;
}
.category-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 58px;
  height: 58px;
  border: 2px solid #000;
  border-radius: 8px;
  background: #fff;
  box-shadow: 4px 4px 0 #000;
  transition: transform 0.3s;
}
.category-item-card:hover .category-icon {
  transform: translateY(-8px);
}
.category-action {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border: 2px solid #000;
  border-radius: 50%;
  background: #000;
  color: #fff;
  transition: transform 0.3s;
}
.category-item-card:hover .category-action {
  transform: rotate(45deg);
}
.category-item-info {
  line-height: 1.7;
  position: relative;
  z-index: 1;
}
.category-name {
  color: #000;
  font-size: 36px;
  font-weight: 900;
  line-height: 1.1;
  transition: transform 0.3s;
}
.category-item-card:hover .category-name {
  transform: scale(1.04);
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
}
.category-item-card a {
  color: inherit;
}
</style>
