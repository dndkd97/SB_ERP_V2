/* ==========================================================================
   SBerp — app.js  (shared UI utilities used across pages)
   ========================================================================== */
window.SB = (function () {
  "use strict";

  /* ---- Toast ---- */
  function toast(msg, kind = "ok", icon) {
    let wrap = document.querySelector(".sb-toast-wrap");
    if (!wrap) {
      wrap = document.createElement("div");
      wrap.className = "sb-toast-wrap";
      document.body.appendChild(wrap);
    }
    const ic = icon || (kind === "warn" ? "bi-exclamation-triangle-fill" : "bi-check-circle-fill");
    const t = document.createElement("div");
    t.className = "sb-toast " + kind;
    t.innerHTML = `<i class="bi ${ic}"></i><span>${msg}</span>`;
    wrap.appendChild(t);
    setTimeout(() => {
      t.style.transition = "opacity .3s, transform .3s";
      t.style.opacity = "0";
      t.style.transform = "translateY(8px)";
      setTimeout(() => t.remove(), 320);
    }, 2600);
  }

  /* ---- formatters ---- */
  const won = (n) => (n === 0 ? "—" : "₩" + n.toLocaleString("ko-KR"));
  const num = (n) => n.toLocaleString("ko-KR");

  /* ---- generic client-side table filter ----
     opts: { input, rows (NodeList/selector), cols (data-search attr used) , empty }
  */
  function wireFilter({ search, rows, getText, onCount }) {
    const inp = typeof search === "string" ? document.querySelector(search) : search;
    if (!inp) return;
    inp.addEventListener("input", () => apply());
    function apply() {
      const q = inp.value.trim().toLowerCase();
      let visible = 0;
      rows().forEach((r) => {
        const hit = !q || getText(r).toLowerCase().includes(q);
        r.style.display = hit ? "" : "none";
        if (hit) visible++;
      });
      if (onCount) onCount(visible, q);
    }
    return apply;
  }

  /* ---- badge html ---- */
  function badge(text, tone = "gray", pip = false) {
    return `<span class="sb-badge sb-badge--${tone}">${pip ? '<span class="pip"></span>' : ""}${text}</span>`;
  }

  /* ---- avatar ---- */
  function avatar(letter, extra = "") {
    return `<span class="sb-avatar" ${extra}>${letter}</span>`;
  }

  /* ---- bootstrap modal helper: build + show a modal from html, auto-dispose ---- */
  function modal({ title, body, footer, size }) {
    const id = "m" + Math.random().toString(36).slice(2, 8);
    const sizeCls = size ? "modal-" + size : "";
    const el = document.createElement("div");
    el.className = "modal fade";
    el.id = id;
    el.tabIndex = -1;
    el.innerHTML = `
      <div class="modal-dialog ${sizeCls} modal-dialog-centered modal-dialog-scrollable">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">${title}</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body">${body}</div>
          ${footer ? `<div class="modal-footer">${footer}</div>` : ""}
        </div>
      </div>`;
    document.body.appendChild(el);
    const inst = new bootstrap.Modal(el);
    el.addEventListener("hidden.bs.modal", () => el.remove());
    inst.show();
    return { el, inst, close: () => inst.hide() };
  }

  /* ---- count-up animation for stat tiles ---- */
  function countUp(el, to, opts = {}) {
    const dur = opts.dur || 900;
    const prefix = opts.prefix || "";
    const suffix = opts.suffix || "";
    const start = performance.now();
    function tick(now) {
      const p = Math.min(1, (now - start) / dur);
      const eased = 1 - Math.pow(1 - p, 3);
      const val = Math.round(to * eased);
      el.textContent = prefix + val.toLocaleString("ko-KR") + suffix;
      if (p < 1) requestAnimationFrame(tick);
    }
    requestAnimationFrame(tick);
  }

  return { toast, won, num, wireFilter, badge, avatar, modal, countUp };
})();
