<template>
  <div
    v-if="images.length"
    class="screenshot-preview"
    :class="'layout-' + safeLayout.toLowerCase()"
  >
    <template v-if="safeLayout === 'CAROUSEL'">
      <div class="carousel-frame media-frame">
        <img class="frame-backdrop" :src="images[activeIndex]" alt="" />
        <el-image
          class="frame-image"
          fit="contain"
          :src="images[activeIndex]"
          :preview-src-list="images"
          :initial-index="activeIndex"
        />
        <button
          v-if="images.length > 1"
          type="button"
          class="carousel-button previous"
          aria-label="上一张"
          @click="changeActive(-1)"
        >
          <i class="el-icon-arrow-left" />
        </button>
        <button
          v-if="images.length > 1"
          type="button"
          class="carousel-button next"
          aria-label="下一张"
          @click="changeActive(1)"
        >
          <i class="el-icon-arrow-right" />
        </button>
        <span class="carousel-count">
          {{ activeIndex + 1 }} / {{ images.length }}
        </span>
      </div>
      <div v-if="images.length > 1" class="thumbnail-list">
        <button
          v-for="(image, index) in images"
          :key="image + index"
          type="button"
          class="thumbnail-item"
          :class="{ active: index === activeIndex }"
          @click="activeIndex = index"
        >
          <img :src="image" :alt="'截图 ' + (index + 1)" />
        </button>
      </div>
    </template>

    <div v-else class="frame-grid">
      <div
        v-for="(image, index) in images"
        :key="image + index"
        class="media-frame grid-frame"
        :class="{ featured: safeLayout === 'FEATURED' && index === 0 }"
      >
        <img class="frame-backdrop" :src="image" alt="" />
        <el-image
          class="frame-image"
          fit="contain"
          :src="image"
          :preview-src-list="images"
          :initial-index="index"
        />
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "GameScreenshotPreview",
  props: {
    images: {
      type: Array,
      default: () => []
    },
    layout: {
      type: String,
      default: "CAROUSEL"
    }
  },
  data() {
    return {
      activeIndex: 0
    };
  },
  computed: {
    safeLayout() {
      return ["CAROUSEL", "FEATURED", "GRID"].includes(this.layout)
        ? this.layout
        : "CAROUSEL";
    }
  },
  watch: {
    images() {
      if (this.activeIndex >= this.images.length) this.activeIndex = 0;
    }
  },
  methods: {
    changeActive(step) {
      this.activeIndex =
        (this.activeIndex + step + this.images.length) % this.images.length;
    }
  }
};
</script>

<style scoped>
.screenshot-preview {
  width: 100%;
}
.media-frame {
  position: relative;
  overflow: hidden;
  border-radius: 10px;
  background: #111b25;
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.12);
}
.frame-backdrop {
  position: absolute;
  inset: -28px;
  width: calc(100% + 56px);
  height: calc(100% + 56px);
  opacity: 0.24;
  filter: blur(22px) saturate(0.85);
  object-fit: cover;
  transform: scale(1.06);
}
.frame-image {
  position: relative;
  z-index: 1;
  display: block;
  width: 100%;
  height: 100%;
}
.carousel-frame {
  width: 100%;
  aspect-ratio: 16 / 9;
}
.carousel-button {
  position: absolute;
  z-index: 3;
  top: 50%;
  display: flex;
  width: 38px;
  height: 38px;
  align-items: center;
  justify-content: center;
  border: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.2);
  color: #263442;
  cursor: pointer;
  transform: translateY(-50%);
}
.carousel-button.previous {
  left: 16px;
}
.carousel-button.next {
  right: 16px;
}
.carousel-count {
  position: absolute;
  z-index: 3;
  right: 14px;
  bottom: 13px;
  padding: 5px 9px;
  border-radius: 12px;
  background: rgba(15, 23, 42, 0.72);
  color: #fff;
  font-size: 12px;
  font-weight: 700;
}
.thumbnail-list {
  display: grid;
  margin-top: 10px;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 8px;
}
.thumbnail-item {
  overflow: hidden;
  padding: 0;
  aspect-ratio: 16 / 9;
  border: 2px solid transparent;
  border-radius: 7px;
  background: #e9eef2;
  cursor: pointer;
}
.thumbnail-item.active {
  border-color: #409eff;
}
.thumbnail-item img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.frame-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}
.grid-frame {
  aspect-ratio: 16 / 10;
}
.layout-featured .grid-frame.featured {
  aspect-ratio: 16 / 9;
  grid-column: 1 / -1;
}
@media (max-width: 760px) {
  .thumbnail-list {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
  .frame-grid {
    grid-template-columns: 1fr;
  }
  .layout-featured .grid-frame.featured {
    aspect-ratio: 16 / 10;
    grid-column: auto;
  }
}
</style>
