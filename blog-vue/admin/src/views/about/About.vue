<template>
  <el-card class="main-card about-setting-card">
    <div class="title">{{ this.$route.name }}</div>
    <el-tabs v-model="activeName" class="about-tabs">
      <el-tab-pane label="基础资料" name="profile">
        <el-form
          label-width="110px"
          label-position="left"
          :model="aboutForm"
          class="about-form"
        >
          <el-form-item label="页面背景">
            <div class="image-setting">
              <el-upload
                class="cover-uploader"
                action="/api/admin/config/images"
                :show-file-list="false"
                :on-success="handleBackgroundSuccess"
              >
                <img
                  v-if="aboutForm.backgroundImage"
                  :src="aboutForm.backgroundImage"
                  class="cover-preview"
                />
                <i v-else class="el-icon-plus cover-uploader-icon" />
              </el-upload>
              <el-input
                v-model="aboutForm.backgroundImage"
                size="small"
                placeholder="留空使用前台生成的默认背景图"
              />
            </div>
          </el-form-item>
          <el-form-item label="头像">
            <div class="image-setting">
              <el-upload
                class="avatar-uploader"
                action="/api/admin/config/images"
                :show-file-list="false"
                :on-success="handleAvatarSuccess"
              >
                <img
                  v-if="aboutForm.avatar"
                  :src="aboutForm.avatar"
                  class="avatar"
                />
                <i v-else class="el-icon-plus avatar-uploader-icon" />
              </el-upload>
              <el-input
                v-model="aboutForm.avatar"
                size="small"
                placeholder="留空使用网站配置里的用户头像"
              />
            </div>
          </el-form-item>
          <el-form-item label="页面标题">
            <el-input v-model="aboutForm.profileTitle" size="small" />
          </el-form-item>
          <el-form-item label="页面副标题">
            <el-input v-model="aboutForm.profileSubtitle" size="small" />
          </el-form-item>
          <el-form-item label="展示身份">
            <el-input v-model="aboutForm.displayName" size="small" />
          </el-form-item>
          <el-form-item label="引用语">
            <el-input v-model="aboutForm.quote" size="small" />
          </el-form-item>
          <el-form-item label="终端角色">
            <el-input
              v-model="terminalRolesText"
              size="small"
              placeholder="用英文逗号分隔，例如 Developer, Researcher"
            />
          </el-form-item>
          <el-form-item label="当前状态">
            <el-input v-model="aboutForm.terminalStatus" size="small" />
          </el-form-item>
          <el-button type="primary" size="medium" @click="updateAbout">
            保存设置
          </el-button>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="专业技能" name="skills">
        <div
          v-for="(skill, index) in aboutForm.skills"
          :key="'skill-' + index"
          class="repeat-row skill-row"
        >
          <el-input
            v-model="skill.name"
            size="small"
            placeholder="技能名称"
            class="skill-name-input"
          />
          <el-input
            v-model="skill.category"
            size="small"
            placeholder="分类"
            class="skill-category-input"
          />
          <el-input-number
            v-model="skill.level"
            size="small"
            :min="0"
            :max="100"
            controls-position="right"
          />
          <el-button
            type="danger"
            size="small"
            icon="el-icon-delete"
            circle
            @click="removeSkill(index)"
          />
        </div>
        <div class="action-row">
          <el-button size="small" icon="el-icon-plus" @click="addSkill">
            添加技能
          </el-button>
          <el-button type="primary" size="medium" @click="updateAbout">
            保存设置
          </el-button>
        </div>
      </el-tab-pane>

      <el-tab-pane label="学术成果" name="papers">
        <div
          v-for="(paper, index) in aboutForm.papers"
          :key="'paper-' + index"
          class="paper-editor"
        >
          <div class="paper-editor-head">
            <strong>成果 {{ index + 1 }}</strong>
            <el-button
              type="danger"
              size="small"
              icon="el-icon-delete"
              circle
              @click="removePaper(index)"
            />
          </div>
          <el-input v-model="paper.title" size="small" placeholder="论文标题" />
          <el-row :gutter="12">
            <el-col :md="8">
              <el-input
                v-model="paper.venue"
                size="small"
                placeholder="会议/期刊"
              />
            </el-col>
            <el-col :md="8">
              <el-input v-model="paper.type" size="small" placeholder="类型" />
            </el-col>
            <el-col :md="8">
              <el-input v-model="paper.award" size="small" placeholder="奖项" />
            </el-col>
          </el-row>
          <el-input v-model="paper.authors" size="small" placeholder="作者" />
          <el-input v-model="paper.link" size="small" placeholder="详情链接" />
          <el-input
            v-model="paper.abstract"
            type="textarea"
            :rows="4"
            placeholder="摘要"
          />
        </div>
        <div class="action-row">
          <el-button size="small" icon="el-icon-plus" @click="addPaper">
            添加成果
          </el-button>
          <el-button type="primary" size="medium" @click="updateAbout">
            保存设置
          </el-button>
        </div>
      </el-tab-pane>

      <el-tab-pane label="补充内容" name="content">
        <mavon-editor
          ref="md"
          @imgAdd="uploadImg"
          v-model="aboutForm.additionalContent"
          style="height:calc(100vh - 290px);margin-top:1rem"
        />
        <el-button
          type="primary"
          size="medium"
          class="edit-btn"
          @click="updateAbout"
        >
          保存设置
        </el-button>
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>

<script>
import * as imageConversion from "image-conversion";

const defaultAboutForm = {
  backgroundImage: "",
  avatar: "",
  displayName: "全栈开发者",
  profileTitle: "关于我",
  profileSubtitle: "Full-stack developer / Researcher / Lifelong builder",
  quote: '"Talk is cheap. Show me the code."',
  terminalRoles: ["Developer", "Researcher", "Otaku", "Night Owl"],
  terminalStatus: "Coding & Debugging...",
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

function cloneDefaultForm() {
  return JSON.parse(JSON.stringify(defaultAboutForm));
}

export default {
  created() {
    this.getAbout();
  },
  data: function() {
    return {
      activeName: "profile",
      aboutForm: cloneDefaultForm(),
      terminalRolesText: defaultAboutForm.terminalRoles.join(", ")
    };
  },
  methods: {
    getAbout() {
      this.axios.get("/api/about").then(({ data }) => {
        this.applyAbout(data.data || "");
      });
    },
    applyAbout(content) {
      const parsed = this.parseAboutConfig(content);
      if (parsed) {
        this.aboutForm = parsed;
      } else {
        this.aboutForm = cloneDefaultForm();
        this.aboutForm.additionalContent = content;
      }
      this.terminalRolesText = this.aboutForm.terminalRoles.join(", ");
    },
    parseAboutConfig(content) {
      if (!content) {
        return cloneDefaultForm();
      }
      try {
        const parsed = JSON.parse(content);
        if (!parsed || typeof parsed !== "object") {
          return null;
        }
        const config = Object.assign(cloneDefaultForm(), parsed);
        if (!Array.isArray(config.skills)) {
          config.skills = [];
        }
        if (!Array.isArray(config.papers)) {
          config.papers = [];
        }
        if (!Array.isArray(config.terminalRoles)) {
          config.terminalRoles = cloneDefaultForm().terminalRoles;
        }
        return config;
      } catch (e) {
        return null;
      }
    },
    handleBackgroundSuccess(response) {
      this.aboutForm.backgroundImage = response.data;
    },
    handleAvatarSuccess(response) {
      this.aboutForm.avatar = response.data;
    },
    addSkill() {
      this.aboutForm.skills.push({
        name: "",
        level: 60,
        category: ""
      });
    },
    removeSkill(index) {
      this.aboutForm.skills.splice(index, 1);
    },
    addPaper() {
      this.aboutForm.papers.push({
        title: "",
        authors: "",
        venue: "",
        type: "",
        award: "",
        abstract: "",
        link: ""
      });
    },
    removePaper(index) {
      this.aboutForm.papers.splice(index, 1);
    },
    uploadImg(pos, file) {
      var formdata = new FormData();
      if (file.size / 1024 < this.config.UPLOAD_SIZE) {
        formdata.append("file", file);
        this.axios
          .post("/api/admin/articles/images", formdata)
          .then(({ data }) => {
            this.$refs.md.$img2Url(pos, data.data);
          });
      } else {
        imageConversion
          .compressAccurately(file, this.config.UPLOAD_SIZE)
          .then(res => {
            formdata.append(
              "file",
              new window.File([res], file.name, { type: file.type })
            );
            this.axios
              .post("/api/admin/articles/images", formdata)
              .then(({ data }) => {
                this.$refs.md.$img2Url(pos, data.data);
              });
          });
      }
    },
    normalizeConfig() {
      const config = Object.assign(cloneDefaultForm(), this.aboutForm);
      config.terminalRoles = this.terminalRolesText
        .split(",")
        .map(item => item.trim())
        .filter(item => item);
      config.skills = config.skills
        .filter(item => item.name)
        .map(item => {
          const level = Number(item.level);
          return {
            name: item.name,
            category: item.category || "",
            level: Number.isNaN(level) ? 0 : Math.min(Math.max(level, 0), 100)
          };
        });
      config.papers = config.papers.filter(item => item.title);
      return config;
    },
    updateAbout() {
      this.axios
        .put("/api/admin/about", {
          aboutContent: JSON.stringify(this.normalizeConfig())
        })
        .then(({ data }) => {
          if (data.flag) {
            this.$notify.success({
              title: "成功",
              message: data.message
            });
          } else {
            this.$notify.error({
              title: "失败",
              message: data.message
            });
          }
        });
    }
  }
};
</script>

<style scoped>
.about-setting-card {
  min-height: calc(100vh - 126px);
}
.about-tabs {
  margin-top: 1rem;
}
.about-form {
  max-width: 720px;
}
.image-setting {
  display: grid;
  grid-template-columns: 180px minmax(0, 1fr);
  gap: 16px;
  align-items: center;
}
.cover-uploader .el-upload,
.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}
.cover-uploader .el-upload:hover,
.avatar-uploader .el-upload:hover {
  border-color: #409eff;
}
.cover-uploader-icon,
.avatar-uploader-icon {
  color: #8c939d;
  font-size: 28px;
  text-align: center;
}
.cover-uploader-icon {
  width: 180px;
  height: 96px;
  line-height: 96px;
}
.avatar-uploader-icon {
  width: 96px;
  height: 96px;
  line-height: 96px;
}
.cover-preview {
  display: block;
  width: 180px;
  height: 96px;
  object-fit: cover;
}
.avatar {
  display: block;
  width: 96px;
  height: 96px;
  object-fit: cover;
}
.repeat-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}
.skill-name-input {
  width: 320px;
}
.skill-category-input {
  width: 180px;
}
.paper-editor {
  display: grid;
  gap: 12px;
  max-width: 900px;
  margin-bottom: 18px;
  padding: 18px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background: #fafafa;
}
.paper-editor-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.action-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 18px;
}
.edit-btn {
  float: right;
  margin: 1rem 0;
}
@media (max-width: 768px) {
  .image-setting {
    grid-template-columns: 1fr;
  }
  .repeat-row {
    align-items: stretch;
    flex-direction: column;
  }
  .skill-name-input,
  .skill-category-input {
    width: 100%;
  }
}
</style>
