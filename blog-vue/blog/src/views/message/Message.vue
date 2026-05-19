<template>
  <div>
    <!-- banner -->
    <div class="message-banner" :style="cover">
      <div class="sky-glow"></div>
      <div class="message-stars" aria-hidden="true"></div>

      <!-- 留言输入框 -->
      <div class="message-container" :class="{ launching: isLaunching }">
        <div
          class="animated fadeInUp message-input-wrapper"
          :class="{ 'is-launching': isLaunching }"
        >
          <input
            v-model="messageContent"
            @keyup.enter="addToList"
            :disabled="isLaunching"
            placeholder="说点什么吧"
          />
          <button
            class="ml-3"
            @click="addToList"
            v-show="!isLaunching"
            :disabled="isLaunching"
          >
            发送
          </button>
          <span class="launch-meteor" aria-hidden="true">
            <img :src="messageMeteor" alt="" />
            <em>{{ launchingContent }}</em>
          </span>
        </div>
      </div>

      <!-- 像素弹幕 -->
      <div class="pixel-sky-field">
        <div
          v-for="(item, index) in skyMessageList"
          :key="'sky-' + getMessageKey(item, index)"
          :class="[
            'pixel-sky-message',
            {
              'is-framed': item._marker === 'frame',
              'is-live-bubble': item._marker === 'bubble'
            }
          ]"
          :style="getSkyStyle(item)"
        >
          {{ item.messageContent }}
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getPersistentPokemonAvatar } from "../../utils/avatar";

const messageMeteor = "https://pic.caiguoyu.cn/20260519195330295.png";
const messageNightSky = "https://pic.caiguoyu.cn/20260519195330296.png";

const artificialMessageContents = [
  "节操碎了一地",
  "月亮打了个喷嚏",
  "今天也要发光",
  "像素风吹过来了",
  "快乐正在加载",
  "星星掉线中",
  "别卷了看星星",
  "灵感砸到地上",
  "晚风有点甜",
  "代码正在冒泡",
  "把烦恼丢出去",
  "今天适合摸鱼",
  "宇宙偷偷眨眼",
  "留言正在坠落",
  "快乐碎成小块",
  "好运发射成功",
  "夜空有回音",
  "心情像素化",
  "星河施工中",
  "烦恼原地解散",
  "明天一定早睡",
  "键盘开始发光",
  "风把字吹歪了",
  "地面收到留言",
  "灵魂正在掉帧",
  "浪漫正在刷新",
  "请接住好运",
  "今天不许emo",
  "碎片也会发光",
  "路过留个像素"
];

const pixelColors = [
  "#7df9ff",
  "#fff36d",
  "#ff8bd1",
  "#9cff6a",
  "#ffa45b",
  "#a78bff",
  "#73ffcf",
  "#ff6b6b",
  "#8ec5ff",
  "#f8a5ff"
];

export default {
  mounted() {
    this.messageClientId = this.createMessageClientId();
    this.artificialMessageList = this.createArtificialMessages();
    this.listMessage();
    this.connectMessageStream();
  },
  data() {
    return {
      messageContent: "",
      barrageList: [],
      artificialMessageList: [],
      isLaunching: false,
      launchingContent: "",
      launchTimer: null,
      pendingMessageTimers: [],
      messageClientId: "",
      messageEventSource: null
    };
  },
  beforeDestroy() {
    clearTimeout(this.launchTimer);
    this.pendingMessageTimers.forEach(timer => clearTimeout(timer));
    if (this.messageEventSource) {
      this.messageEventSource.close();
    }
  },
  methods: {
    addToList() {
      if (this.isLaunching) {
        return false;
      }
      const content = this.messageContent.trim();
      if (content == "") {
        this.$toast({ type: "error", message: "留言不能为空" });
        return false;
      }
      this.startLaunchAnimation(content);
      const userNickname = this.$store.state.nickname
        ? this.$store.state.nickname
        : "游客";
      var message = {
        avatar: this.currentAvatar,
        nickname: userNickname,
        messageContent: content,
        time: Math.floor(Math.random() * (10 - 7)) + 7,
        clientId: this.messageClientId
      };
      this.messageContent = "";
      this.axios.post("/api/messages", message).then(({ data }) => {
        if (data.flag) {
          this.queueSentMessage(message);
        } else {
          this.$toast({ type: "error", message: data.message });
        }
      });
    },
    startLaunchAnimation(content) {
      clearTimeout(this.launchTimer);
      this.launchingContent = content;
      this.isLaunching = true;
      this.launchTimer = setTimeout(() => {
        this.isLaunching = false;
        this.launchingContent = "";
      }, 1500);
    },
    listMessage() {
      this.axios.get("/api/messages").then(({ data }) => {
        if (data.flag) {
          this.barrageList = data.data.map((item, index) =>
            this.normalizeMessage(
              item,
              this.artificialMessageList.length + index
            )
          );
        }
      });
    },
    connectMessageStream() {
      if (!window.EventSource) {
        return;
      }
      const streamUrl =
        "/api/messages/subscribe?clientId=" +
        encodeURIComponent(this.messageClientId);
      this.messageEventSource = new EventSource(streamUrl);
      this.messageEventSource.addEventListener("message", event => {
        this.handleStreamMessage(event);
      });
    },
    handleStreamMessage(event) {
      try {
        const message = JSON.parse(event.data);
        if (!message || message.clientId === this.messageClientId) {
          return;
        }
        this.addLiveMessage(message);
      } catch (error) {
        // SSE 推送异常不影响留言页主流程
      }
    },
    addLiveMessage(message) {
      const liveMessage = this.normalizeMessage(
        message,
        this.messageList.length,
        {
          immediate: true,
          marker: "bubble"
        }
      );
      this.barrageList.push(liveMessage);
      this.clearMessageMarker(liveMessage);
    },
    queueSentMessage(message) {
      const timer = setTimeout(() => {
        const freshMessage = this.normalizeMessage(
          message,
          this.messageList.length,
          {
            immediate: true,
            marker: "frame"
          }
        );
        this.barrageList.push(freshMessage);
        this.clearMessageMarker(freshMessage);
      }, 3000);
      this.pendingMessageTimers.push(timer);
    },
    clearMessageMarker(message) {
      const timer = setTimeout(() => {
        this.$set(message, "_marker", "");
      }, (message._sky.duration + 0.2) * 1000);
      this.pendingMessageTimers.push(timer);
    },
    createMessageClientId() {
      return (
        "message-" + Date.now() + "-" + Math.random().toString(16).slice(2)
      );
    },
    createArtificialMessages() {
      return artificialMessageContents.map((content, index) =>
        this.normalizeMessage(
          {
            id: "artificial-message-" + index,
            nickname: "像素路人",
            avatar: "",
            messageContent: content
          },
          index
        )
      );
    },
    normalizeMessage(item, index, options = {}) {
      const message = Object.assign({}, item);
      message._messageKey = this.getMessageKey(message, index);
      message._marker = options.marker || "";
      message._sky = this.createSkyMeta(index, options);
      return message;
    },
    createSkyMeta(index, options = {}) {
      const lanes = [12, 18, 24, 31, 38, 45, 52, 59, 66, 73];
      const lane = index % lanes.length;
      return {
        top: lanes[lane] + Math.random() * 1.5,
        duration: 22 + Math.random() * 10,
        delay: options.immediate ? 0 : Math.random() * 20,
        floatY: -10 + Math.random() * 20,
        color: this.getPixelColor(index)
      };
    },
    getMessageKey(item, index) {
      return (
        item.id ||
        [item.nickname, item.avatar, item.messageContent, index].join("-")
      );
    },
    getSkyStyle(item) {
      return {
        top: item._sky.top + "vh",
        "--sky-duration": item._sky.duration + "s",
        "--sky-delay": item._sky.delay + "s",
        "--sky-float-y": item._sky.floatY + "px",
        "--pixel-color": item._sky.color
      };
    },
    getPixelColor(index) {
      return pixelColors[index % pixelColors.length];
    }
  },
  computed: {
    messageMeteor() {
      return messageMeteor;
    },
    currentAvatar() {
      return (
        this.$store.state.avatar || getPersistentPokemonAvatar("visitorAvatar")
      );
    },
    cover() {
      return {
        backgroundImage: "url(" + messageNightSky + ")"
      };
    },
    messageList() {
      return this.artificialMessageList.concat(this.barrageList);
    },
    skyMessageList() {
      return this.messageList;
    }
  }
};
</script>

<style scoped>
.message-banner {
  position: absolute;
  top: -60px;
  left: 0;
  right: 0;
  height: 100vh;
  overflow: hidden;
  background-color: #06142f;
  background-position: center center;
  background-size: cover;
  background-repeat: no-repeat;
  animation: header-effect 1s;
}
.message-banner::before {
  content: "";
  position: absolute;
  inset: 0;
  background: linear-gradient(
    180deg,
    rgba(2, 9, 28, 0.22) 0%,
    rgba(4, 16, 40, 0.08) 48%,
    rgba(1, 7, 18, 0.36) 100%
  );
  pointer-events: none;
}
.message-banner::after {
  content: "";
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 16vh;
  background: linear-gradient(
    180deg,
    rgba(3, 12, 24, 0) 0%,
    rgba(3, 10, 18, 0.72) 100%
  );
  pointer-events: none;
}
.sky-glow {
  position: absolute;
  inset: 0;
  background: radial-gradient(
      circle at 78% 18%,
      rgba(255, 235, 168, 0.2),
      transparent 20%
    ),
    radial-gradient(
      circle at 50% 78%,
      rgba(129, 192, 255, 0.12),
      transparent 32%
    );
  pointer-events: none;
}
.message-stars {
  position: absolute;
  inset: 0;
  background-image: radial-gradient(
      circle,
      rgba(255, 255, 255, 0.92) 0 1px,
      transparent 1.5px
    ),
    radial-gradient(circle, rgba(180, 222, 255, 0.75) 0 1px, transparent 1.4px);
  background-position: 0 0, 36px 58px;
  background-size: 120px 120px, 180px 180px;
  opacity: 0.42;
  animation: star-twinkle 5s ease-in-out infinite alternate;
  pointer-events: none;
}
.message-container {
  position: absolute;
  right: 34px;
  bottom: 34px;
  width: min(390px, calc(100vw - 68px));
  text-align: right;
  z-index: 6;
  color: #fff;
}
.message-input-wrapper {
  position: relative;
  display: flex;
  justify-content: flex-end;
  height: 2.75rem;
  transform-origin: right center;
  transition: opacity 0.35s ease, transform 0.35s ease;
}
.message-input-wrapper input {
  outline: none;
  width: min(260px, calc(100vw - 168px));
  border-radius: 20px;
  height: 100%;
  padding: 0 1.25rem;
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.72);
  background: rgba(2, 11, 30, 0.34);
  box-shadow: 0 0 20px rgba(110, 178, 255, 0.16);
  backdrop-filter: blur(8px);
  transition: opacity 0.18s ease, transform 0.18s ease;
}
.message-input-wrapper input::-webkit-input-placeholder {
  color: rgba(255, 255, 255, 0.78);
}
.message-input-wrapper button {
  outline: none;
  border-radius: 20px;
  height: 100%;
  padding: 0 1.25rem;
  color: #fff9db;
  border: 1px solid rgba(255, 244, 190, 0.75);
  background: rgba(255, 210, 116, 0.16);
  box-shadow: 0 0 18px rgba(255, 209, 104, 0.22);
  transition: opacity 0.18s ease, transform 0.18s ease;
}
.message-input-wrapper.is-launching input,
.message-input-wrapper.is-launching button {
  opacity: 0;
  transform: scaleX(0.72);
}
.launch-meteor {
  position: absolute;
  right: 0;
  top: 0;
  width: min(340px, calc(100vw - 68px));
  height: 2.75rem;
  border: 1px solid rgba(255, 244, 190, 0.75);
  border-radius: 999px;
  color: #fff9db;
  background: rgba(255, 210, 116, 0.18);
  box-shadow: 0 0 22px rgba(255, 209, 104, 0.32);
  opacity: 0;
  pointer-events: none;
  overflow: hidden;
}
.launch-meteor img {
  position: absolute;
  right: -10px;
  top: 50%;
  width: 124px;
  height: auto;
  transform: translateY(-58%);
}
.launch-meteor em {
  position: absolute;
  left: 18px;
  right: 54px;
  top: 50%;
  overflow: hidden;
  text-align: left;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-style: normal;
  transform: translateY(-50%);
}
.message-input-wrapper.is-launching .launch-meteor {
  animation: input-meteor-launch 1.2s ease-in forwards;
}
.pixel-sky-field {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  width: 100%;
  pointer-events: none;
}
.pixel-sky-field {
  z-index: 4;
}
.pixel-sky-message {
  --sky-duration: 20s;
  --sky-delay: 0s;
  --sky-float-y: 0px;
  --pixel-color: #eaffff;
  position: absolute;
  left: 100vw;
  max-width: 320px;
  color: var(--pixel-color);
  font-family: "Courier New", "Lucida Console", Monaco, monospace !important;
  font-size: 16px;
  font-weight: 700;
  line-height: 1.4;
  letter-spacing: 4px;
  text-shadow: 2px 0 0 rgba(0, 20, 44, 0.95),
    0 2px 0 rgba(0, 20, 44, 0.95), 3px 3px 0 rgba(0, 8, 20, 0.78),
    0 0 12px var(--pixel-color);
  word-break: keep-all;
  white-space: nowrap;
  image-rendering: pixelated;
  pointer-events: none;
  animation: pixel-sky-pass var(--sky-duration) linear var(--sky-delay)
    infinite both;
}
.pixel-sky-message.is-framed {
  padding: 4px 10px;
  border: 2px solid var(--pixel-color);
  background: rgba(5, 18, 42, 0.46);
  box-shadow: 0 0 0 2px rgba(3, 9, 24, 0.78),
    0 0 18px var(--pixel-color);
}
.pixel-sky-message.is-live-bubble {
  min-width: 46px;
  min-height: 46px;
  padding: 10px 16px;
  border: 2px solid var(--pixel-color);
  border-radius: 999px;
  background: rgba(5, 18, 42, 0.56);
  box-shadow: 0 0 0 3px rgba(3, 9, 24, 0.74),
    0 0 18px var(--pixel-color);
}
.pixel-sky-message.is-live-bubble::after {
  content: "";
  position: absolute;
  left: 18px;
  bottom: -8px;
  width: 12px;
  height: 12px;
  border-right: 2px solid var(--pixel-color);
  border-bottom: 2px solid var(--pixel-color);
  background: rgba(5, 18, 42, 0.56);
  transform: rotate(45deg);
  box-shadow: 3px 3px 0 rgba(3, 9, 24, 0.74);
}
@keyframes pixel-sky-pass {
  0% {
    transform: translate3d(0, 0, 0);
    opacity: 0;
  }
  8% {
    opacity: 0.95;
  }
  50% {
    transform: translate3d(-64vw, var(--sky-float-y), 0);
    opacity: 0.95;
  }
  100% {
    transform: translate3d(-140vw, 0, 0);
    opacity: 0;
  }
}
@keyframes star-twinkle {
  from {
    opacity: 0.28;
  }
  to {
    opacity: 0.52;
  }
}
@keyframes input-meteor-launch {
  0% {
    opacity: 1;
    transform: translate3d(0, 0, 0) rotate(0) scale(1);
    width: min(340px, calc(100vw - 68px));
  }
  24% {
    opacity: 1;
    transform: translate3d(-10px, -42px, 0) rotate(-38deg) scale(0.72);
    width: 210px;
  }
  100% {
    opacity: 0;
    transform: translate3d(-8vw, -82vh, 0) rotate(-82deg) scale(0.2);
    width: 52px;
  }
}

@media (max-width: 600px) {
  .message-banner {
    top: -52px;
    background-position: center center;
  }
  .message-container {
    right: 16px;
    bottom: 22px;
    width: calc(100vw - 32px);
  }
  .message-input-wrapper {
    height: auto;
    min-height: 2.5rem;
  }
  .message-input-wrapper input {
    min-width: 0;
    width: calc(100vw - 124px);
  }
  .message-input-wrapper button {
    padding: 0 0.9rem;
  }
  .pixel-sky-message {
    max-width: 240px;
    font-size: 13px;
  }
}
</style>
