<template>
  <div class="article-detail-page">
    <!-- 封面图 -->
    <div class="banner" :style="articleCover">
      <div class="article-banner-grid"></div>
      <div class="article-banner-glow"></div>
      <span class="article-category">
        <router-link :to="'/categories/' + article.categoryId">
          <v-icon size="14" color="#34d399">mdi-console</v-icon>
          {{ article.categoryName }}
        </router-link>
      </span>
      <div class="article-info-container">
        <!-- 文章标题 -->
        <div class="article-title">{{ article.articleTitle }}</div>
        <div class="article-time-row">
          <span>
            <v-icon size="16" color="#cbd5e1">mdi-calendar-plus</v-icon>
            {{ article.createTime | date }}
          </span>
          <span>
            <v-icon size="16" color="#cbd5e1">mdi-eye-outline</v-icon>
            {{ article.viewsCount || 0 }}
          </span>
          <span>
            <v-icon size="16" color="#cbd5e1">mdi-comment-outline</v-icon>
            {{ commentCount || 0 }}
          </span>
          <span>
            <v-icon size="16" color="#cbd5e1">mdi-text-box-outline</v-icon>
            {{ wordNum | num }}
          </span>
        </div>
        <!-- 文章标签 -->
        <span class="article-tags">
          <i class="iconfont iconbiaoqian article-tag-icon" />
          <template v-if="displayTagList.length">
            <router-link
              v-for="item of displayTagList"
              :key="item.id"
              :to="'/tags/' + item.id"
            >
              <v-icon size="14" class="article-tag-v-icon">mdi-tag-outline</v-icon>
              {{ item.tagName }}
            </router-link>
          </template>
          <template v-else>暂无标签</template>
        </span>
      </div>
    </div>
    <!-- 内容 -->
    <div class="article-container article-detail-layout">
      <aside class="article-layout-side article-left-side">
        <div
          ref="articleSidebar"
          class="article-sidebar-sticky"
          :style="{ '--article-sidebar-top': articleSidebarTop }"
        >
          <!-- 文章目录 -->
          <v-card class="right-container article-side-card toc-card">
            <div class="toc-header">
              <v-icon size="20" color="#334155">mdi-menu</v-icon>
              <span>目录</span>
            </div>
            <div class="toc-doc-entry">
              <div class="toc-doc-icon">
                <v-icon size="18" color="#059669">mdi-book-open-page-variant</v-icon>
              </div>
              <div class="toc-doc-info">
                <div class="toc-doc-title">{{ article.articleTitle }}</div>
                <div class="toc-doc-count">共 {{ tocHeadingCount }} 个核心章节</div>
              </div>
            </div>
            <div id="toc" />
          </v-card>
        </div>
      </aside>
      <main class="article-main">
        <v-card
          class="article-wrapper article-reading-card"
          :class="{ 'has-article-summary': articleSummary }"
        >
          <div class="article-card-actions">
            <button type="button" class="side-action-btn like-action-btn" @click="like">
              <v-icon size="17" color="#ffffff">mdi-thumb-up</v-icon>
              <strong class="action-count">{{ article.likeCount || 0 }}</strong>
              <span
                v-for="effect in likeEffects"
                :key="effect.id"
                class="like-float"
              >
                {{ effect.icon }}
              </span>
            </button>
            <button type="button" class="side-action-btn" @click="copyShareText">
              <v-icon size="17" color="#0f172a">mdi-share-variant</v-icon>
            </button>
          </div>
          <div class="article-summary" v-if="articleSummary">
            <div class="summary-label">摘要</div>
            <div class="summary-text">{{ articleSummary }}</div>
          </div>
          <article
            id="write"
            class="article-content markdown-body"
            ref="article"
          >
            <section
              v-for="item of articleContentChunks"
              :key="item.key"
              class="article-content-chunk"
              v-html="item.html"
            />
          </article>
          <!-- 版权声明 -->
          <div class="aritcle-copyright">
            <div>
              <span>文章作者：</span>
              <router-link to="/">
                {{ blogInfo.websiteConfig.websiteAuthor }}
              </router-link>
            </div>
            <div>
              <span>文章链接：</span>
              <a :href="articleHref" target="_blank">{{ articleHref }} </a>
            </div>
            <div>
              <span>版权声明：</span>本博客所有文章除特别声明外，均采用
              <a
                href="https://creativecommons.org/licenses/by-nc-sa/4.0/"
                target="_blank"
              >
                CC BY-NC-SA 4.0
              </a>
              许可协议。转载请注明文章出处。
            </div>
          </div>
          <!-- 点赞打赏等 -->
          <div class="article-reward" v-if="blogInfo.websiteConfig.isReward == 1">
            <a class="reward-btn">
              <!-- 打赏按钮 -->
              <i class="iconfont iconerweima" /> 打赏
              <!-- 二维码 -->
              <div class="animated fadeInDown reward-main">
                <ul class="reward-all">
                  <li class="reward-item">
                    <img
                      class="reward-img"
                      :src="blogInfo.websiteConfig.weiXinQRCode"
                    />
                    <div class="reward-desc">微信</div>
                  </li>
                  <li class="reward-item">
                    <img
                      class="reward-img"
                      :src="blogInfo.websiteConfig.alipayQRCode"
                    />
                    <div class="reward-desc">支付宝</div>
                  </li>
                </ul>
              </div>
            </a>
          </div>
          <div class="pagination-post">
            <!-- 上一篇 -->
            <div
              :class="isFull(article.lastArticle.id)"
              v-if="article.lastArticle.id"
            >
              <router-link :to="'/articles/' + article.lastArticle.id">
                <img
                  class="post-cover"
                  :src="article.lastArticle.articleCover"
                />
                <div class="post-info">
                  <div class="label">上一篇</div>
                  <div class="post-title">
                    {{ article.lastArticle.articleTitle }}
                  </div>
                </div>
              </router-link>
            </div>
            <!-- 下一篇 -->
            <div
              :class="isFull(article.nextArticle.id)"
              v-if="article.nextArticle.id"
            >
              <router-link :to="'/articles/' + article.nextArticle.id">
                <img
                  class="post-cover"
                  :src="article.nextArticle.articleCover"
                />
                <div class="post-info" style="text-align: right">
                  <div class="label">下一篇</div>
                  <div class="post-title">
                    {{ article.nextArticle.articleTitle }}
                  </div>
                </div>
              </router-link>
            </div>
          </div>
          <!-- 推荐文章 -->
          <div
            class="recommend-container"
            v-if="article.recommendArticleList.length"
          >
            <div class="recommend-title">
              <v-icon size="20" color="#4c4948">mdi-thumb-up</v-icon> 相关推荐
            </div>
            <div class="recommend-list">
              <div
                class="recommend-item"
                v-for="item of article.recommendArticleList"
                :key="item.id"
              >
                <router-link :to="'/articles/' + item.id">
                  <img class="recommend-cover" :src="item.articleCover" />
                  <div class="recommend-info">
                    <div class="recommend-date">
                      <i class="iconfont iconrili" />
                      {{ item.createTime | date }}
                    </div>
                    <div>{{ item.articleTitle }}</div>
                  </div>
                </router-link>
              </div>
            </div>
          </div>
          <!-- 分割线 -->
          <hr />
          <!-- 评论 -->
          <comment :type="commentType" @getCommentCount="getCommentCount" />
        </v-card>
      </main>
    </div>
  </div>
</template>

<script>
import Clipboard from "clipboard";
import markdownToHtml from "../../utils/markdown";
import Comment from "../../components/Comment";
import tocbot from "tocbot";
export default {
  components: {
    Comment
  },
  created() {
    this.getArticle();
  },
  mounted() {
    window.addEventListener("scroll", this.requestSyncArticleSidebarTop, {
      passive: true
    });
    window.addEventListener("resize", this.requestSyncArticleSidebarTop);
    this.$nextTick(() => {
      this.requestSyncArticleSidebarTop();
      window.setTimeout(() => {
        this.requestSyncArticleSidebarTop();
      }, 120);
    });
  },
  destroyed() {
    if (this.clipboard) {
      this.clipboard.destroy();
    }
    window.removeEventListener("scroll", this.requestSyncTocActiveLink);
    window.removeEventListener("scroll", this.requestSyncArticleSidebarTop);
    window.removeEventListener("resize", this.requestSyncArticleSidebarTop);
    if (this.tocScrollRaf) {
      window.cancelAnimationFrame(this.tocScrollRaf);
    }
    if (this.articleSidebarRaf) {
      window.cancelAnimationFrame(this.articleSidebarRaf);
    }
    if (this.articleSidebarTimer) {
      window.clearTimeout(this.articleSidebarTimer);
    }
    if (this.tocClickScrollTimer) {
      window.clearTimeout(this.tocClickScrollTimer);
    }
    this.clearArticleRenderTask();
    tocbot.destroy();
  },
  data: function() {
    return {
      imgList: [],
      article: {
        nextArticle: {
          id: 0,
          articleCover: ""
        },
        lastArticle: {
          id: 0,
          articleCover: ""
        },
        recommendArticleList: [],
        newestArticleList: []
      },
      wordNum: "",
      commentType: 1,
      articleHref: window.location.href,
      clipboard: null,
      commentCount: 0,
      likeEffectId: 0,
      likeEffects: [],
      tocHeadingCount: 0,
      tocScrollRaf: null,
      tocClickScrollTimer: null,
      tocLockedHash: "",
      articleSidebarRaf: null,
      articleSidebarTimer: null,
      articleSidebarTop: "80px",
      articleContentChunks: [],
      articleRenderToken: 0,
      articleRenderCancel: null
    };
  },
  methods: {
    getArticle() {
      const articleId = Number(this.$route.params.articleId);
      if (!Number.isInteger(articleId) || articleId <= 0) {
        this.$router.replace("/");
        return;
      }
      this.clearArticleRenderTask();
      this.articleContentChunks = [];
      //查询文章
      this.axios.get("/api/articles/" + articleId).then(({ data }) => {
        const article = data.data || {};
        document.title = article.articleTitle || "文章详情";
        //将markdown转换为Html
        this.article = {
          ...article,
          nextArticle: article.nextArticle || {
            id: 0,
            articleCover: ""
          },
          lastArticle: article.lastArticle || {
            id: 0,
            articleCover: ""
          },
          recommendArticleList: article.recommendArticleList || [],
          newestArticleList: article.newestArticleList || [],
          articleContent: ""
        };
        this.renderArticleContent(article.articleContent || "");
        });
    },
    renderArticleContent(content) {
      const token = ++this.articleRenderToken;
      const chunks = this.isMobileArticleReader
        ? this.splitMarkdownContent(content)
        : [content];
      let index = 0;
      const renderNext = () => {
        if (token !== this.articleRenderToken) {
          return;
        }
        const chunk = chunks[index];
        if (chunk == null) {
          this.$nextTick(() => {
            this.initArticleDom();
          });
          return;
        }
        this.articleContentChunks.push({
          key: token + "-" + index,
          html: markdownToHtml(chunk)
        });
        index++;
        if (index < chunks.length) {
          this.scheduleArticleRenderTask(renderNext);
          return;
        }
        this.$nextTick(() => {
          this.initArticleDom();
        });
      };
      renderNext();
    },
    splitMarkdownContent(content) {
      if (!content) {
        return [""];
      }
      const chunks = [];
      const lines = content.split(/\n/);
      let buffer = [];
      let bufferLength = 0;
      let inFence = false;
      const flush = () => {
        if (!buffer.length) {
          return;
        }
        chunks.push(buffer.join("\n"));
        buffer = [];
        bufferLength = 0;
      };
      lines.forEach(line => {
        const trimmed = line.trim();
        const isFence = /^(```|~~~)/.test(trimmed);
        const isHeading = /^#{1,2}\s+/.test(line);
        const isBlank = trimmed === "";
        if (!inFence && isHeading && bufferLength > 0) {
          flush();
        }
        buffer.push(line);
        bufferLength += line.length + 1;
        if (isFence) {
          inFence = !inFence;
        }
        if (!inFence && isBlank && bufferLength >= 10000) {
          flush();
        }
      });
      flush();
      return chunks;
    },
    scheduleArticleRenderTask(task) {
      this.clearArticleRenderTask();
      if (window.requestIdleCallback) {
        const handle = window.requestIdleCallback(task, { timeout: 500 });
        this.articleRenderCancel = () => {
          window.cancelIdleCallback(handle);
        };
        return;
      }
      const handle = window.setTimeout(task, 80);
      this.articleRenderCancel = () => {
        window.clearTimeout(handle);
      };
    },
    clearArticleRenderTask() {
      if (this.articleRenderCancel) {
        this.articleRenderCancel();
        this.articleRenderCancel = null;
      }
    },
    initArticleDom() {
      const that = this;
      const articleElement = this.$refs.article;
      const articleHtml = articleElement ? articleElement.innerHTML : "";
      // 统计文章字数
      this.wordNum = this.deleteHTMLTag(articleHtml).length;
      if (this.clipboard) {
        this.clipboard.destroy();
        this.clipboard = null;
      }
      window.removeEventListener("scroll", this.requestSyncTocActiveLink);
      tocbot.destroy();
      // 添加代码复制功能
      this.clipboard = new Clipboard(".copy-btn");
      this.clipboard.on("success", () => {
        this.$toast({ type: "success", message: "复制成功" });
      });
      // 添加文章生成目录功能
      let nodes = articleElement ? articleElement.children : [];
      let tocHeadingCount = 0;
      const headingNodes = articleElement
        ? articleElement.querySelectorAll("h1, h2, h3, h4, h5")
        : [];
      if (headingNodes.length) {
        for (let i = 0; i < headingNodes.length; i++) {
          headingNodes[i].id = i;
          tocHeadingCount++;
        }
      } else if (nodes.length) {
        for (let i = 0; i < nodes.length; i++) {
          let node = nodes[i];
          let reg = /^H[1-5]{1}$/;
          if (reg.exec(node.tagName)) {
            node.id = i;
            tocHeadingCount++;
          }
        }
      }
      this.tocHeadingCount = tocHeadingCount;
      if (!this.isMobileArticleReader) {
        tocbot.init({
          tocSelector: "#toc", //要把目录添加元素位置，支持选择器
          contentSelector: ".article-content", //获取html的元素
          headingSelector: "h1, h2, h3, h4, h5", //要显示的id的目录
          headingsOffset: this.getArticleHeadingOffset(),
          scrollSmoothOffset: -this.getArticleHeadingOffset(),
          hasInnerContainers: true,
          collapseDepth: 6,
          disableTocScrollSync: true,
          onClick: function(e) {
            e.preventDefault();
            e.stopPropagation();
            that.handleTocClick(e);
          }
        });
        window.addEventListener("scroll", this.requestSyncTocActiveLink, {
          passive: true
        });
        this.requestSyncTocActiveLink();
      }
      this.requestSyncArticleSidebarTop();
      // 添加图片预览功能
      this.imgList = [];
      const imgList = articleElement
        ? articleElement.getElementsByTagName("img")
        : [];
      for (var i = 0; i < imgList.length; i++) {
        this.imgList.push(imgList[i].src);
        imgList[i].addEventListener("click", function(e) {
          that.previewImg(e.target.currentSrc);
        });
      }
    },
    requestSyncArticleSidebarTop() {
      if (this.articleSidebarRaf) {
        return;
      }
      this.articleSidebarRaf = window.requestAnimationFrame(() => {
        this.articleSidebarRaf = null;
        this.syncArticleSidebarTop();
      });
      if (this.articleSidebarTimer) {
        window.clearTimeout(this.articleSidebarTimer);
      }
      this.articleSidebarTimer = window.setTimeout(() => {
        this.articleSidebarTimer = null;
        this.syncArticleSidebarTop();
      }, 260);
    },
    syncArticleSidebarTop() {
      const nav = document.querySelector(".v-app-bar");
      const navBottom = nav ? Math.max(nav.getBoundingClientRect().bottom, 0) : 0;
      const nextTop = Math.round(navBottom + 20) + "px";
      if (this.$refs.articleSidebar) {
        this.$refs.articleSidebar.style.setProperty(
          "--article-sidebar-top",
          nextTop
        );
      }
      if (this.articleSidebarTop !== nextTop) {
        this.articleSidebarTop = nextTop;
      }
    },
    getArticleHeadingOffset() {
      const nav = document.querySelector(".v-app-bar");
      const navBottom = nav ? Math.max(nav.getBoundingClientRect().bottom, 0) : 0;
      return Math.round(navBottom + 18);
    },
    requestSyncTocActiveLink() {
      if (this.tocScrollRaf) {
        return;
      }
      this.tocScrollRaf = window.requestAnimationFrame(() => {
        this.tocScrollRaf = null;
        if (this.tocLockedHash) {
          this.syncLockedTocActive();
          this.scheduleReleaseTocLock();
          return;
        }
        this.syncTocActiveLink();
      });
    },
    scheduleReleaseTocLock() {
      if (this.tocClickScrollTimer) {
        window.clearTimeout(this.tocClickScrollTimer);
      }
      this.tocClickScrollTimer = window.setTimeout(() => {
        this.tocLockedHash = "";
        this.tocClickScrollTimer = null;
        this.requestSyncTocActiveLink();
      }, 180);
    },
    syncLockedTocActive() {
      const toc = document.getElementById("toc");
      if (!toc || !this.tocLockedHash) {
        return;
      }
      const link = toc.querySelector(
        `.toc-link[href="${this.tocLockedHash.replace(/"/g, '\\"')}"]`
      );
      if (!link) {
        return;
      }
      toc.querySelectorAll(".is-active-link").forEach(item => {
        item.classList.remove("is-active-link");
      });
      toc.querySelectorAll(".is-active-li").forEach(item => {
        item.classList.remove("is-active-li");
      });
      link.classList.add("is-active-link");
      if (link.parentElement) {
        link.parentElement.classList.add("is-active-li");
      }
      this.scrollTocActiveLinkIntoView(link, "auto");
    },
    syncTocActiveLink() {
      const toc = document.getElementById("toc");
      if (!toc || toc.scrollHeight <= toc.clientHeight) {
        return;
      }
      const activeLink = toc.querySelector(".is-active-link");
      if (!activeLink) {
        return;
      }
      this.scrollTocActiveLinkIntoView(activeLink, "smooth");
    },
    scrollTocActiveLinkIntoView(activeLink, behavior) {
      const toc = document.getElementById("toc");
      if (!toc || toc.scrollHeight <= toc.clientHeight) {
        return;
      }
      const tocRect = toc.getBoundingClientRect();
      const activeRect = activeLink.getBoundingClientRect();
      const safePadding = 24;
      if (
        activeRect.top >= tocRect.top + safePadding &&
        activeRect.bottom <= tocRect.bottom - safePadding
      ) {
        return;
      }
      const activeCenter =
        activeRect.top - tocRect.top + toc.scrollTop + activeRect.height / 2;
      toc.scrollTo({
        top: Math.max(activeCenter - toc.clientHeight / 2, 0),
        behavior
      });
    },
    handleTocClick(e) {
      const link = e.target.closest(".toc-link");
      if (!link || !link.hash) {
        return;
      }
      const targetId = decodeURIComponent(link.hash.slice(1));
      const target = document.getElementById(targetId);
      if (!target) {
        return;
      }
      const toc = document.getElementById("toc");
      if (toc) {
        toc.querySelectorAll(".is-active-link").forEach(item => {
          item.classList.remove("is-active-link");
        });
        toc.querySelectorAll(".is-active-li").forEach(item => {
          item.classList.remove("is-active-li");
        });
      }
      link.classList.add("is-active-link");
      if (link.parentElement) {
        link.parentElement.classList.add("is-active-li");
      }
      this.tocLockedHash = link.hash;
      this.scrollTocActiveLinkIntoView(link, "smooth");
      this.scheduleReleaseTocLock();
      const targetTop =
        target.getBoundingClientRect().top +
        window.pageYOffset -
        this.getArticleHeadingOffset();
      window.scrollTo({
        top: Math.max(targetTop, 0),
        behavior: "smooth"
      });
    },
    like() {
      // 判断登录
      // if (!this.$store.state.userId) {
      //   this.$store.state.loginFlag = true;
      //   return false;
      // }
      this.showLikeEffect();
      //发送请求
      this.axios
        .post("/api/articles/" + this.article.id + "/like")
        .then(({ data }) => {
          if (data.flag) {
            this.$set(
              this.article,
              "likeCount",
              (this.article.likeCount || 0) + 1
            );
          }
        });
    },
    showLikeEffect() {
      const icons = ["👍", "💛", "✨"];
      const effect = {
        id: ++this.likeEffectId,
        icon: icons[this.likeEffectId % icons.length]
      };
      this.likeEffects.push(effect);
      setTimeout(() => {
        this.likeEffects = this.likeEffects.filter(item => item.id != effect.id);
      }, 850);
    },
    previewImg(img) {
      this.$imagePreview({
        images: this.imgList,
        index: this.imgList.indexOf(img)
      });
    },
    deleteHTMLTag(content) {
      return content
        .replace(/<\/?[^>]*>/g, "")
        .replace(/[|]*\n/, "")
        .replace(/&npsp;/gi, "");
    },
    getCommentCount(count) {
      this.commentCount = count;
    },
    copyShareText() {
      const done = () => {
        this.$toast({ type: "success", message: "分享内容已复制" });
      };
      const fail = () => {
        this.$toast({ type: "error", message: "复制失败，请手动复制链接" });
      };
      if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard
          .writeText(this.shareText)
          .then(done)
          .catch(() => {
            this.copyShareTextByTextarea(this.shareText) ? done() : fail();
          });
        return;
      }
      this.copyShareTextByTextarea(this.shareText) ? done() : fail();
    },
    copyShareTextByTextarea(text) {
      const textarea = document.createElement("textarea");
      textarea.value = text;
      textarea.setAttribute("readonly", "readonly");
      textarea.style.position = "fixed";
      textarea.style.top = "-9999px";
      document.body.appendChild(textarea);
      textarea.select();
      const result = document.execCommand("copy");
      document.body.removeChild(textarea);
      return result;
    }
  },
  computed: {
    blogInfo() {
      return this.$store.state.blogInfo;
    },
    articleCover() {
      return (
        "background: url(" +
        this.article.articleCover +
        ") center center / cover no-repeat"
      );
    },
    isFull() {
      return function(id) {
        return id ? "post full" : "post";
      };
    },
    articleSummary() {
      return (this.article.articleSummary || "").replace(/\s+/g, " ").trim();
    },
    shareText() {
      return [
        "《" + this.article.articleTitle + "》",
        "",
        "原文网址：" + this.articleHref,
        "",
        "摘要：" + this.articleSummary
      ].join("\n");
    },
    displayTagList() {
      const tagList = this.article.tagDTOList || [];
      if (!import.meta.env.DEV) {
        return tagList;
      }
      return tagList.concat([
        { id: "mock-1", tagName: "高吞吐设计" },
        { id: "mock-2", tagName: "顺序 I/O" },
        { id: "mock-3", tagName: "Page Cache" },
        { id: "mock-4", tagName: "Zero-Copy 零拷贝" },
        { id: "mock-5", tagName: "消费者组与 Partition 协调机制" },
        { id: "mock-6", tagName: "超长标签用于验证移动端自动换行和容器适配效果" },
        { id: "mock-7", tagName: "Broker" },
        { id: "mock-8", tagName: "Rebalance" }
      ]);
    },
    isMobileArticleReader() {
      return /Android|iPhone|iPod|iPad|Mobile|MicroMessenger|MQQBrowser/i.test(
        navigator.userAgent
      );
    }
  }
};
</script>

<style scoped>
@font-face {
  font-family: "ReejiChaoZeng";
  src: url("../../assets/fonts/sf.ttf") format("truetype");
  font-style: normal;
  font-weight: 400;
  font-display: swap;
}

.article-detail-page {
  min-height: 100vh;
  background: linear-gradient(180deg, rgba(2, 6, 23, 0.02), rgba(248, 250, 252, 0.7) 520px);
}
.banner {
  overflow: hidden;
}
.banner:before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 100%;
  z-index: 1;
  background: linear-gradient(
    180deg,
    rgba(2, 6, 23, 0.48),
    rgba(2, 6, 23, 0.78)
  );
}
.article-banner-grid {
  position: absolute;
  inset: 0;
  z-index: 2;
  pointer-events: none;
  opacity: 0.18;
  background-image: linear-gradient(
      to right,
      rgba(255, 255, 255, 0.18) 1px,
      transparent 1px
    ),
    linear-gradient(to bottom, rgba(255, 255, 255, 0.13) 1px, transparent 1px);
  background-size: 28px 28px;
}
.article-banner-glow {
  position: absolute;
  right: 8%;
  bottom: -120px;
  z-index: 2;
  width: min(520px, 70vw);
  height: 260px;
  pointer-events: none;
  background: rgba(52, 211, 153, 0.22);
  border-radius: 50%;
  filter: blur(80px);
}
.article-banner-glow:before {
  content: "";
  position: absolute;
  left: -78vw;
  bottom: -26px;
  width: min(420px, 62vw);
  height: 220px;
  border-radius: 50%;
  background: rgba(4, 120, 87, 0.18);
  filter: blur(46px);
  pointer-events: none;
}
.article-info-container {
  z-index: 3;
  box-sizing: border-box;
}
.article-category {
  position: absolute;
  top: 90px;
  left: 6%;
  z-index: 3;
  display: block;
  box-sizing: border-box;
  max-width: min(460px, 42vw);
  height: auto;
  margin: 0;
  padding: 0;
  font-family: "SF Pro Display", "Inter", "PingFang SC", "Microsoft YaHei",
    sans-serif !important;
  font-size: 14px !important;
  font-weight: 700;
  line-height: 1;
  letter-spacing: 0.18em;
  text-align: left;
  opacity: 1;
  writing-mode: horizontal-tb;
  text-orientation: mixed;
  transform: none;
  filter: none;
}
@media (min-width: 760px) {
  .banner {
    color: #eee !important;
  }
  .article-info-container {
    position: absolute;
    top: 50%;
    bottom: auto;
    transform: translateY(-36%);
    padding: 0 8%;
    width: 100%;
    text-align: center;
  }
  .article-title {
    position: relative;
    display: inline-block;
    max-width: 1120px;
    margin: 0 auto 28px;
    font-family: "ReejiChaoZeng", "Kaiti SC", "STKaiti", "KaiTi", "PingFang SC",
      "Microsoft YaHei", sans-serif !important;
    font-size: 56px;
    font-weight: 400;
    color: #f8fafc !important;
    letter-spacing: 0;
    line-height: 1.14;
    text-shadow: 0 5px 28px rgba(2, 6, 23, 0.72);
  }
  .article-time-row {
    justify-content: center;
  }
  .pagination-post {
    display: flex;
  }
  .post {
    width: 50%;
  }
  .recommend-item {
    position: relative;
    display: inline-block;
    overflow: hidden;
    margin: 3px;
    width: calc(33.333% - 6px);
    height: 200px;
    background: #000;
    vertical-align: bottom;
  }
}
@media (max-width: 759px) {
  .banner {
    color: #eee !important;
    height: 520px;
  }
  .separator:first-child {
    display: none;
  }
  .blog-container {
    margin: 482px 5px 0 5px;
  }
  .article-container {
    margin-top: 472px !important;
  }
  .article-info-container {
    position: absolute;
    top: 188px;
    bottom: auto;
    transform: none;
    padding: 0 5%;
    width: 100%;
    color: #eee;
    text-align: center;
  }
  .article-category {
    top: 136px;
    left: 6%;
    max-width: 88vw;
    height: auto;
    font-family: "SF Pro Display", "Inter", "PingFang SC", "Microsoft YaHei",
      sans-serif !important;
    font-size: 12px !important;
    line-height: 1;
    letter-spacing: 0.12em;
  }
  .article-category a {
    font-family: "SF Pro Display", "Inter", "PingFang SC", "Microsoft YaHei",
      sans-serif !important;
  }
  .article-title {
    display: block;
    font-family: "ReejiChaoZeng", "Kaiti SC", "STKaiti", "KaiTi", "PingFang SC",
      "Microsoft YaHei", sans-serif !important;
    font-size: 1.45rem;
    font-weight: 400;
    line-height: 1.24;
    margin: 0 auto 0.8rem;
    max-width: 94%;
    color: #f8fafc !important;
    text-shadow: 0 3px 18px rgba(2, 6, 23, 0.58);
  }
  .article-time-row {
    justify-content: center;
    gap: 8px 18px;
    margin-bottom: 0.75rem;
    font-size: 12px;
  }
  .article-tags {
    width: auto !important;
    justify-content: center !important;
    overflow-y: auto !important;
    overflow-x: hidden !important;
    white-space: normal !important;
    max-width: 94%;
    max-height: 154px;
    padding-right: 2px !important;
  }
  .article-tags a {
    max-width: 100%;
    min-height: 30px;
    padding: 7px 10px;
    font-size: 12px;
    line-height: 1.25;
  }
  .article-tag-v-icon {
    flex: 0 0 auto;
  }
  .post {
    width: 100%;
  }
  .pagination-post {
    display: block;
  }
  .recommend-item {
    position: relative;
    display: inline-block;
    overflow: hidden;
    margin: 3px;
    width: calc(100% - 4px);
    height: 150px;
    margin: 2px;
    background: #000;
    vertical-align: bottom;
  }
}
.article-content {
  max-width: none;
  margin: 0 auto;
  color: #263238;
  font-size: 16px;
  line-height: 2.05;
}
.article-content-chunk {
  display: contents;
}
.article-time-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 28px;
  color: rgba(203, 213, 225, 0.78);
  font-size: 14px;
  font-weight: 700;
  line-height: 1.8;
}
.article-time-row span {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  box-sizing: border-box;
  min-height: 30px;
  padding: 0;
  border: 0;
  border-radius: 0;
  background: transparent;
  backdrop-filter: none;
}
.article-detail-layout {
  display: grid;
  grid-template-columns: 380px minmax(0, 1fr);
  align-items: start;
  gap: 24px;
  width: calc(100% - 48px);
  max-width: none;
  padding: 0;
}
.article-main {
  min-width: 0;
}
.article-layout-side {
  align-self: stretch;
  width: 380px;
}
.article-detail-layout .article-wrapper {
  padding: 54px 48px 46px;
}
.article-reading-card {
  position: relative;
  overflow: hidden;
  border: 1px solid rgba(148, 163, 184, 0.22);
  border-radius: 8px !important;
  box-shadow: 0 24px 70px rgba(15, 23, 42, 0.1) !important;
}
.article-reading-card:before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 5px;
  background: linear-gradient(90deg, #34d399, #38bdf8, #f59e0b);
}
.article-reading-card:hover {
  box-shadow: 0 28px 78px rgba(15, 23, 42, 0.14) !important;
}
.article-category a {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-height: 32px;
  padding: 0 14px;
  border: 1px solid rgba(148, 163, 184, 0.32);
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.5);
  color: rgba(203, 213, 225, 0.96) !important;
  font-family: "SF Pro Display", "Inter", "PingFang SC", "Microsoft YaHei",
    sans-serif !important;
  -webkit-text-fill-color: currentColor;
  white-space: nowrap;
  word-break: keep-all;
  text-shadow: none;
  backdrop-filter: blur(14px) saturate(150%);
  box-shadow: 0 10px 26px rgba(2, 6, 23, 0.16);
}
.article-tags {
  display: inline-flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 10px;
  margin-top: 24px;
  padding: 0 !important;
  border: 0 !important;
  background: transparent !important;
  backdrop-filter: none !important;
  max-width: min(920px, 82vw);
}
.article-tag-icon {
  display: none;
}
.article-tags a {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  box-sizing: border-box;
  min-height: 34px;
  height: auto;
  max-width: 280px;
  margin-left: 0;
  padding: 8px 14px;
  border: 1px solid rgba(16, 185, 129, 0.24);
  border-radius: 8px;
  background: rgba(16, 185, 129, 0.1);
  clip-path: none;
  color: #34d399 !important;
  font-size: 14px;
  font-weight: 600;
  line-height: 1.28;
  white-space: normal;
  overflow-wrap: anywhere;
  word-break: break-word;
  text-shadow: none;
  backdrop-filter: blur(8px);
  box-shadow: none;
}
.article-tags a:nth-of-type(2) {
  border-color: rgba(59, 130, 246, 0.24);
  background: rgba(59, 130, 246, 0.1);
  color: #60a5fa !important;
}
.article-tags a:nth-of-type(3) {
  border-color: rgba(168, 85, 247, 0.24);
  background: rgba(168, 85, 247, 0.1);
  color: #c084fc !important;
}
.article-tags a:nth-of-type(4) {
  border-color: rgba(249, 115, 22, 0.26);
  background: rgba(249, 115, 22, 0.1);
  color: #fb923c !important;
}
.article-tags a:nth-of-type(5) {
  border-color: rgba(236, 72, 153, 0.26);
  background: rgba(236, 72, 153, 0.1);
  color: #f472b6 !important;
}
.article-tags a:nth-of-type(6) {
  border-color: rgba(6, 182, 212, 0.26);
  background: rgba(6, 182, 212, 0.1);
  color: #22d3ee !important;
}
.article-tags a:nth-of-type(7) {
  border-color: rgba(234, 179, 8, 0.26);
  background: rgba(234, 179, 8, 0.1);
  color: #facc15 !important;
}
.article-tags a:nth-of-type(8n) {
  border-color: rgba(244, 63, 94, 0.26);
  background: rgba(244, 63, 94, 0.1);
  color: #fb7185 !important;
}
.article-tags a:nth-of-type(8n + 1) {
  border-color: rgba(16, 185, 129, 0.24);
  background: rgba(16, 185, 129, 0.1);
  color: #34d399 !important;
}
.article-tags a:nth-of-type(8n + 2) {
  border-color: rgba(59, 130, 246, 0.24);
  background: rgba(59, 130, 246, 0.1);
  color: #60a5fa !important;
}
.article-tags a:nth-of-type(8n + 3) {
  border-color: rgba(168, 85, 247, 0.24);
  background: rgba(168, 85, 247, 0.1);
  color: #c084fc !important;
}
.article-tags a:nth-of-type(8n + 4) {
  border-color: rgba(249, 115, 22, 0.26);
  background: rgba(249, 115, 22, 0.1);
  color: #fb923c !important;
}
.article-tags a:nth-of-type(8n + 5) {
  border-color: rgba(236, 72, 153, 0.26);
  background: rgba(236, 72, 153, 0.1);
  color: #f472b6 !important;
}
.article-tags a:nth-of-type(8n + 6) {
  border-color: rgba(6, 182, 212, 0.26);
  background: rgba(6, 182, 212, 0.1);
  color: #22d3ee !important;
}
.article-tags a:nth-of-type(8n + 7) {
  border-color: rgba(234, 179, 8, 0.26);
  background: rgba(234, 179, 8, 0.1);
  color: #facc15 !important;
}
.article-tag-v-icon {
  color: currentColor !important;
}
.article-summary {
  max-width: none;
  margin: 0 auto 30px;
  padding: 18px 20px;
  border: 1px solid #dbeafe;
  border-radius: 8px;
  background: linear-gradient(135deg, #f8fafc, #eff6ff);
}
.summary-label {
  margin-bottom: 8px;
  color: #0284c7;
  font-size: 13px;
  font-weight: 800;
}
.summary-text {
  color: #475569;
  font-size: 15px;
  line-height: 1.8;
}
.aritcle-copyright {
  position: relative;
  max-width: none;
  margin: 40px auto 10px;
  font-size: 0.875rem;
  line-height: 2;
  padding: 1rem 1.25rem;
  border: 1px solid #dbeafe;
  border-radius: 8px;
  background: linear-gradient(135deg, #f8fafc, #eff6ff);
}
.aritcle-copyright span {
  color: #0284c7;
  font-weight: bold;
}
.aritcle-copyright a {
  text-decoration: underline !important;
  color: #99a9bf !important;
}
.aritcle-copyright:before {
  position: absolute;
  top: 0.7rem;
  right: 0.7rem;
  width: 1rem;
  height: 1rem;
  border-radius: 1rem;
  background: #34d399;
  content: "";
}
.aritcle-copyright:after {
  position: absolute;
  top: 0.95rem;
  right: 0.95rem;
  width: 0.5rem;
  height: 0.5rem;
  border-radius: 0.5em;
  background: #fff;
  content: "";
}
.article-reward {
  margin-top: 4rem;
  display: flex;
  justify-content: center;
  align-items: center;
}
.reward-btn {
  position: relative;
  display: inline-block;
  width: 100px;
  border-radius: 999px;
  background: #0ea5e9;
  margin: 0 1rem;
  color: #fff !important;
  text-align: center;
  line-height: 36px;
  font-size: 0.875rem;
}
.reward-btn:hover .reward-main {
  display: block;
}
.reward-main {
  display: none;
  position: absolute;
  bottom: 40px;
  left: 0;
  margin: 0;
  padding: 0 0 15px;
  width: 100%;
}
.reward-all {
  display: inline-block;
  margin: 0 0 0 -110px;
  padding: 20px 10px 8px !important;
  width: 320px;
  border-radius: 4px;
  background: #f5f5f5;
}
.reward-all:before {
  position: absolute;
  bottom: -10px;
  left: 0;
  width: 100%;
  height: 20px;
  content: "";
}
.reward-all:after {
  content: "";
  position: absolute;
  right: 0;
  bottom: 2px;
  left: 0;
  margin: 0 auto;
  width: 0;
  height: 0;
  border-top: 13px solid #f5f5f5;
  border-right: 13px solid transparent;
  border-left: 13px solid transparent;
}
.reward-item {
  display: inline-block;
  padding: 0 8px;
  list-style-type: none;
}
.reward-img {
  width: 130px;
  height: 130px;
  display: block;
}
.reward-desc {
  margin: -5px 0;
  color: #858585;
  text-align: center;
}
.like-float {
  position: absolute;
  left: 50%;
  top: -8px;
  pointer-events: none;
  font-size: 22px;
  line-height: 1;
  filter: drop-shadow(0 8px 10px rgba(15, 23, 42, 0.2));
  animation: thumbFloat 0.85s cubic-bezier(0.2, 0.9, 0.25, 1) forwards;
}
@keyframes thumbFloat {
  0% {
    opacity: 0;
    transform: translate(-50%, 8px) scale(0.65) rotate(-8deg);
  }
  20% {
    opacity: 1;
  }
  55% {
    transform: translate(-50%, -22px) scale(1.18) rotate(7deg);
  }
  100% {
    opacity: 0;
    transform: translate(-50%, -42px) scale(1.38) rotate(0deg);
  }
}
.pagination-post {
  margin-top: 40px;
  overflow: hidden;
  width: 100%;
  border-radius: 8px;
  background: #020617;
}
.post {
  position: relative;
  height: 150px;
  overflow: hidden;
}
.post-info {
  position: absolute;
  top: 50%;
  padding: 20px 40px;
  width: 100%;
  transform: translate(0, -50%);
  line-height: 2;
  font-size: 14px;
}
.post-cover {
  position: absolute;
  width: 100%;
  height: 100%;
  opacity: 0.36;
  transition: all 0.6s;
  object-fit: cover;
}
.post a {
  position: relative;
  display: block;
  overflow: hidden;
  height: 150px;
}
.post:hover .post-cover {
  opacity: 0.74;
  transform: scale(1.1);
}
.label {
  font-size: 90%;
  color: #eee;
}
.post-title {
  font-weight: 500;
  color: #fff;
}
hr {
  position: relative;
  margin: 40px auto;
  border: 0;
  border-top: 1px dashed #bfdbfe;
  width: calc(100% - 4px);
}
.full {
  width: 100% !important;
}
.right-container {
  padding: 20px 24px;
  font-size: 14px;
}
.article-sidebar-sticky {
  position: sticky;
  top: var(--article-sidebar-top, 20px);
  max-height: calc(100vh - var(--article-sidebar-top, 20px) - 20px);
  transition: top 0.28s cubic-bezier(0.22, 0.61, 0.36, 1),
    max-height 0.28s cubic-bezier(0.22, 0.61, 0.36, 1);
}
.article-side-card {
  overflow: hidden;
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 8px !important;
}
.article-side-card:before {
  content: "";
  display: block;
  height: 4px;
  margin: -20px -24px 16px;
  background: linear-gradient(90deg, #34d399, #38bdf8);
}
.article-card-actions {
  position: absolute;
  top: 34px;
  right: 72px;
  z-index: 2;
  display: flex;
  gap: 8px;
}
.has-article-summary .article-card-actions {
  top: 14px;
  right: 14px;
  justify-content: flex-end;
}
.has-article-summary .side-action-btn {
  min-width: 34px;
  height: 32px;
  padding: 0 10px;
  font-size: 12px;
}
.has-article-summary .side-action-btn .v-icon {
  font-size: 15px !important;
}
.side-action-btn {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  min-width: 40px;
  height: 36px;
  padding: 0 12px;
  border: 1px solid #e2e8f0;
  border-radius: 999px;
  background: #f8fafc;
  color: #0f172a;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
}
.side-action-btn:hover {
  background: #e0f2fe;
}
.like-action-btn {
  border-color: rgba(251, 146, 60, 0.34);
  background: linear-gradient(
    135deg,
    rgba(253, 186, 116, 0.88),
    rgba(251, 146, 60, 0.8)
  );
  color: #fff;
  box-shadow: 0 10px 22px rgba(249, 115, 22, 0.16);
}
.like-action-btn:hover {
  background: linear-gradient(
    135deg,
    rgba(251, 146, 60, 0.92),
    rgba(234, 88, 12, 0.82)
  );
}
.action-count {
  min-width: 18px;
  padding: 2px 7px;
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.08);
  color: #0f172a;
  font-size: 12px;
  line-height: 1.2;
}
.like-action-btn .action-count {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
}
.toc-card {
  display: flex;
  flex-direction: column;
  max-height: calc(100vh - var(--article-sidebar-top, 20px) - 20px);
  margin-top: 0;
  padding: 0;
  background: #fff;
  box-shadow: 0 8px 30px rgba(15, 23, 42, 0.06) !important;
  transition: max-height 0.28s cubic-bezier(0.22, 0.61, 0.36, 1);
}
.toc-card:before {
  display: none;
}
.toc-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 24px 24px 16px;
  border-bottom: 1px solid rgba(241, 245, 249, 0.9);
  color: #1e293b;
  font-size: 18px;
  font-weight: 800;
  letter-spacing: 0;
}
.toc-doc-entry {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 20px 24px 14px;
}
.toc-doc-icon {
  flex: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: #ecfdf5;
}
.toc-doc-info {
  min-width: 0;
}
.toc-doc-title {
  color: #0f172a;
  font-size: 16px;
  font-weight: 800;
  line-height: 1.45;
}
.toc-doc-count {
  margin-top: 4px;
  color: #94a3b8;
  font-size: 13px;
  line-height: 1.4;
}
.right-title {
  display: flex;
  align-items: center;
  line-height: 2;
  font-size: 16.8px;
  margin-bottom: 6px;
  color: #0f172a;
  font-weight: 800;
}
.right-title i {
  font-weight: bold;
}
#toc {
  max-height: calc(100vh - var(--article-sidebar-top, 20px) - 170px);
  overflow-y: auto;
  direction: rtl;
  padding: 4px 16px 20px 20px;
  scroll-behavior: smooth;
  transition: max-height 0.28s cubic-bezier(0.22, 0.61, 0.36, 1);
}
.recommend-container {
  margin-top: 40px;
}
.recommend-title {
  font-size: 18px;
  line-height: 2;
  font-weight: bold;
  margin-bottom: 5px;
  color: #0f172a;
}
.recommend-cover {
  width: 100%;
  height: 100%;
  opacity: 0.4;
  transition: all 0.6s;
  object-fit: cover;
}
.recommend-info {
  line-height: 2;
  color: #fff;
  position: absolute;
  top: 50%;
  padding: 0 20px;
  width: 100%;
  transform: translate(0, -50%);
  text-align: center;
  font-size: 14px;
  text-shadow: 0 2px 14px rgba(2, 6, 23, 0.75);
}
.recommend-date {
  font-size: 90%;
}
.recommend-item:hover .recommend-cover {
  opacity: 0.8;
  transform: scale(1.1);
}
.article-item {
  display: flex;
  align-items: center;
  padding: 6px 0;
}
.article-item:first-child {
  padding-top: 0;
}
.article-item:last-child {
  padding-bottom: 0;
}
.article-item:not(:last-child) {
  border-bottom: 1px dashed #f5f5f5;
}
.article-item img {
  width: 100%;
  height: 100%;
  transition: all 0.6s;
  object-fit: cover;
}
.article-item img:hover {
  transform: scale(1.1);
}
.content {
  flex: 1;
  padding-left: 10px;
  word-break: break-all;
  display: -webkit-box;
  overflow: hidden;
  -webkit-box-orient: vertical;
}
.content-cover {
  width: 58.8px;
  height: 58.8px;
  overflow: hidden;
}
.content-title a {
  transition: all 0.2s;
  font-size: 95%;
}
.content-title a:hover {
  color: #2ba1d1;
}
.content-time {
  color: #858585;
  font-size: 85%;
  line-height: 2;
}
@media (max-width: 1199px) {
  .article-detail-layout {
    display: block;
    width: auto;
    max-width: 1200px;
    padding: 0 5px;
  }
  .article-layout-side {
    display: none;
  }
}
@media (max-width: 759px) {
  .article-detail-layout .article-wrapper {
    padding: 34px 18px 32px;
  }
  .article-card-actions {
    position: static;
    justify-content: flex-end;
    margin: 0 0 18px;
  }
  .side-action-btn {
    min-width: 34px;
    height: 32px;
    padding: 0 10px;
    font-size: 12px;
  }
}
</style>

<style>
.article-detail-page .article-content.markdown-body h1,
.article-detail-page .article-content.markdown-body h2,
.article-detail-page .article-content.markdown-body h3,
.article-detail-page .article-content.markdown-body h4,
.article-detail-page .article-content.markdown-body h5,
.article-detail-page .article-content.markdown-body h6 {
  color: #14532d;
  letter-spacing: 0;
  scroll-margin-top: 92px;
}

.article-detail-page .article-content.markdown-body h1 {
  margin: 48px 0 24px;
  padding-bottom: 14px;
  border-bottom: 2px solid #bbf7d0;
  font-size: 2em;
  font-weight: 900;
  line-height: 1.25;
}

.article-detail-page .article-content.markdown-body h2 {
  position: relative;
  margin: 44px 0 20px;
  padding: 0 0 10px 18px;
  border-bottom: 1px solid #dcfce7;
  font-size: 1.58em;
  font-weight: 850;
  line-height: 1.3;
}

.article-detail-page .article-content.markdown-body h2::before {
  position: absolute;
  left: 0;
  top: 0.2em;
  width: 5px;
  height: 1.05em;
  margin: 0;
  border-radius: 999px;
  background: #16a34a;
  vertical-align: 0;
}

.article-detail-page .article-content.markdown-body h3 {
  margin: 36px 0 16px;
  padding: 0 0 8px 12px;
  border-left: 4px solid #22c55e;
  border-bottom: 1px solid #dcfce7;
  font-size: 1.28em;
  font-weight: 800;
  line-height: 1.35;
}

.article-detail-page .article-content.markdown-body h4 {
  margin: 30px 0 12px;
  padding-left: 12px;
  border-left: 3px solid #86efac;
  color: #166534;
  font-size: 1.08em;
  font-weight: 760;
  line-height: 1.4;
}

.article-detail-page .article-content.markdown-body h5 {
  margin: 24px 0 10px;
  color: #15803d;
  font-size: 0.98em;
  font-weight: 720;
  line-height: 1.45;
}

.article-detail-page .article-content.markdown-body h5::before {
  display: inline-block;
  width: 6px;
  height: 6px;
  margin-right: 8px;
  border-radius: 999px;
  background: #22c55e;
  vertical-align: 0.16em;
  content: "";
}

.article-detail-page .article-content.markdown-body h6 {
  margin: 20px 0 8px;
  color: #166534;
  font-size: 0.92em;
  font-weight: 700;
  line-height: 1.45;
}

.article-detail-page .article-content.markdown-body p {
  margin-bottom: 26px !important;
  line-height: 2.05 !important;
}

.article-detail-page .article-content.markdown-body ul,
.article-detail-page .article-content.markdown-body ol {
  margin-left: 0;
  padding-left: 1.35em !important;
}

.article-detail-page .article-content.markdown-body li {
  padding-left: 0.15em;
}

.article-detail-page .article-content.markdown-body blockquote {
  padding: 14px 18px 12px;
  color: #475569;
  background: #f8fafc;
  border-left-color: #cbd5e1;
}

.article-detail-page .article-content.markdown-body blockquote > :last-child {
  margin-bottom: 0 !important;
}

.article-detail-page #toc .toc-list,
.article-detail-page #toc .toc-list-item,
.article-detail-page #toc .toc-link {
  direction: ltr;
}

.article-detail-page #toc .toc-list {
  margin: 0;
  padding-left: 0 !important;
}

.article-detail-page #toc .toc-list .toc-list {
  position: relative;
  margin: 4px 0 8px 22px;
  padding-left: 0 !important;
  border-left: 1px solid #e2e8f0;
}

.article-detail-page #toc .toc-list-item {
  position: relative;
  line-height: 1.45;
  list-style: none;
}

.article-detail-page #toc .toc-link {
  position: relative;
  display: block;
  box-sizing: border-box;
  width: 100%;
  margin: 2px 0;
  padding: 9px 10px;
  border-left: 0;
  border-radius: 8px;
  letter-spacing: 0;
  transition: color 0.08s ease, background 0.08s ease;
}

.article-detail-page #toc .toc-link:hover {
  background: #f1f5f9;
  color: #0f172a !important;
}

.article-detail-page #toc .toc-link.node-name--H1 {
  padding-left: 8px;
  color: #0f172a !important;
  font-size: 16px;
  font-weight: 800;
  line-height: 1.35;
}

.article-detail-page #toc .toc-link.node-name--H2 {
  margin-left: 0;
  padding: 8px 12px 8px 16px;
  border-radius: 0 8px 8px 0;
  color: #334155 !important;
  font-size: 14px;
  font-weight: 650;
  line-height: 1.75;
}

.article-detail-page #toc .toc-link.node-name--H3 {
  margin-left: 14px;
  width: calc(100% - 14px);
  padding: 7px 12px 7px 16px;
  border-radius: 0 8px 8px 0;
  color: #475569 !important;
  font-size: 13px;
  font-weight: 600;
  line-height: 1.65;
}

.article-detail-page #toc .toc-link.node-name--H4 {
  margin-left: 28px;
  width: calc(100% - 28px);
  padding: 6px 12px 6px 14px;
  border-radius: 0 8px 8px 0;
  color: #64748b !important;
  font-size: 12px;
  font-weight: 600;
  line-height: 1.55;
}

.article-detail-page #toc .toc-link.node-name--H5 {
  margin-left: 42px;
  width: calc(100% - 42px);
  padding: 5px 10px 5px 12px;
  border-radius: 0 8px 8px 0;
  color: #64748b !important;
  font-size: 12px;
  font-weight: 500;
  line-height: 1.45;
}

.article-detail-page #toc .toc-link.is-active-link {
  background: rgba(209, 250, 229, 0.72);
  color: #065f46 !important;
  font-weight: 800;
}

.article-detail-page #toc .toc-link.is-active-link:before {
  content: "";
  position: absolute;
  left: -1px;
  top: 20%;
  width: 3px;
  height: 60%;
  border-radius: 999px;
  background: #10b981;
}

.article-detail-page #toc .is-collapsed {
  display: block;
}

.article-detail-page #toc::-webkit-scrollbar {
  width: 6px;
}

.article-detail-page #toc::-webkit-scrollbar-thumb {
  border-radius: 999px;
  border: 2px solid transparent;
  background: linear-gradient(
      135deg,
      rgba(255, 255, 255, 0.68),
      rgba(168, 85, 247, 0.38) 42%,
      rgba(124, 58, 237, 0.5)
    )
    padding-box;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.72),
    0 0 16px rgba(168, 85, 247, 0.2);
}

.article-detail-page #toc::-webkit-scrollbar-track {
  background: transparent;
}
</style>

<style lang="scss">
pre.hljs {
  padding: 14px 2px 14px 42px !important;
  border-radius: 5px !important;
  position: relative;
  background: #f6f8fa !important;
  border: 1px solid #e5e7eb !important;
  color: #24292f !important;
  font-size: 14px !important;
  font-family: ui-monospace, SFMono-Regular, "SF Mono", Menlo, Consolas,
    "Liberation Mono", monospace !important;
  line-height: 24px !important;
  overflow: hidden !important;
  &:hover .copy-btn {
    display: flex;
    justify-content: center;
    align-items: center;
  }
  code {
    background: transparent !important;
    display: block !important;
    margin: 0 10px !important;
    overflow-x: auto !important;
    span {
      background: transparent !important;
    }
    &::-webkit-scrollbar {
      z-index: 11;
      width: 6px;
    }
    &::-webkit-scrollbar:horizontal {
      height: 6px;
    }
    &::-webkit-scrollbar-thumb {
      border-radius: 5px;
      width: 6px;
      border: 1px solid transparent;
      background: linear-gradient(
          135deg,
          rgba(255, 255, 255, 0.68),
          rgba(168, 85, 247, 0.38) 42%,
          rgba(124, 58, 237, 0.5)
        )
        padding-box;
      box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.72);
    }
    &::-webkit-scrollbar-corner,
    &::-webkit-scrollbar-track {
      background: transparent;
    }
    &::-webkit-scrollbar-track-piece {
      background: transparent;
      width: 6px;
    }
  }
  .line-numbers-rows {
    position: absolute;
    pointer-events: none;
    top: 12px;
    bottom: 14px;
    left: 0;
    font-size: 100%;
    width: 42px;
    text-align: center;
    letter-spacing: -1px;
    border-right: 1px solid #d0d7de;
    user-select: none;
    counter-reset: linenumber;
    span {
      pointer-events: none;
      display: block;
      counter-increment: linenumber;
      &:before {
        content: counter(linenumber);
        color: #8c959f;
        display: block;
        text-align: center;
      }
    }
  }
  b.name {
    display: none;
  }
  .copy-btn {
    position: absolute;
    top: 6px;
    right: 6px;
    z-index: 1;
    color: #57606a;
    background-color: #eaeef2;
    border-radius: 6px;
    display: none;
    font-size: 14px;
    width: 32px;
    height: 24px;
    outline: none;
  }
}
</style>
