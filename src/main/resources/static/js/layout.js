/* ==========================================================================
   SBerp — layout.js
   Injects layout partials, manages active nav, sidebar collapse,
   layout variants (standard / rail / topnav), accent color.
   Fires `layout:ready` when the chrome is in place.
   ========================================================================== */
(function () {
  "use strict";

  const LS = {
    layout: "sberp.layout",
    accent: "sberp.accent",
  };

  // NOTE: sidebar.jsp / header.jsp / footer.jsp 는 더 이상 JS의 fetch()로
  // 끼워넣지 않습니다. <%@include%> 디렉티브를 통해 서버(JSP) 컴파일
  // 시점에 이미 하나의 HTML로 합쳐져서 내려오기 때문에, 클라이언트에서
  // 다시 inject()할 필요가 없습니다. (과거 순수 html/js 버전의 잔재 코드 제거)

  function applyAccent(hex) {
    if (!hex) return;
    const rgb = hexToRgb(hex);
    const r = document.documentElement.style;
    r.setProperty("--sb-accent", hex);
    r.setProperty("--sb-accent-rgb", `${rgb.r}, ${rgb.g}, ${rgb.b}`);
    r.setProperty("--sb-accent-hover", shade(hex, -14));
    r.setProperty("--sb-accent-soft", mix(hex, "#ffffff", 0.92));
    r.setProperty("--sb-accent-soft-2", mix(hex, "#ffffff", 0.82));
    // reflect selection in dropdown
    document.querySelectorAll(".sb-accent-opt").forEach((b) => {
      b.classList.toggle("on", b.dataset.accent.toLowerCase() === hex.toLowerCase());
    });
  }

  function applyLayout(mode) {
    const app = document.querySelector(".sb-app");
    if (!app) return;
    app.setAttribute("data-layout", mode);
    document.querySelectorAll(".sb-layout-opt").forEach((b) => {
      b.classList.toggle("active", b.dataset.layout === mode);
      const isActive = b.dataset.layout === mode;
      b.innerHTML = b.innerHTML.replace(/\s*<i class="bi bi-check2[^>]*><\/i>$/, "");
      if (isActive) b.innerHTML += ' <i class="bi bi-check2 ms-auto"></i>';
    });
  }

  function buildTopbar() {
    // Clone nav links into the horizontal topbar for topnav mode
    const nav = document.getElementById("sbNav");
    const bar = document.getElementById("sbTopbar");
    if (!nav || !bar || bar.dataset.built) return;
    nav.querySelectorAll(".sb-nav__item").forEach((item) => {
      const a = document.createElement("a");
      a.className = "sb-topbar__item";
      a.href = item.getAttribute("href");
      a.dataset.page = item.dataset.page;
      const icon = item.querySelector("i").className;
      a.innerHTML = `<i class="${icon}"></i><span>${item.dataset.tip}</span>`;
      bar.appendChild(a);
    });
    bar.dataset.built = "1";
  }

  function markActive() {
    const page = document.body.dataset.page;
    if (!page) return;
    document.querySelectorAll('.sb-nav__item, .sb-topbar__item').forEach((el) => {
      el.classList.toggle("active", el.dataset.page === page);
    });
  }

  function wireChrome() {
    // sidebar collapse toggle (standard <-> rail)
    const toggle = document.getElementById("sbToggleSidebar");
    if (toggle) {
      toggle.addEventListener("click", () => {
        const app = document.querySelector(".sb-app");
        const cur = app.getAttribute("data-layout");
        if (cur === "topnav") return; // no-op in topnav
        const next = cur === "rail" ? "standard" : "rail";
        applyLayout(next);
        localStorage.setItem(LS.layout, next);
      });
    }

    // layout option buttons
    document.querySelectorAll(".sb-layout-opt").forEach((b) => {
      b.addEventListener("click", () => {
        const mode = b.dataset.layout;
        applyLayout(mode);
        localStorage.setItem(LS.layout, mode);
      });
    });

    // accent buttons
    document.querySelectorAll(".sb-accent-opt").forEach((b) => {
      b.addEventListener("click", () => {
        const hex = b.dataset.accent;
        applyAccent(hex);
        localStorage.setItem(LS.accent, hex);
      });
    });

    // global search shortcut
    document.addEventListener("keydown", (e) => {
      if ((e.metaKey || e.ctrlKey) && e.key.toLowerCase() === "k") {
        e.preventDefault();
        const inp = document.getElementById("sbGlobalSearch");
        if (inp) inp.focus();
      }
    });

    const gs = document.getElementById("sbGlobalSearch");
    if (gs) {
      gs.addEventListener("keydown", (e) => {
        if (e.key === "Enter" && gs.value.trim() && window.SB) {
          window.SB.toast(`'${gs.value.trim()}' 검색 결과 — 데모 환경입니다.`, "ok");
        }
      });
    }
  }

  async function boot() {
    buildTopbar();
    markActive();
    wireChrome();

    applyLayout(localStorage.getItem(LS.layout) || "standard");
    applyAccent(localStorage.getItem(LS.accent) || "#2563eb");

    document.dispatchEvent(new Event("layout:ready"));
  }

  /* ---- tiny color helpers ---- */
  function hexToRgb(hex) {
    const h = hex.replace("#", "");
    return { r: parseInt(h.slice(0, 2), 16), g: parseInt(h.slice(2, 4), 16), b: parseInt(h.slice(4, 6), 16) };
  }
  function mix(a, b, t) {
    const A = hexToRgb(a), B = hexToRgb(b);
    const r = Math.round(A.r * (1 - t) + B.r * t);
    const g = Math.round(A.g * (1 - t) + B.g * t);
    const bl = Math.round(A.b * (1 - t) + B.b * t);
    return `#${[r, g, bl].map((x) => x.toString(16).padStart(2, "0")).join("")}`;
  }
  function shade(hex, pct) {
    const { r, g, b } = hexToRgb(hex);
    const f = (x) => Math.max(0, Math.min(255, Math.round(x + (x * pct) / 100)));
    return `#${[f(r), f(g), f(b)].map((x) => x.toString(16).padStart(2, "0")).join("")}`;
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", boot);
  } else {
    boot();
  }
})();