<template>
  <div v-if="normalizedItems.length" ref="grid" class="screenshot-gallery">
    <button
      v-for="(item, index) in normalizedItems"
      :key="item.displayUrl + index"
      type="button"
      class="screenshot-item"
      :class="'frame-' + frameType(item).toLowerCase()"
      :style="itemStyle(item, index)"
      :aria-label="'放大查看' + imageAlt(index)"
      @click="preview(index)"
    >
      <img
        :src="item.displayUrl"
        :alt="imageAlt(index)"
        loading="lazy"
        referrerpolicy="no-referrer"
        @load="imageLoaded($event, index)"
      />
    </button>
  </div>
</template>

<script>
export default {
  name: "GameScreenshotGallery",
  props: {
    items: {
      type: Array,
      default: () => []
    },
    images: {
      type: Array,
      default: () => []
    },
    gameName: {
      type: String,
      default: "游戏"
    }
  },
  data() {
    return {
      gridWidth: 0,
      imageRatios: {},
      resizeObserver: null,
      isMobile: false
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
    }
  },
  mounted() {
    this.measureGrid();
    if (window.ResizeObserver) {
      this.resizeObserver = new window.ResizeObserver(this.measureGrid);
      this.resizeObserver.observe(this.$refs.grid);
    }
    window.addEventListener("resize", this.measureGrid);
  },
  beforeDestroy() {
    if (this.resizeObserver) this.resizeObserver.disconnect();
    window.removeEventListener("resize", this.measureGrid);
  },
  methods: {
    measureGrid() {
      if (this.$refs.grid) this.gridWidth = this.$refs.grid.clientWidth;
      this.isMobile = window.innerWidth <= 760;
    },
    frameType(item) {
      return ["AUTO", "LANDSCAPE", "PORTRAIT", "SQUARE"].includes(
        item.frameType
      )
        ? item.frameType
        : "AUTO";
    },
    columnSpan(item) {
      if (this.isMobile) return 12;
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
      const gap = this.isMobile ? 12 : 16;
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
    },
    preview(index) {
      this.$imagePreview({ images: this.previewImages, index });
    },
    imageAlt(index) {
      return this.gameName + " 游戏截图 " + (index + 1);
    }
  }
};
</script>

<style scoped>
.screenshot-gallery {
  display: grid;
  width: 100%;
  grid-template-columns: repeat(12, minmax(0, 1fr));
  grid-auto-flow: dense;
  grid-auto-rows: 8px;
  gap: 16px;
}
.screenshot-item {
  position: relative;
  min-width: 0;
  overflow: hidden;
  padding: 0;
  border: 1px solid rgba(220, 230, 231, 0.9);
  border-radius: 14px;
  background: #edf2f2;
  box-shadow: 0 16px 38px rgba(15, 23, 42, 0.09);
  cursor: zoom-in;
  transition: box-shadow 0.22s ease, transform 0.22s ease;
}
.screenshot-item:hover {
  box-shadow: 0 20px 46px rgba(15, 23, 42, 0.14);
  transform: translateY(-2px);
}
.screenshot-item:focus-visible {
  outline: 3px solid rgba(15, 124, 117, 0.28);
  outline-offset: 3px;
}
.screenshot-item img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.frame-auto img {
  object-fit: contain;
  background: #fff;
}
@media (prefers-reduced-motion: reduce) {
  .screenshot-item {
    transition: none;
  }
}
@media (max-width: 760px) {
  .screenshot-gallery {
    gap: 12px;
  }
  .screenshot-item {
    grid-column-end: span 12 !important;
    border-radius: 12px;
  }
}
</style>
