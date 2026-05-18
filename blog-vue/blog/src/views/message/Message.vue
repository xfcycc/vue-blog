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

      <!-- 流星留言 -->
      <div class="meteor-field">
        <button
          v-for="(item, index) in meteorList"
          :key="getMessageKey(item, index)"
          class="meteor-message"
          :class="{ active: activeMessage === item._messageKey }"
          :style="getMeteorStyle(item)"
          @click="toggleMessage(item)"
          type="button"
        >
          <img class="meteor-sprite" :src="messageMeteor" alt="" />
          <span class="message-bubble">
            <strong>
              <img :src="getMessageAvatar(item)" alt="" />
              {{ item.nickname }}
            </strong>
            <span>{{ item.messageContent }}</span>
          </span>
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import messageMeteor from "../../assets/images/message-meteor-soft.png";
import messageNightSky from "../../assets/images/message-night-sky.png";
import {
  getPersistentPokemonAvatar,
  getPokemonAvatarBySeed,
  isPokemonAvatar,
  normalizePokemonAvatar
} from "../../utils/avatar";

export default {
  mounted() {
    this.listMessage();
  },
  data() {
    return {
      messageContent: "",
      barrageList: [],
      activeMessage: "",
      isLaunching: false,
      launchingContent: "",
      launchTimer: null
    };
  },
  beforeDestroy() {
    clearTimeout(this.launchTimer);
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
        time: Math.floor(Math.random() * (10 - 7)) + 7
      };
      this.messageContent = "";
      this.axios.post("/api/messages", message).then(({ data }) => {
        if (data.flag) {
          this.barrageList.push(
            this.normalizeMessage(message, this.barrageList.length)
          );
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
            this.normalizeMessage(item, index)
          );
        }
      });
    },
    normalizeMessage(item, index) {
      const message = Object.assign({}, item);
      message._messageKey = this.getMessageKey(message, index);
      message._meteor = this.createMeteorMeta(index);
      return message;
    },
    createMeteorMeta(index) {
      const columns = [-10, -4, 2, 8, 14, 20, 26, 32];
      const lane = index % columns.length;
      return {
        left: columns[lane] + Math.random() * 4,
        top: -10 - Math.random() * 8,
        driftX: 46 + Math.random() * 20,
        driftY: 52 + Math.random() * 24,
        duration: 18 + Math.random() * 6,
        delay: 0.5 + index * 3.2 + Math.random() * 0.8,
        scale: 0.58 + Math.random() * 0.22
      };
    },
    getMessageKey(item, index) {
      return (
        item.id ||
        [item.nickname, item.avatar, item.messageContent, index].join("-")
      );
    },
    getMeteorStyle(item) {
      return {
        left: item._meteor.left + "%",
        top: item._meteor.top + "vh",
        "--drift-x": item._meteor.driftX + "vw",
        "--drift-y": item._meteor.driftY + "vh",
        "--fall-duration": item._meteor.duration + "s",
        "--fall-delay": item._meteor.delay + "s",
        "--meteor-scale": item._meteor.scale
      };
    },
    toggleMessage(item) {
      this.activeMessage =
        this.activeMessage === item._messageKey ? "" : item._messageKey;
    },
    getMessageAvatar(item) {
      if (isPokemonAvatar(item.avatar)) {
        return normalizePokemonAvatar(item.avatar);
      }
      return getPokemonAvatarBySeed(
        ["message", item.nickname, item.avatar].join("-")
      );
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
    meteorList() {
      return this.barrageList.slice(-12);
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
.meteor-field {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  width: 100%;
  z-index: 4;
  pointer-events: none;
}
.meteor-message {
  --drift-x: 96vw;
  --drift-y: 90vh;
  --fall-duration: 22s;
  --fall-delay: 0s;
  --meteor-scale: 1;
  position: absolute;
  width: 128px;
  height: 110px;
  padding: 0;
  border: 0;
  background: transparent;
  cursor: pointer;
  opacity: 0;
  pointer-events: auto;
  transform: translate3d(0, 0, 0) scale(var(--meteor-scale));
  animation: meteor-fall var(--fall-duration) linear var(--fall-delay) infinite
    both;
}
.meteor-message:focus {
  outline: none;
}
.meteor-message:focus .meteor-sprite,
.meteor-message:hover .meteor-sprite,
.meteor-message.active .meteor-sprite {
  filter: drop-shadow(0 0 18px rgba(255, 224, 137, 0.98));
}
.meteor-sprite {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: contain;
  opacity: 0.84;
  filter: drop-shadow(0 0 8px rgba(255, 218, 132, 0.42));
}
.message-bubble {
  position: absolute;
  left: 74%;
  bottom: 74px;
  width: max-content;
  max-width: 260px;
  padding: 10px 14px;
  border-radius: 18px 18px 18px 4px;
  color: #1b2542;
  text-align: left;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 12px 34px rgba(0, 0, 0, 0.25);
  opacity: 0;
  visibility: hidden;
  transform: translate(-50%, 8px);
  transition: opacity 0.2s ease, transform 0.2s ease, visibility 0.2s;
}
.message-bubble::after {
  content: "";
  position: absolute;
  left: 24px;
  bottom: -8px;
  width: 16px;
  height: 16px;
  background: rgba(255, 255, 255, 0.92);
  transform: rotate(45deg);
}
.message-bubble strong,
.message-bubble span {
  position: relative;
  z-index: 1;
  display: block;
}
.message-bubble strong {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 4px;
  color: #5966a7;
  font-size: 0.85rem;
}
.message-bubble strong img {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  object-fit: cover;
}
.message-bubble span {
  font-size: 0.95rem;
  line-height: 1.5;
  word-break: break-word;
}
.meteor-message.active .message-bubble,
.meteor-message:hover .message-bubble {
  opacity: 1;
  visibility: visible;
  transform: translate(-50%, 0);
}
@keyframes meteor-fall {
  0% {
    transform: translate3d(0, 0, 0) scale(var(--meteor-scale));
    opacity: 0;
  }
  6% {
    opacity: 1;
  }
  84% {
    transform: translate3d(var(--drift-x), var(--drift-y), 0)
      scale(var(--meteor-scale));
    opacity: 1;
  }
  90% {
    transform: translate3d(var(--drift-x), var(--drift-y), 0)
      scale(calc(var(--meteor-scale) * 0.72));
    opacity: 0;
  }
  100% {
    transform: translate3d(var(--drift-x), var(--drift-y), 0)
      scale(calc(var(--meteor-scale) * 0.72));
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
  .message-bubble {
    max-width: 210px;
  }
}
</style>
