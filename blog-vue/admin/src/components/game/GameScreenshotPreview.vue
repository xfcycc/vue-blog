<template>
  <div
    v-if="normalizedItems.length"
    ref="grid"
    class="screenshot-masonry"
    :class="{ 'is-mobile': previewMode === 'mobile' }"
  >
    <div
      v-for="(item, index) in normalizedItems"
      :key="item.displayUrl + index"
      class="masonry-item"
      :class="'frame-' + frameType(item).toLowerCase()"
      :style="itemStyle(item, index)"
    >
      <el-image
        class="masonry-image"
        :fit="frameType(item) === 'AUTO' ? 'contain' : 'cover'"
        :src="item.displayUrl"
        :preview-src-list="previewImages"
        :initial-index="index"
        @load="imageLoaded($event, index)"
      />
      <span class="image-order">{{ index + 1 }}</span>
    </div>
  </div>
</template>

<script>
export default {
  name: "GameScreenshotPreview",
  props: {
    items: {
      type: Array,
      default: () => []
    },
    images: {
      type: Array,
      default: () => []
    },
    previewMode: {
      type: String,
      default: "desktop"
    }
  },
  data() {
    return {
      gridWidth: 0,
      imageRatios: {},
      resizeObserver: null
    };
  },
  computed: {
    normalizedItems() {
      if (this.items.length) return this.items;
      return this.images.map((url, index) => ({
        displayUrl: url,
        frameType: "AUTO",
        columnSpan: 6,
        sortOrder: index + 1
      }));
    },
    previewImages() {
      return this.normalizedItems.map(item => item.displayUrl);
    }
  },
  watch: {
    normalizedItems: {
      deep: true,
      handler() {
        this.$nextTick(this.measureGrid);
      }
    },
    previewMode() {
      this.$nextTick(this.measureGrid);
    }
  },
  mounted() {
    this.measureGrid();
    if (window.ResizeObserver) {
      this.resizeObserver = new window.ResizeObserver(this.measureGrid);
      this.resizeObserver.observe(this.$refs.grid);
    } else {
      window.addEventListener("resize", this.measureGrid);
    }
  },
  beforeDestroy() {
    if (this.resizeObserver) this.resizeObserver.disconnect();
    window.removeEventListener("resize", this.measureGrid);
  },
  methods: {
    measureGrid() {
      if (this.$refs.grid) this.gridWidth = this.$refs.grid.clientWidth;
    },
    frameType(item) {
      return ["AUTO", "LANDSCAPE", "PORTRAIT", "SQUARE"].includes(
        item.frameType
      )
        ? item.frameType
        : "AUTO";
    },
    columnSpan(item) {
      if (this.previewMode === "mobile") return 12;
      return [4, 6, 8, 12].includes(Number(item.columnSpan))
        ? Number(item.columnSpan)
        : 6;
    },
    aspectRatio(item, index) {
      const frame = this.frameType(item);
      if (frame === "LANDSCAPE") return 16 / 9;
      if (frame === "PORTRAIT") return 9 / 16;
      if (frame === "SQUARE") return 1;
      if (item.displayWidth && item.displayHeight) {
        return item.displayWidth / item.displayHeight;
      }
      return this.imageRatios[index] || 16 / 9;
    },
    itemStyle(item, index) {
      const gap = this.previewMode === "mobile" ? 12 : 16;
      const rowHeight = 8;
      const span = this.columnSpan(item);
      const columnWidth = Math.max(0, (this.gridWidth - gap * 11) / 12);
      const width = columnWidth * span + gap * (span - 1);
      const height = width / this.aspectRatio(item, index);
      const rowSpan = Math.max(
        1,
        Math.ceil((height + gap) / (rowHeight + gap))
      );
      return {
        gridColumnEnd: "span " + span,
        gridRowEnd: "span " + rowSpan
      };
    },
    imageLoaded(event, index) {
      const image = event && event.target ? event.target : null;
      if (image && image.naturalWidth && image.naturalHeight) {
        this.$set(
          this.imageRatios,
          index,
          image.naturalWidth / image.naturalHeight
        );
      }
    }
  }
};
</script>

<style scoped>
.screenshot-masonry {
  display: grid;
  width: 100%;
  grid-template-columns: repeat(12, minmax(0, 1fr));
  grid-auto-flow: dense;
  grid-auto-rows: 8px;
  gap: 16px;
}
.screenshot-masonry.is-mobile {
  max-width: 390px;
  margin: 0 auto;
  gap: 12px;
}
.masonry-item {
  position: relative;
  min-width: 0;
  overflow: hidden;
  border: 1px solid #e4e7ed;
  border-radius: 10px;
  background: #eef1f5;
  box-shadow: 0 8px 24px rgba(31, 45, 61, 0.08);
}
.masonry-image {
  display: block;
  width: 100%;
  height: 100%;
}
.frame-auto .masonry-image {
  background: #fff;
}
.image-order {
  position: absolute;
  z-index: 2;
  top: 8px;
  left: 8px;
  display: inline-flex;
  width: 26px;
  height: 26px;
  align-items: center;
  justify-content: center;
  border-radius: 7px;
  background: rgba(31, 45, 61, 0.72);
  color: #fff;
  font-size: 12px;
  font-weight: 700;
}
</style>
