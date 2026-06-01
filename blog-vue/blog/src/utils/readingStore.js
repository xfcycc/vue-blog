const TREE_KEY = "vueblog:reading:tree";
const MAX_HISTORY_SIZE = 80;

function canUseStorage() {
  return typeof window !== "undefined" && !!window.localStorage;
}

function readStorage(key) {
  if (!canUseStorage()) {
    return [];
  }
  try {
    const value = window.localStorage.getItem(key);
    const list = value ? JSON.parse(value) : [];
    return Array.isArray(list) ? list : [];
  } catch (e) {
    return [];
  }
}

function writeStorage(key, list) {
  if (!canUseStorage()) {
    return;
  }
  try {
    window.localStorage.setItem(key, JSON.stringify(list));
  } catch (e) {
    // 本地存储失败时不阻断阅读。
  }
}

function normalizeArticle(article) {
  const articleId = Number(article && (article.articleId || article.id));
  if (!Number.isInteger(articleId) || articleId <= 0) {
    return null;
  }
  return {
    articleId,
    articleTitle: article.articleTitle || "未命名文章",
    articleCover: article.articleCover || "",
    articleSummary: article.articleSummary || "",
    categoryId: article.categoryId || "",
    categoryName: article.categoryName || "",
    createTime: article.createTime || "",
    articleHref: "/articles/" + articleId
  };
}

function normalizeTocPath(path, fallbackText) {
  const tocPath = Array.isArray(path)
    ? path
        .map(item => String(item || "").replace(/\s+/g, " ").trim())
        .filter(Boolean)
    : [];
  if (tocPath.length) {
    return tocPath.slice(0, 5);
  }
  return [fallbackText || "文章开头"];
}

function normalizePosition(position, type, fallbackId) {
  const now = Date.now();
  const source = position || {};
  const anchorText = source.anchorText || "文章开头";
  return {
    type,
    positionId: source.positionId || fallbackId || type + "-" + now,
    anchorId: source.anchorId || "",
    anchorText,
    tocPath: normalizeTocPath(source.tocPath, anchorText),
    scrollY: Math.max(Number(source.scrollY || 0), 0),
    percent: Math.max(Math.min(Number(source.percent || 0), 100), 0),
    createdAt: source.createdAt || now,
    updatedAt: source.updatedAt || now
  };
}

function isCurrentTreeShape(tree) {
  return tree.every(node => {
    if (!node || !Array.isArray(node.positions)) {
      return false;
    }
    const hasValidHistory =
      !node.history || Array.isArray(node.history.tocPath);
    const hasValidBookmarks = node.positions.every(item =>
      Array.isArray(item && item.tocPath)
    );
    return hasValidHistory && hasValidBookmarks;
  });
}

function normalizeNode(node) {
  const article = normalizeArticle(node);
  if (!article) {
    return null;
  }
  const positions = Array.isArray(node.positions)
    ? node.positions
        .map(item => normalizePosition(item, "bookmark", item.positionId))
        .filter(Boolean)
    : [];
  const history = node.history
    ? normalizePosition(node.history, "history", "history-" + article.articleId)
    : null;
  return {
    ...article,
    history,
    positions
  };
}

function getNodeLatestTime(node) {
  const historyTime = node.history ? node.history.updatedAt || node.history.createdAt : 0;
  const bookmarkTime = node.positions.reduce((latest, item) => {
    return Math.max(latest, item.updatedAt || item.createdAt || 0);
  }, 0);
  return Math.max(historyTime, bookmarkTime);
}

function sortTree(tree) {
  return tree
    .filter(Boolean)
    .sort((a, b) => getNodeLatestTime(b) - getNodeLatestTime(a))
    .slice(0, MAX_HISTORY_SIZE);
}

function createNode(article) {
  return {
    ...article,
    history: null,
    positions: []
  };
}

function readTree() {
  const tree = readStorage(TREE_KEY);
  if (!tree.length) {
    return [];
  }
  if (!isCurrentTreeShape(tree)) {
    writeStorage(TREE_KEY, []);
    return [];
  }
  return sortTree(tree.map(normalizeNode));
}

function writeTree(tree) {
  const nextTree = sortTree(tree);
  writeStorage(TREE_KEY, nextTree);
  return nextTree;
}

function upsertNode(tree, article) {
  const normalized = normalizeArticle(article);
  if (!normalized) {
    return {
      tree,
      node: null
    };
  }
  const index = tree.findIndex(item => item.articleId === normalized.articleId);
  if (index !== -1) {
    tree[index] = {
      ...tree[index],
      ...normalized
    };
    return {
      tree,
      node: tree[index]
    };
  }
  const node = createNode(normalized);
  tree.push(node);
  return {
    tree,
    node
  };
}

export function listReadingTree() {
  return readTree();
}

export function saveReadingHistory(article, position) {
  const tree = readTree();
  const result = upsertNode(tree, article);
  if (!result.node) {
    return tree;
  }
  result.node.history = normalizePosition(
    position,
    "history",
    "history-" + result.node.articleId
  );
  return writeTree(result.tree);
}

export function addArticleBookmark(article, position) {
  const tree = readTree();
  const result = upsertNode(tree, article);
  if (!result.node) {
    return {
      tree,
      position: null,
      count: 0
    };
  }
  const bookmark = normalizePosition(
    position,
    "bookmark",
    "bookmark-" + result.node.articleId + "-" + Date.now()
  );
  result.node.positions.unshift(bookmark);
  const nextTree = writeTree(result.tree);
  const nextNode = nextTree.find(item => item.articleId === result.node.articleId);
  return {
    tree: nextTree,
    position: bookmark,
    count: nextNode ? nextNode.positions.length : 0
  };
}

export function getArticleBookmarkCount(articleId) {
  const id = Number(articleId);
  const node = readTree().find(item => item.articleId === id);
  return node ? node.positions.length : 0;
}

export function getArticleBookmarkAnchorIds(articleId) {
  const id = Number(articleId);
  const node = readTree().find(item => item.articleId === id);
  if (!node) {
    return [];
  }
  return node.positions
    .map(item => item.anchorId)
    .filter(Boolean);
}

export function getReadingPosition(articleId, positionId) {
  const id = Number(articleId);
  const node = readTree().find(item => item.articleId === id);
  if (!node) {
    return null;
  }
  if (!positionId || positionId === "history") {
    return node.history;
  }
  if (node.history && node.history.positionId === positionId) {
    return node.history;
  }
  return node.positions.find(item => item.positionId === positionId) || null;
}

export function removeArticleBookmark(articleId, positionId) {
  const id = Number(articleId);
  const tree = readTree()
    .map(node => {
      if (node.articleId !== id) {
        return node;
      }
      return {
        ...node,
        positions: node.positions.filter(item => item.positionId !== positionId)
      };
    })
    .filter(node => node.history || node.positions.length);
  return writeTree(tree);
}

export function removeArticleBookmarkByAnchorId(articleId, anchorId) {
  const id = Number(articleId);
  const targetAnchorId = String(anchorId || "");
  const tree = readTree()
    .map(node => {
      if (node.articleId !== id) {
        return node;
      }
      return {
        ...node,
        positions: node.positions.filter(item => item.anchorId !== targetAnchorId)
      };
    })
    .filter(node => node.history || node.positions.length);
  const nextTree = writeTree(tree);
  const nextNode = nextTree.find(node => node.articleId === id);
  return {
    tree: nextTree,
    count: nextNode ? nextNode.positions.length : 0
  };
}

export function clearReadingTree() {
  return writeTree([]);
}

export function listReadingHistory() {
  return readTree()
    .filter(node => node.history)
    .map(node => ({
      ...node,
      ...node.history,
      id: node.articleId
    }));
}

export function listArticleBookmarks() {
  return readTree().flatMap(node =>
    node.positions.map(position => ({
      ...node,
      ...position,
      id: node.articleId
    }))
  );
}

export function isArticleBookmarked(articleId) {
  return getArticleBookmarkCount(articleId) > 0;
}

export function toggleArticleBookmark(article, position) {
  const result = addArticleBookmark(article, position);
  return {
    active: result.count > 0,
    list: result.tree
  };
}

export function removeReadingHistory(articleId) {
  const id = Number(articleId);
  const tree = readTree()
    .map(node => {
      if (node.articleId !== id) {
        return node;
      }
      return {
        ...node,
        history: null
      };
    })
    .filter(node => node.history || node.positions.length);
  return writeTree(tree);
}

export function clearReadingHistory() {
  const tree = readTree()
    .map(node => ({
      ...node,
      history: null
    }))
    .filter(node => node.positions.length);
  return writeTree(tree);
}

export function clearArticleBookmarks() {
  const tree = readTree()
    .map(node => ({
      ...node,
      positions: []
    }))
    .filter(node => node.history);
  return writeTree(tree);
}
