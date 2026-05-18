import source from "tocbot/dist/tocbot.js?raw";

let tocbot = {
  init() {},
  destroy() {},
  refresh() {}
};

if (typeof window !== "undefined") {
  new Function(source).call(window);
  tocbot = window.tocbot || tocbot;
}

export default tocbot;
