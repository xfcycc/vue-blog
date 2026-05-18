import source from "vue-baberrage/dist/vue-baberrage.js?raw";

const browserTimers =
  typeof window === "undefined"
    ? {}
    : {
        setTimeout: window.setTimeout.bind(window),
        clearTimeout: window.clearTimeout.bind(window),
        setInterval: window.setInterval.bind(window),
        clearInterval: window.clearInterval.bind(window)
      };

const toPx = value => {
  if (typeof value === "number") {
    return value;
  }

  const numericValue = parseFloat(value);
  if (Number.isNaN(numericValue)) {
    return 0;
  }

  if (typeof document === "undefined" || String(value).endsWith("px")) {
    return numericValue;
  }

  const element = document.createElement("div");
  element.style.position = "absolute";
  element.style.visibility = "hidden";
  element.style.width = value;
  document.body.appendChild(element);
  const pixels = element.offsetWidth;
  document.body.removeChild(element);
  return pixels || numericValue;
};

let plugin = {
  vueBaberrage: { install() {} },
  MESSAGE_TYPE: {}
};

if (typeof window !== "undefined") {
  const module = { exports: {} };
  const exports = module.exports;
  const requireShim = id => {
    if (id === "timers") {
      return browserTimers;
    }

    if (id === "to-px") {
      return toPx;
    }

    throw new Error(`Unsupported vue-baberrage dependency: ${id}`);
  };

  new Function("module", "exports", "define", "require", source).call(
    window,
    module,
    exports,
    undefined,
    requireShim
  );
  plugin = module.exports;
}

export const vueBaberrage = plugin.vueBaberrage;
export const MESSAGE_TYPE = plugin.MESSAGE_TYPE;
export default plugin.vueBaberrage;
