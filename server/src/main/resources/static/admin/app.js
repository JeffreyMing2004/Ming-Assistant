// Ming Assistant · 电脑管理端
// 图标全部使用 morphicons 动态图标库（<morph-icon>），状态变化时图标会以弹簧物理动画变形。
import { icons } from "./icons.js";
import { defineMorphIcon } from "../morphicons/element.js";

defineMorphIcon();

const $ = (sel) => document.querySelector(sel);
const API = "/api";

let token = localStorage.getItem("ma_token") || "";
let user = JSON.parse(localStorage.getItem("ma_user") || "null");
let ownerUsername = null;
const isOwner = () => !!ownerUsername && !!user && user.username === ownerUsername;

/* ---------------- 图标辅助 ---------------- */

function mkIcon(name, size, extra) {
  const el = document.createElement("morph-icon");
  setIcon(el, name, size);
  if (extra) for (const k in extra) el.setAttribute(k, extra[k]);
  return el;
}

function setIcon(el, name, size) {
  el.setAttribute("icon", icons[name] || "");
  if (size) el.setAttribute("size", String(size));
  return el;
}

// 把页面静态占位 <morph-icon data-icon="..."> 点亮
function applyStaticIcons() {
  document.querySelectorAll("[data-icon]").forEach((el) => {
    if (el.tagName === "MORPH-ICON") setIcon(el, el.dataset.icon, el.dataset.size);
  });
}

// 某个图标短暂切换为另一形态再还原（如 刷新 -> ✓、+ -> ✓），演示 morph 动画
function flash(ic, to, ms = 850) {
  if (!ic) return;
  const was = ic.getAttribute("icon");
  setIcon(ic, to);
  clearTimeout(ic._flash);
  ic._flash = setTimeout(() => {
    if (was) ic.setAttribute("icon", was);
  }, ms);
}

/* ---------------- API ---------------- */

async function api(path, { method = "GET", body } = {}) {
  let res;
  try {
    res = await fetch(API + path, {
      method,
      headers: {
        ...(body ? { "Content-Type": "application/json" } : {}),
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
      body: body ? JSON.stringify(body) : undefined,
    });
  } catch {
    throw new Error("无法连接服务器，请确认后端已启动");
  }
  if (res.status === 401) {
    logout("登录已过期，请重新登录");
    throw new Error("未登录或登录已过期");
  }
  const ct = res.headers.get("content-type") || "";
  const data = ct.includes("json") ? await res.json() : null;
  if (!res.ok) throw new Error((data && data.message) || `请求失败 (${res.status})`);
  return data;
}

/* ---------------- 登录 / 注销 ---------------- */

function fmtTime(iso) {
  if (!iso) return "—";
  const d = new Date(iso);
  return isNaN(d) ? String(iso) : d.toLocaleString("zh-CN", { hour12: false });
}

function showLogin(msg) {
  $("#app-view").classList.add("hidden");
  $("#login-view").classList.remove("hidden");
  if (msg) {
    const p = $("#login-err");
    p.textContent = msg;
    p.classList.remove("hidden");
  }
}

function logout(msg) {
  token = "";
  user = null;
  localStorage.removeItem("ma_token");
  localStorage.removeItem("ma_user");
  $("input[name=username]").focus();
  showLogin(msg);
}

$("#login-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  const form = e.target;
  if (!form.reportValidity()) return;
  const fd = new FormData(form);
  const btn = $("#login-btn");
  btn.disabled = true;
  try {
    const res = await api("/auth/login", {
      method: "POST",
      body: { username: fd.get("username"), password: fd.get("password") },
    });
    token = res.token;
    user = { userId: res.userId, username: res.username, bilibiliUid: res.bilibiliUid || "" };
    localStorage.setItem("ma_token", token);
    localStorage.setItem("ma_user", JSON.stringify(user));
    $("#login-err").classList.add("hidden");
    enterApp();
  } catch (err) {
    const p = $("#login-err");
    p.textContent = err.message;
    p.classList.remove("hidden");
  } finally {
    btn.disabled = false;
  }
});

function enterApp() {
  $("#login-view").classList.add("hidden");
  $("#app-view").classList.remove("hidden");
  $("#who-name").textContent = (user && user.username) || "";
  refreshMe();
  applyGiftGate();
  loadLive();
  loadGifts();
  loadAppConfig().then(() => {
    applySongGate();
    loadSongs();
  });
}

/* 读取后端基础配置（歌单站长账号名），用于歌单增删的权限门 */
async function loadAppConfig() {
  try {
    const cfg = await api("/app/config");
    ownerUsername = (cfg && cfg.ownerUsername) || null;
  } catch (e) {
    if (e && e.message.includes("登录")) return;
  }
}

function applySongGate() {
  const btn = document.querySelector('[data-action="add-song"]');
  if (btn) {
    btn.disabled = !isOwner();
    btn.classList.toggle("disabled", !isOwner());
  }
  const warn = document.getElementById("song-warn");
  if (warn) {
    warn.textContent = "仅站长账号可新增 / 删除歌曲，其他账号只读。";
    warn.classList.toggle("hidden", isOwner());
  }
}

/* 从服务器同步账号信息（B站UID 等），并据此启闭“新增舰礼”能力 */
async function refreshMe() {
  try {
    const me = await api("/auth/me");
    if (me && me.bilibiliUid != null) {
      user = { userId: me.userId, username: me.username, bilibiliUid: me.bilibiliUid || "" };
      localStorage.setItem("ma_user", JSON.stringify(user));
      $("#who-name").textContent = user.username || "";
      applyGiftGate();
    }
  } catch (e) {
    if (e && e.message.includes("登录")) return;
  }
}

function applyGiftGate() {
  const blank = !user || !user.bilibiliUid;
  const btn = document.querySelector('[data-action="add-gift"]');
  const warn = document.getElementById("gift-warn");
  if (btn) {
    btn.disabled = blank;
    btn.classList.toggle("disabled", blank);
  }
  if (warn) {
    warn.textContent = "当前账号注册时未填写本人B站UID，无法登记舰礼。";
    warn.classList.toggle("hidden", !blank);
  }
}

$("#logout").addEventListener("click", () => logout());

/* ---------------- 顶栏 Tab ---------------- */

document.querySelectorAll(".tab").forEach((t) =>
  t.addEventListener("click", () => {
    document.querySelectorAll(".tab").forEach((x) => x.classList.toggle("active", x === t));
    document.querySelectorAll(".section").forEach((s) =>
      s.classList.toggle("active", s.id === `sec-${t.dataset.tab}`),
    );
  }),
);

/* ---------------- 直播状态 ---------------- */

let liveTimer = null;
async function loadLive() {
  try {
    const s = await api("/live/status");
    const on = s.liveStatus === 1;
    const ic = $("#live-status-icon");
    setIcon(ic, on ? "zap" : "radio");
    ic.setAttribute("color", on ? "#4ade80" : "#6f7d90");

    const badge = $("#live-badge");
    badge.textContent = on ? "● 直播中" : "● 未开播";
    badge.classList.toggle("live", on);
    badge.classList.toggle("off", !on);

    $("#live-title").textContent = s.title || "（无标题）";
    $("#live-room").textContent = `房间 ${s.roomId}`;
    $("#live-meta").textContent = s.online != null ? `${s.online} 人在线` : "";
    $("#live-checked").textContent = fmtTime(s.checkedAt);
    const url = $("#live-url");
    if (s.url) {
      url.href = s.url;
      url.hidden = false;
    }
  } catch (err) {
    if (err.message.includes("登录")) return;
  }
}

$("#live-refresh").addEventListener("click", async () => {
  const ic = $("#live-refresh").querySelector("morph-icon");
  flash(ic, "check");
  await loadLive();
});

/* ---------------- 舰礼 ---------------- */

const giftKinds = [
  { k: "nickname", label: "姓名", ph: "如：张三", required: true, max: 50 },
  { k: "bilibiliUid", label: "B站 UID", ph: "数字，可留空", required: false, pattern: "^[0-9]{1,20}$" },
  { k: "phone", label: "联系电话", ph: "手机号", required: true, max: 20 },
  { k: "address", label: "住址", ph: "收件地址", required: true, max: 255 },
  { k: "giftType", label: "礼物类型", ph: "如：舰长 / 提督 / 总督", required: true, max: 50 },
];

function giftRow(g) {
  const tr = document.createElement("tr");
  tr.dataset.id = g.id;
  const ops = document.createElement("td");
  ops.className = "ops";
  const del = document.createElement("button");
  del.className = "row-btn";
  del.title = "删除（再次点击确认）";
  del.append(mkIcon("trash-2", 16));
  ops.append(del);

  const td = (txt, cls) => {
    const t = document.createElement("td");
    if (cls) t.className = cls;
    t.textContent = txt;
    return t;
  };

  tr.append(
    td(g.id, "mono"),
    td(g.nickname),
    td(g.bilibiliUid || "—", "mono"),
    td(g.phone || "—", "mono"),
    td(g.address || "—"),
    (() => { const t = document.createElement("td"); const s = document.createElement("span"); s.className = "tag"; s.textContent = g.giftType; t.append(s); return t; })(),
    td(fmtTime(g.createdAt), "mono"),
    ops,
  );
  return tr;
}

async function loadGifts() {
  const list = await api("/gifts");
  $("#gift-count").textContent = `${list.length} 条`;
  const tbody = $("#gift-tbody");
  tbody.textContent = "";
  $("#gift-empty").classList.toggle("hidden", list.length > 0);
  const frag = document.createDocumentFragment();
  for (const g of list) frag.append(giftRow(g));
  tbody.append(frag);
}

/* ---------------- 歌单 ---------------- */

const songKinds = [
  { k: "title", label: "歌名", ph: "必填", required: true, max: 200 },
  { k: "artist", label: "歌手", ph: "可留空", required: false, max: 100 },
  { k: "note", label: "备注", ph: "可留空", required: false, max: 500 },
];

function songRow(s) {
  const tr = document.createElement("tr");
  tr.dataset.id = s.id;
  const ops = document.createElement("td");
  ops.className = "ops";
  if (isOwner()) {
    const del = document.createElement("button");
    del.className = "row-btn";
    del.title = "删除（再次点击确认）";
    del.append(mkIcon("trash-2", 16));
    ops.append(del);
  }

  const td = (txt, cls) => {
    const t = document.createElement("td");
    if (cls) t.className = cls;
    t.textContent = txt;
    return t;
  };

  tr.append(
    td(s.id, "mono"),
    td(s.title),
    td(s.artist || "—"),
    td(s.note || "—"),
    td(fmtTime(s.createdAt), "mono"),
    ops,
  );
  return tr;
}

async function loadSongs() {
  const list = await api("/songs");
  $("#song-count").textContent = `${list.length} 首`;
  const tbody = $("#song-tbody");
  tbody.textContent = "";
  $("#song-empty").classList.toggle("hidden", list.length > 0);
  const frag = document.createDocumentFragment();
  for (const s of list) frag.append(songRow(s));
  tbody.append(frag);
}

/* ---------------- 两段式删除（第一次点击进入待确认态，图标变为 ✓，再点删除） ---------------- */

let armedBtn = null;

function resetArmed() {
  if (!armedBtn) return;
  armedBtn.classList.remove("arm");
  armedBtn.dataset.armed = "";
  armedBtn.querySelector("morph-icon") && setIcon(armedBtn.querySelector("morph-icon"), "trash-2");
  armedBtn.closest("tr") && armedBtn.closest("tr").classList.remove("armed");
  armedBtn = null;
}

document.addEventListener("click", (e) => {
  if (!e.target.closest(".row-btn")) resetArmed();
});

function bindDelete(tbody, kind) {
  tbody.addEventListener("click", async (e) => {
    const btn = e.target.closest(".row-btn");
    if (!btn) return;
    e.stopPropagation();
    if (kind === "songs" && !isOwner()) {
      toast("仅站长可删除歌曲");
      return;
    }
    const tr = btn.closest("tr");
    if (armedBtn === btn) {
      const id = tr.dataset.id;
      resetArmed();
      const ic = btn.querySelector("morph-icon");
      try {
        await api(`/${kind}/${id}`, { method: "DELETE" });
        flash(ic, "check", 500);
        tr.remove();
        const cnt = `#${kind === "gifts" ? "gift" : "song"}-count`;
        const rest = $(`#${kind}-tbody`).children.length;
        $(cnt).textContent = kind === "gifts" ? `${rest} 条` : `${rest} 首`;
        $(kind === "gifts" ? "#gift-empty" : "#song-empty").classList.toggle("hidden", rest > 0);
        toast(`已删除 #${id}`);
      } catch (err) {
        toast(err.message);
      }
    } else {
      resetArmed();
      armedBtn = btn;
      btn.dataset.armed = "1";
      btn.classList.add("arm");
      setIcon(btn.querySelector("morph-icon"), "check");
      tr.classList.add("armed");
    }
  });
}

bindDelete($("#gift-tbody"), "gifts");
bindDelete($("#song-tbody"), "songs");

/* ---------------- 新增弹窗 ---------------- */

const modal = $("#modal");
const modalForm = $("#modal-form");
let modalKind = null;

function fieldHtml(f) {
  const attrs = [
    `name="${f.k}"`,
    `placeholder="${f.ph}"`,
    f.required ? "required" : "",
    f.pattern ? `pattern="${f.pattern}" title="${f.ph}"` : "",
    f.max ? `maxlength="${f.max}"` : "",
  ].join(" ");
  return `<label class="field">${f.label}<input ${attrs}></label>`;
}

function openModal(title, kind, fields) {
  modalKind = kind;
  $("#modal-title").textContent = title;
  $("#modal-fields").innerHTML = fields.map(fieldHtml).join("");
  $("#modal-err").classList.add("hidden");
  modal.showModal();
  setTimeout(() => modalForm.querySelector("input").focus(), 30);
}

document.addEventListener("click", (e) => {
  const btn = e.target.closest("[data-action]");
  if (!btn) return;
  const a = btn.dataset.action;
  if (a === "add-gift") {
    if (!user || !user.bilibiliUid) {
      toast("当前账号未填写本人B站UID，无法登记舰礼");
      return;
    }
    openModal("新增舰礼登记", "gift", giftKinds);
  }
  if (a === "add-song") {
    if (!isOwner()) {
      toast("仅站长可新增歌曲");
      return;
    }
    openModal("新增歌曲", "song", songKinds);
  }
});

function closeModal() {
  modal.close();
  modalKind = null;
}

modal.addEventListener("click", (e) => {
  if (e.target === modal) closeModal(); // 点击遮罩关闭
});
$("#modal-close").addEventListener("click", closeModal);
$("#modal-cancel").addEventListener("click", closeModal);

modalForm.addEventListener("submit", async (e) => {
  e.preventDefault();
  if (!modalForm.reportValidity()) return;
  const kind = modalKind;
  const data = Object.fromEntries(new FormData(modalForm).entries());
  const btn = $("#modal-save");
  btn.disabled = true;
  try {
    await api(`/${kind === "gift" ? "gifts" : "songs"}`, { method: "POST", body: data });
    closeModal();
    const headBtn = $(`[data-action="add-${kind}"]`);
    headBtn && flash(headBtn.querySelector("morph-icon"), "check");
    toast("已保存");
    if (kind === "gift") loadGifts().catch(() => {});
    else loadSongs().catch(() => {});
  } catch (err) {
    const p = $("#modal-err");
    p.textContent = err.message;
    p.classList.remove("hidden");
  } finally {
    btn.disabled = false;
  }
});

/* ---------------- Toast ---------------- */

let toastTimer = null;
function toast(msg) {
  let el = $("#toast");
  if (!el) {
    el = document.createElement("div");
    el.id = "toast";
    document.body.append(el);
  }
  el.textContent = msg;
  el.classList.add("show");
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => el.classList.remove("show"), 2400);
}

/* ---------------- 启动 ---------------- */

applyStaticIcons();
if (token && user) {
  enterApp();
  liveTimer = setInterval(() => loadLive().catch(() => {}), 60000);
} else {
  logout();
}