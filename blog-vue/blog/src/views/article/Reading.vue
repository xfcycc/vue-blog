<template>
  <div class="reading-page">
    <div class="reading-shell">
      <main
        v-if="articleTrees.length"
        class="tree-scroll"
      >
        <section class="tree-stage">
          <article
            v-for="(article, index) of articleTrees"
            :key="article.articleId"
            class="article-tree-group"
            :style="{ animationDelay: index * 80 + 'ms' }"
          >
            <router-link
              :to="getArticleLink(article)"
              class="root-card"
              :title="article.articleTitle"
              :data-title="article.articleTitle"
            >
              <span class="root-kicker">
                <v-icon size="15" color="#059669">mdi-book-open-page-variant</v-icon>
                ARTICLE
              </span>
              <strong>{{ article.articleTitle }}</strong>
            </router-link>

            <div class="root-connector"></div>

            <div class="tree-root">
              <tree-node
                :node="article.rootNode"
                :article-id="article.articleId"
                @remove-bookmark="removeBookmark"
              />
            </div>
          </article>
        </section>
      </main>

      <section v-else class="reading-empty">
        <span class="empty-leaf" aria-hidden="true"></span>
        <h2>还没有书叶</h2>
        <p>打开文章会生成黄色阅读叶，目录旁的小绿叶可以保存书签。</p>
        <router-link to="/archives">去归档里翻一篇</router-link>
      </section>
    </div>
  </div>
</template>

<script>
import {
  listReadingTree,
  removeArticleBookmark
} from "../../utils/readingStore";

function formatDate(value) {
  if (!value) {
    return "刚刚";
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return "刚刚";
  }
  return date.toLocaleString("zh-CN", {
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit"
  });
}

function normalizePath(path, fallback) {
  const nextPath = Array.isArray(path)
    ? path
        .map(item => String(item || "").replace(/\s+/g, " ").trim())
        .filter(Boolean)
    : [];
  return nextPath.length ? nextPath : [fallback || "文章开头"];
}

function createBranch(title) {
  return {
    title,
    children: [],
    branchMap: new Map()
  };
}

function insertRecord(root, record) {
  let current = root;
  record.tocPath.forEach(title => {
    if (!current.branchMap.has(title)) {
      const child = createBranch(title);
      current.branchMap.set(title, child);
      current.children.push(child);
    }
    current = current.branchMap.get(title);
  });
  current.children.push({
    title: record.anchorText || record.tocPath[record.tocPath.length - 1],
    type: record.type,
    positionId: record.positionId,
    progress: Math.round(Number(record.percent || 0)),
    date: formatDate(record.updatedAt || record.createdAt)
  });
}

function stripBranchMap(node) {
  return {
    ...node,
    branchMap: undefined,
    children: (node.children || []).map(child =>
      child.children ? stripBranchMap(child) : child
    )
  };
}

const TreeNode = {
  name: "TreeNode",
  props: {
    node: {
      type: Object,
      required: true
    },
    articleId: {
      type: Number,
      required: true
    }
  },
  methods: {
    getLeafLink(node) {
      return {
        path: "/articles/" + this.articleId,
        query: {
          bookmark: node.positionId
        }
      };
    },
    emitRemove(node, event) {
      event.preventDefault();
      event.stopPropagation();
      this.$emit("remove-bookmark", this.articleId, node.positionId);
    }
  },
  render(h) {
    const hasChildren = this.node.children && this.node.children.length > 0;
    const isLeaf = this.node.type === "history" || this.node.type === "bookmark";
    const cardChildren = [
      h("span", { class: "tree-node-title" }, this.node.title)
    ];

    if (isLeaf) {
      cardChildren.push(
        h("span", { class: "tree-leaf-meta" }, [
          h("i", { class: ["tree-leaf-icon", "is-" + this.node.type] }),
          h("strong", String(this.node.progress || 0) + "%"),
          h("small", this.node.date)
        ])
      );
    }

    let nodeCard = h(
      isLeaf ? "router-link" : "div",
      {
        class: ["tree-node-card", isLeaf ? "is-leaf is-" + this.node.type : "is-branch"],
        attrs: {
          "data-title": this.node.title,
          title: isLeaf ? "跳到：" + this.node.title : this.node.title
        },
        props: isLeaf ? { to: this.getLeafLink(this.node) } : undefined
      },
      cardChildren
    );

    if (isLeaf && this.node.type === "bookmark") {
      nodeCard = h(
        "div",
        { class: "tree-node-card-wrap" },
        [
          nodeCard,
          h(
            "button",
            {
              class: "tree-leaf-remove",
              attrs: {
                type: "button",
                title: "摘下记录",
                "aria-label": "摘下书签"
              },
              on: {
                click: event => this.emitRemove(this.node, event)
              }
            },
            "×"
          )
        ]
      );
    }

    const children = [nodeCard];
    if (hasChildren) {
      children.push(
        h("div", { class: "tree-children-wrap" }, [
          h("div", { class: "tree-horizontal-line" }),
          h(
            "div",
            { class: "tree-children" },
            this.node.children.map((child, index) => {
              const count = this.node.children.length;
              return h("div", {
                class: [
                  "tree-child",
                  index === 0 ? "is-first" : "",
                  index === count - 1 ? "is-last" : "",
                  count === 1 ? "is-only" : ""
                ],
                key: index,
                style: {
                  animationDelay: index * 35 + "ms"
                }
              }, [
                h(TreeNode, {
                  props: {
                    node: child,
                    articleId: this.articleId
                  },
                  on: {
                    "remove-bookmark": (articleId, positionId) =>
                      this.$emit("remove-bookmark", articleId, positionId)
                  }
                })
              ]);
            })
          )
        ])
      );
    }
    return h("div", { class: "tree-node" }, children);
  }
};

export default {
  components: {
    TreeNode
  },
  mounted() {
    this.refreshReadingTree();
  },
  data: function() {
    return {
      treeList: []
    };
  },
  methods: {
    refreshReadingTree() {
      this.treeList = listReadingTree();
    },
    getArticleLink(article) {
      const link = {
        path: "/articles/" + article.articleId
      };
      if (article.history && article.history.positionId) {
        link.query = {
          bookmark: article.history.positionId
        };
      }
      return link;
    },
    removeBookmark(articleId, positionId) {
      this.treeList = removeArticleBookmark(articleId, positionId);
    },
    createArticleTree(article) {
      const root = createBranch(article.articleTitle);
      if (article.history) {
        insertRecord(root, {
          ...article.history,
          type: "history",
          tocPath: normalizePath(article.history.tocPath, article.history.anchorText)
        });
      }
      article.positions.forEach(position => {
        insertRecord(root, {
          ...position,
          type: "bookmark",
          tocPath: normalizePath(position.tocPath, position.anchorText)
        });
      });
      return stripBranchMap(root);
    }
  },
  computed: {
    articleTrees() {
      return this.treeList.map(article => ({
        ...article,
        rootNode: this.createArticleTree(article)
      }));
    }
  }
};
</script>

<style>
.reading-page {
  position: relative;
  min-height: 100vh;
  overflow-x: hidden;
  padding: 92px 24px 64px;
  background:
    linear-gradient(180deg, rgba(247, 251, 252, 0.58), rgba(248, 250, 252, 0.7)),
    url("../../assets/images/link-galaxy/space-bg.png") center top / cover fixed no-repeat;
}
.reading-page:before {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  background:
    radial-gradient(circle at 14% 12%, rgba(112, 193, 179, 0.14), transparent 32%),
    radial-gradient(circle at 78% 42%, rgba(255, 255, 255, 0.36), transparent 34%),
    linear-gradient(90deg, rgba(248, 250, 252, 0.22), rgba(248, 250, 252, 0.46));
  content: "";
}
.reading-page,
.reading-page * {
  box-sizing: border-box;
}
.reading-shell {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: none;
  margin: 0 auto;
}
.tree-leaf-icon {
  display: inline-block;
  width: 14px;
  height: 16px;
  border-radius: 80% 0 80% 0;
  transform: rotate(-26deg);
  transform-origin: 70% 70%;
  animation: readingLeafFloat 2.8s ease-in-out infinite;
}
.is-history.tree-leaf-icon {
  background: linear-gradient(135deg, #fbbf24, #f59e0b);
  box-shadow: 0 3px 10px rgba(245, 158, 11, 0.2);
}
.is-bookmark.tree-leaf-icon {
  background: linear-gradient(135deg, #86efac, #22c55e);
  box-shadow: 0 3px 10px rgba(34, 197, 94, 0.22);
}
.tree-scroll {
  overflow: visible;
  padding: 72px clamp(28px, 5vw, 96px) 72px;
}
.tree-stage {
  width: 100%;
  min-width: 0;
  padding: 2px 0 0;
}
.article-tree-group {
  display: flex;
  align-items: center;
  width: 100%;
  margin-bottom: 34px;
  opacity: 0;
  transform: translateY(12px);
  animation: readingTreeEnter 0.52s ease forwards;
}
.root-card {
  position: relative;
  z-index: 20;
  display: flex;
  flex: 0 0 clamp(210px, 12vw, 260px);
  flex-direction: column;
  justify-content: center;
  width: clamp(210px, 12vw, 260px);
  height: 108px;
  overflow: visible;
  padding: 18px 20px;
  border: 1px solid rgba(207, 229, 231, 0.92);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.86);
  color: #26323f !important;
  box-shadow: 0 10px 28px rgba(31, 41, 55, 0.05);
  backdrop-filter: blur(12px);
  transition: box-shadow 0.22s ease, transform 0.22s ease, border-color 0.22s ease;
}
.root-card:after {
  position: absolute;
  right: -36px;
  bottom: -42px;
  width: 108px;
  height: 108px;
  border-radius: 50%;
  background: rgba(112, 193, 179, 0.12);
  content: "";
  transition: transform 0.35s ease;
}
.root-card:hover {
  border-color: rgba(112, 193, 179, 0.45);
  box-shadow: 0 16px 34px rgba(31, 41, 55, 0.08);
  transform: translateY(-2px);
}
.root-card:active {
  transform: translateY(0) scale(0.995);
}
.root-card:hover:before,
.tree-node-card:hover:before {
  position: absolute;
  bottom: calc(100% + 8px);
  left: 50%;
  z-index: 40;
  max-width: 320px;
  padding: 8px 10px;
  border: 1px solid rgba(203, 213, 225, 0.9);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 16px 32px rgba(15, 23, 42, 0.12);
  color: #334155;
  content: attr(data-title);
  font-size: 12px;
  font-weight: 700;
  line-height: 1.55;
  overflow-wrap: anywhere;
  pointer-events: none;
  transform: translateX(-50%);
  white-space: normal;
}
.root-card:hover:after {
  transform: scale(1.18);
}
.root-kicker {
  position: relative;
  z-index: 1;
  display: inline-flex;
  align-items: center;
  gap: 7px;
  margin-bottom: 10px;
  color: rgba(35, 132, 118, 0.9);
  font-size: 9px;
  font-weight: 800;
  letter-spacing: 0.16em;
}
.root-card strong {
  position: relative;
  z-index: 1;
  display: -webkit-box;
  overflow: hidden;
  font-size: 14px;
  font-weight: 800;
  line-height: 1.38;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}
.root-connector,
.tree-horizontal-line {
  flex: 1 1 clamp(18px, 2vw, 54px);
  width: auto;
  min-width: 18px;
  height: 2px;
  border-radius: 999px;
  background: linear-gradient(90deg, rgba(112, 193, 179, 0.35), rgba(112, 193, 179, 0.78));
  transform-origin: left center;
  animation: readingLineGrow 0.46s ease both;
}
.tree-root {
  display: flex;
  align-items: center;
  flex: 1 1 auto;
  min-width: 0;
  padding: 14px 0;
}
.tree-node {
  display: flex;
  align-items: center;
  flex: 1 1 auto;
  min-width: 0;
}
.tree-node-card,
.tree-node-card-wrap {
  position: relative;
  z-index: 10;
  flex: 0 0 auto;
  min-width: 0;
}
.tree-node-card {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  width: clamp(124px, 9vw, 178px);
  max-width: 100%;
  min-height: 42px;
  padding: 10px 13px;
  border: 1px solid rgba(226, 232, 240, 0.9);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.88);
  color: #334155 !important;
  box-shadow: 0 8px 18px rgba(31, 41, 55, 0.045);
  backdrop-filter: blur(10px);
  transition: box-shadow 0.2s ease, transform 0.2s ease, border-color 0.2s ease, background 0.2s ease;
}
.tree-node-card:hover {
  border-color: rgba(112, 193, 179, 0.52);
  background: rgba(255, 255, 255, 0.98);
  box-shadow: 0 13px 24px rgba(31, 41, 55, 0.075);
  transform: translateY(-2px);
}
.tree-node-card.is-leaf:active {
  transform: translateY(0) scale(0.995);
}
.tree-node-card.is-leaf {
  width: clamp(138px, 10vw, 184px);
}
.tree-node-card.is-history:hover {
  border-color: rgba(245, 158, 11, 0.38);
}
.tree-node-card.is-bookmark:hover {
  border-color: rgba(34, 197, 94, 0.36);
}
.tree-node-title {
  display: block;
  min-width: 0;
  overflow: hidden;
  color: #334155;
  font-size: 13px;
  font-weight: 700;
  line-height: 1.35;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.tree-leaf-meta {
  display: inline-flex;
  flex: 0 0 auto;
  align-items: center;
  gap: 7px;
  margin-left: 6px;
  padding-left: 8px;
  border-left: 1px solid rgba(226, 232, 240, 0.9);
}
.tree-leaf-meta strong {
  color: #334155;
  font-size: 12px;
  font-weight: 800;
}
.tree-leaf-meta small {
  display: none;
  color: #94a3b8;
  font-size: 11px;
  font-weight: 600;
  white-space: nowrap;
}
.tree-leaf-remove {
  position: absolute;
  top: 50%;
  right: 4px;
  z-index: 12;
  width: 24px;
  height: 24px;
  border: 1px solid #f1f5f9;
  border-radius: 999px;
  background: #fff;
  color: #94a3b8;
  font-size: 16px;
  font-weight: 900;
  line-height: 1;
  cursor: pointer;
  opacity: 0;
  transform: translate(12px, -50%);
  transition: opacity 0.18s ease, transform 0.18s ease, color 0.18s ease;
}
.tree-node-card-wrap .tree-node-card {
  padding-right: 34px;
}
.tree-node-card-wrap:hover .tree-leaf-remove {
  opacity: 1;
  transform: translate(0, -50%);
}
.tree-leaf-remove:hover {
  border-color: #fecaca;
  background: #fef2f2;
  color: #ef4444;
}
.tree-children-wrap {
  display: flex;
  align-items: center;
  flex: 1 1 auto;
  min-width: 0;
}
.tree-children {
  position: relative;
  display: flex;
  flex: 1 1 auto;
  flex-direction: column;
  justify-content: center;
  min-width: 0;
  padding: 2px 0;
}
.tree-child {
  position: relative;
  display: flex;
  align-items: center;
  min-width: 0;
  padding: 10px 0 10px clamp(18px, 2vw, 54px);
  opacity: 0;
  transform: translateX(-8px);
  animation: readingBranchEnter 0.42s ease forwards;
}
.tree-child:before,
.tree-child:after {
  position: absolute;
  left: 0;
  background: rgba(112, 193, 179, 0.58);
  content: "";
}
.tree-child:before {
  width: 2px;
}
.tree-child:after {
  top: calc(50% - 1px);
  width: clamp(18px, 2vw, 54px);
  height: 2px;
  border-radius: 999px;
  transform: scaleX(0);
  transform-origin: left center;
  animation: readingLineGrow 0.38s ease forwards;
}
.tree-child.is-first:before {
  top: 50%;
  bottom: 0;
}
.tree-child.is-last:before {
  top: 0;
  bottom: 50%;
}
.tree-child:not(.is-first):not(.is-last):before {
  top: 0;
  bottom: 0;
}
.tree-child.is-only:before {
  display: none;
}
.article-tree-group:hover .root-connector,
.tree-node:hover > .tree-children-wrap > .tree-horizontal-line,
.tree-child:hover:before,
.tree-child:hover:after {
  background: rgba(73, 167, 151, 0.86);
}
.reading-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 320px;
  text-align: center;
  animation: readingTreeEnter 0.42s ease both;
}
.empty-leaf {
  width: 64px;
  height: 42px;
  margin-bottom: 20px;
  border-radius: 100% 0 100% 0;
  background: linear-gradient(135deg, #86efac, #22c55e);
  transform: rotate(-18deg);
}
.reading-empty h2 {
  margin: 0 0 10px;
  color: #2f3a4a;
  font-size: 24px;
}
.reading-empty p {
  max-width: 480px;
  margin: 0 0 18px;
  color: #64748b;
  line-height: 1.8;
}
.reading-empty a {
  display: inline-flex;
  align-items: center;
  min-height: 38px;
  padding: 0 16px;
  border-radius: 8px;
  background: #31425f;
  color: #fff !important;
  font-weight: 800;
  transition: background 0.2s ease, transform 0.2s ease;
}
.reading-empty a:hover {
  background: #253248;
  transform: translateY(-1px);
}
@keyframes readingTreeEnter {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
@keyframes readingBranchEnter {
  from {
    opacity: 0;
    transform: translateX(-8px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}
@keyframes readingLineGrow {
  from {
    transform: scaleX(0);
  }
  to {
    transform: scaleX(1);
  }
}
@keyframes readingLeafFloat {
  0%,
  100% {
    transform: rotate(-26deg) translateY(0);
  }
  50% {
    transform: rotate(-22deg) translateY(-1px);
  }
}
@media (max-width: 759px) {
  .reading-page {
    padding: 82px 18px 44px;
    background-attachment: scroll;
  }
  .tree-scroll {
    overflow: visible;
    padding: 54px 0 36px;
    cursor: default;
  }
  .tree-stage {
    min-width: 0;
    width: 100%;
    padding: 0;
  }
  .article-tree-group {
    align-items: stretch;
    flex-direction: column;
    margin-bottom: 30px;
  }
  .root-card {
    width: 100%;
    height: auto;
    min-height: 104px;
    flex-basis: auto;
  }
  .root-card strong {
    -webkit-line-clamp: 2;
  }
  .root-connector,
  .tree-horizontal-line {
    width: 2px;
    height: 26px;
    flex: 0 0 26px;
    margin-left: 26px;
    background: linear-gradient(180deg, rgba(112, 193, 179, 0.28), rgba(112, 193, 179, 0.78));
    animation-name: readingVerticalLineGrow;
    transform-origin: center top;
  }
  .tree-root {
    align-items: stretch;
    padding: 0;
  }
  .tree-node {
    align-items: stretch;
    flex-direction: column;
    width: 100%;
  }
  .tree-node-card,
  .tree-node-card-wrap {
    width: 100%;
  }
  .tree-node-card {
    align-items: flex-start;
    flex-wrap: wrap;
    justify-content: space-between;
  }
  .tree-node-title {
    min-width: 0;
    white-space: normal;
    overflow-wrap: anywhere;
  }
  .tree-leaf-meta {
    flex: 0 0 auto;
    margin-left: 0;
  }
  .tree-leaf-meta small {
    display: inline;
  }
  .tree-leaf-remove {
    opacity: 1;
    transform: translate(0, -50%);
  }
  .tree-children-wrap {
    align-items: stretch;
    flex-direction: column;
  }
  .tree-children {
    padding: 0;
  }
  .tree-child {
    display: block;
    padding: 0 0 12px 28px;
  }
  .tree-child:before {
    left: 12px;
  }
  .tree-child:after {
    top: 21px;
    left: 12px;
    width: 16px;
  }
  .tree-child.is-first:before {
    top: 0;
    bottom: 0;
  }
  .tree-child.is-last:before {
    top: 0;
    bottom: calc(100% - 22px);
  }
  .tree-child.is-only:before {
    display: block;
    top: 0;
    bottom: calc(100% - 22px);
  }
  .tree-child:last-child {
    padding-bottom: 0;
  }
}
@keyframes readingVerticalLineGrow {
  from {
    transform: scaleY(0);
  }
  to {
    transform: scaleY(1);
  }
}
</style>
