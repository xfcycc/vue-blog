<template>
  <el-dialog
    :visible.sync="dialogVisible"
    width="94%"
    top="3vh"
    custom-class="game-screenshot-editor-dialog"
    append-to-body
    :close-on-click-modal="false"
    :before-close="closeEditor"
    @opened="initializeEditor"
    @closed="destroyEditor"
  >
    <div slot="title" class="editor-dialog-title">
      <div>
        <strong>编辑游戏截图</strong>
        <span>编辑后生成新的展示图，原图仍可恢复</span>
      </div>
      <div class="history-actions">
        <el-button size="mini" :disabled="historyIndex <= 0" @click="undo">
          <i class="el-icon-refresh-left" /> 撤销
        </el-button>
        <el-button
          size="mini"
          :disabled="historyIndex >= historyList.length - 1"
          @click="redo"
        >
          <i class="el-icon-refresh-right" /> 重做
        </el-button>
        <el-button size="mini" @click="restoreOriginal">
          <i class="el-icon-picture-outline" /> 恢复原图
        </el-button>
      </div>
    </div>

    <div v-loading="loading" class="screenshot-editor-shell">
      <aside class="editor-tools">
        <button type="button" @click="startSelection('crop')">
          <i class="el-icon-crop" />
          <span>裁剪</span>
        </button>
        <button type="button" @click="rotateImage">
          <i class="el-icon-refresh-right" />
          <span>旋转</span>
        </button>
        <button type="button" @click="startSelection('mosaic')">
          <i class="el-icon-menu" />
          <span>马赛克</span>
        </button>
        <button type="button" @click="addText">
          <i class="el-icon-edit-outline" />
          <span>文字</span>
        </button>
        <button type="button" @click="addCover">
          <i class="el-icon-full-screen" />
          <span>遮挡块</span>
        </button>
      </aside>

      <main class="editor-stage">
        <div class="canvas-wrap">
          <canvas ref="canvas" />
        </div>
        <div v-if="selectionMode" class="selection-actions">
          <span>
            拖动并调整选区，然后应用{{
              selectionMode === "crop" ? "裁剪" : "马赛克"
            }}
          </span>
          <el-button size="mini" @click="cancelSelection">取消选区</el-button>
          <el-button
            size="mini"
            type="primary"
            @click="selectionMode === 'crop' ? applyCrop() : applyMosaic()"
          >
            应用
          </el-button>
        </div>
      </main>

      <aside class="editor-inspector">
        <section>
          <h4>输出尺寸</h4>
          <div class="dimension-row">
            <el-input-number
              v-model="outputWidth"
              :min="1"
              :max="8000"
              :controls="false"
              @change="outputWidthChanged"
            />
            <span>×</span>
            <el-input-number
              v-model="outputHeight"
              :min="1"
              :max="8000"
              :controls="false"
              @change="outputHeightChanged"
            />
          </div>
          <el-checkbox v-model="lockRatio">锁定宽高比</el-checkbox>
        </section>

        <section>
          <h4>文字</h4>
          <el-input
            v-model="textValue"
            size="small"
            placeholder="输入要添加的文字"
            @input="updateSelectedText"
          />
          <div class="property-row">
            <span>字号</span>
            <el-input-number
              v-model="textSize"
              size="mini"
              :min="12"
              :max="160"
              @change="updateSelectedText"
            />
          </div>
        </section>

        <section>
          <h4>颜色</h4>
          <div class="property-row">
            <span>文字 / 遮挡</span>
            <el-color-picker
              v-model="drawColor"
              @change="updateSelectedColor"
            />
          </div>
        </section>

        <section>
          <h4>马赛克强度</h4>
          <el-slider v-model="mosaicStrength" :min="6" :max="30" />
        </section>

        <p class="editor-tip">
          马赛克和遮挡只作用于生成的展示图。保留原图不等同于销毁敏感信息。
        </p>
      </aside>
    </div>

    <span slot="footer" class="dialog-footer">
      <el-button @click="closeEditor">取消</el-button>
      <el-button type="primary" :loading="saving" @click="saveEditedImage">
        保存编辑
      </el-button>
    </span>
  </el-dialog>
</template>

<script>
import { fabric } from "fabric";

export default {
  name: "GameScreenshotEditor",
  props: {
    value: {
      type: Boolean,
      default: false
    },
    screenshot: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      canvas: null,
      loading: false,
      saving: false,
      selectionMode: "",
      historyList: [],
      historyIndex: -1,
      historyLocked: false,
      outputWidth: 1,
      outputHeight: 1,
      lockRatio: true,
      currentRatio: 1,
      textValue: "游戏截图",
      textSize: 40,
      drawColor: "#111827",
      mosaicStrength: 14
    };
  },
  computed: {
    dialogVisible: {
      get() {
        return this.value;
      },
      set(value) {
        this.$emit("input", value);
      }
    }
  },
  methods: {
    async initializeEditor() {
      if (!this.value || !this.$refs.canvas) return;
      this.destroyEditor();
      this.canvas = new fabric.Canvas(this.$refs.canvas, {
        preserveObjectStacking: true,
        selectionColor: "rgba(64, 158, 255, 0.12)",
        selectionBorderColor: "#409eff"
      });
      this.canvas.on("object:modified", this.recordHistory);
      this.canvas.on("selection:created", this.syncSelectedObject);
      this.canvas.on("selection:updated", this.syncSelectedObject);
      this.canvas.on("selection:cleared", this.syncSelectedObject);
      await this.loadRemoteImage(
        this.screenshot.displayUrl || this.screenshot.originalUrl,
        true
      );
    },
    destroyEditor() {
      if (this.canvas) {
        this.canvas.dispose();
        this.canvas = null;
      }
      this.historyList = [];
      this.historyIndex = -1;
      this.selectionMode = "";
    },
    closeEditor(done) {
      if (this.saving) return;
      if (typeof done === "function") {
        done();
        return;
      }
      this.dialogVisible = false;
    },
    async loadRemoteImage(url, resetHistory) {
      if (!url) return;
      this.loading = true;
      try {
        const { data } = await this.axios.post(
          "/api/admin/games/images/source",
          { url }
        );
        if (!data.flag || !data.data) {
          throw new Error(data.message || "读取图片失败");
        }
        await this.loadDataUrl(
          data.data.dataUrl,
          data.data.width,
          data.data.height,
          resetHistory
        );
      } catch (error) {
        this.$notify.error({
          title: "图片读取失败",
          message: error.message || "无法读取这张截图"
        });
      } finally {
        this.loading = false;
      }
    },
    loadDataUrl(dataUrl, width, height, resetHistory) {
      return new Promise((resolve, reject) => {
        fabric.Image.fromURL(
          dataUrl,
          image => {
            if (!image || !this.canvas) {
              reject(new Error("图片加载失败"));
              return;
            }
            const sourceWidth = width || image.width;
            const sourceHeight = height || image.height;
            const scale = Math.min(840 / sourceWidth, 560 / sourceHeight);
            const canvasWidth = Math.max(1, Math.round(sourceWidth * scale));
            const canvasHeight = Math.max(1, Math.round(sourceHeight * scale));
            this.historyLocked = true;
            this.canvas.clear();
            this.canvas.setWidth(canvasWidth);
            this.canvas.setHeight(canvasHeight);
            image.set({
              left: 0,
              top: 0,
              scaleX: canvasWidth / image.width,
              scaleY: canvasHeight / image.height,
              selectable: false,
              evented: false,
              name: "base-image"
            });
            this.canvas.add(image);
            this.canvas.sendToBack(image);
            this.canvas.renderAll();
            this.outputWidth = Math.max(1, Math.round(sourceWidth));
            this.outputHeight = Math.max(1, Math.round(sourceHeight));
            this.currentRatio = this.outputWidth / this.outputHeight;
            this.historyLocked = false;
            if (resetHistory) {
              this.historyList = [];
              this.historyIndex = -1;
            }
            this.recordHistory();
            resolve();
          },
          { crossOrigin: "anonymous" }
        );
      });
    },
    startSelection(mode) {
      if (!this.canvas) return;
      this.cancelSelection();
      this.selectionMode = mode;
      const selection = new fabric.Rect({
        left: this.canvas.getWidth() * 0.15,
        top: this.canvas.getHeight() * 0.15,
        width: this.canvas.getWidth() * 0.7,
        height: this.canvas.getHeight() * 0.7,
        fill:
          mode === "crop" ? "rgba(64,158,255,0.10)" : "rgba(245,158,11,0.14)",
        stroke: mode === "crop" ? "#409eff" : "#f59e0b",
        strokeWidth: 2,
        strokeDashArray: [8, 6],
        transparentCorners: false,
        cornerColor: mode === "crop" ? "#409eff" : "#f59e0b",
        name: "selection-" + mode
      });
      this.canvas.add(selection);
      this.canvas.setActiveObject(selection);
      this.canvas.renderAll();
    },
    cancelSelection() {
      if (!this.canvas) return;
      this.canvas
        .getObjects()
        .filter(item => /^selection-/.test(item.name || ""))
        .forEach(item => this.canvas.remove(item));
      this.selectionMode = "";
      this.canvas.discardActiveObject();
      this.canvas.renderAll();
    },
    async applyCrop() {
      const selection = this.findSelection("crop");
      if (!selection || !this.canvas) return;
      const bounds = selection.getBoundingRect(true, true);
      const left = Math.max(
        0,
        Math.min(this.canvas.getWidth() - 1, bounds.left)
      );
      const top = Math.max(
        0,
        Math.min(this.canvas.getHeight() - 1, bounds.top)
      );
      const right = Math.max(
        left + 1,
        Math.min(this.canvas.getWidth(), bounds.left + bounds.width)
      );
      const bottom = Math.max(
        top + 1,
        Math.min(this.canvas.getHeight(), bounds.top + bounds.height)
      );
      const cropWidth = Math.max(1, right - left);
      const cropHeight = Math.max(1, bottom - top);
      this.canvas.remove(selection);
      this.canvas.discardActiveObject();
      this.canvas.renderAll();
      const dataUrl = this.canvas.toDataURL({
        format: "png",
        left,
        top,
        width: cropWidth,
        height: cropHeight,
        multiplier: 1
      });
      const width = Math.max(
        1,
        Math.round((this.outputWidth * cropWidth) / this.canvas.getWidth())
      );
      const height = Math.max(
        1,
        Math.round((this.outputHeight * cropHeight) / this.canvas.getHeight())
      );
      this.selectionMode = "";
      await this.loadDataUrl(dataUrl, width, height, false);
    },
    applyMosaic() {
      const selection = this.findSelection("mosaic");
      if (!selection || !this.canvas) return;
      const bounds = selection.getBoundingRect(true, true);
      const left = Math.max(
        0,
        Math.min(this.canvas.getWidth() - 1, bounds.left)
      );
      const top = Math.max(
        0,
        Math.min(this.canvas.getHeight() - 1, bounds.top)
      );
      const width = Math.max(
        1,
        Math.min(bounds.width, this.canvas.getWidth() - left)
      );
      const height = Math.max(
        1,
        Math.min(bounds.height, this.canvas.getHeight() - top)
      );
      this.canvas.remove(selection);
      this.canvas.discardActiveObject();
      this.canvas.renderAll();
      const sourceCanvas = this.canvas.toCanvasElement();
      const mosaicCanvas = document.createElement("canvas");
      mosaicCanvas.width = Math.round(width);
      mosaicCanvas.height = Math.round(height);
      const context = mosaicCanvas.getContext("2d");
      const smallWidth = Math.max(1, Math.round(width / this.mosaicStrength));
      const smallHeight = Math.max(1, Math.round(height / this.mosaicStrength));
      const smallCanvas = document.createElement("canvas");
      smallCanvas.width = smallWidth;
      smallCanvas.height = smallHeight;
      const smallContext = smallCanvas.getContext("2d");
      smallContext.drawImage(
        sourceCanvas,
        left,
        top,
        width,
        height,
        0,
        0,
        smallWidth,
        smallHeight
      );
      context.imageSmoothingEnabled = false;
      context.drawImage(smallCanvas, 0, 0, width, height);
      const mosaicImage = new fabric.Image(mosaicCanvas, {
        left,
        top,
        name: "mosaic"
      });
      this.canvas.add(mosaicImage);
      this.canvas.setActiveObject(mosaicImage);
      this.selectionMode = "";
      this.canvas.renderAll();
      this.recordHistory();
    },
    findSelection(mode) {
      return this.canvas
        ? this.canvas
            .getObjects()
            .find(item => item.name === "selection-" + mode)
        : null;
    },
    async rotateImage() {
      if (!this.canvas) return;
      this.cancelSelection();
      const source = await this.loadHtmlImage(
        this.canvas.toDataURL({ format: "png", multiplier: 1 })
      );
      const rotatedCanvas = document.createElement("canvas");
      rotatedCanvas.width = source.height;
      rotatedCanvas.height = source.width;
      const context = rotatedCanvas.getContext("2d");
      context.translate(rotatedCanvas.width / 2, rotatedCanvas.height / 2);
      context.rotate(Math.PI / 2);
      context.drawImage(source, -source.width / 2, -source.height / 2);
      const width = this.outputHeight;
      const height = this.outputWidth;
      await this.loadDataUrl(
        rotatedCanvas.toDataURL("image/png"),
        width,
        height,
        false
      );
    },
    addText() {
      if (!this.canvas) return;
      const text = new fabric.IText(this.textValue || "双击编辑文字", {
        left: this.canvas.getWidth() * 0.2,
        top: this.canvas.getHeight() * 0.2,
        fill: this.drawColor,
        fontSize: this.textSize,
        fontFamily: "PingFang SC, Microsoft YaHei, sans-serif",
        fontWeight: 700,
        name: "text"
      });
      this.canvas.add(text);
      this.canvas.setActiveObject(text);
      this.canvas.renderAll();
      this.recordHistory();
    },
    addCover() {
      if (!this.canvas) return;
      const cover = new fabric.Rect({
        left: this.canvas.getWidth() * 0.2,
        top: this.canvas.getHeight() * 0.2,
        width: Math.max(100, this.canvas.getWidth() * 0.3),
        height: Math.max(50, this.canvas.getHeight() * 0.14),
        fill: this.drawColor,
        rx: 4,
        ry: 4,
        name: "cover"
      });
      this.canvas.add(cover);
      this.canvas.setActiveObject(cover);
      this.canvas.renderAll();
      this.recordHistory();
    },
    syncSelectedObject() {
      if (!this.canvas) return;
      const object = this.canvas.getActiveObject();
      if (!object) return;
      if (object.type === "i-text" || object.type === "text") {
        this.textValue = object.text;
        this.textSize = Math.round(object.fontSize || this.textSize);
      }
      if (object.fill && typeof object.fill === "string") {
        this.drawColor = object.fill;
      }
    },
    updateSelectedText() {
      if (!this.canvas) return;
      const object = this.canvas.getActiveObject();
      if (!object || (object.type !== "i-text" && object.type !== "text"))
        return;
      object.set({ text: this.textValue, fontSize: this.textSize });
      object.setCoords();
      this.canvas.renderAll();
      this.recordHistory();
    },
    updateSelectedColor() {
      if (!this.canvas) return;
      const object = this.canvas.getActiveObject();
      if (!object || !["i-text", "text", "rect"].includes(object.type)) return;
      object.set("fill", this.drawColor);
      this.canvas.renderAll();
      this.recordHistory();
    },
    outputWidthChanged(value) {
      if (this.lockRatio && value && this.currentRatio) {
        this.outputHeight = Math.max(1, Math.round(value / this.currentRatio));
      }
      this.$nextTick(this.recordHistory);
    },
    outputHeightChanged(value) {
      if (this.lockRatio && value && this.currentRatio) {
        this.outputWidth = Math.max(1, Math.round(value * this.currentRatio));
      }
      this.$nextTick(this.recordHistory);
    },
    recordHistory() {
      if (!this.canvas || this.historyLocked || this.selectionMode) return;
      const state = JSON.stringify({
        canvas: this.canvas.toDatalessJSON(["name"]),
        width: this.canvas.getWidth(),
        height: this.canvas.getHeight(),
        outputWidth: this.outputWidth,
        outputHeight: this.outputHeight,
        ratio: this.currentRatio
      });
      if (this.historyList[this.historyIndex] === state) return;
      this.historyList = this.historyList.slice(0, this.historyIndex + 1);
      this.historyList.push(state);
      if (this.historyList.length > 25) this.historyList.shift();
      this.historyIndex = this.historyList.length - 1;
    },
    undo() {
      if (this.historyIndex <= 0) return;
      this.historyIndex -= 1;
      this.restoreHistory();
    },
    redo() {
      if (this.historyIndex >= this.historyList.length - 1) return;
      this.historyIndex += 1;
      this.restoreHistory();
    },
    restoreHistory() {
      if (!this.canvas || this.historyIndex < 0) return;
      const state = JSON.parse(this.historyList[this.historyIndex]);
      this.historyLocked = true;
      this.canvas.setWidth(state.width);
      this.canvas.setHeight(state.height);
      this.canvas.loadFromJSON(state.canvas, () => {
        this.outputWidth = state.outputWidth;
        this.outputHeight = state.outputHeight;
        this.currentRatio = state.ratio;
        this.canvas.renderAll();
        this.historyLocked = false;
      });
    },
    restoreOriginal() {
      this.cancelSelection();
      return this.loadRemoteImage(
        this.screenshot.originalUrl || this.screenshot.displayUrl,
        false
      );
    },
    async saveEditedImage() {
      if (!this.canvas) return;
      this.saving = true;
      try {
        this.cancelSelection();
        if (this.outputWidth * this.outputHeight > 40000000) {
          throw new Error("输出图片不能超过4000万像素");
        }
        const source = await this.loadHtmlImage(
          this.canvas.toDataURL({ format: "png", multiplier: 1 })
        );
        const outputCanvas = document.createElement("canvas");
        outputCanvas.width = Math.max(1, Math.round(this.outputWidth));
        outputCanvas.height = Math.max(1, Math.round(this.outputHeight));
        const context = outputCanvas.getContext("2d");
        context.drawImage(
          source,
          0,
          0,
          outputCanvas.width,
          outputCanvas.height
        );
        const blob = await new Promise(resolve =>
          outputCanvas.toBlob(resolve, "image/png")
        );
        if (!blob) throw new Error("生成编辑图片失败");
        const formData = new FormData();
        formData.append("file", blob, "game-screenshot-edited.png");
        const { data } = await this.axios.post(
          "/api/admin/games/images",
          formData
        );
        if (!data.flag || !data.data) {
          throw new Error(data.message || "上传编辑图片失败");
        }
        this.$emit("saved", {
          displayUrl: data.data,
          displayWidth: outputCanvas.width,
          displayHeight: outputCanvas.height
        });
        this.dialogVisible = false;
      } catch (error) {
        this.$notify.error({
          title: "保存失败",
          message: error.message || "截图编辑结果保存失败"
        });
      } finally {
        this.saving = false;
      }
    },
    loadHtmlImage(dataUrl) {
      return new Promise((resolve, reject) => {
        const image = new Image();
        image.onload = () => resolve(image);
        image.onerror = () => reject(new Error("图片处理失败"));
        image.src = dataUrl;
      });
    }
  }
};
</script>

<style>
.game-screenshot-editor-dialog {
  max-width: 1440px;
  border-radius: 10px;
}
.game-screenshot-editor-dialog .el-dialog__header {
  padding: 18px 22px;
  border-bottom: 1px solid #ebeef5;
}
.game-screenshot-editor-dialog .el-dialog__body {
  padding: 0;
}
.game-screenshot-editor-dialog .el-dialog__footer {
  padding: 14px 22px;
  border-top: 1px solid #ebeef5;
}
.editor-dialog-title,
.editor-dialog-title > div,
.history-actions {
  display: flex;
  align-items: center;
}
.editor-dialog-title {
  justify-content: space-between;
  padding-right: 28px;
}
.editor-dialog-title > div:first-child {
  align-items: flex-start;
  flex-direction: column;
  gap: 4px;
}
.editor-dialog-title strong {
  color: #202a34;
  font-size: 17px;
}
.editor-dialog-title span {
  color: #909399;
  font-size: 12px;
}
.history-actions {
  gap: 8px;
}
.history-actions .el-button + .el-button {
  margin-left: 0;
}
.screenshot-editor-shell {
  display: grid;
  min-height: 650px;
  grid-template-columns: 92px minmax(0, 1fr) 250px;
  background: #f4f6f8;
}
.editor-tools {
  display: flex;
  padding: 16px 10px;
  border-right: 1px solid #dfe4ea;
  background: #fff;
  flex-direction: column;
  gap: 8px;
}
.editor-tools button {
  display: flex;
  min-height: 66px;
  align-items: center;
  justify-content: center;
  border: 0;
  border-radius: 8px;
  background: transparent;
  color: #606266;
  cursor: pointer;
  flex-direction: column;
  font: 13px/1.2 -apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC",
    "Microsoft YaHei", sans-serif;
  gap: 7px;
}
.editor-tools button:hover {
  background: #ecf5ff;
  color: #409eff;
}
.editor-tools i {
  font-size: 21px;
}
.editor-stage {
  display: flex;
  min-width: 0;
  align-items: center;
  justify-content: center;
  padding: 24px;
  flex-direction: column;
  gap: 14px;
}
.canvas-wrap {
  display: flex;
  max-width: 100%;
  overflow: auto;
  align-items: center;
  justify-content: center;
  padding: 18px;
  border-radius: 10px;
  background-color: #d9dee5;
  background-image: linear-gradient(45deg, #c9cfd7 25%, transparent 25%),
    linear-gradient(-45deg, #c9cfd7 25%, transparent 25%),
    linear-gradient(45deg, transparent 75%, #c9cfd7 75%),
    linear-gradient(-45deg, transparent 75%, #c9cfd7 75%);
  background-position: 0 0, 0 8px, 8px -8px, -8px 0;
  background-size: 16px 16px;
  box-shadow: inset 0 0 0 1px rgba(31, 45, 61, 0.08);
}
.canvas-wrap .canvas-container {
  box-shadow: 0 12px 34px rgba(31, 45, 61, 0.18);
}
.selection-actions {
  display: flex;
  min-height: 44px;
  align-items: center;
  padding: 6px 8px 6px 14px;
  border: 1px solid #d9ecff;
  border-radius: 8px;
  background: #f4faff;
  color: #606266;
  font-size: 13px;
  gap: 8px;
}
.selection-actions span {
  margin-right: 6px;
}
.editor-inspector {
  padding: 18px;
  border-left: 1px solid #dfe4ea;
  background: #fff;
}
.editor-inspector section {
  padding: 0 0 18px;
  margin-bottom: 18px;
  border-bottom: 1px solid #ebeef5;
}
.editor-inspector h4 {
  margin: 0 0 12px;
  color: #303133;
  font-size: 14px;
}
.dimension-row,
.property-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.dimension-row {
  margin-bottom: 10px;
}
.dimension-row .el-input-number {
  width: 92px;
}
.property-row {
  justify-content: space-between;
  margin-top: 10px;
  color: #606266;
  font-size: 13px;
}
.property-row .el-input-number {
  width: 112px;
}
.editor-tip {
  margin: 0;
  color: #909399;
  font-size: 12px;
  line-height: 1.7;
}
@media (max-width: 1000px) {
  .screenshot-editor-shell {
    grid-template-columns: 76px minmax(0, 1fr);
  }
  .editor-inspector {
    grid-column: 1 / -1;
    border-top: 1px solid #dfe4ea;
    border-left: 0;
  }
  .editor-dialog-title {
    align-items: flex-start;
    flex-direction: column;
    gap: 10px;
  }
}
</style>
