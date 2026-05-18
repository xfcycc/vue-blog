<template>
  <v-app-bar
    app
    :class="[navClass, { 'archive-nav': isArchivePage }]"
    hide-on-scroll
    flat
    height="60"
  >
    <!-- 手机端导航栏 -->
    <div class="d-md-none nav-mobile-container">
      <div style="font-size:18px;font-weight:bold">
        <router-link to="/">
          {{ blogInfo.websiteConfig.websiteAuthor }}
        </router-link>
      </div>
      <div style="margin-left:auto">
        <a @click="openSearch"><i class="iconfont iconsousuo"/></a>
        <a @click="openDrawer" style="margin-left:10px;font-size:20px">
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
      </div>
      <div class="float-right nav-title">
        <div class="menus-item">
          <a class="menu-btn" @click="openSearch">
            <i class="iconfont iconsousuo" /> 搜索
          </a>
        </div>
        <div class="menus-item">
          <router-link class="menu-btn" to="/">
            <i class="iconfont iconzhuye" /> 首页
          </router-link>
        </div>
        <div class="menus-item">
          <router-link class="menu-btn" to="/archives">
            <i class="iconfont iconguidang" /> 归档
          </router-link>
        </div>
        <div class="menus-item">
          <router-link class="menu-btn" to="/categories">
            <i class="iconfont iconfenlei" /> 分类
          </router-link>
        </div>
        <div class="menus-item">
          <router-link class="menu-btn" to="/tags">
            <i class="iconfont iconbiaoqian" /> 标签
          </router-link>
        </div>
        <div class="menus-item">
          <router-link class="menu-btn" to="/links">
            <i class="iconfont iconlianjie" /> 友链
          </router-link>
        </div>
        <div class="menus-item">
          <router-link class="menu-btn" to="/about">
            <i class="iconfont iconzhifeiji" /> 关于
          </router-link>
        </div>
        <div class="menus-item">
          <router-link class="menu-btn" to="/message">
            <i class="iconfont iconpinglunzu" /> 留言
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
  },
  data: function() {
    return {
      navClass: ""
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
    openSearch() {
      this.$store.state.searchFlag = true;
    },
    openDrawer() {
      this.$store.state.drawer = true;
    }
  },
  computed: {
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
i {
  margin-right: 4px;
}
ul {
  list-style: none;
}
.nav {
  background: rgba(0, 0, 0, 0) !important;
}
.nav a {
  color: #eee !important;
}
.nav .menu-btn {
  text-shadow: 0.05rem 0.05rem 0.1rem rgba(0, 0, 0, 0.3);
}
.nav .blog-title a {
  text-shadow: 0.1rem 0.1rem 0.2rem rgba(0, 0, 0, 0.15);
}
.theme--light.nav-fixed {
  background: rgba(255, 255, 255, 0.8) !important;
  box-shadow: 0 5px 6px -5px rgba(133, 133, 133, 0.6);
}
.theme--dark.nav-fixed {
  background: rgba(18, 18, 18, 0.8) !important;
}
.theme--dark.nav-fixed a {
  color: rgba(255, 255, 255, 0.8) !important;
}
.theme--light.nav-fixed a {
  color: #4c4948 !important;
}
.theme--light.archive-nav.nav-fixed,
.theme--dark.archive-nav.nav-fixed {
  background: rgba(3, 7, 18, 0.72) !important;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.24);
  backdrop-filter: blur(14px);
}
.theme--light.archive-nav.nav-fixed a,
.theme--dark.archive-nav.nav-fixed a,
.archive-nav a {
  color: #e2e8f0 !important;
}
.archive-nav .menu-btn:hover {
  color: #34d399 !important;
}
.nav-fixed .menus-item a,
.nav-fixed .blog-title a {
  text-shadow: none;
}
.nav-container {
  font-size: 14px;
  width: 100%;
  height: 100%;
}
.nav-mobile-container {
  width: 100%;
  display: flex;
  align-items: center;
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
.menus-item {
  position: relative;
  display: inline-block;
  margin: 0 0 0 0.875rem;
}
.menus-item a {
  transition: all 0.2s;
}
.nav-fixed .menu-btn:hover {
  color: #49b1f5 !important;
}
.menu-btn:hover:after {
  width: 100%;
}
.menus-item a:after {
  position: absolute;
  bottom: -5px;
  left: 0;
  z-index: -1;
  width: 0;
  height: 3px;
  background-color: #80c8f8;
  content: "";
  transition: all 0.3s ease-in-out;
}
.user-avatar {
  cursor: pointer;
  border-radius: 50%;
}
</style>
