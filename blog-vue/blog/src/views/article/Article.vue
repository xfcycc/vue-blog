<template>
  <div class="article-detail-page">
    <!-- 封面图 -->
    <div class="banner" :style="articleCover">
      <div class="article-banner-grid"></div>
      <div class="article-banner-glow"></div>
      <div class="article-info-container">
        <!-- 文章标题 -->
        <div class="article-title">{{ article.articleTitle }}</div>
        <div class="article-info">
          <div class="article-taxonomy-row">
            <!-- 文章分类 -->
            <span class="article-category">
              <i class="iconfont iconfenlei1" />
              <router-link :to="'/categories/' + article.categoryId">
                {{ article.categoryName }}
              </router-link>
            </span>
            <!-- 文章标签 -->
            <span class="article-tags">
              <i class="iconfont iconbiaoqian article-tag-icon" />
              <template v-if="article.tagDTOList && article.tagDTOList.length">
                <router-link
                  v-for="item of article.tagDTOList"
                  :key="item.id"
                  :to="'/tags/' + item.id"
                >
                  {{ item.tagName }}
                </router-link>
              </template>
              <template v-else>暂无标签</template>
            </span>
          </div>
        </div>
      </div>
    </div>
    <!-- 内容 -->
    <v-row class="article-container">
      <v-col md="9" cols="12">
        <v-card class="article-wrapper article-reading-card">
          <div class="article-summary" v-if="articleSummary">
            <div class="summary-label">摘要</div>
            <div class="summary-text">{{ articleSummary }}</div>
          </div>
          <article
            id="write"
            class="article-content markdown-body"
            v-html="article.articleContent"
            ref="article"
          />
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
      </v-col>
      <!-- 侧边功能 -->
      <v-col md="3" cols="12" class="d-md-block d-none">
        <div class="article-sidebar-sticky">
          <v-card class="right-container article-side-card article-stat-card">
            <div class="article-stat-list">
              <div class="article-stat-item">
                <span>字数</span>
                <strong>{{ wordNum | num }}</strong>
              </div>
              <div class="article-stat-item">
                <span>阅读时长</span>
                <strong>{{ readTime }}</strong>
              </div>
              <div class="article-stat-item">
                <span>阅读量</span>
                <strong>{{ article.viewsCount || 0 }}</strong>
              </div>
              <div class="article-stat-item">
                <span>评论数</span>
                <strong>{{ commentCount || 0 }}</strong>
              </div>
              <div class="article-stat-item article-stat-wide">
                <span>发表时间</span>
                <strong>{{ article.createTime | date }}</strong>
              </div>
              <div class="article-stat-item article-stat-wide">
                <span>更新时间</span>
                <strong>
                  <template v-if="article.updateTime">
                    {{ article.updateTime | date }}
                  </template>
                  <template v-else>
                    {{ article.createTime | date }}
                  </template>
                </strong>
              </div>
            </div>
            <div class="article-side-actions">
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
                <span>分享</span>
              </button>
            </div>
          </v-card>
          <!-- 文章目录 -->
          <v-card class="right-container article-side-card toc-card">
            <div class="right-title">
              <i class="iconfont iconhanbao" style="font-size:16.8px" />
              <span style="margin-left:10px">目录</span>
            </div>
            <div id="toc" />
          </v-card>
          <!-- 最新文章 -->
          <v-card class="right-container article-side-card newest-card">
            <div class="right-title">
              <i class="iconfont icongengxinshijian" style="font-size:16.8px" />
              <span style="margin-left:10px">最新文章</span>
            </div>
            <div class="article-list">
              <div
                class="article-item"
                v-for="item of article.newestArticleList"
                :key="item.id"
              >
                <router-link :to="'/articles/' + item.id" class="content-cover">
                  <img :src="item.articleCover" />
                </router-link>
                <div class="content">
                  <div class="content-title">
                    <router-link :to="'/articles/' + item.id">
                      {{ item.articleTitle }}
                    </router-link>
                  </div>
                  <div class="content-time">{{ item.createTime | date }}</div>
                </div>
              </div>
            </div>
          </v-card>
        </div>
      </v-col>
    </v-row>
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
  destroyed() {
    if (this.clipboard) {
      this.clipboard.destroy();
    }
    window.removeEventListener("scroll", this.requestSyncTocActiveLink);
    if (this.tocScrollRaf) {
      window.cancelAnimationFrame(this.tocScrollRaf);
    }
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
      readTime: "",
      commentType: 1,
      articleHref: window.location.href,
      clipboard: null,
      commentCount: 0,
      likeEffectId: 0,
      likeEffects: [],
      tocScrollRaf: null
    };
  },
  methods: {
    getArticle() {
      const that = this;
      //查询文章
      this.axios.get("/api" + this.$route.path).then(({ data }) => {
        document.title = data.data.articleTitle;
        //将markdown转换为Html
        this.article = data.data;
        this.article.articleContent = markdownToHtml(
          this.article.articleContent
        );
        this.$nextTick(() => {
          // 统计文章字数
          this.wordNum = this.deleteHTMLTag(this.article.articleContent).length;
          // 计算阅读时间
          this.readTime = Math.round(this.wordNum / 400) + "分钟";
          // 添加代码复制功能
          this.clipboard = new Clipboard(".copy-btn");
          this.clipboard.on("success", () => {
            this.$toast({ type: "success", message: "复制成功" });
          });
          // 添加文章生成目录功能
          let nodes = this.$refs.article.children;
          if (nodes.length) {
            for (let i = 0; i < nodes.length; i++) {
              let node = nodes[i];
              let reg = /^H[1-4]{1}$/;
              if (reg.exec(node.tagName)) {
                node.id = i;
              }
            }
          }
          tocbot.init({
            tocSelector: "#toc", //要把目录添加元素位置，支持选择器
            contentSelector: ".article-content", //获取html的元素
            headingSelector: "h1, h2, h3", //要显示的id的目录
            hasInnerContainers: true,
            disableTocScrollSync: true,
            onClick: function(e) {
              e.preventDefault();
            }
          });
          window.removeEventListener("scroll", this.requestSyncTocActiveLink);
          window.addEventListener("scroll", this.requestSyncTocActiveLink, {
            passive: true
          });
          this.requestSyncTocActiveLink();
          // 添加图片预览功能
          const imgList = this.$refs.article.getElementsByTagName("img");
          for (var i = 0; i < imgList.length; i++) {
            this.imgList.push(imgList[i].src);
            imgList[i].addEventListener("click", function(e) {
              that.previewImg(e.target.currentSrc);
            });
          }
        });
        });
    },
    requestSyncTocActiveLink() {
      if (this.tocScrollRaf) {
        return;
      }
      this.tocScrollRaf = window.requestAnimationFrame(() => {
        this.tocScrollRaf = null;
        this.syncTocActiveLink();
      });
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
      toc.scrollTop = Math.max(activeCenter - toc.clientHeight / 2, 0);
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
    }
  }
};
</script>

<style scoped>
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
  opacity: 0.32;
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
.article-info-container {
  z-index: 3;
  box-sizing: border-box;
}
.article-info i {
  font-size: 14px;
}
.article-info {
  display: flex;
  align-items: center;
  justify-content: center;
  max-width: 900px;
  margin: 0 auto;
  font-size: 13px;
  line-height: 1.6;
}
.article-info > div {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: center;
  gap: 12px;
}
.article-info span:not(.separator) {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  box-sizing: border-box;
  padding: 5px 11px;
  border: 1px solid rgba(226, 232, 240, 0.22);
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.48);
  color: #e5e7eb;
  backdrop-filter: blur(10px);
}
.article-category {
  box-sizing: border-box;
  height: 34px;
  padding: 0 15px !important;
  border-color: rgba(52, 211, 153, 0.5) !important;
  background: rgba(16, 185, 129, 0.28) !important;
  font-size: 13px !important;
  font-weight: 700;
  line-height: 1;
  box-shadow: inset 0 0 0 1px rgba(187, 247, 208, 0.12);
}
.article-info .separator {
  display: none;
}
@media (min-width: 760px) {
  .banner {
    color: #eee !important;
  }
  .article-info span {
    font-size: 95%;
  }
  .article-info-container {
    position: absolute;
    bottom: 5.1rem;
    padding: 0 8%;
    width: 100%;
    text-align: center;
  }
  .article-title {
    max-width: 900px;
    margin: 0 auto 28px;
    font-family: "SF Pro Display", "Inter", "PingFang SC", "Microsoft YaHei",
      sans-serif !important;
    font-size: 48px;
    font-weight: 900;
    letter-spacing: 0;
    line-height: 1.12;
    text-shadow: 0 4px 28px rgba(2, 6, 23, 0.62);
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
    height: 360px;
  }
  .article-info span {
    font-size: 90%;
  }
  .separator:first-child {
    display: none;
  }
  .blog-container {
    margin: 322px 5px 0 5px;
  }
  .article-info-container {
    position: absolute;
    bottom: 1.3rem;
    padding: 0 5%;
    width: 100%;
    color: #eee;
    text-align: left;
  }
  .article-title {
    font-size: 1.7rem;
    font-weight: 900;
    line-height: 1.24;
    margin-bottom: 0.8rem;
    text-shadow: 0 3px 18px rgba(2, 6, 23, 0.58);
  }
  .article-info {
    align-items: center;
    font-size: 12px;
  }
  .article-info > div {
    gap: 7px;
  }
  .article-info span:not(.separator) {
    min-width: 0;
    width: 100%;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    justify-content: center;
    padding: 4px 9px;
  }
  .article-category {
    width: auto !important;
  }
  .article-tags {
    width: auto !important;
    justify-content: flex-start !important;
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
  max-width: 860px;
  margin: 0 auto;
  color: #263238;
  font-size: 16px;
  line-height: 1.9;
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
  color: #fff !important;
}
.article-tags {
  padding: 0 !important;
  border: 0 !important;
  background: transparent !important;
  backdrop-filter: none !important;
  max-width: 520px;
}
.article-tag-icon {
  display: none;
}
.article-tags a {
  display: inline-flex;
  align-items: center;
  box-sizing: border-box;
  height: 34px;
  margin-left: 8px;
  padding: 0 15px;
  border: 1px solid rgba(255, 255, 255, 0.36);
  border-radius: 999px;
  background: linear-gradient(
    135deg,
    rgba(255, 255, 255, 0.28),
    rgba(125, 211, 252, 0.14)
  );
  color: #f8fafc !important;
  font-size: 13px;
  font-weight: 700;
  line-height: 1;
  backdrop-filter: blur(14px) saturate(150%);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.34),
    0 10px 24px rgba(2, 6, 23, 0.14);
}
.article-summary {
  max-width: 860px;
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
  margin-top: 40px;
  margin-bottom: 10px;
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
  top: 20px;
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
.article-stat-card {
  margin-bottom: 20px;
}
.article-side-actions {
  display: flex;
  gap: 10px;
  margin-top: 12px;
}
.side-action-btn {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 1;
  gap: 7px;
  height: 40px;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
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
.article-stat-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}
.article-stat-item {
  padding: 10px 8px;
  border-radius: 8px;
  background: #f8fafc;
  text-align: center;
}
.article-stat-item span {
  display: block;
  margin-bottom: 4px;
  color: #64748b;
  font-size: 12px;
}
.article-stat-item strong {
  color: #0f172a;
  font-size: 14px;
}
.article-stat-wide {
  grid-column: 1 / -1;
  display: grid;
  grid-template-columns: max-content max-content;
  align-items: center;
  justify-content: center;
  column-gap: 18px;
  text-align: center;
}
.article-stat-wide span {
  margin-bottom: 0;
}
.toc-card {
  margin-top: 0;
}
.newest-card {
  margin-top: 20px;
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
  max-height: 50vh;
  overflow-y: auto;
  padding-right: 4px;
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
</style>

<style>
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
}

.article-detail-page .article-content.markdown-body blockquote > :last-child {
  margin-bottom: 0 !important;
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
      background: #c9d1d9;
    }
    &::-webkit-scrollbar-corner,
    &::-webkit-scrollbar-track {
      background: #f6f8fa;
    }
    &::-webkit-scrollbar-track-piece {
      background: #f6f8fa;
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
    position: absolute;
    top: 8px;
    right: 45px;
    z-index: 1;
    font-size: 12px;
    color: #57606a;
    pointer-events: none;
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
