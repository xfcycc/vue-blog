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
    <BackTop></BackTop>
    <!-- 搜索模态框 -->
    <searchModel></searchModel>
    <!-- 音乐播放器 -->
    <Player v-if="blogInfo.websiteConfig.isMusicPlayer == 1 && !isMobile" />
    <!-- 聊天室 -->
    <ChatRoom v-if="blogInfo.websiteConfig.isChatRoom == 1"></ChatRoom>
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
export default {
  created() {
    // 主站关闭登录后，清理旧会话里持久化的用户身份。
    this.$store.commit("enableAnonymousMode");
    // 获取博客信息
    this.getBlogInfo();
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
    }
  },
  computed: {
    blogInfo() {
      return this.$store.state.blogInfo;
    },
    isMobile() {
      const flag = navigator.userAgent.match(
        /(phone|pad|pod|iPhone|iPod|ios|iPad|Android|Mobile|BlackBerry|IEMobile|MQQBrowser|JUC|Fennec|wOSBrowser|BrowserNG|WebOS|Symbian|Windows Phone)/i
      );
      return flag;
    }
  }
};
</script>
