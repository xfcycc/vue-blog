<template>
  <div
    v-if="images.length"
    class="screenshot-gallery"
    :class="'layout-' + safeLayout.toLowerCase()"
  >
    <template v-if="safeLayout === 'CAROUSEL'">
      <div
        class="carousel-frame media-frame"
        role="button"
        tabindex="0"
        :aria-label="'放大查看' + imageAlt(activeIndex)"
        @click="preview(activeIndex)"
        @keyup.enter="preview(activeIndex)"
      >
        <img class="frame-backdrop" :src="images[activeIndex]" alt="" />
        <img
          class="frame-image"
          :src="images[activeIndex]"
          :alt="imageAlt(activeIndex)"
          referrerpolicy="no-referrer"
        />
        <button
          v-if="images.length > 1"
          type="button"
          class="carousel-button previous"
          aria-label="上一张截图"
          @click.stop="changeActive(-1)"
        >
          <v-icon size="22" color="currentColor">mdi-chevron-left</v-icon>
        </button>
        <button
          v-if="images.length > 1"
          type="button"
          class="carousel-button next"
          aria-label="下一张截图"
          @click.stop="changeActive(1)"
        >
          <v-icon size="22" color="currentColor">mdi-chevron-right</v-icon>
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
          :aria-label="'查看' + imageAlt(index)"
          @click="activeIndex = index"
        >
          <img
            :src="image"
            :alt="imageAlt(index)"
            referrerpolicy="no-referrer"
          />
        </button>
      </div>
    </template>

    <div v-else class="frame-grid">
      <div
        v-for="(image, index) in images"
        :key="image + index"
        class="media-frame grid-frame"
        :class="{ featured: safeLayout === 'FEATURED' && index === 0 }"
        role="button"
        tabindex="0"
        :aria-label="'放大查看' + imageAlt(index)"
        @click="preview(index)"
        @keyup.enter="preview(index)"
      >
        <img class="frame-backdrop" :src="image" alt="" />
        <img
          class="frame-image"
          :src="image"
          :alt="imageAlt(index)"
          loading="lazy"
          referrerpolicy="no-referrer"
        />
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "GameScreenshotGallery",
  props: {
    images: {
      type: Array,
      default: () => []
    },
    layout: {
      type: String,
      default: "CAROUSEL"
    },
    gameName: {
      type: String,
      default: "游戏"
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
    },
    preview(index) {
      this.$imagePreview({ images: this.images, index });
    },
    imageAlt(index) {
      return this.gameName + " 游戏截图 " + (index + 1);
    }
  }
};
</script>

<style scoped>
.screenshot-gallery {
  width: 100%;
}
.media-frame {
  position: relative;
  overflow: hidden;
  border: 1px solid rgba(220, 230, 231, 0.9);
  border-radius: 14px;
  background: #101923;
  box-shadow: 0 16px 42px rgba(15, 23, 42, 0.09);
  cursor: zoom-in;
}
.frame-backdrop {
  position: absolute;
  inset: -34px;
  width: calc(100% + 68px);
  height: calc(100% + 68px);
  opacity: 0.26;
  filter: blur(26px) saturate(0.9);
  object-fit: cover;
  transform: scale(1.06);
}
.frame-image {
  position: relative;
  z-index: 1;
  display: block;
  width: 100%;
  height: 100%;
  object-fit: contain;
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
  width: 44px;
  height: 44px;
  align-items: center;
  justify-content: center;
  border: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.2);
  color: #15232f;
  cursor: pointer;
  transform: translateY(-50%);
  transition: background 0.2s ease, transform 0.2s ease;
}
.carousel-button:hover {
  background: #fff;
  transform: translateY(-50%) scale(1.05);
}
.carousel-button.previous {
  left: 18px;
}
.carousel-button.next {
  right: 18px;
}
.carousel-count {
  position: absolute;
  z-index: 3;
  right: 16px;
  bottom: 15px;
  min-width: 52px;
  padding: 6px 10px;
  border-radius: 14px;
  background: rgba(15, 23, 42, 0.76);
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  line-height: 1;
  text-align: center;
}
.thumbnail-list {
  display: grid;
  margin-top: 12px;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 10px;
}
.thumbnail-item {
  overflow: hidden;
  padding: 0;
  aspect-ratio: 16 / 9;
  border: 2px solid transparent;
  border-radius: 8px;
  background: #e7edef;
  cursor: pointer;
  opacity: 0.72;
  transition: border-color 0.2s ease, opacity 0.2s ease, transform 0.2s ease;
}
.thumbnail-item:hover,
.thumbnail-item.active {
  opacity: 1;
  transform: translateY(-1px);
}
.thumbnail-item.active {
  border-color: #0f7c75;
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
  gap: 14px;
}
.grid-frame {
  aspect-ratio: 16 / 10;
}
.layout-featured .grid-frame.featured {
  aspect-ratio: 16 / 9;
  grid-column: 1 / -1;
}
@media (prefers-reduced-motion: reduce) {
  .carousel-button,
  .thumbnail-item {
    transition: none;
  }
}
@media (max-width: 760px) {
  .carousel-frame {
    aspect-ratio: 4 / 3;
  }
  .thumbnail-list {
    overflow-x: auto;
    grid-auto-columns: 112px;
    grid-template-columns: none;
    grid-auto-flow: column;
    padding-bottom: 4px;
  }
  .frame-grid {
    grid-template-columns: 1fr;
  }
  .layout-featured .grid-frame.featured {
    aspect-ratio: 4 / 3;
    grid-column: auto;
  }
}
@media (max-width: 560px) {
  .media-frame {
    border-radius: 12px;
  }
  .carousel-button {
    width: 36px;
    height: 36px;
  }
  .carousel-button.previous {
    left: 10px;
  }
  .carousel-button.next {
    right: 10px;
  }
  .carousel-count {
    right: 10px;
    bottom: 10px;
  }
  .grid-frame {
    aspect-ratio: 4 / 3;
  }
}
</style>
