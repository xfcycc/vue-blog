/**
 * 聊天室组件
 * @author caiguoyu
 * @date 2025/2/25
 * @version 0.0.1
 * @description 聊天室组件增强版
 */
<template>
  <!-- eslint-disable -->
  <div>
    <!-- 聊天界面 -->
    <div
      class="chat-container animated bounceInUp"
      v-show="isShow"
      @click="closeAll"
      @contextmenu.prevent.stop="closeAll"
    >
      <!-- 标题 -->
      <div class="header">
        <img
          width="32"
          height="32"
          src="https://pic.blog.caiguoyu.cn/config/chatroomIcon.png"
        />
        <div class="header-title">
          <div>聊天室</div>
          <div style="font-size:12px">当前{{ count }}人在线</div>
        </div>
        <div class="chat-profile">
          <div class="chat-name">
            你的名称: <span class="nickname-text">{{ chatNickname }}</span>
            <v-icon
              small
              class="refresh-name"
              title="刷新匿名"
              @click.stop="refreshName"
            >
              mdi-refresh
            </v-icon>
          </div>
          <div class="refresh-count">
            今日可刷新次数：<span class="remain-count">{{ chatRemainCount }}</span>
          </div>
        </div>
        <v-icon class="close" @click.stop="closeChat">
          mdi-close
        </v-icon>
      </div>
      <!-- 对话内容 -->
      <div class="message" id="message" ref="messageContainer">
        <!-- 录音遮罩层 -->
        <div
          v-show="voiceActive"
          class="voice"
          @mousemove.prevent.stop="translationmove($event)"
          @mouseup.prevent.stop="translationEnd($event)"
        >
          <v-icon ref="voiceClose" class="close-voice">mdi-close</v-icon>
        </div>
        <!-- 历史消息加载提示 -->
        <div v-if="isLoading" class="loading-container">
          <v-progress-circular
            indeterminate
            color="primary"
            size="24"
            width="2"
          ></v-progress-circular>
          <span class="loading-text ml-2">加载聊天记录中...</span>
        </div>
        <!-- 连接状态提示 -->
        <div v-if="!isConnected && !isLoading" class="connection-status">
          <v-icon color="warning">mdi-wifi-off</v-icon>
          <span class="ml-2">聊天室连接已断开，正在尝试重新连接...</span>
        </div>
        <div
          :class="isMyMessage(item)"
          v-for="(item, index) of chatRecordList"
          :key="index"
          v-if="item"
        >
          <!-- 头像 -->
          <img :src="getChatAvatar(item)" :class="isleft(item)" />
          <div>
            <div class="nickname">
              {{ item.nickname || '匿名用户' }}
              <span style="margin-left:12px">{{ item.createTime | hour }}</span>
            </div>
            <!-- 内容 -->
            <div
              ref="content"
              @contextmenu.prevent.stop="showBack(item, index, $event)"
              :class="getContentClass(item)"
            >
              <!-- 文字消息 -->
              <div
                v-if="item.type == 3"
                class="text-content"
                :class="{ 'emoji-only-content': isEmojiOnlyMessage(item.content) }"
              >
                <template v-for="(part, partIndex) in parseMessageContent(item.content)">
                  <span
                    v-if="part.type === 'emoji'"
                    :key="'emoji-' + partIndex + '-' + part.value"
                    class="chat-emoji-wrap"
                  >
                    <img
                      :src="part.value"
                      class="chat-emoji"
                    />
                  </span>
                  <span
                    v-else
                    :key="'text-' + partIndex + '-' + part.value"
                  >{{ part.value }}</span>
                </template>
              </div>
              <!-- 语音消息 -->
              <div
                v-if="item.type == 5"
                class="voice-message"
                @click.prevent.stop="playVoice(item)"
              >
                <audio
                  @ended="endVoice(item)"
                  @canplay="getVoiceTime(item)"
                  :ref="getVoiceRef(item)"
                  :src="item.content"
                  style="display:none"
                />
                <!-- 播放 -->
                <v-icon
                  v-show="!isVoicePlaying(item)"
                  :color="isSelf(item) ? '#2f4558' : '#000'"
                  style="display:inline-flex;cursor: pointer;"
                >
                  mdi-arrow-right-drop-circle
                </v-icon>
                <!-- 暂停 -->
                <v-icon
                  v-show="isVoicePlaying(item)"
                  :color="isSelf(item) ? '#2f4558' : '#000'"
                  style="display:inline-flex;cursor: pointer;"
                >
                  mdi-pause-circle
                </v-icon>
                <!-- 音频时长 -->
                <span>{{ getVoiceDurationText(item) }}</span>
              </div>
              <div class="back-menu" ref="backBtn">
                <div
                  class="menu-item"
                  @click.stop="back(currentUser, getCurrentIndex(currentUser))"
                  v-if="currentUser && currentUser.ipAddress && isSelf(currentUser)"
                >
                  撤回
                </div>
                <div
                  class="menu-item"
                  @click.stop="mentionUser(currentUser)"
                  v-else-if="currentUser && currentUser.ipAddress"
                >
                  @TA
                </div>
              </div>
            </div>
          </div>
        </div>
        <!-- 无消息时的提示 -->
        <div v-if="chatRecordList.length === 0 && !isLoading" class="empty-message">
          <v-icon large color="grey lighten-1">mdi-message-outline</v-icon>
          <p>暂无消息记录，开始聊天吧！</p>
        </div>
      </div>
      <!-- 输入框 -->
      <div class="footer">
        <!-- 表情框 -->
        <div class="emoji-box" v-show="isEmoji">
          <Emoji :chooseEmoji="true" @addEmoji="addEmoji" />
        </div>
        <div class="emoji-border" v-show="isEmoji" />
        <!-- 切换输入方式 -->
        <v-icon
          v-show="!isVoice"
          @click="isVoice = !isVoice"
          style="margin-right: 8px"
        >
          mdi-microphone
        </v-icon>
        <v-icon
          v-show="isVoice"
          @click="isVoice = !isVoice"
          style="margin-right: 8px"
        >
          mdi-keyboard
        </v-icon>
        <!-- 文字输入 -->
        <textarea
          v-show="!isVoice"
          ref="chatInput"
          v-model="content"
          @keydown.enter.exact.prevent="saveMessage"
          placeholder="请输入内容"
        />
        <!-- 语音输入 -->
        <button
          class="voice-btn"
          v-show="isVoice"
          :disabled="!isConnected || isConnecting"
          @mousedown.prevent.stop="translationStart"
          @mouseup.prevent.stop="translationEnd($event)"
          @touchstart.prevent.stop="translationStart"
          @touchend.prevent.stop="translationEnd($event)"
          @touchmove.prevent.stop="translationmove($event)"
        >
          按住说话
        </button>
        <!-- 表情 -->
        <i
          class="iconfont iconbiaoqing emoji"
          :style="isEmoji ? 'color:#FFC83D' : ''"
          @click.prevent.stop="openEmoji"
        />
        <!-- 发送按钮 -->
        <i
          :class="isInput"
          @click="saveMessage"
          style="font-size: 1.5rem"
        />
      </div>
    </div>
    <!-- 未读数量 -->
    <div class="chat-btn" @click="toggleChat">
      <span class="unread" v-if="unreadCount > 0">{{ unreadText }}</span>
      <img
        width="100%"
        height="100%"
        src="https://pic.blog.caiguoyu.cn/config/chatroomIcon.png"
      />
    </div>
  </div>
</template>

<script>
/* eslint-disable prettier/prettier */
import Recorderx, { ENCODE_TYPE } from "recorderx";
import Emoji from "./Emoji";
import EmojiList from "../assets/js/emoji";
import axios from "axios";
import {
  getPersistentPokemonAvatar,
  getPokemonAvatarBySeed,
  isPokemonAvatar
} from "../utils/avatar";
export default {
  components: {
    Emoji
  },
  created() {
    // 获取初始数据
    this.getNickname().catch(error => {
      console.warn("初始化时获取昵称失败:", error);
    });
    this.loadCachedMessages();
  },
  beforeDestroy() {
    // 清理心跳定时器
    if (this.heartBeat) {
      clearInterval(this.heartBeat);
    }
    
    // 清理重连定时器
    if (this.reconnectTimer) {
      clearInterval(this.reconnectTimer);
    }
    
    // 关闭WebSocket连接
    if (this.websocket) {
      try {
        this.isManualClose = true;
        this.websocket.close();
      } catch (error) {
        console.warn("关闭WebSocket时出错:", error);
      }
    }
    
    console.log("聊天室组件已销毁，资源已清理");
  },
  data: function() {
    return {
      isEmoji: false,
      isShow: false,
      websocket: null,
      websocketUrl: null,
      content: "",
      chatRecordList: [],
      rc: null,
      ipAddress: "",
      ipSource: "",
      count: 0,
      unreadCount: 0,
      chatNickname: "匿名用户",
      chatRefreshCount: 0,
      chatRemainCount: null,
      isVoice: false,
      voiceActive: false,
      isRecording: false,
      startVoiceTime: null,
      playingVoiceMap: {},
      voiceDurationMap: {},
      heartBeat: null,
      currentUser: null,
      isLoading: false, // 新增：加载状态
      historyLoaded: false, // 新增：历史消息已加载标志
      isConnected: false, // 新增：连接状态
      isConnecting: false,
      isManualClose: false,
      reconnectTimer: null, // 新增：重连定时器
      reconnectAttempts: 0, // 新增：重连尝试次数
    };
  },
  methods: {
    clearReconnectTimer() {
      if (this.reconnectTimer) {
        clearInterval(this.reconnectTimer);
        this.reconnectTimer = null;
      }
    },
    // 设置重连定时器
    setupReconnectTimer() {
      if (this.isManualClose || this.isConnected || this.reconnectTimer) {
        return;
      }
      
      // 设置新的重连定时器，每5秒尝试一次，最多尝试12次（1分钟）
      this.reconnectTimer = setInterval(() => {
        if (this.isConnected) {
          // 如果已连接，清除定时器
          this.clearReconnectTimer();
          this.reconnectAttempts = 0;
          return;
        }
        
        // 增加重连尝试次数
        this.reconnectAttempts++;
        
        if (this.reconnectAttempts > 12) {
          // 如果尝试次数超过限制，停止重连
          this.clearReconnectTimer();
          console.log("重连尝试已达最大次数，停止重连");
          
          // 显示提示信息
          this.$toast({ 
            type: "error", 
            message: "聊天室连接失败，请刷新页面重试",
            duration: 5000
          });
          
          return;
        }
        
        console.log(`正在尝试重新连接 (${this.reconnectAttempts}/12)...`);
        
        // 尝试重新获取WebSocket URL并连接
        this.getWebsocketUrl().then(() => {
          this.connect();
        }).catch(error => {
          console.error("重新获取WebSocket URL失败:", error);
        });
      }, 5000); // 每5秒尝试一次
    },
    ensureReconnect() {
      if (this.isConnected || this.isConnecting) {
        return;
      }
      if (this.websocketUrl) {
        this.connect();
      } else {
        this.getWebsocketUrl().then(() => {
          this.connect();
        }).catch(error => {
          console.error("重新获取WebSocket URL失败:", error);
          this.setupReconnectTimer();
        });
      }
      this.setupReconnectTimer();
    },
    toggleChat() {
      if (this.isShow) {
        this.closeChat();
        return;
      }
      this.open();
    },
    closeChat() {
      this.isShow = false;
      this.closeAll();
      this.disconnectWebsocket();
    },
    open() {
      try {
        this.isManualClose = false;
        // 打开聊天室前进行连接检查
        if (!this.websocket || this.websocket.readyState !== WebSocket.OPEN) {
          console.log("聊天室连接尚未就绪，重新建立连接");
          this.ensureReconnect();
        }
        
        // 重置未读消息计数
        this.unreadCount = 0;
        this.isShow = true;
        
        // 使用nextTick确保DOM已更新
        this.$nextTick(() => {
          this.scrollToBottom();
        });
        
        // 尝试获取用户昵称
        this.getNickname().catch(error => {
          console.error("获取昵称失败:", error);
        });
      } catch (error) {
        console.error("打开聊天室时出错:", error);
      }
    },
    openEmoji() {
      this.isEmoji = !this.isEmoji;
      this.isVoice = false;
    },
    connect() {
      try {
        if (this.isConnecting) {
          return;
        }
        if (this.websocket && this.websocket.readyState === WebSocket.OPEN) {
          this.isConnected = true;
          return;
        }
        // 设置加载状态
        this.isLoading = true;
        this.isConnecting = true;
        
        // 检查是否配置了websocket URL
        if (!this.websocketUrl) {
          console.error("没有配置聊天室的WebSocket URL");
          this.isLoading = false;
          this.isConnecting = false;
          return;
        }
        
        // 关闭已有连接
        if (this.websocket) {
          try {
            this.websocket.onopen = null;
            this.websocket.onmessage = null;
            this.websocket.onclose = null;
            this.websocket.onerror = null;
            this.websocket.close();
            console.log("已关闭旧的WebSocket连接");
          } catch (err) {
            console.warn("关闭旧连接时出错:", err);
          }
        }
        
        // 创建新连接
        console.log("正在连接到WebSocket:", this.websocketUrl);
        this.websocket = new WebSocket(this.websocketUrl);
        
        // 设置事件处理器
        this.websocket.onopen = this.onOpen;
        this.websocket.onmessage = this.onMessage;
        this.websocket.onclose = this.onClose;
        this.websocket.onerror = this.onError;
        
        console.log("已尝试连接聊天室WebSocket:", this.websocketUrl);
      } catch (error) {
        console.error("连接聊天室WebSocket时出错:", error);
        this.isLoading = false;
        this.isConnecting = false;
        this.isConnected = false;
      }
    },
    disconnectWebsocket() {
      this.isManualClose = true;
      this.clearReconnectTimer();
      this.reconnectAttempts = 0;
      this.isConnected = false;
      this.isConnecting = false;
      this.isLoading = false;
      this.count = 0;
      if (this.heartBeat) {
        clearInterval(this.heartBeat);
        this.heartBeat = null;
      }
      if (this.websocket) {
        try {
          this.websocket.close();
        } catch (error) {
          console.warn("关闭聊天室WebSocket时出错:", error);
        } finally {
          this.websocket = null;
        }
      }
    },
    onOpen() {
      console.log("聊天室WebSocket连接已建立");
      this.isConnected = true;
      this.isConnecting = false;
      this.isManualClose = false;
      this.reconnectAttempts = 0;
      this.clearReconnectTimer();
      
      // 连接建立后，立即请求历史消息
      this.requestHistoryMessages();
      
      // 设置心跳
      if (!this.heartBeat) {
        this.heartBeat = setInterval(() => {
          this.sendSocketMessage(6, null, { silent: true });
        }, 30000); // 30秒发送一次心跳
      }
    },
    onMessage(event) {
      try {
        if (!event || !event.data) {
          console.warn("收到空消息事件");
          return;
        }
        
        const data = JSON.parse(event.data);
        console.log("收到WebSocket消息:", data);
        
        // 处理不同类型的消息
        if (data.type === 1) { // 在线人数
          console.log("收到在线人数消息:", data.count);
          this.count = data.data;
        } else if (data.type === 2) {
          // 处理历史消息
          console.log("收到历史消息:", data.data);
          this.handleHistoryMessages(data.data);
          this.historyLoaded = true; // 标记历史消息已加载
          console.log("WebSocket历史消息加载完成");
        } else if (data.type === 4) { // 撤回消息
          if (data.data && data.data.id) {
            this.handleMessageRevocation(data.data.id);
          }
        } else if (data.type === 6) { // 心跳消息
          console.log("收到心跳消息");
        } else if (data.type === 3 || data.type === 5) { // 普通消息
          const shouldScroll = this.shouldAutoScroll();
          this.chatRecordList.push(data.data);
          
          // 缓存更新后的消息
          this.cacheMessages();
          
          if (!this.isShow) {
            this.unreadCount++;
          } else if (shouldScroll) {
            this.$nextTick(() => {
              this.scrollToBottom();
            });
          }
        }
      } catch (error) {
        console.error("处理WebSocket消息时出错:", error);
      }
    },
    onClose() {
      console.log("聊天室WebSocket连接已关闭");
      this.isConnected = false;
      this.isConnecting = false;
      this.isLoading = false;
      
      // 如果不是主动关闭，则尝试重连
      if (this.isManualClose) {
        this.isManualClose = false;
      } else if (this.isShow) {
        this.setupReconnectTimer();
      }
    },
    onError(error) {
      console.error("聊天室WebSocket连接错误:", error);
      this.isConnected = false;
      this.isConnecting = false;
      this.isLoading = false;
      
      // 尝试重连
      this.setupReconnectTimer();
    },
    sendSocketMessage(type, data, options = {}) {
      if (this.websocket && this.websocket.readyState === WebSocket.OPEN) {
        this.websocket.send(JSON.stringify({ type, data }));
        return true;
      }
      this.isConnected = false;
      if (!options.silent) {
        this.$toast({ type: "error", message: "聊天室连接已断开，正在尝试重连" });
      }
      this.ensureReconnect();
      return false;
    },
    saveMessage(e) {
      if (e) {
        e.preventDefault();
      }
      if (this.content.trim() == "") {
        return false;
      }
      var socketMsg = {
        nickname: this.chatNickname,
        avatar: this.avatar,
        content: this.content,
        userId: this.userId,
        type: 3,
        ipAddress: this.ipAddress,
        ipSource: this.ipSource
      };
      if (this.sendSocketMessage(3, socketMsg)) {
        this.content = "";
      }
    },
    addEmoji(key) {
      this.isEmoji = false;
      this.$refs.chatInput.focus();
      this.content += key;
    },
    getChatAvatar(item) {
      if (isPokemonAvatar(item.avatar)) {
        return item.avatar;
      }
      return getPokemonAvatarBySeed(
        [
          "chat",
          item.userId,
          item.nickname,
          item.ipAddress,
          item.avatar
        ].join("-")
      );
    },
    // 展示菜单
    showBack(item, index, e) {
      // 确保backBtn引用存在且为数组
      if (!this.$refs.backBtn || !Array.isArray(this.$refs.backBtn)) {
        return;
      }
      
      // 关闭所有打开的菜单
      this.$refs.backBtn.forEach(menu => {
        if (menu) {
          menu.style.display = "none";
        }
      });
      
      // 设置当前操作的用户
      this.currentUser = item;
      
      // 确保内容引用存在且索引有效
      if (!this.$refs.content || !this.$refs.content[index]) {
        return;
      }
      
      // 计算菜单位置
      const contentWidth = this.$refs.content[index].offsetWidth;
      const contentHeight = this.$refs.content[index].offsetHeight;
      
      let leftPos = Math.min(e.offsetX, contentWidth - 80); // 菜单宽度为80px
      let bottomPos = Math.min(e.offsetY, contentHeight - 70); // 假设菜单高度约70px
      
      // 确保当前索引的backBtn存在
      if (this.$refs.backBtn[index]) {
        // 设置菜单位置并显示
        this.$refs.backBtn[index].style.left = leftPos + "px";
        this.$refs.backBtn[index].style.bottom = bottomPos + "px";
        this.$refs.backBtn[index].style.display = "block";
      }
    },
    // 撤回消息
    back(item, index) {
      if (!item || index < 0) return;
      
      var socketMsg = {
        id: item.id,
        isVoice: item.type == 5
      };
      this.sendSocketMessage(4, socketMsg);
      
      // 隐藏菜单
      this.closeAll();
    },
    closeAll() {
      try {
        const backBtn = this.$refs.backBtn;
        
        // 检查backBtn是否存在并根据其类型进行处理
        if (backBtn) {
          if (Array.isArray(backBtn) && backBtn.length > 0) {
            // 如果是数组，遍历处理
            backBtn.forEach((item) => {
              if (item && item.style) {
                item.style.display = "none";
              }
            });
          } else if (backBtn.style) {
            // 如果是单个元素
            backBtn.style.display = "none";
          }
        }

        // 清理菜单状态
        if (this.menuVisible) {
          this.menuVisible = false;
        }
        
        if (this.currentUser) {
          this.currentUser = null;
        }
      } catch (error) {
        console.error("关闭菜单时出错:", error);
      }
    },
    // 录音开始
    translationStart() {
      if (!this.isConnected) {
        this.$toast({ type: "error", message: "聊天室连接已断开，正在尝试重连" });
        this.ensureReconnect();
        return;
      }
      this.voiceActive = true;
      this.isRecording = false;
      this.startVoiceTime = null;
      this.rc = new Recorderx();
      this.$nextTick(() => {
        this.rc
          .start()
          .then(() => {
            this.isRecording = true;
            this.startVoiceTime = new Date();
            console.log("start recording");
          })
          .catch(error => {
            this.voiceActive = false;
            this.isRecording = false;
            this.rc = null;
            this.$toast({ type: "error", message: "录音启动失败，请检查麦克风权限" });
            console.log("Recording failed.", error);
          });
      });
    },
    // 录音结束
    translationEnd() {
      console.log("结束");
      this.voiceActive = false;
      if (!this.rc || !this.isRecording || !this.startVoiceTime) {
        this.isRecording = false;
        return false;
      }
      try {
        this.rc.pause();
        if (new Date() - this.startVoiceTime < 1000) {
          this.$toast({ type: "error", message: "按键时间太短" });
          return false;
        }
        var wav = this.rc.getRecord({
          encodeTo: ENCODE_TYPE.WAV
        });
        var file = new File([wav], "voice.wav", {
          type: wav.type
        });
        var formData = new window.FormData();
        formData.append("file", file);
        formData.append("type", 5);
        formData.append("nickname", this.chatNickname);
        formData.append("avatar", this.avatar);
        if (this.userId != null) {
          formData.append("userId", this.userId);
        }
        formData.append("ipAddress", this.ipAddress);
        formData.append("ipSource", this.ipSource);
        var options = {
          url: "/api/voice",
          data: formData,
          method: "post",
          headers: {
            "Content-Type": "multipart/form-data"
          }
        };
        this.axios(options).catch(error => {
          console.error("语音上传失败:", error);
          this.$toast({ type: "error", message: "语音发送失败，请稍后重试" });
        });
      } catch (error) {
        console.error("处理录音失败:", error);
        this.$toast({ type: "error", message: "录音处理失败，请稍后重试" });
      } finally {
        this.isRecording = false;
        this.startVoiceTime = null;
      }
    },
    translationmove() {},
    getMessageKey(item) {
      if (!item) {
        return "unknown";
      }
      if (item.id != null) {
        return item.id;
      }
      return [item.type, item.createTime || "", this.chatRecordList.indexOf(item)].join("-");
    },
    getVoiceRef(item) {
      return "voice-" + this.getMessageKey(item);
    },
    getRefElement(refName) {
      const ref = this.$refs[refName];
      return Array.isArray(ref) ? ref[0] : ref;
    },
    isVoicePlaying(item) {
      return !!this.playingVoiceMap[this.getMessageKey(item)];
    },
    getVoiceDurationText(item) {
      return this.voiceDurationMap[this.getMessageKey(item)] || "";
    },
    pauseOtherVoices(currentKey) {
      this.chatRecordList.forEach(msg => {
        const key = this.getMessageKey(msg);
        if (key === currentKey || !this.playingVoiceMap[key]) {
          return;
        }
        const player = this.getRefElement(this.getVoiceRef(msg));
        if (player && !player.paused) {
          player.pause();
        }
        this.$set(this.playingVoiceMap, key, false);
      });
    },
    // 播放语音
    playVoice(item) {
      var refName = this.getVoiceRef(item);
      var player = this.getRefElement(refName);
      var voiceKey = this.getMessageKey(item);
      if (!player) {
        this.$toast({ type: "error", message: "语音加载失败" });
        return;
      }
      if (player.paused) {
        this.pauseOtherVoices(voiceKey);
        player.play().then(() => {
          this.$set(this.playingVoiceMap, voiceKey, true);
        }).catch(error => {
          console.error("语音播放失败:", error);
          this.$toast({ type: "error", message: "语音播放失败" });
        });
      } else {
        player.pause();
        this.$set(this.playingVoiceMap, voiceKey, false);
      }
    },
    // 语音结束
    endVoice(item) {
      this.$set(this.playingVoiceMap, this.getMessageKey(item), false);
    },
    // 获取语音时长
    getVoiceTime(item) {
      var player = this.getRefElement(this.getVoiceRef(item));
      if (!player || !player.duration) {
        return;
      }
      var time = player.duration;
      time = Math.ceil(time);
      var str = "⬝⬝⬝";
      for (var i = 0; i < time; i++) {
        if (i % 2 == 0) {
          str += "⬝";
        }
      }
      this.$set(this.voiceDurationMap, this.getMessageKey(item), " " + str + " " + time + " ''");
    },
    refreshName() {
      axios
        .post(
          "/api/chatroom/refreshName",
          {},
          { params: { team: "jinyongNickname" } }
        )
        .then(({ data }) => {
          if (data.code === 20000) {
            this.applyChatNickname(data.data);
            this.getNickname().catch(error => {
              console.error("刷新后同步匿名失败:", error);
            });
          } else if (data.code === 203) {
            alert(data.message);
          }
        });
    },
    applyChatNickname(data) {
      const nicknameInfo = typeof data === "string" ? { nickname: data } : data || {};
      this.chatNickname = nicknameInfo.nickname ? nicknameInfo.nickname : "匿名用户";
      this.chatRefreshCount = nicknameInfo.count != null ? nicknameInfo.count : 0;
      this.chatRemainCount = nicknameInfo.remainCount != null ? nicknameInfo.remainCount : null;
    },
    // 添加新方法：@用户
    mentionUser(user) {
      this.content += " @" + user.nickname + " ";
      this.$refs.chatInput.focus();
    },
    // 添加新方法获取当前消息索引
    getCurrentIndex(item) {
      return this.chatRecordList.findIndex(msg => msg.id === item.id);
    },
    // 添加滚动到底部的方法
    getMessageContainer() {
      return this.$refs.messageContainer || document.getElementById("message");
    },
    shouldAutoScroll() {
      const messageContainer = this.getMessageContainer();
      if (!messageContainer) {
        return true;
      }
      return messageContainer.scrollHeight - messageContainer.scrollTop - messageContainer.clientHeight < 80;
    },
    scrollToBottom() {
      this.$nextTick(() => {
        const messageContainer = this.getMessageContainer();
        if (messageContainer) {
          messageContainer.scrollTop = messageContainer.scrollHeight;
        }
      });
    },
    getCacheKey() {
      return "chatRecordList:" + (this.websocketUrl || "default");
    },
    normalizeMessageContent(content) {
      if (content == null) {
        return "";
      }
      let text = String(content);
      const emojiMap = Object.keys(EmojiList).reduce((map, key) => {
        map[EmojiList[key]] = key;
        return map;
      }, {});
      text = text.replace(/<img[^>]*src\s*=\s*['"]([^'"]+)['"][^>]*>/gi, function(match, src) {
        return emojiMap[src] || "";
      });
      return text.replace(/<[^>]*>/g, "");
    },
    parseMessageContent(content) {
      const text = this.normalizeMessageContent(content);
      if (!text) {
        return [];
      }
      return text.split(/(\[[^\]]+\])/g).filter(part => part !== "").map(part => {
        if (EmojiList[part]) {
          return {
            type: "emoji",
            value: EmojiList[part]
          };
        }
        return {
          type: "text",
          value: part
        };
      });
    },
    isEmojiOnlyMessage(content) {
      const parts = this.parseMessageContent(content);
      return parts.length > 0 && parts.every(part => part.type === "emoji");
    },
    // 获取用户昵称方法
    getNickname() {
      return new Promise((resolve, reject) => {
        axios.post("/api/chatroom/nickname")
          .then(({ data }) => {
            if (data.code === 20000) {
              this.applyChatNickname(data.data);
              resolve(data.data);
            } else {
              this.chatNickname = "匿名用户";
              this.chatRefreshCount = 0;
              reject(new Error("获取昵称失败"));
            }
          })
          .catch(error => {
            console.error("获取昵称请求失败:", error);
            this.chatNickname = "匿名用户";
            reject(error);
          });
      });
    },
    
    // 新增：获取WebSocket URL的方法
    getWebsocketUrl() {
      return new Promise((resolve, reject) => {
        axios.get("/api")
          .then(({ data }) => {
            if (data.code === 20000 && data.data && data.data.websiteConfig.websocketUrl) {
              this.websocketUrl = data.data.websiteConfig.websocketUrl;
              this.ipAddress = data.data.ipAddress || "";
              this.ipSource = data.data.ipSource || "";
              console.log("成功获取聊天室WebSocket URL:", this.websocketUrl);
              resolve(data.data);
            } else {
              console.error("获取聊天室配置失败:", data.message || "服务器返回无效配置");
              reject(new Error(data.message || "获取WebSocket配置失败"));
            }
          })
          .catch(error => {
            console.error("获取聊天室配置请求失败:", error);
            reject(error);
          });
      });
    },
    
    // 备用方法：如果服务器未返回IP地址，尝试单独获取
    getIpInfo() {
      return new Promise((resolve, reject) => {
        axios.get("/api/info/ip")
          .then(({ data }) => {
            if (data.code === 20000 && data.data) {
              this.ipAddress = data.data.ipAddress || "";
              this.ipSource = data.data.ipSource || "";
              resolve(data.data);
            } else {
              reject(new Error("获取IP信息失败"));
            }
          })
          .catch(error => {
            console.error("获取IP信息请求失败:", error);
            reject(error);
          });
      });
    },
    
    // 请求历史消息
    requestHistoryMessages() {
      try {
        if (!this.websocket || this.websocket.readyState !== WebSocket.OPEN) {
          console.warn("WebSocket未连接，无法请求历史消息");
          return;
        }
        
        // console.log("请求聊天室历史消息...");
        // this.websocket.send(JSON.stringify({
        //   type: "99",
        //   data: { limit: 20 } // 请求最新的20条消息
        // }));
      } catch (error) {
        console.error("请求历史消息时出错:", error);
      }
    },
    
    // 处理历史消息
    handleHistoryMessages(messages) {
      try {
        // 完成加载，更新状态
        this.isLoading = false;
        if (messages) {
          this.ipAddress = messages.ipAddress || this.ipAddress;
          this.ipSource = messages.ipSource || this.ipSource;
        }
        messages = messages && messages.chatRecordList
        
        if (!messages || !Array.isArray(messages) || messages.length === 0) {
          console.log("没有历史消息可显示");
          return;
        }
        
        console.log("原始历史消息数据:", JSON.stringify(messages));
        
        // 清空现有消息，避免重复
        this.chatRecordList = [];
        this.playingVoiceMap = {};
        this.voiceDurationMap = {};
        
        // 添加到聊天记录列表
        messages.forEach(msg => {
          try {
            // 排除无效的消息
            if (!msg) return;
            
            // 尝试将字符串解析为JSON
            if (typeof msg === 'string') {
              try {
                msg = JSON.parse(msg);
              } catch (e) {
                // 如果无法解析为JSON，跳过
                console.warn("无法解析的消息字符串:", msg);
                return;
              }
            }
            
            // 处理嵌套在data中的消息
            const actualMsg = msg.data && typeof msg.data === 'object' ? msg.data : msg;
            
            if (actualMsg.type === 3 || actualMsg.type === 5) {
              this.chatRecordList.push(actualMsg);
            }
          } catch (error) {
            console.error("处理单条历史消息时出错:", error);
          }
        });
        
        console.log(`已加载${this.chatRecordList.length}条历史消息`);
        
        // 缓存消息
        this.cacheMessages();
        
        // 使用nextTick确保DOM更新后再滚动到底部
        this.$nextTick(() => {
          this.scrollToBottom();
        });
      } catch (error) {
        console.error("处理历史消息时出错:", error);
      }
    },
    
    // 从localStorage加载缓存的消息
    loadCachedMessages() {
      try {
        const cachedMessages = localStorage.getItem(this.getCacheKey()) || localStorage.getItem('chatRecordList');
        if (cachedMessages) {
          const messages = JSON.parse(cachedMessages);
          if (Array.isArray(messages) && messages.length > 0) {
            this.chatRecordList = messages;
            console.log(`从缓存加载了${messages.length}条消息`);
            this.$nextTick(() => {
              this.scrollToBottom();
            });
          }
        }
      } catch (error) {
        console.error("从缓存加载消息失败:", error);
      }
    },
    
    // 缓存消息到localStorage
    cacheMessages() {
      try {
        if (this.chatRecordList && this.chatRecordList.length > 0) {
          // 只保存最新的20条消息
          const messagesToCache = this.chatRecordList.slice(-20);
          localStorage.setItem(this.getCacheKey(), JSON.stringify(messagesToCache));
        }
      } catch (error) {
        console.error("缓存消息失败:", error);
      }
    },
    
    // 处理消息撤回
    handleMessageRevocation(messageId) {
      const index = this.chatRecordList.findIndex(msg => msg.id === messageId);
      if (index !== -1) {
        // 从列表中移除消息
        this.chatRecordList.splice(index, 1);
        console.log(`消息#${messageId}已被撤回`);
        
        // 更新缓存
        this.cacheMessages();
      }
    },
    
  },
  computed: {
    isSelf() {
      return function(item) {
        // 多重防御，确保item和ipAddress有效
        if (!item) return false;
        if (!item.ipAddress) return false;
        if (!this.ipAddress) return false;
        
        try {
          return (
            item.ipAddress === this.ipAddress ||
            (item.userId != null && item.userId === this.userId)
          );
        } catch (error) {
          console.error("isSelf计算出错:", error);
          return false;
        }
      };
    },
    isleft() {
      return function(item) {
        // 防止在isSelf调用时出现问题
        try {
          return this.isSelf(item)
            ? "user-avatar right-avatar"
            : "user-avatar left-avatar";
        } catch (error) {
          console.error("isleft计算出错:", error);
          return "user-avatar left-avatar"; // 默认显示在左边
        }
      };
    },
    isMyMessage() {
      return function(item) {
        // 防止在isSelf调用时出现问题
        try {
          return this.isSelf(item) ? "my-message" : "user-message";
        } catch (error) {
          console.error("isMyMessage计算出错:", error);
          return "user-message"; // 默认显示用户消息样式
        }
      };
    },
    blogInfo() {
      return this.$store.state.blogInfo;
    },
    avatar() {
      return (
        this.$store.state.avatar || getPersistentPokemonAvatar("visitorAvatar")
      );
    },
    userId() {
      return this.$store.state.userId;
    },
    unreadText() {
      return this.unreadCount > 99 ? "99+" : this.unreadCount;
    },
    isSendDisabled() {
      return this.content.trim() === "" || !this.isConnected || this.isConnecting;
    },
    isInput() {
      if (this.isSendDisabled) {
        return "iconfont iconzhifeiji disabled-submit";
      }
      return this.content.trim() != ""
        ? "iconfont iconzhifeiji submit-btn"
        : "iconfont iconzhifeiji";
    },
    // 添加一个新的计算属性来决定消息内容样式
    getContentClass() {
      return function(item) {
        return this.isSelf(item) ? "my-content" : "user-content";
      };
    }
  },
  mounted() {
    // 在DOM挂载后，尝试从localStorage恢复消息
    if (this.chatRecordList.length === 0) {
      this.loadCachedMessages();
    }
  }
};
</script>

<style scoped>
@media (min-width: 760px) {
  .chat-container {
    position: fixed;
    color: #4c4948 !important;
    bottom: 104px;
    right: 20px;
    height: calc(85% - 64px - 20px);
    max-height: 590px !important;
    min-height: 250px !important;
    width: 400px !important;
    border-radius: 16px !important;
  }
  .close {
    display: block;
    margin-left: auto;
  }
}
@media (max-width: 760px) {
  .chat-container {
    position: fixed;
    top: 0;
    bottom: 0;
    right: 0;
    left: 0;
  }
  .close {
    display: block;
    margin-left: auto;
  }
}
.chat-container {
  box-shadow: 0 5px 40px rgba(0, 0, 0, 0.16) !important;
  font-size: 14px;
  background: #f4f6fb;
  z-index: 1200;
}
.chat-btn {
  border-radius: 100px !important;
  position: fixed;
  bottom: 15px;
  right: 5px;
  cursor: pointer;
  height: 60px !important;
  width: 60px !important;
  z-index: 1000 !important;
  user-select: none;
}
.header {
  display: flex;
  align-items: center;
  padding: 20px 24px;
  background: #ffffff;
  border-radius: 1rem 1rem 0 0;
  box-shadow: 0 10px 15px -16px rgba(50, 50, 93, 0.08),
    0 4px 6px -8px rgba(50, 50, 93, 0.04);
}
.header-title {
  margin-left: 12px;
  flex: none;
}
.chat-profile {
  flex: 1;
  margin-left: 16px;
  min-width: 0;
}
.chat-name {
  display: flex;
  align-items: center;
  min-width: 0;
}
.nickname-text {
  color: #009d92;
  display: inline-block;
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  vertical-align: bottom;
  white-space: nowrap;
}
.refresh-name {
  cursor: pointer;
  flex: none;
  margin-left: 4px;
  color: #00a1d6;
}
.refresh-count {
  margin-top: 2px;
}
.remain-count {
  color: red;
}
.footer {
  padding: 8px 16px;
  position: absolute;
  width: 100%;
  bottom: 0;
  background: #f7f7f7;
  border-radius: 0 0 1rem 1rem;
  display: flex;
  align-items: center;
}
.footer textarea {
  background: #fff;
  padding-left: 10px;
  padding-top: 8px;
  width: 100%;
  height: 32px;
  outline: none;
  resize: none;
  overflow: hidden;
  font-size: 13px;
}
.voice-btn {
  font-size: 13px;
  outline: none;
  height: 32px;
  width: 100%;
  background: #fff;
  border-radius: 2px;
}
.voice-btn:disabled {
  color: #999;
  background: #f2f2f2;
  cursor: not-allowed;
}
.message {
  position: absolute;
  width: 100%;
  padding: 20px 16px 0 16px;
  top: 80px;
  bottom: 50px;
  overflow-y: auto;
  overflow-x: hidden;
}
.text {
  color: #999;
  text-align: center;
  font-size: 12px;
  margin-bottom: 12px;
}
.user-message {
  display: flex;
  margin-bottom: 10px;
}
.my-message {
  display: flex;
  margin-bottom: 10px;
  justify-content: flex-end;
}
.left-avatar {
  margin-right: 10px;
}
.right-avatar {
  order: 1;
  margin-left: 10px;
}
.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
}
.nickname {
  display: flex;
  align-items: center;
  font-size: 12px;
  margin-top: 3px;
  margin-bottom: 5px;
}
.user-content {
  position: relative;
  background-color: #fff;
  border: 1px solid rgba(216, 226, 235, 0.8);
  padding: 10px;
  border-radius: 5px 20px 20px 20px;
  box-shadow: 0 6px 18px rgba(47, 69, 88, 0.06);
  width: fit-content;
  max-width: 260px;
  white-space: pre-line;
  word-wrap: break-word;
  word-break: break-all;
}
.my-content {
  position: relative;
  border-radius: 20px 5px 20px 20px;
  padding: 12px;
  background: #e8f3ff;
  border: 1px solid #cde4fa;
  color: #2f4558;
  box-shadow: 0 6px 18px rgba(58, 118, 163, 0.12);
  max-width: 260px;
  white-space: pre-line;
  word-wrap: break-word;
  word-break: break-all;
}
.text-content {
  display: inline-flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 2px;
  line-height: 1.6;
  white-space: pre-wrap;
}
.emoji-only-content {
  gap: 6px;
  line-height: 1;
}
.chat-emoji-wrap {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  vertical-align: middle;
}
.chat-emoji {
  display: block;
  width: 24px;
  height: 24px;
  border-radius: 6px;
  mix-blend-mode: multiply;
  object-fit: cover;
}
.emoji-only-content .chat-emoji {
  width: 36px;
  height: 36px;
  border-radius: 8px;
}
.emoji-only-content .chat-emoji-wrap {
  margin: -2px 0;
}
.voice-message {
  display: inline-flex;
  align-items: center;
}
.submit-btn {
  color: rgb(31, 147, 255);
  cursor: pointer;
}
.disabled-submit {
  color: #bdbdbd;
  cursor: not-allowed;
}
.emoji {
  cursor: pointer;
  font-size: 1.3rem;
  margin: 0 8px;
}
.emoji-box {
  position: absolute;
  box-shadow: 0 8px 16px rgba(50, 50, 93, 0.08), 0 4px 12px rgba(0, 0, 0, 0.07);
  background: #fff;
  border-radius: 8px;
  right: 20px;
  bottom: 52px;
  height: 180px;
  width: 300px;
  overflow-y: auto;
  padding: 6px 16px;
}
.emoji-border:before {
  display: block;
  height: 0;
  width: 0;
  content: "";
  border-left: 14px solid transparent;
  border-right: 14px solid transparent;
  border-top: 12px solid #fff;
  bottom: 40px;
  position: absolute;
  right: 43px;
}
.unread {
  text-align: center;
  border-radius: 50%;
  font-size: 14px;
  height: 20px;
  width: 20px;
  position: absolute;
  background: #f24f2d;
  color: #fff;
}
.back-menu {
  font-size: 13px;
  border-radius: 4px;
  position: absolute;
  background: rgba(255, 255, 255, 0.95);
  color: #000;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  width: 80px;
  display: none;
  overflow: hidden;
}
.menu-item {
  padding: 8px 12px;
  cursor: pointer;
  transition: background-color 0.2s;
}
.menu-item:hover {
  background-color: #f0f0f0;
}
.voice {
  position: fixed;
  z-index: 1500;
  bottom: 52px;
  left: 0;
  right: 0;
  top: 80px;
  background: rgba(0, 0, 0, 0.8);
}
.close-voice {
  position: absolute;
  bottom: 60px;
  left: 30px;
  display: inline-block;
  height: 50px;
  width: 50px;
  line-height: 50px;
  border-radius: 50%;
  text-align: center;
  background: #fff;
}
/* 新增样式 */
.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 15px 0;
  color: #666;
  font-size: 14px;
}

.connection-status {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 10px;
  background: rgba(255, 152, 0, 0.1);
  border-radius: 4px;
  margin: 10px 0;
  color: #f57c00;
  font-size: 13px;
}

.empty-message {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 200px;
  color: #9e9e9e;
  text-align: center;
}

.empty-message p {
  margin-top: 16px;
  font-size: 14px;
}
@media (max-width: 760px) {
  .header {
    padding: 12px 14px;
    min-height: 72px;
  }
  .header-title {
    margin-left: 8px;
    flex: none;
  }
  .chat-profile {
    margin-left: 10px;
    font-size: 12px;
    line-height: 1.5;
  }
  .chat-name {
    max-width: 100%;
  }
  .nickname-text {
    max-width: 96px;
  }
  .refresh-name {
    margin-left: 2px;
  }
  .refresh-count {
    white-space: nowrap;
  }
  .message {
    top: 72px;
  }
  .user-content,
  .my-content {
    max-width: calc(100vw - 96px);
  }
  .emoji-box {
    left: 16px;
    right: 16px;
    width: auto;
  }
}
</style>
