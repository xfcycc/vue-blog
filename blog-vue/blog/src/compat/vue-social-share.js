import source from "vue-social-share/dist/vue-social-share.js?raw";

let Share = {
  install() {}
};

if (typeof window !== "undefined") {
  const module = { exports: {} };
  const exports = module.exports;
  new Function("module", "exports", "define", source).call(
    window,
    module,
    exports,
    undefined
  );
  Share = module.exports.default || module.exports;
}

export default Share;
