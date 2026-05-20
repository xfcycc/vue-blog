<template>
  <div class="rightside" :style="isShow">
    <i @click="backTop" class="iconfont rightside-icon iconziyuanldpi" />
  </div>
</template>

<script>
export default {
  mounted() {
    window.addEventListener("scroll", this.scrollToTop);
  },
  destroyed() {
    window.removeEventListener("scroll", this.scrollToTop);
  },
  data: function() {
    return {
      isShow: ""
    };
  },
  methods: {
    // 回到顶部方法
    backTop() {
      window.scrollTo({
        behavior: "smooth",
        top: 0
      });
    },
    // 为了计算距离顶部的高度，当高度大于100显示回顶部图标，小于100则隐藏
    scrollToTop() {
      const that = this;
      let scrollTop =
        window.pageYOffset ||
        document.documentElement.scrollTop ||
        document.body.scrollTop;
      that.scrollTop = scrollTop;
      if (that.scrollTop > 20) {
        that.isShow = "opacity: 1;transform: translateX(0);";
      } else {
        that.isShow = "";
      }
    }
  }
};
</script>

<style scoped>
.rightside {
  z-index: 4;
  position: fixed;
  right: 5px;
  bottom: 86px;
  opacity: 0;
  transform: translateX(92px);
  transition: all 0.5s;
}
.rightside-icon {
  display: block;
  margin-bottom: 2px;
  width: 60px;
  height: 60px;
  border: 1px solid rgba(45, 212, 191, 0.55);
  border-radius: 50%;
  background: linear-gradient(135deg, #0f766e, #14b8a6);
  color: #fff;
  text-align: center;
  font-size: 24px;
  line-height: 60px;
  box-shadow: 0 14px 30px rgba(15, 118, 110, 0.24);
  cursor: pointer;
}
.rightside-icon:hover {
  background: linear-gradient(135deg, #0f172a, #0f766e);
}
</style>
