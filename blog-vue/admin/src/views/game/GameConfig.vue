<template>
  <el-card class="main-card game-config-card" v-loading="loading">
    <div class="title">游戏同步配置</div>
    <div class="config-actions">
      <el-alert
        title="Steam 使用已保存配置；PSN 使用一次性授权，同步结束后不会保存令牌或登录信息。"
        type="info"
        :closable="false"
        show-icon
      />
      <el-button
        type="primary"
        size="small"
        icon="el-icon-check"
        :loading="saving"
        @click="saveConfigs"
      >
        保存配置
      </el-button>
    </div>

    <el-tabs v-model="activePlatform" type="border-card">
      <el-tab-pane
        v-for="platform in platformOptions"
        :key="platform.value"
        :label="platform.label"
        :name="platform.value"
      >
        <div class="platform-heading">
          <div>
            <h3>{{ platform.label }} 配置</h3>
            <p>配置项仅用于对应平台的数据同步或账号标识。</p>
          </div>
          <el-button
            type="primary"
            plain
            size="small"
            icon="el-icon-plus"
            @click="addConfig(platform.value)"
          >
            添加配置项
          </el-button>
        </div>

        <div v-if="platform.value === 'PSN'" class="one-time-sync-panel">
          <div>
            <strong>{{ platform.label }} 一次性同步</strong>
            <p>
              登录 PlayStation 后同步 PS4、PS5
              游戏记录和游玩时长，本次授权使用后立即丢弃。
            </p>
          </div>
          <el-button
            type="success"
            size="small"
            icon="el-icon-refresh"
            :loading="psnSyncing"
            @click="openPsnSync"
          >
            登录并同步
          </el-button>
        </div>

        <el-table
          :data="configsByPlatform(platform.value)"
          border
          empty-text="暂无配置项"
        >
          <el-table-column label="配置名称" min-width="160">
            <template slot-scope="scope">
              <el-input
                v-model="scope.row.configName"
                placeholder="例如：账号 ID"
              />
            </template>
          </el-table-column>
          <el-table-column label="配置键" min-width="190">
            <template slot-scope="scope">
              <el-input
                v-model="scope.row.configKey"
                :disabled="isSteamRequired(scope.row)"
                placeholder="例如：ACCOUNT_ID"
              />
            </template>
          </el-table-column>
          <el-table-column label="配置值" min-width="260">
            <template slot-scope="scope">
              <el-input
                v-model="scope.row.configValue"
                :show-password="isSecret(scope.row.configKey)"
                :type="isSecret(scope.row.configKey) ? 'password' : 'text'"
                autocomplete="new-password"
                placeholder="请输入配置值"
              />
            </template>
          </el-table-column>
          <el-table-column label="排序" width="110">
            <template slot-scope="scope">
              <el-input-number
                v-model="scope.row.sortOrder"
                :min="0"
                :controls="false"
                style="width:100%"
              />
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" width="90">
            <template slot-scope="scope">
              <el-button
                type="text"
                class="delete-config"
                :disabled="isSteamRequired(scope.row)"
                @click="removeConfig(scope.row)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <el-dialog
      title="PSN 一次性同步"
      :visible.sync="psnDialogVisible"
      width="560px"
      :close-on-click-modal="false"
      @closed="resetPsnDialog"
    >
      <el-alert
        title="授权码等同于账号密码，仅在本次同步请求中使用，不会写入配置表。"
        type="warning"
        :closable="false"
        show-icon
      />
      <div class="sync-steps">
        <p><b>1.</b> 打开 PlayStation 页面并完成登录。</p>
        <el-button size="small" plain @click="openPsnLogin">
          打开 PlayStation 登录
        </el-button>
        <p><b>2.</b> 登录完成后打开授权码页面，复制页面中的 JSON 或 npsso 值。</p>
        <el-button size="small" plain @click="openPsnToken">
          打开授权码页面
        </el-button>
        <p><b>3.</b> 将授权码粘贴到下面并开始同步。</p>
        <el-input
          v-model="psnNpsso"
          type="password"
          show-password
          autocomplete="new-password"
          placeholder='支持粘贴 {"npsso":"..."} 或完整 npsso 值'
        />
      </div>
      <div slot="footer">
        <el-button @click="psnDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="psnSyncing" @click="syncPsn">
          一次性同步
        </el-button>
      </div>
    </el-dialog>
  </el-card>
</template>

<script>
export default {
  created() {
    this.listConfigs();
  },
  data() {
    return {
      loading: false,
      saving: false,
      psnDialogVisible: false,
      psnSyncing: false,
      psnNpsso: "",
      activePlatform: "STEAM",
      configList: [],
      platformOptions: [
        { label: "Steam", value: "STEAM" },
        { label: "PSN", value: "PSN" },
        { label: "PC", value: "PC" },
        { label: "手游", value: "MOBILE" }
      ]
    };
  },
  methods: {
    listConfigs() {
      this.loading = true;
      this.axios
        .post("/api/admin/games/config/list", {})
        .then(({ data }) => {
          this.configList = data.flag && data.data ? data.data : [];
          this.ensureSteamConfigs();
        })
        .finally(() => {
          this.loading = false;
        });
    },
    ensureSteamConfigs() {
      this.addRequiredSteamConfig("Steam Web API Key", "STEAM_WEB_API_KEY", 1);
      this.addRequiredSteamConfig("Steam ID", "STEAM_ID", 2);
    },
    addRequiredSteamConfig(configName, configKey, sortOrder) {
      const exists = this.configList.some(
        item => item.platform === "STEAM" && item.configKey === configKey
      );
      if (!exists) {
        this.configList.push({
          platform: "STEAM",
          configName,
          configKey,
          configValue: "",
          sortOrder
        });
      }
    },
    configsByPlatform(platform) {
      return this.configList
        .filter(item => item.platform === platform)
        .sort((left, right) => (left.sortOrder || 0) - (right.sortOrder || 0));
    },
    addConfig(platform) {
      const currentList = this.configsByPlatform(platform);
      this.configList.push({
        platform,
        configName: "",
        configKey: "",
        configValue: "",
        sortOrder: currentList.length + 1
      });
    },
    removeConfig(config) {
      const index = this.configList.indexOf(config);
      if (index >= 0) this.configList.splice(index, 1);
    },
    isSteamRequired(config) {
      return (
        config.platform === "STEAM" &&
        ["STEAM_WEB_API_KEY", "STEAM_ID"].includes(config.configKey)
      );
    },
    isSecret(configKey) {
      return /(KEY|TOKEN|SECRET|PASSWORD)/i.test(configKey || "");
    },
    openPsnSync() {
      this.psnDialogVisible = true;
    },
    openPsnLogin() {
      window.open("https://www.playstation.com/", "_blank", "noopener");
    },
    openPsnToken() {
      window.open(
        "https://ca.account.sony.com/api/v1/ssocookie",
        "_blank",
        "noopener"
      );
    },
    syncPsn() {
      if (!this.psnNpsso.trim()) {
        this.$message.warning("请先粘贴 PSN 授权码");
        return;
      }
      this.psnSyncing = true;
      this.axios
        .post("/api/admin/games/psn/sync-once", { npsso: this.psnNpsso })
        .then(({ data }) => {
          if (data.flag) {
            this.notifySyncResult("PSN", data.data);
            this.psnDialogVisible = false;
          } else {
            this.$notify.error({ title: "同步失败", message: data.message });
          }
        })
        .finally(() => {
          this.psnSyncing = false;
          this.psnNpsso = "";
        });
    },
    notifySyncResult(platform, result) {
      const value = result || {};
      this.$notify.success({
        title: platform + " 同步完成",
        message:
          "新增 " +
          (value.addedCount || 0) +
          "，更新 " +
          (value.updatedCount || 0) +
          "，跳过 " +
          (value.skippedCount || 0)
      });
    },
    resetPsnDialog() {
      this.psnNpsso = "";
    },
    saveConfigs() {
      this.saving = true;
      this.axios
        .post("/api/admin/games/config/save", {
          configList: this.configList
        })
        .then(({ data }) => {
          if (data.flag) {
            this.$notify.success({
              title: "成功",
              message: "游戏同步配置已保存"
            });
            this.listConfigs();
          } else {
            this.$notify.error({ title: "保存失败", message: data.message });
          }
        })
        .finally(() => {
          this.saving = false;
        });
    }
  }
};
</script>

<style scoped>
.game-config-card {
  min-height: calc(100vh - 126px);
}
.config-actions,
.platform-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}
.config-actions {
  margin: 22px 0;
}
.config-actions .el-alert {
  flex: 1;
}
.platform-heading {
  margin-bottom: 18px;
}
.platform-heading h3 {
  margin: 0 0 6px;
  color: #303133;
  font-size: 17px;
}
.platform-heading p {
  margin: 0;
  color: #909399;
  font-size: 13px;
}
.one-time-sync-panel {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 16px 18px;
  margin-bottom: 18px;
  border: 1px solid #d9ecff;
  border-radius: 4px;
  background: #f5faff;
}
.one-time-sync-panel strong {
  color: #303133;
  font-size: 15px;
}
.one-time-sync-panel p {
  margin: 6px 0 0;
  color: #606266;
  font-size: 13px;
  line-height: 1.6;
}
.sync-steps {
  margin-top: 18px;
}
.sync-steps p {
  margin: 16px 0 10px;
  color: #606266;
  line-height: 1.7;
}
.delete-config {
  color: #f56c6c;
}
@media (max-width: 900px) {
  .config-actions,
  .platform-heading {
    align-items: flex-start;
    flex-direction: column;
  }
  .one-time-sync-panel {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
