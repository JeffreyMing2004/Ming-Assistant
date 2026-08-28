// 自托管 <morph-icon> 自定义元素（无第三方构建依赖）。
// 图标数据由页面提供：`icon` 属性直接携带 feather 图标的 path d 字符串，
// app.js 通过 icons.js 把图标名映射为路径后写入该属性。
const SVG_NS = "http://www.w3.org/2000/svg";

class MorphIconElement extends HTMLElement {
  static get observedAttributes() {
    return ["icon", "size", "color", "stroke-width"];
  }

  connectedCallback() {
    this._render();
  }

  attributeChangedCallback(_name, _oldValue, newValue) {
    if (newValue !== _oldValue) this._render();
  }

  _render() {
    const d = this.getAttribute("icon") || "";
    const size = Number(this.getAttribute("size")) || 24;
    const sw = Number(this.getAttribute("stroke-width")) || 2;
    const color = this.getAttribute("color") || "currentColor";
    if (!d) {
      this.replaceChildren();
      this.style.display = "contents";
      return;
    }
    const svg = document.createElementNS(SVG_NS, "svg");
    svg.setAttribute("xmlns", SVG_NS);
    svg.setAttribute("viewBox", "0 0 24 24");
    svg.setAttribute("width", String(size));
    svg.setAttribute("height", String(size));
    svg.setAttribute("stroke", color);
    svg.setAttribute("stroke-width", String(sw));
    svg.setAttribute("fill", "none");
    svg.setAttribute("stroke-linecap", "round");
    svg.setAttribute("stroke-linejoin", "round");
    svg.setAttribute("aria-hidden", "true");
    const path = document.createElementNS(SVG_NS, "path");
    path.setAttribute("d", d);
    svg.appendChild(path);
    this.replaceChildren(svg);
    this.style.display = "";
  }
}

let registered = false;

/** Defines `<morph-icon>`（或自定义标签）。幂等。 */
export function defineMorphIcon(tag = "morph-icon") {
  if (typeof customElements === "undefined") return;
  if (customElements.get(tag)) return;
  const Cls = registered ? class extends MorphIconElement {} : MorphIconElement;
  registered = true;
  customElements.define(tag, Cls);
}

export { MorphIconElement };