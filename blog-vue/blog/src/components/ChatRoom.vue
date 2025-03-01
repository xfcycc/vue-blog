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
              <!-- AI消息 -->
              <div
                v-if="item && item.type == 7"
                class="ai-message"
                v-html="item.content || ''"
              />
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
    
    // 尝试处理用户提供的样本数据
    setTimeout(() => {
      this.tryProcessSampleData();
    }, 1000);
    
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
    
    // 注册全局测试数据加载方法
    window.loadChatHistory = (jsonData) => {
      this.loadTestHistoryData(jsonData);
    };
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
      mentionedUsers: [],
      aiEnabled: true,
      aiName: "小芸",
      currentUser: null,
      isLoading: false, // 新增：加载状态
      historyLoaded: false, // 新增：历史消息已加载标志
      manualHistoryLoaded: false, // 新增：手动加载历史标志
      isConnected: false, // 新增：连接状态
      reconnectTimer: null, // 新增：重连定时器
      reconnectAttempts: 0, // 新增：重连尝试次数
    };
  },
  methods: {
    // 尝试处理用户提供的样本数据
    tryProcessSampleData() {
      try {
        // 样本数据（从用户查询中提取）
        const sampleData = {
          "data": {
            "chatRecordList": [
              {
                "avatar": "https://pic.blog.caiguoyu.cn/config/90c2ccb54589157125986df9c4a2a90b.jpeg",
                "content": "123",
                "createTime": "2025-03-01T21:08:27",
                "id": 3161,
                "ipAddress": "183.206.160.84",
                "ipSource": "江苏省南京市 移动",
                "nickname": "刘瑛姑",
                "type": 3
              },
              {
                "avatar": "https://pic.blog.caiguoyu.cn/config/ai-avatar.png",
                "content": "\n\n你好！看起来你可能在测试或误触了。有什么需要帮忙的吗？无论是问题、聊天，还是其他需求，我都在这里为你解答！😊",
                "createTime": "2025-03-01T21:08:34",
                "id": 3162,
                "ipAddress": "127.0.0.1",
                "ipSource": "火山引擎",
                "nickname": "小芸",
                "type": 7
              }
            ]
          }
        };
        
        console.log("尝试加载用户提供的样本数据");
        this.loadTestHistoryData(sampleData);
        
        // // 直接打开聊天窗口以便观察结果
        // setTimeout(() => {
        //   this.isShow = true;
        // }, 500);
      } catch (error) {
        console.error("处理样本数据时出错:", error);
      }
    },
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
          
          // 检查AI消息是否正确渲染
          this.checkAiMessages();
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
        } else if (data.type === 7) { // AI消息
          let aiMessage = data.data;
          console.log("收到AI消息:", aiMessage);
          if (aiMessage) {
            this.chatRecordList.push(aiMessage);
            this.$nextTick(() => {
              this.scrollToBottom();
              
              // 缓存更新后的消息
              this.cacheMessages();
            });
            
            if (!this.isShow) {
              this.unreadCount++;
            }
          }
        } else if (data.type === 4) { // 撤回消息
          if (data.data && data.data.id) {
            this.handleMessageRevocation(data.data.id);
          }
        } else if (data.type === 6) { // 心跳消息
          console.log("收到心跳消息");
        } else { // 普通消息
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
      
      // 提取@用户
      /* eslint-disable-next-line no-unused-vars */
      const mentionedIpAddresses = this.extractMentions();
      
      // 判断是否是对AI的消息
      if (this.isMessageToAI(this.content)) {
        // 创建AI消息格式
        var aiMsg = {
          nickname: this.nickname,
          avatar: this.avatar,
          content: processedContent,
          userId: this.userId,
          ipAddress: this.ipAddress,
          ipSource: this.ipSource,
          mentionedIpAddresses: mentionedIpAddresses
        };
        this.WebsocketMessage.type = 7; // AI消息类型
        this.WebsocketMessage.data = aiMsg;
      } else {
        // 常规消息
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
      }
      
      this.websocket.send(JSON.stringify(this.WebsocketMessage));
      this.content = "";
      this.mentionedUsers = []; // 清空@用户列表
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
    // 添加新方法：判断是否是对AI的消息
    isMessageToAI(content) {
      // 开头@小芸
      if (content && content.trim().startsWith("@" + this.aiName)) {
        return true;
      }
      // 内容中包含@小芸
      if (content && content.includes("@" + this.aiName)) {
        return true;
      }
      // AI功能已启用并且没有@其他用户，则认为是与AI对话
      if (this.aiEnabled && this.mentionedUsers && this.mentionedUsers.length === 0) {
        return true;
      }
      return false;
    },
    // 修正提取@的用户方法，移除未使用的content参数的引用
    extractMentions() {
      const mentionedAddresses = [];
      
      // 检查mentionedUsers是否存在
      if (!this.mentionedUsers || !Array.isArray(this.mentionedUsers)) {
        return mentionedAddresses;
      }
      
      // 遍历@用户列表
      this.mentionedUsers.forEach(user => {
        // 确保user对象有效
        if (user && user.nickname && user.ipAddress && user.nickname !== this.aiName) {
          mentionedAddresses.push(user.ipAddress);
        }
      });
      
      return mentionedAddresses;
    },
    // 添加新方法：@用户
    mentionUser(user) {
      // 检查用户是否已经在@列表中
      const isAlreadyMentioned = this.mentionedUsers.some(
        mentioned => mentioned.ipAddress === user.ipAddress
      );
      
      if (!isAlreadyMentioned) {
        this.mentionedUsers.push({
          nickname: user.nickname,
          ipAddress: user.ipAddress
        });
        
        // 在输入框中添加@用户
        this.content += " @" + user.nickname + " ";
        this.$refs.chatInput.focus();
      }
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
    
    // 添加一个强制重新渲染AI消息的方法
    forceRenderAiMessages() {
      try {
        // 先找出所有AI消息
        const aiMessages = this.chatRecordList.filter(msg => msg && (msg.type === 7 || (msg.nickname === "小芸" && msg.ipAddress === "127.0.0.1")));
        if (aiMessages.length === 0) {
          console.log("没有AI消息需要重新渲染");
          return;
        }
        
        console.log(`开始强制重新渲染${aiMessages.length}条AI消息...`);
        
        // 临时存储所有非AI消息
        const nonAiMessages = this.chatRecordList.filter(msg => msg && msg.type !== 7 && !(msg.nickname === "小芸" && msg.ipAddress === "127.0.0.1"));
        
        // 清空消息列表
        this.chatRecordList = [];
        
        // 先添加非AI消息
        nonAiMessages.forEach(msg => {
          this.chatRecordList.push(msg);
        });
        
        // 使用nextTick确保DOM已更新
        this.$nextTick(() => {
          // 然后分批添加AI消息
          let index = 0;
          const addAiMessage = () => {
            if (index < aiMessages.length) {
              const msg = aiMessages[index];
              // 确保消息格式正确
              if (msg.content) {
                this.chatRecordList.push(msg);
              }
              index++;
              
              // 继续添加下一条
              setTimeout(addAiMessage, 50);
            } else {
              console.log("AI消息重新渲染完成");
              // 完成后再次检查
              setTimeout(() => {
                this.checkAiMessages();
                this.scrollToBottom();
              }, 200);
            }
          };
          
          // 开始添加
          addAiMessage();
        });
      } catch (error) {
        console.error("强制重新渲染AI消息时出错:", error);
      }
    },
    
    // 新增方法：检查AI消息状态
    checkAiMessages() {
      try {
        // 获取所有AI消息
        const aiMessages = this.chatRecordList.filter(item => item && (item.type === 7 || (item.nickname === "小芸" && item.ipAddress === "127.0.0.1")));
        console.log(`当前有${aiMessages.length}条AI消息`);
        
        if (aiMessages.length > 0) {
          console.log("最新的AI消息:", aiMessages[aiMessages.length - 1]);
          
          // 检查AI消息DOM元素是否正确渲染
          this.$nextTick(() => {
            const aiMessageElements = document.querySelectorAll('.ai-message');
            console.log(`渲染的AI消息元素数量: ${aiMessageElements.length}`);
            
            // 检查内容是否为空
            let emptyContentFound = false;
            aiMessageElements.forEach(el => {
              if (!el.innerHTML || el.innerHTML.trim() === '') {
                emptyContentFound = true;
              }
            });
            
            if (aiMessages.length !== aiMessageElements.length || emptyContentFound) {
              console.warn("AI消息元素渲染数量与数据不匹配或存在空内容，将尝试重新渲染");
              this.forceRenderAiMessages();
            }
          });
        }
      } catch (error) {
        console.error("检查AI消息时出错:", error);
      }
    },
    updateAiMessages(message) {
      try {
        if (!message) {
          console.warn("收到空的AI消息，忽略处理");
          return;
        }
        
        // 解析消息
        const aiMessage = JSON.parse(message);
        if (!aiMessage || !aiMessage.content) {
          console.warn("AI消息格式不正确:", aiMessage);
          return;
        }
        
        console.log("收到并处理AI消息:", aiMessage);
        
        // 添加到消息列表
        this.chatRecordList.push({
          ...aiMessage,
          type: 7, // 确保类型设为AI消息
          createTime: new Date().toISOString(),
          ipAddress: "ai-assistant", // AI消息的特殊标识
          nickname: "小芸助手"
        });
        
        // 使用nextTick确保DOM更新后滚动
        this.$nextTick(() => {
          this.scrollToBottom();
        });
      } catch (error) {
        console.error("处理AI消息时出错:", error);
      }
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
            
            // 对于AI消息进行特殊处理
            if (actualMsg.type === 7 || (actualMsg.nickname === "小芸" && actualMsg.ipAddress === "127.0.0.1")) {
              const processedAiMsg = this.processAiMessage(actualMsg);
              if (processedAiMsg) {
                this.chatRecordList.push(processedAiMsg);
              }
            } 
            // 处理普通消息
            else if (actualMsg.type === 3 || actualMsg.type === 5 || (actualMsg.content && actualMsg.nickname)) {
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
          this.checkAiMessages();
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
    
    // 新增：直接处理JSON格式的聊天历史数据
    processHistoryData(jsonData) {
      try {
        console.log("正在处理JSON历史数据");
        if (typeof jsonData === 'string') {
          try {
            jsonData = JSON.parse(jsonData);
          } catch (e) {
            console.error("JSON解析失败:", e);
            return false;
          }
        }
        
        // 如果是对象但包含在data字段，提取data
        if (jsonData.data && Array.isArray(jsonData.data.chatRecordList)) {
          this.handleHistoryMessages(jsonData.data.chatRecordList);
          return true;
        }
        
        // 如果是对象但直接包含chatRecordList字段
        if (jsonData.chatRecordList && Array.isArray(jsonData.chatRecordList)) {
          this.handleHistoryMessages(jsonData.chatRecordList);
          return true;
        }
        
        // 如果直接是数组
        if (Array.isArray(jsonData)) {
          this.handleHistoryMessages(jsonData);
          return true;
        }
        
        console.warn("无法识别的历史数据格式:", jsonData);
        return false;
      } catch (error) {
        console.error("处理JSON历史数据时出错:", error);
        return false;
      }
    },
    
    // 添加直接处理消息数据的方法
    processDirectMessageData(data) {
      try {
        if (!data) {
          console.warn("提供的消息数据为空");
          return false;
        }
        
        console.log("正在处理直接提供的消息数据:", data);
        
        // 如果提供的是字符串，尝试解析为JSON
        let jsonData = data;
        if (typeof data === 'string') {
          try {
            jsonData = JSON.parse(data);
          } catch (e) {
            console.error("无法解析提供的JSON字符串:", e);
            return false;
          }
        }
        
        // 处理不同格式的数据
        if (jsonData.data && jsonData.data.chatRecordList) {
          // 包装在data.chatRecordList中的格式
          this.handleHistoryMessages(jsonData.data.chatRecordList);
          return true;
        } else if (jsonData.chatRecordList) {
          // 直接包含chatRecordList字段的格式
          this.handleHistoryMessages(jsonData.chatRecordList);
          return true;
        } else if (Array.isArray(jsonData)) {
          // 直接是消息数组的格式
          this.handleHistoryMessages(jsonData);
          return true;
        } else {
          // 如果是单个消息对象
          this.handleHistoryMessages([jsonData]);
          return true;
        }
      } catch (error) {
        console.error("处理直接提供的消息数据时出错:", error);
        return false;
      }
    },
    
    // 强化处理AI消息的方法
    processAiMessage(aiMessage) {
      try {
        if (!aiMessage) return null;
        
        // 如果是字符串，尝试解析
        if (typeof aiMessage === 'string') {
          try {
            aiMessage = JSON.parse(aiMessage);
          } catch (e) {
            // 如果不是JSON，可能就是纯文本内容
            aiMessage = { content: aiMessage };
          }
        }
        
        // 判断消息格式
        let finalMessage = null;
        
        // 如果是标准格式
        if (aiMessage.type === 7 || (aiMessage.data && aiMessage.data.type === 7)) {
          finalMessage = aiMessage.data || aiMessage;
        } 
        // 如果是简化格式
        else if (aiMessage.content) {
          finalMessage = {
            type: 7,
            content: aiMessage.content,
            nickname: aiMessage.nickname || "小芸",
            avatar: aiMessage.avatar || "https://pic.blog.caiguoyu.cn/config/ai-avatar.png",
            createTime: aiMessage.createTime || new Date().toISOString(),
            ipAddress: "ai-system",
            ipSource: "火山引擎"
          };
        }
        
        return finalMessage;
      } catch (error) {
        console.error("处理AI消息时出错:", error);
        return null;
      }
    },
    
    // 测试用：手动加载指定的历史数据
    loadTestHistoryData(jsonData) {
      this.isLoading = true;
      
      setTimeout(() => {
        const success = this.processHistoryData(jsonData);
        if (success) {
          console.log("测试数据加载成功");
          this.manualHistoryLoaded = true;
        } else {
          console.error("测试数据加载失败");
        }
        this.isLoading = false;
        
        // 处理完测试数据后检查AI消息渲染状态
        setTimeout(() => {
          this.checkAiMessages();
        }, 500);
      }, 300);
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
    // 添加一个新的计算属性来检测AI消息
    isAiMessage() {
      return function(item) {
        return item.nickname === this.aiName || item.type === 7;
      };
    },
    // 添加一个新的计算属性来决定消息内容样式
    getContentClass() {
      return function(item) {
        if (item.type === 7 || item.nickname === this.aiName) {
          return "ai-content";
        } else {
          return this.isSelf(item) ? "my-content" : "user-content";
        }
      };
    }
  },
  mounted() {
    // 在DOM挂载后，尝试从localStorage恢复消息
    if (this.chatRecordList.length === 0) {
      this.loadCachedMessages();
    }
    
    // 尝试处理用户可能已经提供的测试数据
    if (window.__chatTestData) {
      console.log("检测到测试数据，尝试加载");
      this.loadTestHistoryData(window.__chatTestData);
    }
    
    // 延迟检查AI消息渲染状态
    setTimeout(() => {
      this.checkAiMessages();
    }, 1000);
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
.ai-content {
  position: relative;
  border-radius: 12px;
  padding: 12px;
  background: #e9f7fe;
  color: #333;
  white-space: pre-line;
  word-wrap: break-word;
  word-break: break-all;
  border-left: 3px solid #00a1d6;
}

.ai-message {
  position: relative;
  font-size: 14px;
  line-height: 1.6;
}

/* 添加小芸助手特殊样式 */
.ai-content::before {
  content: "";
  position: absolute;
  top: 0;
  left: -4px;
  width: 4px;
  height: 100%;
  background: linear-gradient(to bottom, #00a1d6, #00c3ff);
  border-radius: 2px 0 0 2px;
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
