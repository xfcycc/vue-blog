<template>
  <el-card class="main-card game-edit-card" v-loading="loading">
    <div class="title">{{ isNew ? "新增游戏" : "编辑游戏" }}</div>
    <div class="edit-actions">
      <el-button
        size="small"
        icon="el-icon-back"
        @click="$router.push('/games')"
        >返回列表</el-button
      >
      <el-button
        type="primary"
        size="small"
        icon="el-icon-check"
        :loading="saving"
        @click="saveGame"
      >
        保存游戏
      </el-button>
    </div>

    <el-form label-width="108px" class="game-form">
      <el-row :gutter="24">
        <el-col :span="12">
          <el-form-item :label="isSyncedGame ? '平台名称' : '游戏名称'">
            <el-input
              v-model="game.gameName"
              :disabled="isSyncedGame"
              placeholder="请输入游戏名称"
            />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="游玩状态">
            <el-select v-model="game.playStatus" style="width:100%">
              <el-option
                v-for="item in statusOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="个人评分">
            <el-input-number
              v-model="game.personalScore"
              :min="0"
              :max="10"
              :precision="1"
              :step="0.5"
              style="width:100%"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item v-if="isSyncedGame" label="博客显示名称">
        <el-input
          v-model="game.gameAlias"
          placeholder="留空时使用平台同步名称"
        />
        <div class="form-help">
          博客列表和详情优先展示此名称，平台同步不会覆盖。
        </div>
      </el-form-item>

      <el-row :gutter="24">
        <el-col :span="16">
          <el-form-item label="游戏平台">
            <el-checkbox-group v-model="game.platformList">
              <el-checkbox
                v-for="item in platformOptions"
                :key="item.value"
                :label="item.value"
                :disabled="isSyncedGame && item.value === game.source"
              >
                {{ item.label }}
              </el-checkbox>
            </el-checkbox-group>
          </el-form-item>
        </el-col>
        <el-col :span="4">
          <el-form-item label="公开展示">
            <el-switch
              v-model="game.isVisible"
              :active-value="1"
              :inactive-value="0"
            />
          </el-form-item>
        </el-col>
        <el-col :span="4">
          <el-form-item label="排序">
            <el-input-number
              v-model="game.sortOrder"
              :min="0"
              controls-position="right"
              style="width:100%"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="游戏标签">
        <el-select
          v-model="game.tagList"
          multiple
          filterable
          allow-create
          default-first-option
          style="width:100%"
          placeholder="输入标签后按回车添加，例如 MOBA、竞技、多人"
        >
          <el-option
            v-for="tag in game.tagList"
            :key="tag"
            :label="tag"
            :value="tag"
          />
        </el-select>
        <div class="form-help">标签用于前台卡片和详情页展示。</div>
      </el-form-item>

      <el-form-item label="简介摘要">
        <el-input
          v-model="game.gameIntro"
          type="textarea"
          :rows="3"
          maxlength="1000"
          show-word-limit
          placeholder="用于列表卡片和详情页简介"
        />
      </el-form-item>

      <el-row :gutter="24">
        <el-col :span="12">
          <el-form-item label="游戏链接">
            <el-input
              v-model="game.gameUrl"
              :disabled="isSyncedGame"
              placeholder="游戏官网或资料链接"
            />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="累计小时">
            <el-input-number
              v-model="game.playtimeForeverHours"
              :min="0"
              :precision="1"
              :step="0.5"
              :disabled="isSyncedGame"
              controls-position="right"
              style="width:100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="近两周小时">
            <el-input-number
              v-model="game.playtimeTwoWeeksHours"
              :min="0"
              :precision="1"
              :step="0.5"
              :disabled="isSyncedGame"
              controls-position="right"
              style="width:100%"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="自定义封面">
        <div class="cover-editor">
          <el-upload
            class="game-cover-uploader"
            action="/api/admin/games/images"
            :show-file-list="false"
            :before-upload="beforeUpload"
            :on-success="uploadCover"
          >
            <img v-if="displayCover" :src="displayCover" class="edit-cover" />
            <div v-else class="cover-placeholder">
              <i class="el-icon-plus" />
              <span>上传封面</span>
            </div>
          </el-upload>
          <div class="cover-help">
            <el-input
              v-model="game.customCover"
              placeholder="也可以直接填写图片地址"
            />
            <span v-if="isSyncedGame"
              >留空时使用平台同步封面。</span
            >
          </div>
        </div>
      </el-form-item>
    </el-form>

    <section class="edit-section">
      <div class="section-heading">
        <div>
          <h3>自定义字段</h3>
          <p>全部字段显示在详情页，勾选后的前 3 项显示在列表卡片。</p>
        </div>
        <el-button
          type="primary"
          plain
          size="small"
          icon="el-icon-plus"
          @click="addField"
          >添加字段</el-button
        >
      </div>
      <el-table :data="game.fieldList" border empty-text="暂无自定义字段">
        <el-table-column label="分组" min-width="130">
          <template slot-scope="scope"
            ><el-input v-model="scope.row.groupName" placeholder="资产信息"
          /></template>
        </el-table-column>
        <el-table-column label="字段名称" min-width="130">
          <template slot-scope="scope"
            ><el-input v-model="scope.row.fieldName" placeholder="总资产"
          /></template>
        </el-table-column>
        <el-table-column label="字段值" min-width="170">
          <template slot-scope="scope"
            ><el-input v-model="scope.row.fieldValue" placeholder="12.6"
          /></template>
        </el-table-column>
        <el-table-column label="单位" width="100">
          <template slot-scope="scope"
            ><el-input v-model="scope.row.fieldUnit" placeholder="M"
          /></template>
        </el-table-column>
        <el-table-column label="类型" width="120">
          <template slot-scope="scope">
            <el-select v-model="scope.row.fieldType">
              <el-option
                v-for="item in fieldTypeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="卡片展示" align="center" width="95">
          <template slot-scope="scope">
            <el-checkbox
              v-model="scope.row.showOnCard"
              :true-label="1"
              :false-label="0"
              @change="cardFieldChanged(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="排序" width="100">
          <template slot-scope="scope">
            <el-input-number
              v-model="scope.row.sortOrder"
              :min="0"
              :controls="false"
              style="width:100%"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="78">
          <template slot-scope="scope">
            <el-button
              type="text"
              class="remove-field"
              @click="removeField(scope.$index)"
              >删除</el-button
            >
          </template>
        </el-table-column>
      </el-table>
    </section>

    <section class="edit-section screenshot-section">
      <div class="section-heading">
        <div>
          <h3>游戏截图</h3>
          <p>支持横屏和竖屏图片，可调整顺序并预览前台排版效果。</p>
        </div>
        <el-upload
          action="/api/admin/games/images"
          multiple
          :show-file-list="false"
          :before-upload="beforeUpload"
          :on-success="uploadScreenshot"
          :on-error="screenshotUploadFailed"
        >
          <el-button type="primary" plain size="small" icon="el-icon-upload2">
            上传截图
          </el-button>
        </el-upload>
      </div>

      <template v-if="game.screenshotList.length">
        <div class="screenshot-toolbar">
          <el-radio-group v-model="game.screenshotLayout" size="small">
            <el-radio-button
              v-for="item in screenshotLayoutOptions"
              :key="item.value"
              :label="item.value"
            >
              {{ item.label }}
            </el-radio-button>
          </el-radio-group>
          <span>共 {{ game.screenshotList.length }} 张</span>
        </div>

        <div class="screenshot-editor-grid">
          <div class="screenshot-list-panel">
            <div
              v-for="(image, index) in game.screenshotList"
              :key="image + index"
              class="screenshot-list-item"
            >
              <el-image
                fit="contain"
                :src="image"
                :preview-src-list="game.screenshotList"
                :initial-index="index"
              />
              <div class="screenshot-item-meta">
                <span>截图 {{ index + 1 }}</span>
                <div>
                  <el-button
                    type="text"
                    icon="el-icon-arrow-up"
                    :disabled="index === 0"
                    title="上移"
                    @click="moveScreenshot(index, -1)"
                  />
                  <el-button
                    type="text"
                    icon="el-icon-arrow-down"
                    :disabled="index === game.screenshotList.length - 1"
                    title="下移"
                    @click="moveScreenshot(index, 1)"
                  />
                  <el-button
                    type="text"
                    class="remove-screenshot"
                    icon="el-icon-delete"
                    title="删除"
                    @click="removeScreenshot(index)"
                  />
                </div>
              </div>
            </div>
          </div>

          <div class="screenshot-preview-panel">
            <div class="preview-label">前台效果预览</div>
            <game-screenshot-preview
              :images="game.screenshotList"
              :layout="game.screenshotLayout"
            />
          </div>
        </div>
      </template>

      <el-empty
        v-else
        :image-size="72"
        description="暂无游戏截图，上传后可选择排版并预览"
      />
    </section>

    <section class="edit-section review-section">
      <div class="section-heading">
        <div>
          <h3>详细评测</h3>
          <p>支持 Markdown、表格、代码块和图片，内容展示在游戏详情页。</p>
        </div>
      </div>
      <mavon-editor
        ref="md"
        v-model="game.reviewContent"
        @imgAdd="uploadReviewImage"
        class="game-review-editor"
      />
    </section>

    <div class="bottom-actions">
      <el-button @click="$router.push('/games')">取消</el-button>
      <el-button type="primary" :loading="saving" @click="saveGame"
        >保存游戏</el-button
      >
    </div>
  </el-card>
</template>

<script>
import * as imageConversion from "image-conversion";
import GameScreenshotPreview from "../../components/game/GameScreenshotPreview";

export default {
  components: {
    GameScreenshotPreview
  },
  created() {
    if (!this.isNew) this.getGame();
  },
  data() {
    return {
      loading: false,
      saving: false,
      game: this.emptyGame(),
      statusOptions: [
        { label: "想玩", value: "WANT" },
        { label: "在玩", value: "PLAYING" },
        { label: "玩过", value: "PLAYED" }
      ],
      platformOptions: [
        { label: "Steam", value: "STEAM" },
        { label: "PSN", value: "PSN" },
        { label: "PC", value: "PC" },
        { label: "手游", value: "MOBILE" },
        { label: "Switch", value: "SWITCH" }
      ],
      fieldTypeOptions: [
        { label: "文本", value: "TEXT" },
        { label: "数字", value: "NUMBER" },
        { label: "金额", value: "CURRENCY" },
        { label: "百分比", value: "PERCENT" },
        { label: "日期", value: "DATE" },
        { label: "链接", value: "LINK" }
      ],
      screenshotLayoutOptions: [
        { label: "大图轮播", value: "CAROUSEL" },
        { label: "主图优先", value: "FEATURED" },
        { label: "等宽宫格", value: "GRID" }
      ]
    };
  },
  computed: {
    isNew() {
      return this.$route.params.gameId === "new";
    },
    isSyncedGame() {
      return this.game.source !== "CUSTOM";
    },
    displayCover() {
      return this.game.customCover || this.game.sourceCover || this.game.cover;
    }
  },
  methods: {
    emptyGame() {
      return {
        id: null,
        source: "CUSTOM",
        gameName: "",
        gameAlias: "",
        gameIntro: "",
        sourceCover: "",
        customCover: "",
        gameUrl: "",
        platformList: ["PC"],
        tagList: [],
        playStatus: "WANT",
        personalScore: null,
        sortOrder: 9999,
        isVisible: 0,
        playtimeForever: 0,
        playtimeTwoWeeks: 0,
        playtimeForeverHours: 0,
        playtimeTwoWeeksHours: 0,
        screenshotList: [],
        screenshotLayout: "CAROUSEL",
        reviewContent: "",
        fieldList: []
      };
    },
    getGame() {
      this.loading = true;
      this.axios
        .post("/api/admin/games/detail", {
          id: Number(this.$route.params.gameId)
        })
        .then(({ data }) => {
          if (data.flag && data.data) {
            this.game = Object.assign(this.emptyGame(), data.data);
            this.game.platformList = data.data.platformList || [];
            this.game.tagList = data.data.tagList || [];
            this.game.fieldList = data.data.fieldList || [];
            this.game.screenshotList = data.data.screenshotList || [];
            this.game.screenshotLayout =
              data.data.screenshotLayout || "CAROUSEL";
            this.game.playtimeForeverHours = this.toHours(
              data.data.playtimeForever
            );
            this.game.playtimeTwoWeeksHours = this.toHours(
              data.data.playtimeTwoWeeks
            );
          }
        })
        .finally(() => {
          this.loading = false;
        });
    },
    addField() {
      this.game.fieldList.push({
        fieldName: "",
        fieldValue: "",
        fieldUnit: "",
        fieldType: "TEXT",
        groupName: "游戏数据",
        showOnCard: 0,
        sortOrder: this.game.fieldList.length + 1
      });
    },
    removeField(index) {
      this.game.fieldList.splice(index, 1);
    },
    uploadScreenshot(response) {
      if (response.flag && response.data) {
        this.game.screenshotList.push(response.data);
      } else {
        this.$notify.error({
          title: "上传失败",
          message: response.message || "游戏截图上传失败"
        });
      }
    },
    screenshotUploadFailed() {
      this.$notify.error({ title: "上传失败", message: "游戏截图上传失败" });
    },
    moveScreenshot(index, step) {
      const target = index + step;
      if (target < 0 || target >= this.game.screenshotList.length) return;
      const list = this.game.screenshotList.slice();
      const current = list.splice(index, 1)[0];
      list.splice(target, 0, current);
      this.game.screenshotList = list;
    },
    removeScreenshot(index) {
      this.game.screenshotList.splice(index, 1);
    },
    cardFieldChanged(field) {
      const count = this.game.fieldList.filter(item => item.showOnCard === 1)
        .length;
      if (count > 3) {
        field.showOnCard = 0;
        this.$message.warning("列表卡片最多展示 3 个自定义字段");
      }
    },
    saveGame() {
      if (!this.game.gameName || !this.game.gameName.trim()) {
        this.$message.error("游戏名称不能为空");
        return;
      }
      this.saving = true;
      const payload = Object.assign({}, this.game, {
        playtimeForever: this.toMinutes(this.game.playtimeForeverHours),
        playtimeTwoWeeks: this.toMinutes(this.game.playtimeTwoWeeksHours)
      });
      this.axios
        .post("/api/admin/games/save", payload)
        .then(({ data }) => {
          if (data.flag) {
            this.$notify.success({ title: "成功", message: "游戏档案已保存" });
            this.$router.push({ path: "/games" });
          } else {
            this.$notify.error({ title: "保存失败", message: data.message });
          }
        })
        .finally(() => {
          this.saving = false;
        });
    },
    toHours(minutes) {
      return Number(((minutes || 0) / 60).toFixed(1));
    },
    toMinutes(hours) {
      return Math.round((hours || 0) * 60);
    },
    uploadCover(response) {
      if (response.flag) this.game.customCover = response.data;
    },
    beforeUpload(file) {
      return new Promise(resolve => {
        if (file.size / 1024 < this.config.UPLOAD_SIZE) {
          resolve(file);
          return;
        }
        imageConversion
          .compressAccurately(file, this.config.UPLOAD_SIZE)
          .then(resolve);
      });
    },
    uploadReviewImage(pos, file) {
      const formdata = new FormData();
      const upload = uploadFile => {
        formdata.append("file", uploadFile);
        this.axios
          .post("/api/admin/games/images", formdata)
          .then(({ data }) => {
            if (data.flag) this.$refs.md.$img2Url(pos, data.data);
          });
      };
      if (file.size / 1024 < this.config.UPLOAD_SIZE) {
        upload(file);
      } else {
        imageConversion
          .compressAccurately(file, this.config.UPLOAD_SIZE)
          .then(res => {
            upload(new window.File([res], file.name, { type: file.type }));
          });
      }
    }
  }
};
</script>

<style scoped>
.game-edit-card {
  padding-bottom: 28px;
}
.edit-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin: 8px 0 32px;
}
.game-form {
  padding: 22px 20px 4px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background: #fafbfd;
}
.cover-editor {
  display: flex;
  align-items: center;
  gap: 18px;
}
.edit-cover,
.cover-placeholder {
  width: 240px;
  height: 112px;
  border-radius: 7px;
  object-fit: cover;
}
.cover-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 8px;
  border: 1px dashed #c0ccda;
  color: #8c939d;
}
.cover-help {
  width: min(520px, 60%);
  color: #909399;
  font-size: 12px;
}
.cover-help span {
  display: block;
  margin-top: 8px;
}
.form-help {
  color: #909399;
  font-size: 12px;
  line-height: 1.5;
}
.edit-section {
  margin-top: 28px;
}
.section-heading {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 14px;
}
.section-heading h3 {
  margin: 0 0 6px;
  color: #202a34;
  font-size: 17px;
}
.section-heading p {
  margin: 0;
  color: #909399;
  font-size: 13px;
}
.remove-field {
  color: #f56c6c;
}
.game-review-editor {
  min-height: 520px;
}
.screenshot-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 16px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background: #fafbfd;
}
.screenshot-toolbar > span {
  color: #909399;
  font-size: 13px;
}
.screenshot-editor-grid {
  display: grid;
  align-items: start;
  margin-top: 14px;
  grid-template-columns: 280px minmax(0, 1fr);
  gap: 18px;
}
.screenshot-list-panel {
  display: flex;
  max-height: 620px;
  overflow-y: auto;
  flex-direction: column;
  gap: 10px;
  padding-right: 4px;
}
.screenshot-list-item {
  overflow: hidden;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: #fff;
}
.screenshot-list-item .el-image {
  display: block;
  width: 100%;
  height: 128px;
  background: #111b25;
}
.screenshot-item-meta {
  display: flex;
  min-height: 42px;
  align-items: center;
  justify-content: space-between;
  padding: 4px 10px 4px 12px;
}
.screenshot-item-meta > span {
  color: #606266;
  font-size: 13px;
  font-weight: 600;
}
.screenshot-item-meta .el-button + .el-button {
  margin-left: 4px;
}
.remove-screenshot {
  color: #f56c6c;
}
.screenshot-preview-panel {
  min-width: 0;
  padding: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: #f6f8fa;
}
.preview-label {
  margin-bottom: 12px;
  color: #606266;
  font-size: 13px;
  font-weight: 700;
}
.bottom-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 24px;
}
@media (max-width: 1100px) {
  .cover-editor {
    align-items: flex-start;
    flex-direction: column;
  }
  .cover-help {
    width: 100%;
  }
  .screenshot-editor-grid {
    grid-template-columns: 1fr;
  }
  .screenshot-list-panel {
    display: grid;
    max-height: none;
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}
@media (max-width: 720px) {
  .screenshot-toolbar {
    align-items: flex-start;
    flex-direction: column;
  }
  .screenshot-list-panel {
    grid-template-columns: 1fr;
  }
}
</style>
