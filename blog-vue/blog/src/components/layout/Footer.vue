<template>
  <v-footer app padless absolute v-if="!this.isMessage">
    <div
      class="footer-wrap"
      :class="{
        'tag-footer-wrap': isTagPage,
        'category-footer-wrap': isCategoryPage,
        'archive-footer-wrap': isArchivePage
      }"
    >
      <div>
        ©{{ blogInfo.websiteConfig.websiteCreateTime | year }} -
        {{ new Date().getFullYear() }} By
        {{ blogInfo.websiteConfig.websiteAuthor }}
      </div>
      <a href="https://beian.miit.gov.cn/" target="_blank">
        {{ blogInfo.websiteConfig.websiteRecordNo }}
      </a>
      <a href="https://icp.gov.moe/?keyword=20235555" target="_blank"
        >萌ICP备20235555号</a
      >
    </div>
  </v-footer>
</template>

<script>
export default {
  computed: {
    isMessage() {
      return this.$route.path == "/message";
    },
    isTagPage() {
      return this.$route.path == "/tags";
    },
    isCategoryPage() {
      return this.$route.path.indexOf("/categories") === 0;
    },
    isArchivePage() {
      return this.$route.path == "/archives";
    },
    blogInfo() {
      return this.$store.state.blogInfo;
    }
  }
};
</script>

<style scoped>
.footer-wrap {
  width: 100%;
  line-height: 2;
  position: relative;
  padding: 40px 20px;
  color: #eee;
  font-size: 14px;
  text-align: center;
  background: linear-gradient(-45deg, #ee7752, #ce3e75, #23a6d5, #23d5ab);
  background-size: 400% 400%;
  animation: Gradient 10s ease infinite;
}
.footer-wrap a {
  color: #eee !important;
}

.footer-wrap a:nth-child(3) {
  color: #ffc0cb !important;
}

.tag-footer-wrap {
  overflow: hidden;
  color: rgba(255, 255, 255, 0.86);
  background: url("https://pic.caiguoyu.cn/20260518204951070.jpg")
    center bottom / cover no-repeat;
  animation: none;
  box-shadow: inset 0 26px 44px rgba(2, 6, 23, 0.44);
}
.tag-footer-wrap::before,
.tag-footer-wrap::after {
  content: "";
  position: absolute;
  inset: -40%;
  pointer-events: none;
}
.tag-footer-wrap::before {
  background: linear-gradient(
    180deg,
    rgba(2, 6, 23, 0.62),
    rgba(2, 6, 23, 0.86)
  );
}
.tag-footer-wrap::after {
  opacity: 0.36;
  background-image: radial-gradient(
    circle,
    rgba(255, 255, 255, 0.86) 1px,
    transparent 1.6px
  );
  background-size: 94px 94px;
  animation: footer-star-drift 44s linear infinite;
}
.tag-footer-wrap div,
.tag-footer-wrap a {
  position: relative;
  z-index: 1;
}
.tag-footer-wrap a {
  color: rgba(255, 255, 255, 0.86) !important;
}
.tag-footer-wrap a:nth-child(3) {
  color: #bfdbfe !important;
}
.category-footer-wrap {
  overflow: hidden;
  color: rgba(255, 255, 255, 0.92);
  background: url("https://pic.caiguoyu.cn/20260518204951097.jpg")
    center 78% / cover no-repeat;
  animation: none;
  box-shadow: inset 0 26px 48px rgba(3, 6, 20, 0.72),
    inset 0 -18px 42px rgba(255, 43, 214, 0.28);
}
.category-footer-wrap::before,
.category-footer-wrap::after {
  content: "";
  position: absolute;
  inset: 0;
  pointer-events: none;
}
.category-footer-wrap::before {
  background: linear-gradient(
    100deg,
    rgba(3, 6, 20, 0.88),
    rgba(255, 43, 214, 0.2) 48%,
    rgba(0, 245, 255, 0.24)
  );
}
.category-footer-wrap::after {
  opacity: 0.48;
  background-image: linear-gradient(
      rgba(0, 245, 255, 0.16) 1px,
      transparent 1px
    ),
    linear-gradient(90deg, rgba(255, 43, 214, 0.12) 1px, transparent 1px);
  background-size: 42px 42px;
  animation: footer-grid-drift 14s linear infinite;
}
.category-footer-wrap div,
.category-footer-wrap a {
  position: relative;
  z-index: 1;
  text-shadow: 2px 0 #ff2bd6, 0 0 14px rgba(0, 245, 255, 0.58);
}
.category-footer-wrap a {
  color: #00f5ff !important;
}
.category-footer-wrap a:nth-child(3) {
  color: #ff8df0 !important;
}
.archive-footer-wrap {
  overflow: hidden;
  color: rgba(226, 232, 240, 0.92);
  background: linear-gradient(
      180deg,
      rgba(2, 6, 23, 0.72),
      rgba(2, 6, 23, 0.92)
    ),
    url("../../assets/images/archive-hacker-bg.png") center bottom / cover
      no-repeat;
  animation: none;
  box-shadow: inset 0 28px 52px rgba(3, 7, 18, 0.82);
}
.archive-footer-wrap::before,
.archive-footer-wrap::after {
  content: "";
  position: absolute;
  inset: 0;
  pointer-events: none;
}
.archive-footer-wrap::before {
  background-image: linear-gradient(
      rgba(16, 185, 129, 0.12) 1px,
      transparent 1px
    ),
    linear-gradient(90deg, rgba(16, 185, 129, 0.08) 1px, transparent 1px);
  background-size: 32px 32px;
}
.archive-footer-wrap::after {
  background: radial-gradient(
    circle at 50% 0,
    rgba(52, 211, 153, 0.2),
    transparent 42%
  );
}
.archive-footer-wrap div,
.archive-footer-wrap a {
  position: relative;
  z-index: 1;
}
.archive-footer-wrap a {
  color: rgba(167, 243, 208, 0.92) !important;
}
.archive-footer-wrap a:nth-child(3) {
  color: #67e8f9 !important;
}

@keyframes Gradient {
  0% {
    background-position: 0 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0 50%;
  }
}
@keyframes footer-star-drift {
  0% {
    transform: translate3d(0, 0, 0);
  }
  100% {
    transform: translate3d(94px, 94px, 0);
  }
}
@keyframes footer-grid-drift {
  0% {
    transform: translate3d(0, 0, 0);
  }
  100% {
    transform: translate3d(42px, 42px, 0);
  }
}
</style>
