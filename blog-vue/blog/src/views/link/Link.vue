<template>
  <div class="link-page" :style="pageStyle">
    <div class="link-page-mask" />
    <div class="link-hero">
      <div ref="solarSystem" class="solar-system">
        <div
          v-for="star in starList"
          :key="'star-' + star.id"
          class="space-star"
          :style="getStarStyle(star)"
        />
        <div
          v-for="(item, index) of solarLinkList"
          :key="'orbit-' + item.id"
          class="orbit-path"
          :style="getOrbitStyle(item, index)"
        />
        <div class="station-orbit" :style="getStationOrbitStyle()" />
        <div class="solar-sun" :style="sunStyle">
          <span class="sun-ring" />
        </div>
        <a
          v-for="(item, index) of solarLinkList"
          :key="'planet-' + item.id"
          class="planet-link"
          :href="item.linkAddress"
          target="_blank"
          rel="noopener noreferrer"
          :title="item.linkName + ' - ' + item.linkIntro"
          :style="getPlanetStyle(item, index)"
        >
          <span class="planet-glow" />
          <span class="planet-core" :style="getPlanetCoreStyle(item, index)">
            <img :src="item.linkAvatar" :alt="item.linkName" />
          </span>
          <span class="planet-label">
            <span class="planet-name">{{ item.linkName }}</span>
            <span class="planet-intro">{{ item.linkIntro }}</span>
          </span>
        </a>
        <div v-if="friendLinkList.length === 0" class="empty-galaxy">
          星球正在形成中
        </div>

        <div class="station-zone" :style="getStationStyle()">
          <button
            class="station-button"
            type="button"
            aria-label="打开友链通信"
            @click="stationDialog = true"
          >
            <span class="satellite-signal signal-one" />
            <span class="satellite-signal signal-two" />
            <span class="satellite-antenna" />
            <span class="satellite-panel panel-left" />
            <span class="satellite-core">
              <span class="satellite-lens" />
            </span>
            <span class="satellite-panel panel-right" />
          </button>
          <div class="station-bubble">信号发射中~~</div>
        </div>
      </div>
    </div>

    <v-dialog v-model="stationDialog" max-width="900" scrollable>
      <v-card class="station-dialog">
        <div class="station-dialog-header">
          <div class="station-dialog-heading">
            <div class="station-dialog-mark">
              <span class="mark-panel" />
              <span class="mark-core" />
            </div>
            <div>
              <div class="station-dialog-kicker">SPACE STATION</div>
              <div class="station-dialog-title">友链通信站</div>
            </div>
          </div>
          <v-btn icon color="#fff" @click="stationDialog = false">
            <v-icon>mdi-close</v-icon>
          </v-btn>
        </div>
        <v-card-text class="station-dialog-content">
          <div class="station-info-panel">
            <div class="station-info-row">
              <span>名称</span>
              <strong>{{ blogInfo.websiteConfig.websiteName }}</strong>
            </div>
            <div class="station-info-row">
              <span>简介</span>
              <strong>{{ blogInfo.websiteConfig.websiteIntro }}</strong>
            </div>
            <div class="station-info-row">
              <span>头像</span>
              <strong>{{ blogInfo.websiteConfig.websiteAvatar }}</strong>
            </div>
          </div>
          <div class="station-notice">
            需要交换友链的可在这里留言，格式包含：名称、介绍、链接、头像
          </div>
          <div class="station-comment-shell">
            <div class="station-comment-heading">
              <span>历史通信</span>
              <small>History Logs</small>
            </div>
            <Comment v-if="stationDialog" :type="commentType" />
          </div>
        </v-card-text>
      </v-card>
    </v-dialog>
  </div>
</template>

<script>
import Comment from "../../components/Comment";

const spaceBg = "https://pic.caiguoyu.cn/20260519195330293.png";
const sunImg = "https://pic.caiguoyu.cn/20260519195330294.png";

const PLANET_COLORS = [
  ["#7dd3fc", "#2563eb"],
  ["#fda4af", "#e11d48"],
  ["#fde68a", "#f59e0b"],
  ["#86efac", "#16a34a"],
  ["#c4b5fd", "#7c3aed"],
  ["#67e8f9", "#0891b2"],
  ["#f9a8d4", "#db2777"],
  ["#fdba74", "#ea580c"]
];

export default {
  components: {
    Comment
  },
  created() {
    this.listFriendLink();
  },
  mounted() {
    document.body.classList.add("link-galaxy-active");
    document.documentElement.style.setProperty(
      "--link-galaxy-space-bg",
      "url(" + spaceBg + ")"
    );
    this.isReduceMotion = window.matchMedia(
      "(prefers-reduced-motion: reduce)"
    ).matches;
    this.updateStageSize();
    window.addEventListener("resize", this.updateStageSize);
    if (!this.isReduceMotion) {
      this.startOrbit();
    }
  },
  beforeDestroy() {
    document.body.classList.remove("link-galaxy-active");
    document.documentElement.style.removeProperty("--link-galaxy-space-bg");
    window.removeEventListener("resize", this.updateStageSize);
    cancelAnimationFrame(this.rafId);
  },
  data: function() {
    return {
      friendLinkList: [],
      commentType: 2,
      stationDialog: false,
      orbitTime: 0,
      rafId: 0,
      isReduceMotion: false,
      stageSize: {
        width: 1100,
        height: 680
      },
      starList: Array.from({ length: 68 }, (_, index) => ({
        id: index,
        left: (index * 37 + 11) % 100,
        top: (index * 53 + 17) % 100,
        size: 1 + (index % 3),
        opacity: 0.22 + (index % 5) * 0.13,
        delay: (index % 7) * 0.4
      }))
    };
  },
  methods: {
    listFriendLink() {
      this.axios.get("/api/links").then(({ data }) => {
        this.friendLinkList = data.data;
        this.$nextTick(this.updateStageSize);
      });
    },
    startOrbit() {
      const orbit = time => {
        this.orbitTime = time;
        this.rafId = requestAnimationFrame(orbit);
      };
      this.rafId = requestAnimationFrame(orbit);
    },
    updateStageSize() {
      if (!this.$refs.solarSystem) {
        return;
      }
      const rect = this.$refs.solarSystem.getBoundingClientRect();
      this.stageSize = {
        width: rect.width,
        height: rect.height
      };
    },
    getOrbitConfig(index) {
      const count = Math.max(this.friendLinkList.length, 1);
      const stageMin = Math.min(this.stageSize.width, this.stageSize.height);
      const maxRadius = stageMin * 0.6;
      const sunRadius = stageMin < 520 ? 48 : 74;
      const minRadius = Math.max(stageMin * 0.28, (sunRadius + 58) / 0.58);
      const ringStep = count > 1 ? (maxRadius - minRadius) / (count - 1) : 0;
      const ring = minRadius + ringStep * index;
      const wave = Math.sin(index * 1.7) * stageMin * 0.025;
      const orbitX = ring + wave;
      const orbitY = orbitX * (0.58 + (index % 4) * 0.08);

      return {
        orbitX,
        orbitY,
        tilt: ((index * 31) % 86) - 43,
        startAngle: ((index * 73 + 18) % 360) * (Math.PI / 180),
        duration: 26 + (index % 7) * 7,
        direction: index % 3 === 0 ? -1 : 1,
        size: 44 + (index % 5) * 6
      };
    },
    getStationOrbitConfig() {
      const stageMin = Math.min(this.stageSize.width, this.stageSize.height);
      const orbitX = stageMin * 0.46;
      const orbitY = orbitX * 0.66;

      return {
        orbitX,
        orbitY,
        tilt: -24,
        startAngle: 128 * (Math.PI / 180),
        duration: 58,
        direction: 1
      };
    },
    getOrbitPoint(config) {
      const elapsed = this.isReduceMotion ? 0 : this.orbitTime / 1000;
      const angle =
        config.startAngle +
        (elapsed * config.direction * Math.PI * 2) / config.duration;
      const tilt = (config.tilt * Math.PI) / 180;
      const x = Math.cos(angle) * config.orbitX;
      const y = Math.sin(angle) * config.orbitY;

      return {
        x: x * Math.cos(tilt) - y * Math.sin(tilt),
        y: x * Math.sin(tilt) + y * Math.cos(tilt)
      };
    },
    getStationOrbitStyle() {
      const config = this.getStationOrbitConfig();

      return {
        width: config.orbitX * 2 + "px",
        height: config.orbitY * 2 + "px",
        transform: "translate(-50%, -50%) rotate(" + config.tilt + "deg)"
      };
    },
    getStationStyle() {
      const config = this.getStationOrbitConfig();
      const point = this.getOrbitPoint(config);

      return {
        transform:
          "translate(calc(-50% + " +
          point.x +
          "px), calc(-50% + " +
          point.y +
          "px))"
      };
    },
    getOrbitStyle(item, index) {
      const config = this.getOrbitConfig(index);

      return {
        width: config.orbitX * 2 + "px",
        height: config.orbitY * 2 + "px",
        transform: "translate(-50%, -50%) rotate(" + config.tilt + "deg)",
        animationDelay: index * -0.7 + "s"
      };
    },
    getPlanetStyle(item, index) {
      const config = this.getOrbitConfig(index);
      const point = this.getOrbitPoint(config);

      return {
        width: config.size + "px",
        height: config.size + "px",
        transform:
          "translate(calc(-50% + " +
          point.x +
          "px), calc(-50% + " +
          point.y +
          "px))"
      };
    },
    getPlanetCoreStyle(item, index) {
      const color = PLANET_COLORS[index % PLANET_COLORS.length];

      return {
        background:
          "radial-gradient(circle at 34% 28%, #fff 0, " +
          color[0] +
          " 24%, " +
          color[1] +
          " 72%)",
        boxShadow:
          "0 0 22px " + color[0] + ", inset -10px -12px 20px rgba(0,0,0,0.28)"
      };
    },
    getStarStyle(star) {
      return {
        left: star.left + "%",
        top: star.top + "%",
        width: star.size + "px",
        height: star.size + "px",
        opacity: star.opacity,
        animationDelay: star.delay + "s"
      };
    }
  },
  computed: {
    solarLinkList() {
      return this.friendLinkList.map((item, index) => ({
        ...item,
        id: item.id || item.linkAddress || index
      }));
    },
    blogInfo() {
      return this.$store.state.blogInfo;
    },
    pageStyle() {
      return {
        backgroundImage:
          "linear-gradient(180deg, rgba(3, 7, 18, 0.15), rgba(3, 7, 18, 0.8)), url(" +
          spaceBg +
          ")"
      };
    },
    sunStyle() {
      return {
        backgroundImage: "url(" + sunImg + ")"
      };
    }
  }
};
</script>

<style scoped>
.link-page {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  background-position: center center;
  background-size: cover;
  color: #fff;
}
.link-page-mask {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 50% 52%, rgba(255, 181, 69, 0.16), transparent 19%),
    radial-gradient(circle at 20% 18%, rgba(6, 182, 212, 0.18), transparent 24%),
    linear-gradient(90deg, rgba(2, 6, 23, 0.62), rgba(2, 6, 23, 0.22), rgba(2, 6, 23, 0.68));
  pointer-events: none;
}
.link-hero {
  position: relative;
  z-index: 1;
  min-height: 100vh;
  padding: 66px 36px 56px;
}
.solar-system {
  position: relative;
  width: min(92vw, 1120px);
  height: min(76vw, calc(100vh - 92px));
  min-height: 640px;
  margin: 0 auto;
  overflow: visible;
}
.space-star {
  position: absolute;
  border-radius: 50%;
  background: #fff;
  animation: starTwinkle 2.8s ease-in-out infinite alternate;
}
.orbit-path {
  position: absolute;
  top: 52%;
  left: 50%;
  border: 1px solid rgba(203, 213, 225, 0.18);
  border-radius: 50%;
  box-shadow: 0 0 20px rgba(125, 211, 252, 0.08);
  animation: orbitPulse 5.6s ease-in-out infinite;
  pointer-events: none;
}
.station-orbit {
  position: absolute;
  top: 52%;
  left: 50%;
  border: 1px dashed rgba(125, 211, 252, 0.3);
  border-radius: 50%;
  box-shadow: 0 0 24px rgba(56, 189, 248, 0.16);
  pointer-events: none;
}
.solar-sun {
  position: absolute;
  top: 52%;
  left: 50%;
  z-index: 5;
  width: 148px;
  height: 148px;
  border-radius: 50%;
  background-position: center;
  background-size: cover;
  box-shadow:
    0 0 42px rgba(255, 177, 66, 0.96),
    0 0 116px rgba(255, 107, 53, 0.62),
    0 0 220px rgba(255, 214, 102, 0.35);
  transform: translate(-50%, -50%);
}
.sun-ring {
  position: absolute;
  inset: -22px;
  border: 1px solid rgba(255, 209, 102, 0.36);
  border-radius: 50%;
  animation: sunBreath 4.8s ease-in-out infinite;
}
.planet-link {
  position: absolute;
  top: 52%;
  left: 50%;
  z-index: 8;
  display: block;
  border-radius: 50%;
  color: #fff !important;
  text-decoration: none;
  will-change: transform;
}
.planet-glow {
  position: absolute;
  inset: -7px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.16);
  filter: blur(8px);
  opacity: 0.55;
  transition: opacity 0.25s, transform 0.25s;
}
.planet-core {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  overflow: hidden;
  border: 2px solid rgba(255, 255, 255, 0.52);
  border-radius: 50%;
  transition: transform 0.28s, border-color 0.28s;
}
.planet-core img {
  width: 74%;
  height: 74%;
  border-radius: 50%;
  object-fit: cover;
  box-shadow: 0 0 0 2px rgba(255, 255, 255, 0.48);
}
.planet-label {
  position: absolute;
  left: 50%;
  bottom: -12px;
  min-width: 132px;
  max-width: 190px;
  padding: 8px 10px;
  border: 1px solid rgba(255, 255, 255, 0.14);
  border-radius: 8px;
  background: rgba(9, 14, 35, 0.82);
  box-shadow: 0 14px 34px rgba(0, 0, 0, 0.28);
  opacity: 0;
  pointer-events: none;
  transform: translate(-50%, 100%) scale(0.92);
  transition: opacity 0.24s, transform 0.24s;
  backdrop-filter: blur(10px);
}
.planet-name {
  display: block;
  overflow: hidden;
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  text-align: center;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.planet-intro {
  display: block;
  display: -webkit-box;
  overflow: hidden;
  margin-top: 4px;
  color: rgba(226, 232, 240, 0.82);
  font-size: 12px;
  line-height: 1.45;
  text-align: center;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}
.planet-link:hover {
  z-index: 20;
}
.planet-link:hover .planet-glow,
.planet-link:hover .planet-core {
  transform: scale(1.16);
}
.planet-link:hover .planet-glow {
  opacity: 0.92;
}
.planet-link:hover .planet-core {
  border-color: rgba(255, 255, 255, 0.92);
}
.planet-link:hover .planet-label {
  opacity: 1;
  transform: translate(-50%, 100%) scale(1);
}
.empty-galaxy {
  position: absolute;
  top: 52%;
  left: 50%;
  color: rgba(255, 255, 255, 0.72);
  font-size: 15px;
  transform: translate(-50%, 86px);
}
.station-zone {
  position: absolute;
  top: 52%;
  left: 50%;
  z-index: 24;
  display: flex;
  align-items: center;
  gap: 14px;
  will-change: transform;
}
.station-button {
  position: relative;
  width: 132px;
  height: 96px;
  border: 0;
  background: transparent;
  cursor: pointer;
  filter: drop-shadow(0 0 16px rgba(125, 211, 252, 0.58));
  transition: filter 0.24s, transform 0.24s;
}
.satellite-core {
  position: absolute;
  top: 34px;
  left: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  border: 2px solid rgba(241, 245, 249, 0.9);
  border-radius: 10px;
  background:
    linear-gradient(135deg, rgba(248, 250, 252, 0.95), rgba(56, 189, 248, 0.84) 48%, rgba(30, 58, 138, 0.96));
  box-shadow:
    inset -8px -8px 14px rgba(15, 23, 42, 0.28),
    0 0 18px rgba(56, 189, 248, 0.46);
  transform: translateX(-50%) rotate(45deg);
}
.satellite-lens {
  width: 13px;
  height: 13px;
  border: 2px solid rgba(255, 255, 255, 0.82);
  border-radius: 50%;
  background: #fef3c7;
  box-shadow: 0 0 14px rgba(254, 243, 199, 0.96);
}
.satellite-panel {
  position: absolute;
  top: 39px;
  width: 38px;
  height: 24px;
  border: 1px solid rgba(191, 219, 254, 0.84);
  border-radius: 4px;
  background:
    linear-gradient(90deg, rgba(147, 197, 253, 0.36) 1px, transparent 1px),
    linear-gradient(180deg, rgba(147, 197, 253, 0.28) 1px, transparent 1px),
    linear-gradient(135deg, #0f172a, #1d4ed8 64%, #38bdf8);
  background-size: 10px 100%, 100% 8px, 100% 100%;
  box-shadow: inset 0 0 10px rgba(2, 6, 23, 0.34);
}
.panel-left {
  left: 12px;
  transform: rotate(45deg) skewY(-8deg);
}
.panel-right {
  right: 12px;
  transform: rotate(45deg) skewY(8deg);
}
.satellite-antenna {
  position: absolute;
  top: 12px;
  left: 67px;
  width: 3px;
  height: 30px;
  border-radius: 999px;
  background: #e0f2fe;
  transform: rotate(24deg);
  transform-origin: bottom center;
}
.satellite-antenna:before {
  position: absolute;
  top: -5px;
  left: 50%;
  width: 9px;
  height: 9px;
  border-radius: 50%;
  background: #fef3c7;
  box-shadow: 0 0 14px rgba(254, 243, 199, 0.9);
  content: "";
  transform: translateX(-50%);
}
.satellite-signal {
  position: absolute;
  top: 2px;
  left: 76px;
  border: 2px solid rgba(224, 242, 254, 0.78);
  border-bottom-color: transparent;
  border-left-color: transparent;
  border-radius: 0 999px 0 0;
  opacity: 0.72;
  transform: rotate(16deg);
}
.signal-one {
  width: 18px;
  height: 18px;
}
.signal-two {
  top: -6px;
  left: 78px;
  width: 32px;
  height: 32px;
  opacity: 0.4;
}
.station-button:hover {
  transform: translateY(-4px);
  filter: drop-shadow(0 0 24px rgba(125, 211, 252, 0.84));
}
.station-bubble {
  position: relative;
  padding: 10px 14px;
  border: 1px solid rgba(255, 255, 255, 0.22);
  border-radius: 8px;
  background: rgba(15, 23, 42, 0.78);
  color: #e0f2fe;
  font-size: 14px;
  font-weight: 700;
  white-space: nowrap;
  box-shadow: 0 16px 36px rgba(0, 0, 0, 0.28);
  opacity: 0;
  transform: translateY(8px) scale(0.94);
  animation: stationBubble 10s ease-in-out infinite;
  backdrop-filter: blur(12px);
}
.station-bubble:before {
  position: absolute;
  top: 50%;
  left: -7px;
  width: 12px;
  height: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.22);
  border-left: 1px solid rgba(255, 255, 255, 0.22);
  background: rgba(15, 23, 42, 0.78);
  content: "";
  transform: translateY(-50%) rotate(45deg);
}
.station-dialog {
  overflow: hidden;
  border: 1px solid rgba(148, 163, 184, 0.3);
  border-radius: 12px !important;
  background:
    linear-gradient(180deg, rgba(7, 12, 28, 0.99), rgba(4, 8, 22, 0.98)),
    url("https://pic.caiguoyu.cn/20260519195330293.png") center / cover;
  box-shadow: 0 28px 90px rgba(0, 0, 0, 0.58);
  color: #f8fafc;
}
.station-dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px 18px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.12);
  background: linear-gradient(90deg, rgba(14, 165, 233, 0.18), rgba(99, 102, 241, 0.08));
}
.station-dialog-heading {
  display: flex;
  align-items: center;
  gap: 14px;
}
.station-dialog-mark {
  position: relative;
  width: 44px;
  height: 44px;
  border: 1px solid rgba(125, 211, 252, 0.42);
  border-radius: 12px;
  background: rgba(15, 23, 42, 0.74);
  box-shadow: inset 0 0 18px rgba(14, 165, 233, 0.16);
}
.mark-core {
  position: absolute;
  top: 16px;
  left: 50%;
  width: 14px;
  height: 14px;
  border-radius: 4px;
  background: #e0f2fe;
  box-shadow: 0 0 14px rgba(125, 211, 252, 0.9);
  transform: translateX(-50%) rotate(45deg);
}
.mark-panel {
  position: absolute;
  top: 18px;
  left: 7px;
  width: 30px;
  height: 9px;
  border-radius: 3px;
  background: linear-gradient(90deg, #1d4ed8, #38bdf8);
  transform: rotate(45deg);
}
.station-dialog-kicker {
  color: #93c5fd;
  font-size: 12px;
  font-weight: 900;
  letter-spacing: 0;
}
.station-dialog-title {
  margin-top: 4px;
  color: #fff;
  font-size: 24px;
  font-weight: 900;
  line-height: 1.15;
}
.station-dialog-content {
  max-height: 72vh;
  padding: 24px !important;
  background: rgba(2, 6, 23, 0.52);
}
.station-info-panel {
  display: grid;
  gap: 10px;
  margin-bottom: 16px;
  padding: 14px;
  border: 1px solid rgba(125, 211, 252, 0.2);
  border-radius: 8px;
  background:
    linear-gradient(135deg, rgba(15, 23, 42, 0.92), rgba(15, 23, 42, 0.72)),
    linear-gradient(90deg, rgba(56, 189, 248, 0.16), rgba(129, 140, 248, 0.12));
}
.station-info-row {
  display: grid;
  grid-template-columns: 54px 1fr;
  gap: 12px;
  align-items: start;
}
.station-info-row span {
  color: #93c5fd;
  font-size: 13px;
  font-weight: 800;
}
.station-info-row strong {
  overflow-wrap: anywhere;
  color: #f8fafc;
  font-size: 14px;
  font-weight: 600;
  line-height: 1.7;
}
.station-notice {
  margin: 0 0 18px;
  padding: 10px 12px;
  border: 1px solid rgba(250, 204, 21, 0.22);
  border-radius: 8px;
  background: rgba(250, 204, 21, 0.08);
  color: rgba(248, 250, 252, 0.94);
  font-size: 14px;
}
.station-comment-shell {
  padding: 16px;
  border: 1px solid rgba(148, 163, 184, 0.22);
  border-radius: 10px;
  background:
    linear-gradient(180deg, rgba(15, 23, 42, 0.74), rgba(2, 6, 23, 0.64)),
    radial-gradient(circle at 100% 0, rgba(56, 189, 248, 0.12), transparent 34%);
}
.station-comment-heading {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.18);
}
.station-comment-heading span {
  color: #f8fafc;
  font-size: 18px;
  font-weight: 900;
}
.station-comment-heading small {
  color: rgba(147, 197, 253, 0.86);
  font-size: 12px;
  font-weight: 800;
}
@keyframes starTwinkle {
  from {
    transform: scale(0.7);
  }
  to {
    transform: scale(1.6);
  }
}
@keyframes orbitPulse {
  0%,
  100% {
    opacity: 0.34;
  }
  50% {
    opacity: 0.72;
  }
}
@keyframes sunBreath {
  0%,
  100% {
    opacity: 0.4;
    transform: scale(0.92);
  }
  50% {
    opacity: 0.86;
    transform: scale(1.08);
  }
}
@keyframes stationBubble {
  0%,
  62%,
  100% {
    opacity: 0;
    transform: translateY(8px) scale(0.94);
  }
  8%,
  34% {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}
@media (max-width: 759px) {
  .link-hero {
    padding: 66px 12px 42px;
  }
  .solar-system {
    width: 100%;
    height: calc(100vh - 108px);
    min-height: 660px;
  }
  .solar-sun {
    width: 96px;
    height: 96px;
  }
  .planet-label {
    min-width: 116px;
    max-width: 150px;
    padding: 7px 8px;
  }
  .station-zone {
    flex-direction: column-reverse;
    align-items: flex-end;
    gap: 8px;
  }
  .station-button {
    width: 98px;
    height: 74px;
  }
  .satellite-core {
    top: 28px;
    width: 30px;
    height: 30px;
  }
  .satellite-panel {
    top: 32px;
    width: 30px;
    height: 20px;
  }
  .panel-left {
    left: 8px;
  }
  .panel-right {
    right: 8px;
  }
  .satellite-antenna {
    top: 8px;
    left: 50px;
    height: 24px;
  }
  .satellite-signal {
    left: 58px;
  }
  .signal-two {
    left: 60px;
  }
  .station-bubble {
    font-size: 13px;
  }
}
</style>

<style>
body.link-galaxy-active .theme--light.v-application {
  background:
    linear-gradient(180deg, rgba(3, 7, 18, 0.12), rgba(3, 7, 18, 0.78)),
    var(--link-galaxy-space-bg) center center / cover fixed no-repeat !important;
}
body.link-galaxy-active .theme--light.v-app-bar.v-toolbar.v-sheet,
body.link-galaxy-active .theme--light.nav-fixed {
  background: rgba(2, 6, 23, 0.34) !important;
  box-shadow: none !important;
  backdrop-filter: blur(14px);
}
body.link-galaxy-active .theme--light.v-app-bar.v-toolbar.v-sheet a,
body.link-galaxy-active .theme--light.nav-fixed a {
  color: #e5e7eb !important;
}
body.link-galaxy-active .theme--light.v-footer {
  background: transparent !important;
}
body.link-galaxy-active .footer-wrap {
  background:
    linear-gradient(180deg, rgba(2, 6, 23, 0.34), rgba(2, 6, 23, 0.76)),
    var(--link-galaxy-space-bg) center bottom / cover fixed no-repeat !important;
  animation: none !important;
  box-shadow: inset 0 28px 58px rgba(2, 6, 23, 0.62);
}
body.link-galaxy-active .footer-wrap,
body.link-galaxy-active .footer-wrap a {
  color: #e5e7eb !important;
}
body.link-galaxy-active .station-dialog .v-card__text,
body.link-galaxy-active .station-dialog .comment-title,
body.link-galaxy-active .station-dialog .comment-wrapper,
body.link-galaxy-active .station-dialog .comment-content,
body.link-galaxy-active .station-dialog .comment-info,
body.link-galaxy-active .station-dialog .reply-content,
body.link-galaxy-active .station-dialog .count,
body.link-galaxy-active .station-dialog .comment-user,
body.link-galaxy-active .station-dialog .v-input input,
body.link-galaxy-active .station-dialog .v-input textarea {
  color: #f8fafc !important;
}
body.link-galaxy-active .station-dialog .comment-input-wrapper {
  border-color: rgba(125, 211, 252, 0.28) !important;
  background: rgba(15, 23, 42, 0.72);
  box-shadow: inset 0 0 24px rgba(14, 165, 233, 0.08);
}
body.link-galaxy-active .station-dialog .count {
  margin-top: 14px;
  padding: 12px 0 6px !important;
  border-top: 1px solid rgba(148, 163, 184, 0.18);
}
body.link-galaxy-active .station-dialog .comment-title {
  margin-bottom: 12px;
}
body.link-galaxy-active .station-dialog .v-input__slot,
body.link-galaxy-active .station-dialog .comment-textarea {
  min-height: 96px;
  border: 0;
  border-radius: 8px;
  background: rgba(2, 6, 23, 0.72) !important;
  color: #f8fafc !important;
  outline: none;
}
body.link-galaxy-active .station-dialog .comment-textarea::placeholder {
  color: rgba(203, 213, 225, 0.72);
}
body.link-galaxy-active .station-dialog .comment-meta {
  border-bottom-color: rgba(148, 163, 184, 0.18) !important;
}
body.link-galaxy-active .station-dialog .agent,
body.link-galaxy-active .station-dialog .comment-user a,
body.link-galaxy-active .station-dialog .comment-nickname {
  color: #67e8f9 !important;
}
body.link-galaxy-active .station-dialog .reply-btn,
body.link-galaxy-active .station-dialog .like {
  color: #fbbf24 !important;
}
body.link-galaxy-active .station-dialog .v-comment-btn,
body.link-galaxy-active .station-dialog .upload-btn {
  border: 0;
  border-radius: 999px;
  background: linear-gradient(135deg, #38bdf8, #6366f1) !important;
  color: #fff !important;
  box-shadow: 0 10px 24px rgba(37, 99, 235, 0.3);
}
body.link-galaxy-active .station-dialog .emoji-btn,
body.link-galaxy-active .station-dialog .emoji-btn-active {
  color: #e0f2fe !important;
}
body.link-galaxy-active .station-dialog .load-wrapper .v-btn {
  border-color: rgba(125, 211, 252, 0.45) !important;
  color: #e0f2fe !important;
}
</style>
