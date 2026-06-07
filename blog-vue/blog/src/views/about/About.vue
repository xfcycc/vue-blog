<template>
  <div class="about-page" :style="pageStyle">
    <section class="about-hero" :style="heroStyle">
      <div class="about-hero-mask" />
      <div class="about-hero-content">
        <h1>{{ aboutConfig.profileTitle }}</h1>
        <p>{{ aboutConfig.profileSubtitle }}</p>
      </div>
    </section>

    <main class="about-container">
      <section class="about-profile about-animate" :class="{ mounted }">
        <div class="about-avatar-wrap">
          <v-avatar size="132" class="about-avatar">
            <img :src="avatar" />
          </v-avatar>
        </div>
        <div class="about-intro">
          <h2>{{ aboutConfig.displayName }}</h2>
          <p class="about-quote">
            <v-icon size="18" color="#f59e0b">mdi-lightning-bolt</v-icon>
            {{ aboutConfig.quote }}
            <v-icon size="18" color="#f59e0b">mdi-lightning-bolt</v-icon>
          </p>
        </div>
      </section>

      <section v-if="!isLegacyMode" class="about-grid">
        <div class="about-panel skill-panel about-animate" :class="{ mounted }">
          <div class="panel-heading">
            <div class="panel-icon skill-icon">
              <v-icon color="#fff">mdi-code-tags</v-icon>
            </div>
            <div>
              <h3>专业技能</h3>
            </div>
          </div>

          <div class="skill-list">
            <div
              v-for="(skill, index) in sortedSkills"
              :key="skill.name + index"
              class="skill-item"
              :class="{ mounted }"
              :style="{ '--skill-delay': getSkillDelay(index) + 'ms' }"
            >
              <div class="skill-meta">
                <div>
                  <span :class="['skill-grade', getGrade(skill.level).text]">
                    {{ getGrade(skill.level).grade }}
                  </span>
                  <strong>{{ skill.name }}</strong>
                  <small>{{ skill.category }}</small>
                </div>
              </div>
              <div class="skill-track">
                <span
                  :class="['skill-bar', getGrade(skill.level).bar]"
                  :style="{ width: normalizedLevel(skill.level) + '%' }"
                />
              </div>
            </div>
          </div>
        </div>

        <div class="about-panel paper-panel about-animate" :class="{ mounted }">
          <div class="panel-heading">
            <div class="panel-icon paper-icon">
              <v-icon color="#fff">mdi-book-open-page-variant</v-icon>
            </div>
            <div>
              <h3>学术成果</h3>
            </div>
          </div>

          <div class="paper-list">
            <article
              v-for="(paper, index) in aboutConfig.papers"
              :key="paper.title + index"
              class="paper-item"
              :class="{ mounted }"
              :style="{ '--paper-delay': getPaperDelay(index) + 'ms' }"
            >
              <h4>{{ paper.title }}</h4>
              <div class="paper-tags">
                <span>{{ paper.venue }}</span>
                <span v-if="paper.type" class="blue-tag">{{ paper.type }}</span>
                <span v-if="paper.award" class="award-tag">
                  <v-icon size="15" color="#d97706">mdi-trophy-award</v-icon>
                  {{ paper.award }}
                </span>
              </div>
              <p class="paper-authors">Authors: {{ paper.authors }}</p>
              <div class="paper-abstract">
                <b>Abstract</b>
                {{ paper.abstract }}
              </div>
              <a
                v-if="paper.link"
                class="paper-link"
                :href="paper.link"
                target="_blank"
                rel="noopener noreferrer"
              >
                View Details
                <v-icon size="16" color="currentColor">mdi-chevron-right</v-icon>
              </a>
            </article>
          </div>

          <div class="training-tip">
            <v-icon size="18" color="#facc15">mdi-star-four-points</v-icon>
            <span>持续修炼中...</span>
            <v-icon size="18" color="#facc15">mdi-star-four-points</v-icon>
          </div>
        </div>
      </section>

      <v-card v-if="renderedMarkdown" class="about-markdown blog-container">
        <div
          ref="about"
          class="about-content markdown-body"
          v-html="renderedMarkdown"
        />
      </v-card>
    </main>
  </div>
</template>

<script>
import Clipboard from "clipboard";
import markdownToHtml from "../../utils/markdown";

const generatedAboutCover = "https://pic.caiguoyu.cn/20260519195330292.png";

const defaultAboutConfig = {
  backgroundImage: "",
  avatar: "",
  displayName: "全栈开发者",
  profileTitle: "关于我",
  profileSubtitle: "Full-stack developer / Researcher / Lifelong builder",
  quote: '"Talk is cheap. Show me the code."',
  skills: [
    { name: "JavaScript / TypeScript", level: 95, category: "语言" },
    { name: "React / Vue.js", level: 92, category: "前端框架" },
    { name: "Python / PyTorch", level: 85, category: "AI & 后端" },
    { name: "C++ / 算法", level: 80, category: "语言" },
    { name: "Node.js / Express", level: 78, category: "后端框架" },
    { name: "Docker / K8s", level: 65, category: "DevOps" },
    { name: "Rust / WebAssembly", level: 50, category: "探索中" }
  ],
  papers: [
    {
      title: "Optimizing State Management in Large-scale Web Applications",
      authors: "Your Name, Alex Johnson, et al.",
      venue: "WWW 2025",
      type: "Oral",
      award: "Best Paper Award",
      abstract:
        "本文提出了一种针对现代前端框架中大规模状态管理的性能瓶颈与优化方案，通过依赖图裁剪技术提升了渲染效率。",
      link: "#"
    },
    {
      title: "A Novel Approach to Quantum Error Correction in Neural Networks",
      authors: "Your Name, Jane Smith",
      venue: "ICML 2024",
      type: "Poster",
      award: "",
      abstract:
        "探讨了在量子神经网络中引入经典纠错算法的可行性，实验证明该方案在特定噪声环境下能提升模型鲁棒性。",
      link: "#"
    }
  ],
  additionalContent: ""
};

function cloneDefaultConfig() {
  return JSON.parse(JSON.stringify(defaultAboutConfig));
}

export default {
  created() {
    this.getAboutContent();
  },
  mounted() {
    this.mounted = true;
  },
  destroyed() {
    if (this.clipboard) {
      this.clipboard.destroy();
    }
  },
  data: function() {
    return {
      mounted: false,
      aboutConfig: cloneDefaultConfig(),
      renderedMarkdown: "",
      isLegacyMode: false,
      clipboard: null,
      imgList: []
    };
  },
  methods: {
    getAboutContent() {
      const that = this;
      this.axios.get("/api/about").then(({ data }) => {
        this.applyAboutContent(data.data || "");
        this.$nextTick(() => {
          if (!this.$refs.about) {
            return;
          }
          if (this.clipboard) {
            this.clipboard.destroy();
          }
          this.clipboard = new Clipboard(".copy-btn");
          this.clipboard.on("success", () => {
            this.$toast({ type: "success", message: "复制成功" });
          });
          this.imgList = [];
          const imgList = this.$refs.about.getElementsByTagName("img");
          for (var i = 0; i < imgList.length; i++) {
            this.imgList.push(imgList[i].src);
            imgList[i].addEventListener("click", function(e) {
              that.previewImg(e.target.currentSrc);
            });
          }
        });
      });
    },
    applyAboutContent(content) {
      const parsedConfig = this.parseAboutConfig(content);
      if (parsedConfig) {
        this.isLegacyMode = false;
        this.aboutConfig = parsedConfig;
        this.renderedMarkdown = parsedConfig.additionalContent
          ? markdownToHtml(parsedConfig.additionalContent)
          : "";
        return;
      }
      this.isLegacyMode = true;
      this.aboutConfig = cloneDefaultConfig();
      this.renderedMarkdown = markdownToHtml(content);
    },
    parseAboutConfig(content) {
      if (!content) {
        return cloneDefaultConfig();
      }
      try {
        const parsed = JSON.parse(content);
        if (!parsed || typeof parsed !== "object") {
          return null;
        }
        const config = Object.assign(cloneDefaultConfig(), parsed);
        if (!Array.isArray(config.skills) || config.skills.length === 0) {
          config.skills = cloneDefaultConfig().skills;
        }
        if (!Array.isArray(config.papers) || config.papers.length === 0) {
          config.papers = cloneDefaultConfig().papers;
        }
        return config;
      } catch (e) {
        return null;
      }
    },
    normalizedLevel(level) {
      const value = Number(level);
      if (Number.isNaN(value)) {
        return 0;
      }
      return Math.min(Math.max(value, 0), 100);
    },
    getGrade(level) {
      const value = this.normalizedLevel(level);
      if (value >= 90) {
        return { grade: "S", text: "grade-s", bar: "bar-s" };
      }
      if (value >= 80) {
        return { grade: "A", text: "grade-a", bar: "bar-a" };
      }
      if (value >= 60) {
        return { grade: "B", text: "grade-b", bar: "bar-b" };
      }
      return { grade: "C", text: "grade-c", bar: "bar-c" };
    },
    getSkillDelay(index) {
      const lastIndex = Math.max(this.sortedSkills.length - 1, 1);
      return Math.round((1080 / lastIndex) * index);
    },
    getPaperDelay(index) {
      return 1600 + index * 160;
    },
    previewImg(img) {
      this.$imagePreview({
        images: this.imgList,
        index: this.imgList.indexOf(img)
      });
    }
  },
  computed: {
    avatar() {
      return (
        this.aboutConfig.avatar ||
        this.$store.state.blogInfo.websiteConfig.websiteAvatar
      );
    },
    fallbackCover() {
      var cover = "";
      this.$store.state.blogInfo.pageList.forEach(item => {
        if (item.pageLabel == "about") {
          cover = item.pageCover;
        }
      });
      return cover || generatedAboutCover;
    },
    pageStyle() {
      const cover = this.aboutConfig.backgroundImage || generatedAboutCover;
      return {
        backgroundImage:
          "linear-gradient(180deg, rgba(255,247,251,0.2), rgba(248,244,255,0.36)), url(" +
          cover +
          ")"
      };
    },
    heroStyle() {
      return {
        backgroundImage:
          "linear-gradient(180deg, rgba(15,23,42,0.2), rgba(15,23,42,0.06) 46%, rgba(255,247,251,0.4))"
      };
    },
    sortedSkills() {
      return this.aboutConfig.skills
        .slice()
        .sort((a, b) => this.normalizedLevel(b.level) - this.normalizedLevel(a.level));
    }
  }
};
</script>

<style scoped>
.about-page {
  min-height: 100vh;
  padding-bottom: 48px;
  background-color: #f8f4ff;
  background-attachment: fixed;
  background-position: center top;
  background-repeat: no-repeat;
  background-size: cover;
  color: #1f2937;
}
.about-hero {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 420px;
  overflow: hidden;
  background-position: center;
  background-size: cover;
}
.about-hero-mask {
  position: absolute;
  inset: 0;
  background:
    linear-gradient(180deg, rgba(15, 23, 42, 0.26), transparent 42%),
    radial-gradient(circle at 50% 48%, rgba(255, 255, 255, 0.36), transparent 34%);
}
.about-hero-content {
  position: relative;
  z-index: 1;
  width: min(100% - 32px, 960px);
  padding-bottom: 78px;
  text-align: center;
  color: #fff;
  text-shadow: 0 12px 34px rgba(15, 23, 42, 0.54);
}
.about-hero h1 {
  margin: 0;
  color: #fff;
  font-size: 58px;
  font-weight: 900;
  line-height: 1;
  text-shadow:
    0 2px 0 rgba(255, 255, 255, 0.22),
    0 18px 44px rgba(76, 29, 149, 0.38);
}
.about-hero p {
  max-width: 640px;
  margin: 18px auto 0;
  color: rgba(255, 255, 255, 0.92);
  font-size: 17px;
  font-weight: 700;
  line-height: 1.8;
}
.about-container {
  position: relative;
  z-index: 2;
  width: min(100% - 32px, 1120px);
  margin: -82px auto 0;
}
.about-profile,
.about-panel,
.about-markdown {
  border: 1px solid rgba(255, 255, 255, 0.62);
  background: rgba(255, 255, 255, 0.56);
  box-shadow: 0 18px 54px rgba(31, 41, 55, 0.12);
  backdrop-filter: blur(12px);
}
.about-profile {
  padding: 34px;
  border-radius: 24px;
  text-align: center;
}
.about-avatar-wrap {
  display: flex;
  justify-content: center;
  margin-top: -102px;
}
.about-avatar {
  border: 5px solid #fff;
  box-shadow: 0 18px 38px rgba(99, 102, 241, 0.22);
  transition: transform 0.35s;
}
.about-avatar:hover {
  transform: translateY(-4px) rotate(4deg);
}
.about-intro h2 {
  margin: 24px 0 0;
  color: #111827;
  font-size: 32px;
  font-weight: 900;
}
.about-quote {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin: 10px 0 0;
  color: #6b7280;
  font-size: 15px;
  font-weight: 700;
}
.about-grid {
  display: grid;
  grid-template-columns: minmax(0, 5fr) minmax(0, 7fr);
  gap: 28px;
  margin-top: 28px;
}
.about-panel {
  min-width: 0;
  padding: 30px;
  border-radius: 22px;
}
.panel-heading {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 26px;
}
.panel-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 52px;
  height: 52px;
  border-radius: 12px;
  box-shadow: 0 14px 28px rgba(99, 102, 241, 0.25);
}
.skill-icon {
  background: linear-gradient(135deg, #7c3aed, #2563eb);
}
.paper-icon {
  background: linear-gradient(135deg, #ec4899, #fb7185);
}
.panel-heading h3 {
  margin: 0;
  color: #111827;
  font-size: 25px;
  font-weight: 900;
}
.panel-heading p {
  margin: 4px 0 0;
  color: #8b95a5;
  font-size: 13px;
}
.skill-list {
  display: grid;
  gap: 22px;
}
.skill-item {
  opacity: 1;
}
.skill-item.mounted {
  animation: none;
}
.skill-item.mounted:hover {
  transform: translateX(4px);
}
.skill-meta {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 9px;
}
.skill-meta div {
  display: flex;
  align-items: baseline;
  min-width: 0;
  gap: 8px;
}
.skill-grade {
  font-size: 22px;
  font-style: italic;
  font-weight: 900;
  line-height: 1;
}
.grade-s {
  color: #d97706;
}
.grade-a {
  color: #7c3aed;
}
.grade-b {
  color: #2563eb;
}
.grade-c {
  color: #059669;
}
.skill-meta strong {
  overflow: hidden;
  color: #374151;
  font-size: 15px;
  font-weight: 900;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.skill-meta small {
  flex: 0 0 auto;
  color: #9ca3af;
  font-size: 12px;
}
.skill-meta em {
  flex: 0 0 auto;
  color: #9ca3af;
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 13px;
  font-style: normal;
}
.skill-track {
  position: relative;
  height: 12px;
  overflow: hidden;
  border-radius: 999px;
  background: #f3f4f6;
  box-shadow: inset 0 1px 3px rgba(15, 23, 42, 0.08);
}
.skill-bar {
  display: block;
  height: 100%;
  border-radius: 999px;
  transition: width 0.42s cubic-bezier(0.16, 1, 0.3, 1) var(--skill-delay);
}
.bar-s {
  background: linear-gradient(90deg, #f59e0b, #facc15);
}
.bar-a {
  background: linear-gradient(90deg, #7c3aed, #c084fc);
}
.bar-b {
  background: linear-gradient(90deg, #2563eb, #60a5fa);
}
.bar-c {
  background: linear-gradient(90deg, #059669, #34d399);
}
.paper-list {
  display: grid;
  gap: 18px;
}
.paper-item {
  position: relative;
  overflow: hidden;
  opacity: 1;
  padding: 22px;
  border: 1px solid #f1f5f9;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.74);
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.04);
  transition:
    transform 0.28s,
    box-shadow 0.28s,
    border-color 0.28s;
}
.paper-item.mounted {
  animation: none;
}
.paper-item::before {
  position: absolute;
  top: 0;
  bottom: 0;
  left: 0;
  width: 5px;
  background: linear-gradient(180deg, #f472b6, #8b5cf6);
  content: "";
  transform: scaleY(0);
  transform-origin: top;
  transition: transform 0.28s;
}
.paper-item.mounted:hover {
  border-color: #fbcfe8;
  box-shadow: 0 18px 36px rgba(236, 72, 153, 0.12);
  transform: translateY(-3px);
}
.paper-item.mounted:hover::before {
  transform: scaleY(1);
}
.paper-item h4 {
  margin: 0 0 12px;
  color: #1f2937;
  font-size: 18px;
  font-weight: 900;
  line-height: 1.45;
}
.paper-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}
.paper-tags span {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 5px 10px;
  border: 1px solid #e5e7eb;
  border-radius: 7px;
  background: #f8fafc;
  color: #64748b;
  font-size: 13px;
  font-weight: 800;
}
.paper-tags .blue-tag {
  border-color: #bfdbfe;
  background: #eff6ff;
  color: #2563eb;
}
.paper-tags .award-tag {
  border-color: #fde68a;
  background: #fffbeb;
  color: #b45309;
}
.paper-authors {
  margin: 0 0 14px;
  color: #6b7280;
  font-size: 13px;
  font-style: italic;
}
.paper-abstract {
  position: relative;
  padding: 17px 15px 13px;
  border: 1px solid #f1f5f9;
  border-radius: 10px;
  background: rgba(248, 250, 252, 0.68);
  color: #4b5563;
  font-size: 14px;
  line-height: 1.8;
}
.paper-abstract b {
  position: absolute;
  top: -11px;
  left: 14px;
  padding: 0 8px;
  border: 1px solid #f3e8ff;
  border-radius: 999px;
  background: #fff;
  color: #a855f7;
  font-size: 12px;
}
.paper-link {
  display: inline-flex;
  align-items: center;
  float: right;
  gap: 2px;
  margin-top: 14px;
  color: #8b5cf6 !important;
  font-size: 14px;
  font-weight: 900;
  text-decoration: none;
}
.paper-link:hover {
  color: #ec4899 !important;
}
.training-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 26px;
  color: #94a3b8;
  font-size: 14px;
  font-weight: 700;
}
.about-markdown {
  margin-top: 28px !important;
  border-radius: 18px !important;
}
.about-content {
  word-break: break-word;
  line-height: 1.8;
}
.about-animate {
  opacity: 1;
  transform: none;
  transition: none;
}
.about-animate.mounted {
  opacity: 1;
  transform: none;
}
.paper-panel.about-animate {
  opacity: 1;
  transform: none;
}
.paper-panel.about-animate.mounted {
  animation: none;
}
@media (max-width: 960px) {
  .about-hero {
    min-height: 360px;
  }
  .about-hero h1 {
    font-size: 40px;
  }
  .about-container {
    width: min(100% - 24px, 720px);
  }
  .about-grid {
    grid-template-columns: 1fr;
  }
}
@media (max-width: 600px) {
  .about-hero {
    min-height: 320px;
  }
  .about-hero h1 {
    font-size: 34px;
  }
  .about-hero p {
    font-size: 15px;
  }
  .about-profile,
  .about-panel {
    padding: 22px 18px;
  }
  .about-avatar-wrap {
    margin-top: -82px;
  }
  .about-intro h2 {
    font-size: 26px;
  }
  .about-quote {
    flex-wrap: wrap;
  }
  .skill-meta,
  .skill-meta div {
    align-items: flex-start;
  }
  .skill-meta div {
    flex-direction: column;
    gap: 4px;
  }
  .skill-meta strong {
    white-space: normal;
  }
}
</style>

<style lang="scss">
pre.hljs {
  padding: 14px 2px 14px 42px !important;
  border-radius: 5px !important;
  position: relative;
  background: #f6f8fa !important;
  border: 1px solid #e5e7eb !important;
  color: #24292f !important;
  font-size: 14px !important;
  font-family: ui-monospace, SFMono-Regular, "SF Mono", Menlo, Consolas,
    "Liberation Mono", monospace !important;
  line-height: 24px !important;
  overflow: hidden !important;
  &:hover .copy-btn {
    display: flex;
    justify-content: center;
    align-items: center;
  }
  code {
    background: transparent !important;
    display: block !important;
    margin: 0 10px !important;
    overflow-x: auto !important;
    span {
      background: transparent !important;
    }
    &::-webkit-scrollbar {
      z-index: 11;
      width: 6px;
    }
    &::-webkit-scrollbar:horizontal {
      height: 6px;
    }
    &::-webkit-scrollbar-thumb {
      border-radius: 5px;
      width: 6px;
      border: 1px solid transparent;
      background: linear-gradient(
          135deg,
          rgba(255, 255, 255, 0.68),
          rgba(168, 85, 247, 0.38) 42%,
          rgba(124, 58, 237, 0.5)
        )
        padding-box;
      box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.72);
    }
    &::-webkit-scrollbar-corner,
    &::-webkit-scrollbar-track {
      background: transparent;
    }
    &::-webkit-scrollbar-track-piece {
      background: transparent;
      width: 6px;
    }
  }
  .line-numbers-rows {
    position: absolute;
    pointer-events: none;
    top: 12px;
    bottom: 14px;
    left: 0;
    font-size: 100%;
    width: 42px;
    text-align: center;
    letter-spacing: -1px;
    border-right: 1px solid #d0d7de;
    user-select: none;
    counter-reset: linenumber;
    span {
      pointer-events: none;
      display: block;
      counter-increment: linenumber;
      &:before {
        content: counter(linenumber);
        color: #8c959f;
        display: block;
        text-align: center;
      }
    }
  }
  b.name {
    position: absolute;
    top: 8px;
    right: 45px;
    z-index: 1;
    font-size: 12px;
    color: #57606a;
    pointer-events: none;
  }
  .copy-btn {
    position: absolute;
    top: 6px;
    right: 6px;
    z-index: 1;
    color: #57606a;
    background-color: #eaeef2;
    border-radius: 6px;
    display: none;
    font-size: 14px;
    width: 32px;
    height: 24px;
    outline: none;
  }
}
</style>
