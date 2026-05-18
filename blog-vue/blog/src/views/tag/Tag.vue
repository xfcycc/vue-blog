<template>
  <div class="tag-page" :style="cover">
    <div class="tag-page-mask"></div>
    <div class="star-layer star-layer-one"></div>
    <div class="star-layer star-layer-two"></div>
    <div class="star-layer star-layer-three"></div>

    <div class="tag-field">
      <router-link
        v-for="item of styledTagList"
        :key="item.id"
        :to="'/tags/' + item.id"
        class="tag-cloud-item"
        :style="getTagStyle(item)"
      >
        <span class="tag-cloud-text">{{ item.tagName }}</span>
      </router-link>
    </div>
  </div>
</template>

<script>
const TAG_COVER =
  "https://pic.caiguoyu.cn/20260518204951070.jpg";
const TAG_COLORS = [
  "#3b82f6",
  "#ec4899",
  "#8b5cf6",
  "#10b981",
  "#f59e0b",
  "#6366f1",
  "#14b8a6",
  "#f43f5e",
  "#0ea5e9",
  "#475569"
];

export default {
  created() {
    this.listTags();
  },
  mounted() {
    document.documentElement.classList.add("tag-page-scrollbar");
    document.body.classList.add("tag-page-scrollbar");
  },
  beforeDestroy() {
    document.documentElement.classList.remove("tag-page-scrollbar");
    document.body.classList.remove("tag-page-scrollbar");
  },
  data: function() {
    return {
      tagList: [],
      styledTagList: []
    };
  },
  methods: {
    listTags() {
      this.axios.get("/api/tags").then(({ data }) => {
        this.tagList = data.data.recordList;
        this.styledTagList = this.tagList.map(item => {
          const size = Math.floor(Math.random() * 24) + 16;
          const driftX = Math.floor(Math.random() * 280) - 140;
          const driftY = Math.floor(Math.random() * 220) - 110;
          return {
            ...item,
            size,
            color: TAG_COLORS[Math.floor(Math.random() * TAG_COLORS.length)],
            x: Math.floor(Math.random() * 78) + 8,
            y: Math.floor(Math.random() * 72) + 14,
            driftX,
            driftY,
            driftHalfX: Math.round(driftX * -0.45),
            driftHalfY: Math.round(driftY * 0.45),
            duration: Math.floor(Math.random() * 4) + 5,
            delay: (Math.random() * -6).toFixed(2)
          };
        });
      });
    },
    getTagStyle(item) {
      return {
        left: item.x + "%",
        top: item.y + "%",
        color: item.color,
        fontSize: item.size + "px",
        fontWeight: item.size > 24 ? 600 : 400,
        animationDelay: item.delay + "s",
        animationDuration: item.duration + "s",
        "--drift-x": item.driftX + "px",
        "--drift-y": item.driftY + "px",
        "--drift-half-x": item.driftHalfX + "px",
        "--drift-half-y": item.driftHalfY + "px"
      };
    }
  },
  computed: {
    cover() {
      return "background: url(" + TAG_COVER + ") center top / cover no-repeat";
    }
  }
};
</script>

<style scoped>
.tag-page {
  position: relative;
  overflow: hidden;
  min-height: 100vh;
  margin-top: -60px;
  padding-top: 60px;
  background: #f3f4f6;
}
.tag-page-mask {
  position: absolute;
  inset: 0;
  background: radial-gradient(
      circle at 20% 18%,
      rgba(96, 165, 250, 0.22),
      transparent 28%
    ),
    radial-gradient(
      circle at 78% 36%,
      rgba(236, 72, 153, 0.16),
      transparent 30%
    ),
    linear-gradient(180deg, rgba(2, 6, 23, 0.34), rgba(2, 6, 23, 0.62));
}
.star-layer {
  position: absolute;
  inset: -20%;
  pointer-events: none;
  background-repeat: repeat;
  will-change: transform, opacity;
}
.star-layer-one {
  opacity: 0.42;
  background-image: radial-gradient(
    circle,
    rgba(255, 255, 255, 0.82) 1px,
    transparent 1.5px
  );
  background-size: 78px 78px;
  animation: star-drift 36s linear infinite;
}
.star-layer-two {
  opacity: 0.34;
  background-image: radial-gradient(
    circle,
    rgba(191, 219, 254, 0.78) 1px,
    transparent 1.7px
  );
  background-size: 126px 126px;
  background-position: 28px 42px;
  animation: star-drift-reverse 58s linear infinite;
}
.star-layer-three {
  opacity: 0.28;
  background-image: radial-gradient(
      circle,
      rgba(255, 255, 255, 0.9) 1px,
      transparent 1.4px
    ),
    radial-gradient(circle, rgba(125, 211, 252, 0.72) 1px, transparent 1.5px);
  background-size: 180px 180px, 240px 240px;
  background-position: 12px 70px, 90px 24px;
  animation: star-twinkle 4.8s ease-in-out infinite;
}
@media (max-width: 759px) {
  .tag-page {
    min-height: 86vh;
  }
}
.tag-field {
  position: relative;
  z-index: 1;
  min-height: calc(100vh - 60px);
}
.tag-cloud-item {
  position: absolute;
  display: inline-block;
  opacity: 0.88;
  text-decoration: none;
  white-space: nowrap;
  line-height: 1.5;
  transform-origin: center center;
  animation-name: tag-drift;
  animation-timing-function: ease-in-out;
  animation-iteration-count: infinite;
  animation-direction: alternate;
  transition: opacity 0.25s, text-shadow 0.25s;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.32);
}
.tag-cloud-item:hover {
  z-index: 2;
  opacity: 1;
  animation-play-state: paused;
  text-shadow: 0 8px 16px rgba(0, 0, 0, 0.15);
}
.tag-cloud-text {
  display: inline-block;
  transform-origin: center center;
}
.tag-cloud-item:hover .tag-cloud-text {
  animation: tag-shake 0.9s ease-in-out infinite;
}
@keyframes tag-drift {
  0% {
    transform: translate3d(0, 0, 0) scale(1);
  }
  50% {
    transform: translate3d(var(--drift-half-x), var(--drift-half-y), 0)
      scale(1.04);
  }
  100% {
    transform: translate3d(var(--drift-x), var(--drift-y), 0) scale(1);
  }
}
@keyframes tag-shake {
  0%,
  100% {
    transform: translate3d(0, 0, 0) scale(1.08);
  }
  25% {
    transform: translate3d(1px, -1px, 0) scale(1.08);
  }
  50% {
    transform: translate3d(-1px, 1px, 0) scale(1.08);
  }
  75% {
    transform: translate3d(1px, 1px, 0) scale(1.08);
  }
}
@keyframes star-drift {
  0% {
    transform: translate3d(0, 0, 0);
  }
  100% {
    transform: translate3d(78px, 78px, 0);
  }
}
@keyframes star-drift-reverse {
  0% {
    transform: translate3d(0, 0, 0);
  }
  100% {
    transform: translate3d(-126px, 126px, 0);
  }
}
@keyframes star-twinkle {
  0%,
  100% {
    opacity: 0.2;
    transform: scale(1);
  }
  50% {
    opacity: 0.46;
    transform: scale(1.015);
  }
}
</style>
