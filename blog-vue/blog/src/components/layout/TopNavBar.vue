<template>
  <v-app-bar
    app
    :class="[navClass, currentRouteStyle.className]"
    hide-on-scroll
    flat
    height="60"
  >
    <!-- 手机端导航栏 -->
    <div class="d-md-none nav-mobile-container">
      <div class="mobile-brand">
        <router-link to="/">
          {{ blogInfo.websiteConfig.websiteAuthor }}
        </router-link>
        <span>{{ currentRouteStyle.label }}</span>
      </div>
      <div class="mobile-nav-actions">
        <button
          v-if="isArticleDetailPage"
          type="button"
          class="mobile-nav-action mobile-toc-nav-btn"
          @click="toggleArticleToc"
          aria-label="打开文章目录"
        >
          <v-icon size="17" color="currentColor">mdi-menu</v-icon>
          <span>目录</span>
        </button>
        <a
          v-else
          class="mobile-nav-action mobile-drawer-btn"
          @click="openDrawer"
        >
          <i class="iconfont iconhanbao" />
        </a>
      </div>
    </div>
    <!-- 电脑导航栏 -->
    <div class="d-md-block d-none nav-container">
      <div class="float-left blog-title">
        <router-link to="/">
          {{ blogInfo.websiteConfig.websiteAuthor }}
        </router-link>
        <span class="nav-context">
          {{ currentRouteStyle.tagline }}
        </span>
      </div>
      <div class="float-right nav-title">
        <div
          v-for="item of navItems"
          :key="item.to"
          class="menus-item"
        >
          <router-link
            class="menu-btn"
            :class="{ active: isActive(item) }"
            :to="item.to"
          >
            <i :class="['iconfont', item.icon]" /> {{ item.label }}
          </router-link>
        </div>
      </div>
    </div>
  </v-app-bar>
</template>

<script>
export default {
  mounted() {
    window.addEventListener("scroll", this.scroll);
    this.scroll();
  },
  beforeDestroy() {
    window.removeEventListener("scroll", this.scroll);
  },
  data: function() {
    return {
      navClass: "nav",
      navItems: [
        { to: "/", label: "星港", icon: "iconzhuye", exact: true },
        { to: "/archives", label: "星历", icon: "iconguidang" },
        { to: "/reading", label: "书叶", icon: "iconbiaoqian" },
        { to: "/categories", label: "星图", icon: "iconfenlei" },
        { to: "/tags", label: "星标", icon: "iconbiaoqian" },
        { to: "/links", label: "星链", icon: "iconlianjie" },
        { to: "/about", label: "星星", icon: "iconyueliang" },
        { to: "/message", label: "星声", icon: "iconqunliao" }
      ],
      routeStyles: [
        {
          match: path => path === "/",
          label: "星港",
          tagline: "从这里启程去看灵感发光",
          className: "nav-theme-home"
        },
        {
          match: path => path.startsWith("/articles/"),
          label: "正文",
          tagline: "安静阅读一段想法",
          className: "nav-theme-article"
        },
        {
          match: path => path.startsWith("/archives"),
          label: "星历",
          tagline: "把时间折进一册航行日志",
          className: "nav-theme-archive"
        },
        {
          match: path => path.startsWith("/reading"),
          label: "书叶",
          tagline: "夹住想反复回看的段落",
          className: "nav-theme-reading"
        },
        {
          match: path => path.startsWith("/categories"),
          label: "星图",
          tagline: "沿着主题航线寻找答案",
          className: "nav-theme-category"
        },
        {
          match: path => path.startsWith("/tags"),
          label: "星标",
          tagline: "在关键词坐标里捕捉灵感",
          className: "nav-theme-tag"
        },
        {
          match: path => path.startsWith("/links"),
          label: "星链",
          tagline: "接入星河里的朋友信号",
          className: "nav-theme-link"
        },
        {
          match: path => path.startsWith("/about"),
          label: "星星",
          tagline: "看看这趟航行的驾驶者",
          className: "nav-theme-about"
        },
        {
          match: path => path.startsWith("/message"),
          label: "星声",
          tagline: "向这片星域留下一段讯息",
          className: "nav-theme-message"
        },
        {
          match: path => path.startsWith("/albums"),
          label: "相册",
          tagline: "收集片刻的光",
          className: "nav-theme-album"
        },
        {
          match: path => path.startsWith("/talks"),
          label: "说说",
          tagline: "短句也有气候",
          className: "nav-theme-talk"
        }
      ]
    };
  },
  methods: {
    scroll() {
      const that = this;
      let scrollTop =
        window.pageYOffset ||
        document.documentElement.scrollTop ||
        document.body.scrollTop;
      that.scrollTop = scrollTop;
      if (that.scrollTop > 60) {
        that.navClass = "nav-fixed";
      } else {
        that.navClass = "nav";
      }
    },
    openDrawer() {
      this.$store.state.drawer = true;
    },
    toggleArticleToc() {
      window.dispatchEvent(new CustomEvent("toggle-article-toc"));
    },
    isActive(item) {
      if (item.exact) {
        return this.$route.path === item.to;
      }
      return this.$route.path.startsWith(item.to);
    }
  },
  computed: {
    currentRouteStyle() {
      return (
        this.routeStyles.find(item => item.match(this.$route.path)) ||
        this.routeStyles[0]
      );
    },
    isArticleDetailPage() {
      return this.$route.path.startsWith("/articles/");
    },
    blogInfo() {
      return this.$store.state.blogInfo;
    }
  }
};
</script>

<style scoped>
i {
  margin-right: 4px;
}
ul {
  list-style: none;
}
.nav {
  background: linear-gradient(
    120deg,
    var(--glass-start),
    var(--glass-end)
  ) !important;
  border-bottom: none;
  box-shadow: 0 12px 36px var(--glass-shadow);
  backdrop-filter: blur(18px) saturate(145%);
  -webkit-backdrop-filter: blur(18px) saturate(145%);
}
.nav,
.nav-fixed {
  overflow: hidden;
  color: var(--nav-text) !important;
}
.nav::before,
.nav-fixed::before {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background:
    radial-gradient(circle at 14% 18%, var(--liquid-glow), transparent 28%),
    radial-gradient(circle at 76% 8%, var(--liquid-glow-soft), transparent 22%),
    linear-gradient(110deg, transparent 0%, rgba(255, 255, 255, 0.18) 42%, transparent 56%);
  content: "";
  opacity: 0.9;
  transform: translateX(var(--liquid-shift));
  transition: transform 0.4s ease, opacity 0.3s ease;
}
.nav-fixed::before {
  opacity: 0.72;
}
.nav a,
.nav-fixed a {
  color: var(--nav-text) !important;
}
.nav-fixed a {
  color: var(--fixed-nav-text, var(--nav-text)) !important;
}
.nav .menu-btn {
  text-shadow: 0 8px 24px var(--text-glow);
}
.nav .blog-title a {
  text-shadow: 0 8px 30px var(--brand-glow);
}
.nav-fixed {
  background: linear-gradient(
    120deg,
    var(--fixed-glass-start),
    var(--fixed-glass-end)
  ) !important;
  border-bottom: none;
  box-shadow: 0 10px 34px var(--fixed-shadow);
  backdrop-filter: blur(22px) saturate(160%);
  -webkit-backdrop-filter: blur(22px) saturate(160%);
}
.nav-fixed .menus-item a,
.nav-fixed .blog-title a {
  text-shadow: none;
}
.nav-container {
  position: relative;
  z-index: 1;
  font-size: 14px;
  width: 100%;
  height: 100%;
}
.nav-mobile-container {
  position: relative;
  z-index: 1;
  width: 100%;
  display: flex;
  align-items: center;
}
.mobile-brand {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}
.mobile-brand a {
  font-size: 18px;
  font-weight: bold;
}
.mobile-brand span,
.nav-context {
  background: linear-gradient(90deg, var(--copy-start), var(--copy-end));
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  text-shadow: 0 8px 24px var(--copy-glow);
}
.nav-fixed .mobile-brand span,
.nav-fixed .nav-context {
  background: linear-gradient(
    90deg,
    var(--fixed-copy-start, var(--copy-start)),
    var(--fixed-copy-end, var(--copy-end))
  );
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}
.mobile-brand span {
  margin-top: 2px;
  font-size: 11px;
}
.mobile-nav-actions {
  display: flex;
  align-items: center;
  margin-left: auto;
}
.mobile-nav-action {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 34px;
  margin-left: 10px;
  border: 0;
  background: transparent;
  color: inherit;
  cursor: pointer;
}
.mobile-drawer-btn {
  font-size: 20px;
}
.mobile-toc-nav-btn {
  gap: 5px;
  padding: 0 10px;
  border: 1px solid var(--active-border);
  border-radius: 999px;
  background: linear-gradient(
    135deg,
    var(--active-glass-start),
    var(--active-glass-end)
  );
  color: var(--active-text);
  font-size: 13px;
  font-weight: 800;
  line-height: 1;
  box-shadow: 0 8px 24px var(--active-shadow);
}
.blog-title,
.nav-title {
  display: flex;
  align-items: center;
  height: 100%;
}
.blog-title a {
  font-size: 18px;
  font-weight: bold;
}
.nav-context {
  position: relative;
  margin-left: 14px;
  padding-left: 14px;
  font-size: 12px;
  font-weight: 600;
}
.nav-context::before {
  position: absolute;
  top: 50%;
  left: 0;
  width: 1px;
  height: 16px;
  background: var(--glass-border);
  content: "";
  transform: translateY(-50%);
}
.menus-item {
  position: relative;
  display: inline-block;
  margin: 0 0 0 0.5rem;
}
.menus-item a {
  transition: color 0.2s ease, background 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}
.menu-btn {
  position: relative;
  z-index: 0;
  display: inline-flex;
  align-items: center;
  min-height: 34px;
  padding: 0 12px;
  border: 1px solid transparent;
  border-radius: 999px;
  overflow: hidden;
}
.menu-btn:hover,
.menu-btn.active {
  color: var(--active-text) !important;
  background:
    linear-gradient(135deg, var(--active-glass-start), var(--active-glass-end));
  border-color: var(--active-border);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.46),
    0 10px 30px var(--active-shadow);
  transform: translateY(-1px);
}
.menu-btn::before {
  position: absolute;
  inset: 1px;
  z-index: -1;
  content: "";
  border-radius: inherit;
  background:
    radial-gradient(circle at 22% 18%, rgba(255, 255, 255, 0.72), transparent 28%),
    radial-gradient(circle at 78% 70%, var(--active-liquid), transparent 34%);
  opacity: 0;
  transition: opacity 0.2s ease;
}
.menu-btn:hover::before,
.menu-btn.active::before {
  opacity: 1;
}
.user-avatar {
  cursor: pointer;
  border-radius: 50%;
}
.nav-theme-home {
  --glass-start: rgba(14, 116, 144, 0.18);
  --glass-end: rgba(124, 58, 237, 0.14);
  --fixed-glass-start: rgba(255, 255, 255, 0.72);
  --fixed-glass-end: rgba(224, 242, 254, 0.62);
  --nav-text: #f8fafc;
  --fixed-nav-text: #0f172a;
  --active-text: #0f172a;
  --copy-start: #ffffff;
  --copy-end: #a7f3d0;
  --copy-glow: rgba(20, 184, 166, 0.45);
  --fixed-copy-start: #0891b2;
  --fixed-copy-end: #059669;
  --text-glow: rgba(12, 74, 110, 0.34);
  --brand-glow: rgba(14, 165, 233, 0.36);
  --glass-border: rgba(255, 255, 255, 0.34);
  --fixed-border: rgba(14, 165, 233, 0.18);
  --glass-shadow: rgba(15, 23, 42, 0.12);
  --fixed-shadow: rgba(14, 116, 144, 0.16);
  --liquid-glow: rgba(45, 212, 191, 0.26);
  --liquid-glow-soft: rgba(196, 181, 253, 0.2);
  --liquid-shift: 0;
  --active-glass-start: rgba(255, 255, 255, 0.72);
  --active-glass-end: rgba(186, 230, 253, 0.46);
  --active-border: rgba(255, 255, 255, 0.72);
  --active-shadow: rgba(14, 165, 233, 0.26);
  --active-liquid: rgba(45, 212, 191, 0.42);
}
.nav-theme-article {
  --glass-start: rgba(15, 23, 42, 0.2);
  --glass-end: rgba(71, 85, 105, 0.16);
  --fixed-glass-start: rgba(248, 250, 252, 0.74);
  --fixed-glass-end: rgba(226, 232, 240, 0.68);
  --nav-text: #f8fafc;
  --fixed-nav-text: #111827;
  --active-text: #111827;
  --copy-start: #ffffff;
  --copy-end: #fde68a;
  --copy-glow: rgba(250, 204, 21, 0.28);
  --fixed-copy-start: #475569;
  --fixed-copy-end: #b45309;
  --text-glow: rgba(15, 23, 42, 0.36);
  --brand-glow: rgba(245, 158, 11, 0.24);
  --glass-border: rgba(255, 255, 255, 0.3);
  --fixed-border: rgba(148, 163, 184, 0.26);
  --glass-shadow: rgba(15, 23, 42, 0.18);
  --fixed-shadow: rgba(71, 85, 105, 0.14);
  --liquid-glow: rgba(251, 191, 36, 0.16);
  --liquid-glow-soft: rgba(203, 213, 225, 0.22);
  --liquid-shift: 4%;
  --active-glass-start: rgba(255, 255, 255, 0.78);
  --active-glass-end: rgba(254, 243, 199, 0.5);
  --active-border: rgba(255, 255, 255, 0.7);
  --active-shadow: rgba(245, 158, 11, 0.22);
  --active-liquid: rgba(251, 191, 36, 0.34);
}
.nav-theme-archive {
  --glass-start: rgba(3, 7, 18, 0.34);
  --glass-end: rgba(6, 78, 59, 0.2);
  --fixed-glass-start: rgba(3, 7, 18, 0.72);
  --fixed-glass-end: rgba(6, 78, 59, 0.56);
  --nav-text: #e2e8f0;
  --fixed-nav-text: #e2e8f0;
  --active-text: #ecfdf5;
  --copy-start: #a7f3d0;
  --copy-end: #67e8f9;
  --copy-glow: rgba(52, 211, 153, 0.32);
  --text-glow: rgba(16, 185, 129, 0.22);
  --brand-glow: rgba(34, 211, 238, 0.22);
  --glass-border: rgba(148, 163, 184, 0.24);
  --fixed-border: rgba(52, 211, 153, 0.2);
  --glass-shadow: rgba(0, 0, 0, 0.26);
  --fixed-shadow: rgba(0, 0, 0, 0.28);
  --liquid-glow: rgba(16, 185, 129, 0.18);
  --liquid-glow-soft: rgba(34, 211, 238, 0.14);
  --liquid-shift: 8%;
  --active-glass-start: rgba(16, 185, 129, 0.28);
  --active-glass-end: rgba(34, 211, 238, 0.14);
  --active-border: rgba(167, 243, 208, 0.38);
  --active-shadow: rgba(16, 185, 129, 0.26);
  --active-liquid: rgba(52, 211, 153, 0.4);
}
.nav-theme-reading {
  --glass-start: rgba(20, 83, 45, 0.22);
  --glass-end: rgba(217, 164, 65, 0.16);
  --fixed-glass-start: rgba(240, 253, 244, 0.76);
  --fixed-glass-end: rgba(254, 243, 199, 0.62);
  --nav-text: #f8fafc;
  --fixed-nav-text: #13251c;
  --active-text: #13251c;
  --copy-start: #ffffff;
  --copy-end: #bbf7d0;
  --copy-glow: rgba(34, 197, 94, 0.34);
  --fixed-copy-start: #2f855a;
  --fixed-copy-end: #b7791f;
  --text-glow: rgba(20, 83, 45, 0.32);
  --brand-glow: rgba(34, 197, 94, 0.34);
  --glass-border: rgba(255, 255, 255, 0.34);
  --fixed-border: rgba(47, 133, 90, 0.18);
  --glass-shadow: rgba(20, 83, 45, 0.16);
  --fixed-shadow: rgba(47, 133, 90, 0.14);
  --liquid-glow: rgba(134, 239, 172, 0.26);
  --liquid-glow-soft: rgba(252, 211, 77, 0.2);
  --liquid-shift: 2%;
  --active-glass-start: rgba(255, 255, 255, 0.74);
  --active-glass-end: rgba(220, 252, 231, 0.52);
  --active-border: rgba(255, 255, 255, 0.72);
  --active-shadow: rgba(47, 133, 90, 0.22);
  --active-liquid: rgba(252, 211, 77, 0.34);
}
.nav-theme-category {
  --glass-start: rgba(37, 99, 235, 0.18);
  --glass-end: rgba(20, 184, 166, 0.16);
  --fixed-glass-start: rgba(239, 246, 255, 0.76);
  --fixed-glass-end: rgba(204, 251, 241, 0.64);
  --nav-text: #f8fafc;
  --fixed-nav-text: #0f172a;
  --active-text: #0f172a;
  --copy-start: #dbeafe;
  --copy-end: #99f6e4;
  --copy-glow: rgba(20, 184, 166, 0.35);
  --fixed-copy-start: #2563eb;
  --fixed-copy-end: #0f766e;
  --text-glow: rgba(30, 64, 175, 0.28);
  --brand-glow: rgba(20, 184, 166, 0.28);
  --glass-border: rgba(255, 255, 255, 0.32);
  --fixed-border: rgba(59, 130, 246, 0.18);
  --glass-shadow: rgba(30, 64, 175, 0.15);
  --fixed-shadow: rgba(20, 184, 166, 0.14);
  --liquid-glow: rgba(96, 165, 250, 0.24);
  --liquid-glow-soft: rgba(45, 212, 191, 0.22);
  --liquid-shift: -6%;
  --active-glass-start: rgba(255, 255, 255, 0.75);
  --active-glass-end: rgba(153, 246, 228, 0.42);
  --active-border: rgba(255, 255, 255, 0.68);
  --active-shadow: rgba(20, 184, 166, 0.24);
  --active-liquid: rgba(96, 165, 250, 0.36);
}
.nav-theme-tag {
  --glass-start: rgba(236, 72, 153, 0.16);
  --glass-end: rgba(14, 165, 233, 0.15);
  --fixed-glass-start: rgba(253, 242, 248, 0.74);
  --fixed-glass-end: rgba(224, 242, 254, 0.66);
  --nav-text: #ffffff;
  --fixed-nav-text: #172554;
  --active-text: #172554;
  --copy-start: #fbcfe8;
  --copy-end: #bae6fd;
  --copy-glow: rgba(236, 72, 153, 0.32);
  --fixed-copy-start: #db2777;
  --fixed-copy-end: #0284c7;
  --text-glow: rgba(14, 116, 144, 0.24);
  --brand-glow: rgba(236, 72, 153, 0.24);
  --glass-border: rgba(255, 255, 255, 0.3);
  --fixed-border: rgba(236, 72, 153, 0.16);
  --glass-shadow: rgba(236, 72, 153, 0.14);
  --fixed-shadow: rgba(14, 165, 233, 0.14);
  --liquid-glow: rgba(244, 114, 182, 0.22);
  --liquid-glow-soft: rgba(56, 189, 248, 0.2);
  --liquid-shift: 10%;
  --active-glass-start: rgba(255, 255, 255, 0.76);
  --active-glass-end: rgba(251, 207, 232, 0.42);
  --active-border: rgba(255, 255, 255, 0.7);
  --active-shadow: rgba(236, 72, 153, 0.22);
  --active-liquid: rgba(56, 189, 248, 0.34);
}
.nav-theme-link {
  --glass-start: rgba(30, 41, 59, 0.2);
  --glass-end: rgba(88, 28, 135, 0.16);
  --fixed-glass-start: rgba(30, 41, 59, 0.7);
  --fixed-glass-end: rgba(88, 28, 135, 0.48);
  --nav-text: #f8fafc;
  --fixed-nav-text: #f8fafc;
  --active-text: #fdf4ff;
  --copy-start: #c4b5fd;
  --copy-end: #f0abfc;
  --copy-glow: rgba(192, 132, 252, 0.32);
  --text-glow: rgba(88, 28, 135, 0.3);
  --brand-glow: rgba(217, 70, 239, 0.24);
  --glass-border: rgba(255, 255, 255, 0.26);
  --fixed-border: rgba(216, 180, 254, 0.18);
  --glass-shadow: rgba(30, 41, 59, 0.2);
  --fixed-shadow: rgba(88, 28, 135, 0.22);
  --liquid-glow: rgba(168, 85, 247, 0.2);
  --liquid-glow-soft: rgba(232, 121, 249, 0.16);
  --liquid-shift: -10%;
  --active-glass-start: rgba(168, 85, 247, 0.26);
  --active-glass-end: rgba(232, 121, 249, 0.14);
  --active-border: rgba(233, 213, 255, 0.34);
  --active-shadow: rgba(168, 85, 247, 0.22);
  --active-liquid: rgba(217, 70, 239, 0.35);
}
.nav-theme-about {
  --glass-start: rgba(255, 255, 255, 0.82);
  --glass-end: rgba(252, 231, 243, 0.72);
  --fixed-glass-start: rgba(255, 255, 255, 0.88);
  --fixed-glass-end: rgba(252, 231, 243, 0.78);
  --nav-text: #4c1d95;
  --fixed-nav-text: #4c1d95;
  --active-text: #581c87;
  --copy-start: #7c3aed;
  --copy-end: #db2777;
  --copy-glow: rgba(255, 255, 255, 0);
  --fixed-copy-start: #7c3aed;
  --fixed-copy-end: #db2777;
  --text-glow: rgba(255, 255, 255, 0);
  --brand-glow: rgba(255, 255, 255, 0);
  --glass-border: rgba(124, 58, 237, 0.18);
  --fixed-border: rgba(124, 58, 237, 0.18);
  --glass-shadow: rgba(124, 58, 237, 0.16);
  --fixed-shadow: rgba(124, 58, 237, 0.14);
  --liquid-glow: rgba(244, 114, 182, 0.14);
  --liquid-glow-soft: rgba(124, 58, 237, 0.1);
  --liquid-shift: 6%;
  --active-glass-start: rgba(255, 255, 255, 0.92);
  --active-glass-end: rgba(251, 207, 232, 0.72);
  --active-border: rgba(219, 39, 119, 0.28);
  --active-shadow: rgba(124, 58, 237, 0.22);
  --active-liquid: rgba(244, 114, 182, 0.24);
}
.nav-theme-message {
  --glass-start: rgba(14, 165, 233, 0.18);
  --glass-end: rgba(249, 115, 22, 0.12);
  --fixed-glass-start: rgba(240, 249, 255, 0.76);
  --fixed-glass-end: rgba(255, 237, 213, 0.62);
  --nav-text: #ffffff;
  --fixed-nav-text: #0c4a6e;
  --active-text: #0c4a6e;
  --copy-start: #bae6fd;
  --copy-end: #fed7aa;
  --copy-glow: rgba(251, 146, 60, 0.26);
  --fixed-copy-start: #0284c7;
  --fixed-copy-end: #ea580c;
  --text-glow: rgba(14, 116, 144, 0.26);
  --brand-glow: rgba(251, 146, 60, 0.22);
  --glass-border: rgba(255, 255, 255, 0.32);
  --fixed-border: rgba(251, 146, 60, 0.16);
  --glass-shadow: rgba(14, 116, 144, 0.14);
  --fixed-shadow: rgba(251, 146, 60, 0.13);
  --liquid-glow: rgba(56, 189, 248, 0.23);
  --liquid-glow-soft: rgba(251, 146, 60, 0.18);
  --liquid-shift: -4%;
  --active-glass-start: rgba(255, 255, 255, 0.76);
  --active-glass-end: rgba(254, 215, 170, 0.44);
  --active-border: rgba(255, 255, 255, 0.68);
  --active-shadow: rgba(251, 146, 60, 0.2);
  --active-liquid: rgba(56, 189, 248, 0.34);
}
.nav-theme-album,
.nav-theme-talk {
  --glass-start: rgba(20, 184, 166, 0.16);
  --glass-end: rgba(217, 70, 239, 0.13);
  --fixed-glass-start: rgba(240, 253, 250, 0.76);
  --fixed-glass-end: rgba(250, 232, 255, 0.64);
  --nav-text: #ffffff;
  --fixed-nav-text: #134e4a;
  --active-text: #134e4a;
  --copy-start: #ccfbf1;
  --copy-end: #f5d0fe;
  --copy-glow: rgba(20, 184, 166, 0.28);
  --fixed-copy-start: #0f766e;
  --fixed-copy-end: #c026d3;
  --text-glow: rgba(15, 118, 110, 0.24);
  --brand-glow: rgba(217, 70, 239, 0.2);
  --glass-border: rgba(255, 255, 255, 0.32);
  --fixed-border: rgba(20, 184, 166, 0.16);
  --glass-shadow: rgba(20, 184, 166, 0.13);
  --fixed-shadow: rgba(217, 70, 239, 0.12);
  --liquid-glow: rgba(45, 212, 191, 0.2);
  --liquid-glow-soft: rgba(232, 121, 249, 0.16);
  --liquid-shift: 2%;
  --active-glass-start: rgba(255, 255, 255, 0.76);
  --active-glass-end: rgba(204, 251, 241, 0.44);
  --active-border: rgba(255, 255, 255, 0.68);
  --active-shadow: rgba(20, 184, 166, 0.2);
  --active-liquid: rgba(232, 121, 249, 0.3);
}
</style>
