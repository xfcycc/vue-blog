<template>
  <v-dialog v-model="phoneFlag" :fullscreen="isMobile" max-width="460">
    <v-card class="login-container" style="border-radius:4px">
      <v-icon class="float-right" @click="phoneFlag = false">
        mdi-close
      </v-icon>
      <div class="login-wrapper">
        <!-- 用户名 -->
        <v-text-field
          v-model="username"
          label="手机号"
          placeholder="请输入您的手机号"
          clearable
          @keyup.enter="phone"
        />
        <!-- 验证码 -->
        <div class="mt-7 send-wrapper">
          <v-text-field
            maxlength="6"
            v-model="code"
            label="验证码"
            placeholder="请输入6位验证码"
            @keyup.enter="phone"
          />
          <v-btn text small :disabled="flag" @click="sendSms">
            {{ codeMsg }}
          </v-btn>
        </div>
        <div style="color: #009d92">未注册的手机号将直接登录</div>
        <!-- 按钮 -->
        <v-btn class="mt-7" block color="red" style="color:#fff" @click="phone">
          登录
        </v-btn>
        <!-- 登录 -->
        <div class="mt-10 login-tip">
          <span @click="switchLogin">账号密码登录</span>
        </div>
      </div>
    </v-card>
  </v-dialog>
</template>

<script>
export default {
  data: function() {
    return {
      username: "",
      code: "",
      flag: true,
      codeMsg: "发送",
      time: 60,
      show: false
    };
  },
  methods: {
    switchLogin() {
      this.$store.state.registerFlag = false;
      this.$store.state.loginFlag = true;
      this.$store.state.phoneFlag = false;
    },
    sendSms() {
      const that = this;
      //发送短信
      that.countDown();
      that.axios
        .post("/api/sms/send", {}, { params: { phone: that.username } })
        .then(({ data }) => {
          if (data.flag) {
            that.$toast({ type: "success", message: "发送成功" });
          } else {
            that.$toast({ type: "error", message: data.message });
          }
        });
      // 显示验证码
      // captcha.show();
    },
    countDown() {
      this.flag = true;
      this.timer = setInterval(() => {
        this.time--;
        this.codeMsg = this.time + "s";
        if (this.time <= 0) {
          clearInterval(this.timer);
          this.codeMsg = "发送";
          this.time = 60;
          this.flag = false;
        }
      }, 1000);
    },
    phone() {
      var reg = /^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\d{8}$/;
      if (!reg.test(this.username)) {
        this.$toast({ type: "error", message: "手机号格式不正确" });
        return false;
      }
      if (this.code.trim().length !== 4) {
        this.$toast({ type: "error", message: "请输入4位验证码" });
        return false;
      }
      const that = this;

      //发送登录请求
      let param = new URLSearchParams();
      param.append("phone", that.username);
      param.append("code", that.code);
      that.axios.post("/api/phoneLogin", param).then(({ data }) => {
        if (data.flag) {
          that.username = "";
          that.code = "";
          that.$store.commit("login", data.data);
          that.$store.commit("closeModel");
          that.$toast({ type: "success", message: "登录成功" });
        } else {
          that.$toast({ type: "error", message: data.message });
        }
      });
    }
  },
  computed: {
    phoneFlag: {
      set(value) {
        this.$store.state.phoneFlag = value;
      },
      get() {
        return this.$store.state.phoneFlag;
      }
    },
    isMobile() {
      const clientWidth = document.documentElement.clientWidth;
      if (clientWidth > 960) {
        return false;
      }
      return true;
    }
  },
  watch: {
    username(value) {
      var reg = /^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\d{8}$/;
      this.flag = !reg.test(value);
    }
  }
};
</script>

<style scoped>
.social-login-title {
  margin-top: 1.5rem;
  color: #b5b5b5;
  font-size: 0.75rem;
  text-align: center;
}

.social-login-title::before {
  content: "";
  display: inline-block;
  background-color: #d8d8d8;
  width: 60px;
  height: 1px;
  margin: 0 12px;
  vertical-align: middle;
}

.social-login-title::after {
  content: "";
  display: inline-block;
  background-color: #d8d8d8;
  width: 60px;
  height: 1px;
  margin: 0 12px;
  vertical-align: middle;
}

.social-login-wrapper {
  margin-top: 1rem;
  font-size: 2rem;
  text-align: center;
}

.social-login-wrapper a {
  text-decoration: none;
}
</style>
