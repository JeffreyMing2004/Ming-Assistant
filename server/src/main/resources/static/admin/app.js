// Ming Assistant · 电脑管理端
// 图标全部使用 morphicons 动态图标库（<morph-icon>），状态变化时图标会以弹簧物理动画变形。
import { icons } from "./icons.js";
import { defineMorphIcon } from "../morphicons/element.js?v=1";

defineMorphIcon();

const $ = (sel) => document.querySelector(sel);
const API = "/api";

let token = localStorage.getItem("ma_token") || "";
let user = JSON.parse(localStorage.getItem("ma_user") || "null");
// 后台管理员身份：登录走独立的管理员账号表（/api/admin/login），
// 凭据在 admin_users 表，与 App 用户表（users 表）完全分离。
const isAdmin = () => !!token && !!user && !!user.admin;

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

/* ---------------- 极验 GT4 验证码（网页端登录保护） ---------------- */

let gt4Captcha = null;
let gt4Result = null;
let gt4Config = null;
const gt4ConfigPromise = loadGt4Config();

async function loadGt4Config() {
  try {
    gt4Config = await api("/app/config");
  } catch (e) {
    gt4Config = null;
  }
  return gt4Config;
}

function loadGt4Sdk() {
  return new Promise((resolve, reject) => {
    if (window.initGeetest4) return resolve();
    const s = document.createElement("script");
    s.src = "https://static.geetest.com/v4/gt4.js";
    s.onload = () => resolve();
    s.onerror = () => reject(new Error("验证码 SDK 加载失败，请刷新后重试"));
    document.head.append(s);
  });
}

async function initGt4Captcha() {
  const cfg = await gt4ConfigPromise;
  if (!cfg || !cfg.captchaId) return; // 未配置验证码
  try {
    await loadGt4Sdk();
  } catch (e) {
    captchaLoadError(e && e.message ? e.message : "验证码 SDK 加载失败");
    return;
  }
  await new Promise((resolve) => {
    let done = false;
    const finish = (failMsg) => {
      if (done) return;
      done = true;
      resolve();
      if (failMsg) captchaLoadError(failMsg);
    };
    try {
      window.initGeetest4(
        { captchaId: cfg.captchaId, product: "float" },
        (captcha) => {
          gt4Captcha = captcha;
          captcha.appendTo("#gt4-captcha");
          captcha.onReady(() => finish(null));
          captcha.onError(() => finish("安全验证加载失败，请刷新页面后重试"));
          captcha.onSuccess(() => {
            const result = captcha.getValidate();
            if (result) {
              result.captcha_id = cfg.captchaId;
              gt4Result = result;
            }
          });
        },
      );
    } catch (e) {
      finish("验证码初始化失败，请刷新页面后重试");
    }
    // 超时保护：SDK 或组件 8 秒内无响应则提示，不再无限等待
    setTimeout(() => finish("安全验证加载超时，请刷新页面后重试"), 8000);
  });
}

function captchaLoadError(msg) {
  const p = $("#login-err");
  if (p) {
    p.textContent = msg;
    p.classList.remove("hidden");
  }
  toast(msg);
}

function resetGt4() {
  gt4Result = null;
  if (gt4Captcha) {
    try { gt4Captcha.reset(); } catch (e) { /* ignore */ }
  }
}

// 页面加载后初始化验证码（登录页）
gt4ConfigPromise
  .then((cfg) => (cfg && cfg.captchaId) ? initGt4Captcha() : null)
  .catch((e) => toast(e && e.message ? e.message : "验证码加载失败"));

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
    const cfg = await gt4ConfigPromise;
    const body = {
      username: fd.get("username"),
      password: fd.get("password"),
      lotNumber: "",
      captchaOutput: "",
      passToken: "",
      genTime: "",
    };
    if (cfg && cfg.captchaId) {
      if (!gt4Result) {
        const p = $("#login-err");
        p.textContent = "请先点击「安全验证」并完成滑块验证，再点击登录。";
        p.classList.remove("hidden");
        toast("请先完成安全验证");
        if (gt4Captcha) {
          try { gt4Captcha.showCaptcha(); } catch (e) { /* ignore */ }
        }
        return;
      }
      body.lotNumber = gt4Result.lot_number || "";
      body.captchaOutput = gt4Result.captcha_output || "";
      body.passToken = gt4Result.pass_token || "";
      body.genTime = gt4Result.gen_time == null ? "" : String(gt4Result.gen_time);
    }
    const res = await api("/admin/login", {
      method: "POST",
      body,
    });
    token = res.token;
    user = { username: res.username, name: res.name || res.username, admin: true };
    localStorage.setItem("ma_token", token);
    localStorage.setItem("ma_user", JSON.stringify(user));
    $("#login-err").classList.add("hidden");
    enterApp();
    toast(`登录成功，欢迎回来，${user.name}`);
  } catch (err) {
    resetGt4(); // 验证码一次一验，失败需重新完成
    const p = $("#login-err");
    p.textContent = err.message;
    p.classList.remove("hidden");
    toast(`登录失败：${err.message}`);
  } finally {
    btn.disabled = false;
  }
});

function enterApp() {
  $("#login-view").classList.add("hidden");
  $("#app-view").classList.remove("hidden");
  $("#who-name").textContent = (user && (user.name || user.username)) || "";
  refreshMe();
  applyGiftGate();
  applySongGate();
  applyAnnounceGate();
  loadLive();
  loadGifts();
  loadSongs();
  loadAnnounce();
}

function applySongGate() {
  const btn = document.querySelector('[data-action="add-song"]');
  if (btn) {
    btn.disabled = !isAdmin();
    btn.classList.toggle("disabled", !isAdmin());
  }
  const warn = document.getElementById("song-warn");
  if (warn) {
    warn.textContent = "仅站长后台管理员可新增 / 删除歌曲，其他账号只读。";
    warn.classList.toggle("hidden", isAdmin());
  }
}

/* 从服务器同步账号信息（B站UID 等），并据此启闭“新增舰礼”能力。
   管理员 token 代行站长用户身份，/auth/me 返回站长用户的资料（含 B站 UID）。 */
async function refreshMe() {
  try {
    const me = await api("/auth/me");
    if (me && me.bilibiliUid != null) {
      user = { ...(user || {}), userId: me.userId, bilibiliUid: me.bilibiliUid || "" };
      localStorage.setItem("ma_user", JSON.stringify(user));
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

/* ---------------- 公告 ---------------- */

function applyAnnounceGate() {
  const area = $("#announce-text");
  const btn = $("#announce-save");
  const editing = isAdmin();
  if (area) {
    area.disabled = !editing;
    area.classList.toggle("disabled", !editing);
  }
  if (btn) {
    btn.disabled = !editing;
    btn.classList.toggle("disabled", !editing);
  }
  const warn = $("#announce-warn");
  if (warn) {
    warn.textContent = "仅站长后台管理员可编辑公告，其他账号只读。";
    warn.classList.toggle("hidden", editing);
  }
}

async function loadAnnounce() {
  const status = $("#announce-status");
  try {
    const r = await api("/announcement");
    $("#announce-text").value = (r && r.text) || "";
    status.textContent = r && r.updatedAt ? `更新于 ${fmtTime(r.updatedAt)}` : "暂无";
    $("#announce-saved-hint").textContent = "";
  } catch (e) {
    status.textContent = "加载失败";
  }
}

$("#announce-save").addEventListener("click", async () => {
  if (!isAdmin()) {
    toast("仅站长可编辑公告");
    return;
  }
  const btn = $("#announce-save");
  btn.disabled = true;
  const hint = $("#announce-saved-hint");
  hint.textContent = "";
  try {
    const r = await api("/announcement", {
      method: "PUT",
      body: { text: ($("#announce-text").value || "").trim() },
    });
    hint.textContent = r && r.text ? "已发布，App 首页已更新" : "公告已清空";
    toast(r && r.text ? "公告已发布" : "公告已清空");
    loadAnnounce().catch(() => {});
  } catch (e) {
    toast(e && e.message ? e.message : "保存失败");
  } finally {
    applyAnnounceGate();
  }
});

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
  { k: "giftType", label: "大航海", options: ["舰长", "提督", "总督"], required: true },
];

function giftRow(g) {
  const tr = document.createElement("tr");
  tr.dataset.id = g.id;
  tr.dataset.tracking = g.trackingNumber || "";
  const ops = document.createElement("td");
  ops.className = "ops";
  const track = document.createElement("button");
  track.className = "row-btn track-btn";
  track.title = g.trackingNumber ? "修改快递单号" : "登记快递单号";
  track.append(mkIcon("edit-2", 16));
  ops.append(track);
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
  const tierTag = () => {
    const t = document.createElement("td");
    const s = document.createElement("span");
    s.className = "tag";
    s.textContent = g.giftType;
    t.append(s);
    return t;
  };

  tr.append(
    td(g.id, "mono"),
    td(g.username || "—"),
    td(g.nickname),
    td(g.bilibiliUid || "—", "mono"),
    td(g.phone || "—", "mono"),
    td(g.address || "—"),
    tierTag(),
    td(g.trackingNumber || "—", "mono"),
    td(fmtTime(g.createdAt), "mono"),
    ops,
  );
  return tr;
}

async function loadGifts() {
  const list = await api("/admin/gifts");
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
  if (isAdmin()) {
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

function bindDelete(tbody, kind, path) {
  tbody.addEventListener("click", async (e) => {
    const btn = e.target.closest(".row-btn");
    if (!btn) return;
    if (e.target.closest(".track-btn")) return; // 快递单号按钮由独立事件处理
    e.stopPropagation();
    if (kind === "songs" && !isAdmin()) {
      toast("仅站长可删除歌曲");
      return;
    }
    const tr = btn.closest("tr");
    if (armedBtn === btn) {
      const id = tr.dataset.id;
      resetArmed();
      const ic = btn.querySelector("morph-icon");
      try {
        await api(`/${path}/${id}`, { method: "DELETE" });
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

bindDelete($("#gift-tbody"), "gifts", "admin/gifts");
bindDelete($("#song-tbody"), "songs", "songs");

/* 登记 / 修改快递单号：弹窗填写后保存到对应舰礼 */
let currentTrackingId = 0;
document.addEventListener("click", (e) => {
  const tb = e.target.closest(".track-btn");
  if (!tb) return;
  e.stopPropagation();
  const tr = tb.closest("tr");
  currentTrackingId = Number(tr.dataset.id);
  openModal("快递单号", "tracking", [
    { k: "trackingNumber", label: "快递单号", ph: "如：SF1234567890（留空可清除）", value: tr.dataset.tracking || "", max: 64 },
  ]);
});

/* ---------------- 新增弹窗 ---------------- */

const modal = $("#modal");
const modalForm = $("#modal-form");
let modalKind = null;

function fieldHtml(f) {
  const val = f.value ? ` value="${f.value}"` : "";
  if (f.options && f.options.length) {
    const opts = f.options.map((o) => `<option>${o}</option>`).join("");
    return `<label class="field">${f.label}<select name="${f.k}"${f.required ? " required" : ""}>${opts}</select></label>`;
  }
  const attrs = [
    `name="${f.k}"`,
    `placeholder="${f.ph}"`,
    f.required ? "required" : "",
    f.pattern ? `pattern="${f.pattern}" title="${f.ph}"` : "",
    f.max ? `maxlength="${f.max}"` : "",
    val,
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
    if (!isAdmin()) {
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
    if (kind === "tracking") {
      await api(`/admin/gifts/${currentTrackingId}/tracking`, {
        method: "POST",
        body: { trackingNumber: (data.trackingNumber || "").trim() },
      });
      closeModal();
      toast("快递单号已登记");
      loadGifts().catch(() => {});
      return;
    }
    const path = kind === "gift" ? "admin/gifts" : "songs";
    await api(`/${path}`, { method: "POST", body: data });
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

window.__adminBooted = true;
applyStaticIcons();
if (token && user) {
  enterApp();
  liveTimer = setInterval(() => loadLive().catch(() => {}), 60000);
} else {
  logout();
}