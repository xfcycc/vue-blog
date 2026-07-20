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
          <p>拖拽调整顺序，每张图可以独立设置形状、宽度并编辑内容。</p>
        </div>
        <div class="screenshot-heading-actions">
          <el-button
            size="small"
            icon="el-icon-magic-stick"
            :disabled="!game.screenshotItemList.length"
            @click="autoLayoutScreenshots"
          >
            自动排版
          </el-button>
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
      </div>

      <template v-if="game.screenshotItemList.length">
        <div class="screenshot-toolbar">
          <el-radio-group v-model="screenshotPreviewMode" size="small">
            <el-radio-button label="desktop">
              <i class="el-icon-monitor" /> 桌面预览
            </el-radio-button>
            <el-radio-button label="mobile">
              <i class="el-icon-mobile-phone" /> 手机预览
            </el-radio-button>
          </el-radio-group>
          <span>
            共 {{ game.screenshotItemList.length }} 张
            <em v-if="screenshotDirty">有未保存调整</em>
          </span>
        </div>

        <div class="screenshot-editor-grid">
          <div class="screenshot-list-panel">
            <draggable
              v-model="game.screenshotItemList"
              handle=".screenshot-drag-handle"
              ghost-class="screenshot-drag-ghost"
              :animation="180"
              @end="screenshotOrderChanged"
            >
              <article
                v-for="(item, index) in game.screenshotItemList"
                :key="item.displayUrl + index"
                class="screenshot-list-item"
              >
                <button
                  type="button"
                  class="screenshot-drag-handle"
                  title="拖拽调整顺序"
                >
                  <i class="el-icon-rank" />
                </button>
                <el-image
                  fit="cover"
                  :src="item.displayUrl"
                  :preview-src-list="screenshotDisplayList"
                  :initial-index="index"
                />
                <div class="screenshot-item-content">
                  <div class="screenshot-item-heading">
                    <div>
                      <strong>截图 {{ index + 1 }}</strong>
                      <span>{{ screenshotSizeText(item) }}</span>
                    </div>
                    <div class="screenshot-item-actions">
                      <el-button
                        type="text"
                        icon="el-icon-edit-outline"
                        @click="editScreenshot(index)"
                      >
                        编辑
                      </el-button>
                      <el-button
                        type="text"
                        class="remove-screenshot"
                        icon="el-icon-delete"
                        @click="removeScreenshot(index)"
                      >
                        删除
                      </el-button>
                    </div>
                  </div>
                  <div class="screenshot-item-options">
                    <label>
                      <span>形状</span>
                      <el-select
                        v-model="item.frameType"
                        size="mini"
                        @change="markScreenshotDirty"
                      >
                        <el-option
                          v-for="option in screenshotFrameOptions"
                          :key="option.value"
                          :label="option.label"
                          :value="option.value"
                        />
                      </el-select>
                    </label>
                    <label>
                      <span>宽度</span>
                      <el-select
                        v-model="item.columnSpan"
                        size="mini"
                        @change="markScreenshotDirty"
                      >
                        <el-option
                          v-for="option in screenshotWidthOptions"
                          :key="option.value"
                          :label="option.label"
                          :value="option.value"
                        />
                      </el-select>
                    </label>
                  </div>
                </div>
              </article>
            </draggable>
          </div>

          <div class="screenshot-preview-panel">
            <div class="preview-label">
              <span>前台效果预览</span>
              <small>按保存顺序自动紧凑填充</small>
            </div>
            <game-screenshot-preview
              :items="game.screenshotItemList"
              :preview-mode="screenshotPreviewMode"
            />
          </div>
        </div>
      </template>

      <el-empty
        v-else
        :image-size="72"
        description="暂无游戏截图，上传横图或竖图后即可自由排版"
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

    <game-screenshot-editor
      v-model="screenshotEditorVisible"
      :screenshot="editingScreenshot"
      @saved="screenshotEdited"
    />
  </el-card>
</template>

<script>
import * as imageConversion from "image-conversion";
import draggable from "vuedraggable";
import GameScreenshotEditor from "../../components/game/GameScreenshotEditor";
import GameScreenshotPreview from "../../components/game/GameScreenshotPreview";

export default {
  components: {
    draggable,
    GameScreenshotEditor,
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
      screenshotFrameOptions: [
        { label: "自动", value: "AUTO" },
        { label: "横图 16:9", value: "LANDSCAPE" },
        { label: "竖图 9:16", value: "PORTRAIT" },
        { label: "方图 1:1", value: "SQUARE" }
      ],
      screenshotWidthOptions: [
        { label: "1/3", value: 4 },
        { label: "1/2", value: 6 },
        { label: "2/3", value: 8 },
        { label: "整行", value: 12 }
      ],
      screenshotPreviewMode: "desktop",
      screenshotEditorVisible: false,
      editingScreenshot: {},
      editingScreenshotIndex: -1,
      screenshotDirty: false
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
    },
    screenshotDisplayList() {
      return this.game.screenshotItemList.map(item => item.displayUrl);
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
        screenshotItemList: [],
        screenshotLayout: "CUSTOM",
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
            this.game.screenshotItemList = this.normalizeScreenshotItems(
              data.data.screenshotItemList,
              data.data.screenshotList
            );
            this.game.screenshotList = this.game.screenshotItemList.map(
              item => item.displayUrl
            );
            this.game.screenshotLayout = "CUSTOM";
            this.enrichScreenshotSizes();
            this.screenshotDirty = false;
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
    async uploadScreenshot(response) {
      if (response.flag && response.data) {
        const size = await this.loadImageSize(response.data);
        this.game.screenshotItemList.push({
          originalUrl: response.data,
          displayUrl: response.data,
          originalWidth: size.width,
          originalHeight: size.height,
          displayWidth: size.width,
          displayHeight: size.height,
          frameType: "AUTO",
          columnSpan: size.height > size.width ? 4 : 6,
          sortOrder: this.game.screenshotItemList.length + 1
        });
        this.markScreenshotDirty();
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
    screenshotOrderChanged() {
      this.reindexScreenshots();
      this.markScreenshotDirty();
    },
    removeScreenshot(index) {
      this.$confirm(
        "确认从当前游戏中移除这张截图吗？保存游戏后才会生效。",
        "删除截图",
        {
          confirmButtonText: "确认删除",
          cancelButtonText: "取消",
          type: "warning"
        }
      )
        .then(() => {
          this.game.screenshotItemList.splice(index, 1);
          this.reindexScreenshots();
          this.markScreenshotDirty();
        })
        .catch(() => {});
    },
    editScreenshot(index) {
      this.editingScreenshotIndex = index;
      this.editingScreenshot = Object.assign(
        {},
        this.game.screenshotItemList[index]
      );
      this.screenshotEditorVisible = true;
    },
    screenshotEdited(result) {
      if (this.editingScreenshotIndex < 0) return;
      const current = this.game.screenshotItemList[this.editingScreenshotIndex];
      this.$set(
        this.game.screenshotItemList,
        this.editingScreenshotIndex,
        Object.assign({}, current, result)
      );
      this.markScreenshotDirty();
      this.$message.success("截图编辑结果已上传，保存游戏后正式生效");
    },
    async autoLayoutScreenshots() {
      await this.enrichScreenshotSizes();
      this.game.screenshotItemList.forEach(item => {
        item.frameType = "AUTO";
        item.columnSpan =
          item.displayHeight > item.displayWidth || !item.displayWidth ? 4 : 6;
      });
      this.markScreenshotDirty();
    },
    normalizeScreenshotItems(itemList, legacyList) {
      const sourceList =
        itemList && itemList.length
          ? itemList
          : (legacyList || []).map((url, index) => ({
              originalUrl: url,
              displayUrl: url,
              frameType: "AUTO",
              columnSpan: 6,
              sortOrder: index + 1
            }));
      return sourceList
        .filter(item => item && item.displayUrl)
        .map((item, index) => ({
          originalUrl: item.originalUrl || item.displayUrl,
          displayUrl: item.displayUrl,
          originalWidth: item.originalWidth || item.displayWidth || null,
          originalHeight: item.originalHeight || item.displayHeight || null,
          displayWidth: item.displayWidth || null,
          displayHeight: item.displayHeight || null,
          frameType: ["AUTO", "LANDSCAPE", "PORTRAIT", "SQUARE"].includes(
            item.frameType
          )
            ? item.frameType
            : "AUTO",
          columnSpan: [4, 6, 8, 12].includes(Number(item.columnSpan))
            ? Number(item.columnSpan)
            : 6,
          sortOrder: index + 1
        }));
    },
    enrichScreenshotSizes() {
      const tasks = this.game.screenshotItemList.map((item, index) => {
        if (item.displayWidth && item.displayHeight) return Promise.resolve();
        return this.loadImageSize(item.displayUrl).then(size => {
          const updated = Object.assign({}, item, {
            displayWidth: size.width,
            displayHeight: size.height,
            originalWidth: item.originalWidth || size.width,
            originalHeight: item.originalHeight || size.height
          });
          this.$set(this.game.screenshotItemList, index, updated);
        });
      });
      return Promise.all(tasks);
    },
    loadImageSize(url) {
      return new Promise(resolve => {
        const image = new Image();
        image.onload = () =>
          resolve({
            width: image.naturalWidth || 1,
            height: image.naturalHeight || 1
          });
        image.onerror = () => resolve({ width: null, height: null });
        image.src = url;
      });
    },
    screenshotSizeText(item) {
      if (!item.displayWidth || !item.displayHeight) return "尺寸读取中";
      const direction =
        item.displayHeight > item.displayWidth
          ? "竖图"
          : item.displayHeight === item.displayWidth
          ? "方图"
          : "横图";
      return item.displayWidth + " × " + item.displayHeight + " · " + direction;
    },
    reindexScreenshots() {
      this.game.screenshotItemList.forEach((item, index) => {
        item.sortOrder = index + 1;
      });
    },
    markScreenshotDirty() {
      this.screenshotDirty = true;
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
        playtimeTwoWeeks: this.toMinutes(this.game.playtimeTwoWeeksHours),
        screenshotList: this.screenshotDisplayList,
        screenshotItemList: this.game.screenshotItemList.map((item, index) =>
          Object.assign({}, item, { sortOrder: index + 1 })
        ),
        screenshotLayout: "CUSTOM"
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
.screenshot-heading-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}
.screenshot-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 12px 14px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background: #fafbfd;
}
.screenshot-toolbar > span {
  color: #909399;
  font-size: 13px;
}
.screenshot-toolbar em {
  margin-left: 8px;
  color: #e6a23c;
  font-size: 12px;
  font-style: normal;
}
.screenshot-editor-grid {
  display: grid;
  align-items: start;
  margin-top: 14px;
  grid-template-columns: minmax(360px, 36%) minmax(0, 1fr);
  gap: 18px;
}
.screenshot-list-panel {
  max-height: 720px;
  overflow-y: auto;
  padding-right: 4px;
}
.screenshot-list-panel > div {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.screenshot-list-item {
  position: relative;
  display: grid;
  min-height: 128px;
  overflow: visible;
  grid-template-columns: 118px minmax(0, 1fr);
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 4px 16px rgba(31, 45, 61, 0.05);
  transition: border-color 0.18s ease, box-shadow 0.18s ease;
}
.screenshot-list-item:hover {
  border-color: #c6e2ff;
  box-shadow: 0 7px 20px rgba(31, 45, 61, 0.08);
}
.screenshot-list-item .el-image {
  display: block;
  width: 118px;
  height: 100%;
  min-height: 128px;
  overflow: hidden;
  border-radius: 7px 0 0 7px;
  background: #eef1f5;
}
.screenshot-drag-handle {
  position: absolute;
  z-index: 3;
  top: 8px;
  left: 8px;
  display: flex;
  width: 30px;
  height: 30px;
  align-items: center;
  justify-content: center;
  border: 0;
  border-radius: 7px;
  background: rgba(31, 45, 61, 0.78);
  color: #fff;
  cursor: grab;
  font-size: 16px;
}
.screenshot-drag-handle:active {
  cursor: grabbing;
}
.screenshot-drag-ghost {
  opacity: 0.46;
}
.screenshot-item-content {
  min-width: 0;
  padding: 12px;
}
.screenshot-item-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 8px;
}
.screenshot-item-heading > div:first-child {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 4px;
}
.screenshot-item-heading strong {
  color: #303133;
  font-size: 14px;
}
.screenshot-item-heading span {
  overflow: hidden;
  color: #909399;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.screenshot-item-actions {
  display: flex;
  flex-shrink: 0;
  gap: 8px;
}
.screenshot-item-actions .el-button + .el-button {
  margin-left: 0;
}
.remove-screenshot {
  color: #f56c6c !important;
}
.screenshot-item-options {
  display: grid;
  margin-top: 12px;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
}
.screenshot-item-options label {
  display: grid;
  min-width: 0;
  align-items: center;
  grid-template-columns: 38px minmax(0, 1fr);
  color: #909399;
  font-size: 12px;
  gap: 4px;
}
.screenshot-item-options .el-select {
  width: 100%;
}
.screenshot-preview-panel {
  min-width: 0;
  min-height: 420px;
  padding: 18px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: #f6f8fa;
}
.preview-label {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  color: #606266;
  font-size: 13px;
  font-weight: 700;
}
.preview-label small {
  color: #a0a8b3;
  font-size: 12px;
  font-weight: 400;
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
    max-height: none;
  }
}
@media (max-width: 720px) {
  .screenshot-section .section-heading,
  .screenshot-toolbar {
    align-items: flex-start;
    flex-direction: column;
  }
  .screenshot-heading-actions {
    width: 100%;
  }
  .screenshot-list-item {
    grid-template-columns: 96px minmax(0, 1fr);
  }
  .screenshot-list-item .el-image {
    width: 96px;
  }
  .screenshot-item-options {
    grid-template-columns: 1fr;
  }
}
</style>
