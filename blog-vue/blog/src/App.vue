<template>
  <v-app id="app">
    <!-- 导航栏 -->
    <TopNavBar></TopNavBar>
    <!-- 侧边导航栏 -->
    <SideNavBar></SideNavBar>
    <!-- 内容 -->
    <v-content>
      <router-view :key="$route.fullPath" />
    </v-content>
    <!-- 页脚 -->
    <Footer></Footer>
    <!-- 返回顶部 -->
    <BackTop v-if="showFloatingTools"></BackTop>
    <!-- 搜索模态框 -->
    <searchModel></searchModel>
    <!-- 音乐播放器 -->
    <Player v-if="websiteConfig.isMusicPlayer == 1 && !isMobile" />
    <!-- 聊天室 -->
    <ChatRoom
      v-if="showFloatingTools && websiteConfig.isChatRoom == 1"
    ></ChatRoom>
  </v-app>
</template>

<script>
import TopNavBar from "./components/layout/TopNavBar";
import SideNavBar from "./components/layout/SideNavBar";
import Footer from "./components/layout/Footer";
import BackTop from "./components/BackTop";
import searchModel from "./components/model/SearchModel";
import Player from "./components/zw-player/player.vue";
import ChatRoom from "./components/ChatRoom";
import { getPersistentPokemonAvatar } from "./utils/avatar";
export default {
  created() {
    // 主站关闭登录后，清理旧会话里持久化的用户身份。
    this.$store.commit("enableAnonymousMode");
    // 获取博客信息
    this.getBlogInfo();
    // 获取游客头像
    this.getVisitorAvatar();
    // 上传访客信息
    this.axios.post("/api/report");
  },
  components: {
    TopNavBar,
    Player,
    SideNavBar,
    Footer,
    BackTop,
    searchModel,
    ChatRoom
  },
  methods: {
    getBlogInfo() {
      this.axios.get("/api/").then(({ data }) => {
        this.$store.commit("checkBlogInfo", data.data);
      });
    },
    getVisitorAvatar() {
      this.axios
        .get("/api/visitor/avatar")
        .then(({ data }) => {
          if (data.flag) {
            this.$store.commit("updateVisitorAvatar", data.data);
          }
        })
        .catch(() => {
          this.$store.commit("updateVisitorAvatar", {
            avatarId: null,
            avatar: getPersistentPokemonAvatar("visitorAvatar")
          });
        });
    }
  },
  computed: {
    blogInfo() {
      return this.$store.state.blogInfo;
    },
    websiteConfig() {
      return this.blogInfo.websiteConfig || {};
    },
    isMobile() {
      const flag = navigator.userAgent.match(
        /(phone|pad|pod|iPhone|iPod|ios|iPad|Android|Mobile|BlackBerry|IEMobile|MQQBrowser|JUC|Fennec|wOSBrowser|BrowserNG|WebOS|Symbian|Windows Phone)/i
      );
      return flag;
    },
    showFloatingTools() {
      return (
        this.$route.path === "/" || this.$route.path.startsWith("/articles/")
      );
    }
  }
};
</script>
