<template>
  <div>
    <!-- banner -->
    <div class="banner" :style="cover">
      <h1 class="banner-title">关于我</h1>
    </div>
    <!-- 关于我内容 -->
    <v-card class="blog-container">
      <!-- 博主头像 -->
      <div class="my-wrapper">
        <v-avatar size="110">
          <img class="author-avatar" :src="avatar" />
        </v-avatar>
      </div>
      <!-- 介绍 -->
      <div
        ref="about"
        class="about-content markdown-body"
        v-html="aboutContent"
      />
    </v-card>
  </div>
</template>

<script>
import Clipboard from "clipboard";
import markdownToHtml from "../../utils/markdown";
export default {
  created() {
    this.getAboutContent();
  },
  destroyed() {
    this.clipboard.destroy();
  },
  data: function() {
    return {
      aboutContent: "",
      clipboard: null,
      imgList: []
    };
  },
  methods: {
    getAboutContent() {
      const that = this;
      this.axios.get("/api/about").then(({ data }) => {
        this.aboutContent = markdownToHtml(data.data);
        this.$nextTick(() => {
          // 添加代码复制功能
          this.clipboard = new Clipboard(".copy-btn");
          this.clipboard.on("success", () => {
            this.$toast({ type: "success", message: "复制成功" });
          });
          // 添加图片预览功能
          const imgList = this.$refs.about.getElementsByTagName("img");
          for (var i = 0; i < imgList.length; i++) {
            this.imgList.push(imgList[i].src);
            imgList[i].addEventListener("click", function(e) {
              that.previewImg(e.target.currentSrc);
            });
          }
        });
      });
    },
    previewImg(img) {
      this.$imagePreview({
        images: this.imgList,
        index: this.imgList.indexOf(img)
      });
    }
  },
  computed: {
    avatar() {
      return this.$store.state.blogInfo.websiteConfig.websiteAvatar;
    },
    cover() {
      var cover = "";
      this.$store.state.blogInfo.pageList.forEach(item => {
        if (item.pageLabel == "about") {
          cover = item.pageCover;
        }
      });
      return "background: url(" + cover + ") center center / cover no-repeat";
    }
  }
};
</script>

<style scoped>
.about-content {
  word-break: break-word;
  line-height: 1.8;
}
.my-wrapper {
  text-align: center;
}
.author-avatar {
  transition: all 0.5s;
}
.author-avatar:hover {
  transform: rotate(360deg);
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
