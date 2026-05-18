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
        <div style="margin-left:12px">
          <div>聊天室</div>
          <div style="font-size:12px">当前{{ count }}人在线</div>
        </div>
        <div style="margin: auto">
          <div style="float: left">
            你的名称: <s style="color: #009d92">{{ nickname }}</s>
          </div>
          <span
            style="cursor: pointer;float:right; padding-left:10px;color: #00a1d6"
            @click="refreshName"
            >刷新匿名</span
          >
          <div>
            今日可刷新次数：<s style="color: red">{{ remainCount }}</s>
          </div>
        </div>
        <v-icon class="close" @click="isShow = false">
          mdi-close
        </v-icon>
      </div>
      <!-- 对话内容 -->
      <div class="message" id="message">
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
          <img :src="item.avatar" :class="isleft(item)" />
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
              <div v-if="item.type == 3" v-html="item.content" />
              <!-- 语音消息 -->
              <div v-if="item.type == 5" @click.prevent.stop="playVoice(item)">
                <audio
                  @ended="endVoice(item)"
                  @canplay="getVoiceTime(item)"
                  ref="voices"
                  :src="item.content"
                  style="display:none"
                />
                <!-- 播放 -->
                <v-icon
                  :color="isSelf(item) ? '#fff' : '#000'"
                  ref="plays"
                  style="display:inline-flex;cursor: pointer;"
                >
                  mdi-arrow-right-drop-circle
                </v-icon>
                <!-- 暂停 -->
                <v-icon
                  :color="isSelf(item) ? '#fff' : '#000'"
                  ref="pauses"
                  style="display:none;cursor: pointer;"
                >
                  mdi-pause-circle
                </v-icon>
                <!-- 音频时长 -->
                <span ref="voiceTimes" />
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
          @keydown.enter="saveMessage($event)"
          placeholder="请输入内容"
        />
        <!-- 语音输入 -->
        <button
          class="voice-btn"
          v-show="isVoice"
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
        <i :class="isInput" @click="saveMessage" style="font-size: 1.5rem" />
      </div>
    </div>
    <!-- 未读数量 -->
    <div class="chat-btn" @click="open">
      <span class="unread" v-if="unreadCount > 0">{{ unreadCount }}</span>
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
export default {
  components: {
    Emoji
  },
  updated() {
    // 优化滚动逻辑，确保消息容器存在
    this.$nextTick(() => {
      const messageContainer = document.getElementById("message");
      if (messageContainer) {
        messageContainer.scrollTop = messageContainer.scrollHeight;
      }
    });
  },
  created() {
    // 创建全局错误处理器
    this.errorHandler = (error) => {
      console.error("聊天室捕获到未处理的错误:", error);
      // 防止错误向上传播导致整个应用崩溃
      event.preventDefault();
    };
    
    // 添加全局错误处理
    window.addEventListener("error", this.errorHandler);
    
    // 获取初始数据
    this.getNickname().catch(error => {
      console.warn("初始化时获取昵称失败:", error);
    });
    
    // 获取WebSocket URL
    this.getWebsocketUrl().then(() => {
      // 初始化WebSocket连接
      this.connect();
      // WebSocket连接后会自动请求历史消息，不需要额外的HTTP请求
    }).catch(error => {
      console.error("获取WebSocket URL失败:", error);
      this.$toast({ type: "error", message: "聊天室连接失败，请稍后再试" });
      
      // 如果WebSocket URL获取失败，尝试从本地缓存加载消息
      this.loadCachedMessages();
      
      // 设置重试计时器
      this.setupReconnectTimer();
    });
    
  },
  beforeDestroy() {
    // 清理错误处理器
    if (this.errorHandler) {
      window.removeEventListener("error", this.errorHandler);
    }
    
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
      voiceList: [],
      rc: null,
      ipAddress: "",
      ipSource: "",
      count: 0,
      unreadCount: 0,
      isVoice: false,
      voiceActive: false,
      startVoiceTime: null,
      WebsocketMessage: {
        type: null,
        data: null
      },
      heartBeat: null,
      currentUser: null,
      isLoading: false, // 新增：加载状态
      historyLoaded: false, // 新增：历史消息已加载标志
      isConnected: false, // 新增：连接状态
      reconnectTimer: null, // 新增：重连定时器
      reconnectAttempts: 0, // 新增：重连尝试次数
    };
  },
  methods: {
    // 设置重连定时器
    setupReconnectTimer() {
      // 清除可能存在的旧定时器
      if (this.reconnectTimer) {
        clearInterval(this.reconnectTimer);
      }
      
      // 设置新的重连定时器，每5秒尝试一次，最多尝试12次（1分钟）
      this.reconnectTimer = setInterval(() => {
        if (this.isConnected) {
          // 如果已连接，清除定时器
          clearInterval(this.reconnectTimer);
          this.reconnectTimer = null;
          this.reconnectAttempts = 0;
          return;
        }
        
        // 增加重连尝试次数
        this.reconnectAttempts++;
        
        if (this.reconnectAttempts > 12) {
          // 如果尝试次数超过限制，停止重连
          clearInterval(this.reconnectTimer);
          this.reconnectTimer = null;
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
    open() {
      try {
        // 打开聊天室前进行连接检查
        if (!this.websocket || this.websocket.readyState !== WebSocket.OPEN) {
          console.log("聊天室连接尚未就绪，重新建立连接");
          // 如果没有URL，先获取URL
          if (!this.websocketUrl) {
            this.getWebsocketUrl().then(() => {
              this.connect();
              // WebSocket会自动请求历史消息
            }).catch(error => {
              console.error("重新获取WebSocket URL失败:", error);
              this.$toast({ type: "error", message: "聊天室连接失败，请刷新页面重试" });
            });
          } else {
            this.connect();
            // WebSocket会自动请求历史消息
          }
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
        // 设置加载状态
        this.isLoading = true;
        
        // 检查是否配置了websocket URL
        if (!this.websocketUrl) {
          console.error("没有配置聊天室的WebSocket URL");
          this.isLoading = false;
          return;
        }
        
        // 关闭已有连接
        if (this.websocket) {
          try {
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
        this.isConnected = false;
      }
    },
    onOpen() {
      console.log("聊天室WebSocket连接已建立");
      this.isConnected = true;
      this.reconnectAttempts = 0;
      
      // 连接建立后，立即请求历史消息
      this.requestHistoryMessages();
      
      // 设置心跳
      if (!this.heartBeat) {
        this.heartBeat = setInterval(() => {
          if (this.websocket && this.websocket.readyState === WebSocket.OPEN) {
            // 发送心跳消息
            this.websocket.send(JSON.stringify({ type: "6" }));
          }
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
          this.chatRecordList.push(data.data);
          
          // 缓存更新后的消息
          this.cacheMessages();
          
          if (!this.isShow) {
            this.unreadCount++;
          } else {
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
      this.isLoading = false;
      
      // 如果不是主动关闭，则尝试重连
      if (this.isShow) {
        this.setupReconnectTimer();
      }
    },
    onError(error) {
      console.error("聊天室WebSocket连接错误:", error);
      this.isConnected = false;
      this.isLoading = false;
      
      // 尝试重连
      this.setupReconnectTimer();
    },
    saveMessage(e) {
      if (e) {
        e.preventDefault();
      }
      if (this.content.trim() == "") {
        this.$toast({ type: "error", message: "内容不能为空" });
        return false;
      }
      //解析表情
      var reg = /\[.+?\]/g;
      let processedContent = this.content.replace(reg, function(str) {
        return (
          "<img src= '" +
          EmojiList[str] +
          "' width='24'height='24' style='margin: 0 1px;vertical-align: text-bottom'/>"
        );
      });
      var socketMsg = {
        nickname: this.nickname,
        avatar: this.avatar,
        content: processedContent,
        userId: this.userId,
        type: 3,
        ipAddress: this.ipAddress,
        ipSource: this.ipSource
      };
      this.WebsocketMessage.type = 3;
      this.WebsocketMessage.data = socketMsg;
      
      this.websocket.send(JSON.stringify(this.WebsocketMessage));
      this.content = "";
    },
    addEmoji(key) {
      this.isEmoji = false;
      this.$refs.chatInput.focus();
      this.content += key;
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
      this.WebsocketMessage.type = 4;
      this.WebsocketMessage.data = socketMsg;
      this.websocket.send(JSON.stringify(this.WebsocketMessage));
      
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
      this.voiceActive = true;
      let that = this;
      that.rc = new Recorderx();
      that.$nextTick(() => {
        that.rc
          .start()
          .then(() => {
            that.startVoiceTime = new Date();
            console.log("start recording");
          })
          .catch(error => {
            console.log("Recording failed.", error);
          });
      });
    },
    // 录音结束
    translationEnd() {
      console.log("结束");
      this.voiceActive = false;
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
      formData.append("nickname", this.nickname);
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
      this.axios(options);
    },
    translationmove() {},
    // 播放语音
    playVoice(item) {
      var player = this.$refs.voices[this.voiceList.indexOf(item.id)];
      if (player.paused) {
        player.play();
        this.$refs.plays[this.voiceList.indexOf(item.id)].$el.style.display =
          "none";
        this.$refs.pauses[this.voiceList.indexOf(item.id)].$el.style.display =
          "inline-flex";
      } else {
        this.$refs.plays[this.voiceList.indexOf(item.id)].$el.style.display =
          "inline-flex";
        this.$refs.pauses[this.voiceList.indexOf(item.id)].$el.style.display =
          "none";
        player.pause();
      }
    },
    // 语音结束
    endVoice(item) {
      this.$refs.plays[this.voiceList.indexOf(item.id)].$el.style.display =
        "inline-flex";
      this.$refs.pauses[this.voiceList.indexOf(item.id)].$el.style.display =
        "none";
    },
    // 获取语音时长
    getVoiceTime(item) {
      var time = this.$refs.voices[this.voiceList.indexOf(item.id)].duration;
      time = Math.ceil(time);
      var str = "⬝⬝⬝";
      for (var i = 0; i < time; i++) {
        if (i % 2 == 0) {
          str += "⬝";
        }
      }
      this.$refs.voiceTimes[this.voiceList.indexOf(item.id)].innerHTML =
        " " + str + " " + time + " ''";
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
            this.$store.state.nickname = data.data.nickname;
            this.$store.state.refreshCount = data.data.count;
            this.$store.state.remainCount = data.data.remainCount;
          } else if (data.code === 203) {
            alert(data.message);
          }
        });
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
    scrollToBottom() {
      this.$nextTick(() => {
        const messageContainer = document.getElementById("message");
        if (messageContainer) {
          messageContainer.scrollTop = messageContainer.scrollHeight;
        }
      });
    },
    // 获取用户昵称方法
    getNickname() {
      return new Promise((resolve, reject) => {
        axios.post("/api/chatroom/nickname")
          .then(({ data }) => {
            if (data.code === 20000) {
              this.$store.state.nickname = data.data.nickname;
              this.$store.state.refreshCount = data.data.count;
              this.$store.state.remainCount = data.data.remainCount;
              resolve(data.data);
            } else {
              this.$store.state.nickname = "匿名用户";
              this.$store.state.refreshCount = 0;
              reject(new Error("获取昵称失败"));
            }
          })
          .catch(error => {
            console.error("获取昵称请求失败:", error);
            this.$store.state.nickname = "匿名用户";
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
        messages = messages.chatRecordList
        
        if (!messages || !Array.isArray(messages) || messages.length === 0) {
          console.log("没有历史消息可显示");
          return;
        }
        
        console.log("原始历史消息数据:", JSON.stringify(messages));
        
        // 清空现有消息，避免重复
        this.chatRecordList = [];
        this.voiceList = []; // 重置语音列表
        
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
              
              // 记录语音消息ID
              if (actualMsg.type === 5 && actualMsg.id) {
                this.voiceList.push(actualMsg.id);
              }
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
        const cachedMessages = localStorage.getItem('chatRecordList');
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
          localStorage.setItem('chatRecordList', JSON.stringify(messagesToCache));
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
    nickname() {
      return this.$store.state.nickname != null
        ? this.$store.state.nickname
        : this.ipAddress;
    },
    refreshCount() {
      return this.$store.state.refreshCount;
    },
    remainCount() {
      return this.$store.state.remainCount;
    },
    avatar() {
      return this.$store.state.avatar != null
        ? this.$store.state.avatar
        : this.$store.state.blogInfo.websiteConfig.touristAvatar;
    },
    userId() {
      return this.$store.state.userId;
    },
    isInput() {
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
    display: none;
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
  padding: 10px;
  border-radius: 5px 20px 20px 20px;
  width: fit-content;
  white-space: pre-line;
  word-wrap: break-word;
  word-break: break-all;
}
.my-content {
  position: relative;
  border-radius: 20px 5px 20px 20px;
  padding: 12px;
  background: #12b7f5;
  color: #fff;
  white-space: pre-line;
  word-wrap: break-word;
  word-break: break-all;
}
.submit-btn {
  color: rgb(31, 147, 255);
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
</style>
