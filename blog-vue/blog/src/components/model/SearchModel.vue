<template>
  <v-dialog v-model="searchFlag" max-width="640" :fullscreen="isMobile">
    <v-card class="search-wrapper">
      <div class="mb-3">
        <span class="search-title">搜索文章</span>
        <v-icon class="float-right close-btn" @click="searchFlag = false">
          mdi-close
        </v-icon>
      </div>
      <div class="search-input-wrapper">
        <v-icon>mdi-magnify</v-icon>
        <input
          ref="searchInput"
          v-model="keywords"
          placeholder="输入文章标题或内容..."
          aria-label="搜索文章"
        />
      </div>
      <v-progress-linear
        v-if="loading"
        class="search-progress"
        indeterminate
        height="2"
        color="#8e8cd8"
      />
      <div class="search-result-wrapper">
        <hr class="divider" />
        <ul v-if="articleList.length">
          <li class="search-result" v-for="item of articleList" :key="item.id">
            <a @click="goTo(item.id)" v-html="item.articleTitle" />
            <p
              class="search-result-content text-justify"
              @click="goTo(item.id)"
              v-html="item.articleContent"
            />
          </li>
        </ul>
        <div v-else-if="loading" class="search-status">正在搜索...</div>
        <div v-else-if="searchError" class="search-status">
          搜索暂时不可用，请稍后重试
        </div>
        <div v-else-if="flag" class="search-status">
          找不到您查询的内容：{{ keywords }}
        </div>
        <div v-else class="search-status">输入关键词，查找标题或正文内容</div>
      </div>
    </v-card>
  </v-dialog>
</template>

<script>
export default {
  data: function() {
    return {
      keywords: "",
      articleList: [],
      flag: false,
      loading: false,
      searchError: false,
      searchTimer: null,
      searchRequestId: 0
    };
  },
  beforeDestroy() {
    clearTimeout(this.searchTimer);
  },
  methods: {
    goTo(articleId) {
      this.$store.state.searchFlag = false;
      this.$router.push({ path: "/articles/" + articleId });
    }
  },
  computed: {
    searchFlag: {
      set(value) {
        this.$store.state.searchFlag = value;
      },
      get() {
        return this.$store.state.searchFlag;
      }
    },
    isMobile() {
      const clientWidth = document.documentElement.clientWidth;
      if (clientWidth > 960) {
        return false;
      }
      return true;
    }
  },
  watch: {
    searchFlag(value) {
      if (value) {
        this.$nextTick(() => this.$refs.searchInput.focus());
      }
    },
    keywords(value) {
      clearTimeout(this.searchTimer);
      const keywords = value.trim();
      this.flag = keywords !== "";
      this.searchError = false;

      if (!keywords) {
        this.articleList = [];
        this.loading = false;
        this.searchRequestId += 1;
        return;
      }

      this.loading = true;
      this.articleList = [];
      const requestId = ++this.searchRequestId;
      this.searchTimer = setTimeout(() => {
        this.axios
          .get("/api/articles/search", {
            params: { current: 1, keywords }
          })
          .then(({ data }) => {
            if (requestId === this.searchRequestId) {
              this.articleList = data.data || [];
            }
          })
          .catch(() => {
            if (requestId === this.searchRequestId) {
              this.articleList = [];
              this.searchError = true;
            }
          })
          .finally(() => {
            if (requestId === this.searchRequestId) {
              this.loading = false;
            }
          });
      }, 300);
    }
  }
};
</script>

<style scoped>
.search-wrapper {
  padding: 1.25rem;
  height: 100%;
  background: #fff !important;
  border-radius: 16px !important;
}
.search-title {
  color: #6f6db6;
  font-size: 1.25rem;
  font-weight: 700;
  line-height: 1;
}
.close-btn {
  cursor: pointer;
}
.search-input-wrapper {
  display: flex;
  align-items: center;
  padding: 5px;
  height: 42px;
  width: 100%;
  border: 2px solid #8e8cd8;
  border-radius: 2rem;
}
.search-input-wrapper input {
  width: 100%;
  margin-left: 5px;
  outline: none;
}
.search-progress {
  margin-top: 8px;
}
@media (min-width: 960px) {
  .search-result-wrapper {
    padding-right: 5px;
    height: 450px;
    overflow: auto;
  }
}
@media (max-width: 959px) {
  .search-result-wrapper {
    height: calc(100vh - 110px);
    overflow: auto;
  }
}
.search-result {
  margin-bottom: 18px;
}
.search-result a {
  color: #555;
  font-weight: bold;
  border-bottom: 1px solid #999;
  text-decoration: none;
  cursor: pointer;
}
.search-result-content {
  color: #555;
  cursor: pointer;
  border-bottom: 1px dashed #ccc;
  padding: 5px 0;
  line-height: 2;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}
.search-status {
  color: #777;
  font-size: 0.875rem;
  text-align: center;
  padding: 42px 0;
}
.divider {
  margin: 20px 0;
  border: 2px dashed #d2ebfd;
}
</style>
