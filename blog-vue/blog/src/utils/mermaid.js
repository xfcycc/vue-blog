let mermaidPromise;
let diagramId = 0;

// 无入参；按需加载并返回初始化后的 Mermaid，普通文章不加载图表依赖。
function loadMermaid() {
  if (!mermaidPromise) {
    mermaidPromise = import("mermaid")
      .then(({ default: mermaid }) => {
        mermaid.initialize({
          startOnLoad: false,
          securityLevel: "strict",
          suppressErrorRendering: true
        });
        return mermaid;
      })
      .catch(error => {
        mermaidPromise = null;
        throw error;
      });
  }
  return mermaidPromise;
}

// 输入已挂载的正文容器；将 Mermaid 代码块替换为 SVG，失败时保留源码。
export async function renderMermaid(root) {
  const blocks = root
    ? Array.from(root.querySelectorAll("pre > code.language-mermaid"))
    : [];
  if (!blocks.length) {
    return;
  }
  let mermaid;
  try {
    mermaid = await loadMermaid();
  } catch (error) {
    console.warn("Mermaid 加载失败，保留图表源码", error);
    return;
  }
  for (const block of blocks) {
    if (!root.isConnected || !root.contains(block)) {
      return;
    }
    try {
      const { svg } = await mermaid.render(
        "article-mermaid-" + ++diagramId,
        block.textContent
      );
      if (!root.isConnected || !root.contains(block)) {
        return;
      }
      const diagram = document.createElement("div");
      diagram.className = "article-mermaid";
      diagram.innerHTML = svg;
      block.parentElement.replaceWith(diagram);
    } catch (error) {
      console.warn("Mermaid 渲染失败，保留图表源码", error);
    }
  }
}
