/* ==========================================================================
   SBerp — com-ocr.js
   회사 등록(com/add) 전용: 사업자등록증 OCR 자동입력 모달 로직.
   app.js(SB.toast)를 사용하며, com/add.html 인라인 스크립트가 window에 노출한
   syncIndustryCode() / checkBizNo()를 그대로 재사용합니다.
   업종 대분류/세부업종 매핑 및 렌더링은 공용 모듈 js/industry-code.js(IndustryCode)를
   사용하므로, 이 스크립트보다 industry-code.js가 먼저 로드되어 있어야 합니다.
   ========================================================================== */
(function () {
  "use strict";

  function resetOcrModal() {
    document.getElementById("ocrFile").value = "";
    document.getElementById("ocrResultFields").style.display = "none";
    document.getElementById("ocrApplyBtn").disabled = true;
    document.getElementById("ocrIndustryRaw").textContent = "";
    ["ocrBizNo", "ocrComName", "ocrComCeo", "ocrStartDt"].forEach((id) => {
      document.getElementById(id).value = "";
    });
    document.getElementById("ocrIndustryGrpCode").value = "";
    IndustryCode.renderOptions("ocrIndustryGrpCode", "ocrIndustryCode", "");
  }

  function handleOcrFile(e) {
    const file = e.target.files[0];
    if (!file) return;

    const loading = document.getElementById("ocrLoading");
    const applyBtn = document.getElementById("ocrApplyBtn");
    const resultFields = document.getElementById("ocrResultFields");

    applyBtn.disabled = true;
    resultFields.style.display = "none";
    loading.classList.remove("d-none");

    const formData = new FormData();
    formData.append("file", file);

    const csrfToken  = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;
    const fetchHeaders = {};
    if (csrfToken && csrfHeader) {
        fetchHeaders[csrfHeader] = csrfToken;
    }

    fetch("/api/util/ocr", {
        method: "POST",
        headers: fetchHeaders,
        body: formData
    })
    .then(res => res.json())
    .then(json => {
        if (json.status === "success") {
            const d = json.data;
            document.getElementById("ocrBizNo").value = d.bizNo || "";
            document.getElementById("ocrComName").value = d.comName || "";
            document.getElementById("ocrComCeo").value = d.comCeo || "";
            document.getElementById("ocrStartDt").value = d.startDt || "";

            const grpCode = IndustryCode.matchGrpFromText(d.industryGrpText);
            document.getElementById("ocrIndustryGrpCode").value = grpCode;

            const detailCode = IndustryCode.matchCodeFromText(grpCode, d.industryCodeText);
            IndustryCode.renderOptions("ocrIndustryGrpCode", "ocrIndustryCode", detailCode, "인식된 값");

            const rawLines = [];
            if (d.industryGrpText) {
                rawLines.push('업종 인식 원문: "' + IndustryCode.escapeHtml(d.industryGrpText) + '"' + (grpCode ? "" : " (자동 매칭 실패 - 직접 선택해주세요)"));
            }
            if (d.industryCodeText) {
                rawLines.push('세부업종 인식 원문: "' + IndustryCode.escapeHtml(d.industryCodeText) + '"' + (detailCode ? "" : " (자동 매칭 실패 - 직접 선택해주세요)"));
            }
            document.getElementById("ocrIndustryRaw").innerHTML = rawLines.join("<br>");

            resultFields.style.display = "";
            applyBtn.disabled = false;
            if (window.SB) SB.toast("사업자등록증 인식이 완료되었습니다. 내용을 확인해주세요.", "ok");
        } else {
            if (window.SB) SB.toast(json.message || "OCR 인식에 실패했습니다.", "warn");
        }
    })
    .catch(() => {
        if (window.SB) SB.toast("서버 요청 중 오류가 발생했습니다.", "warn");
    })
    .finally(() => {
        loading.classList.add("d-none");
    });
}

  function applyOcrToForm() {
    document.getElementById("bizNo").value   = document.getElementById("ocrBizNo").value.trim();
    document.getElementById("comName").value = document.getElementById("ocrComName").value.trim();
    document.getElementById("comCeo").value  = document.getElementById("ocrComCeo").value.trim();
    document.getElementById("startDt").value = document.getElementById("ocrStartDt").value;

    const grp  = document.getElementById("ocrIndustryGrpCode").value;
    const code = document.getElementById("ocrIndustryCode").value;
    document.getElementById("industryGrpCode").value = grp;
    IndustryCode.renderOptions("industryGrpCode", "industryCode", code);

    /* 값이 바뀌었으니 진위확인 스냅샷 무효화 로직(input 리스너)을 정상 트리거 */
    ["bizNo", "comCeo", "startDt"].forEach((id) => {
      document.getElementById(id).dispatchEvent(new Event("input", { bubbles: true }));
    });

    /* 사업자번호 중복 체크 자동 실행 */
    if (typeof window.checkBizNo === "function") window.checkBizNo();

    const modalEl = document.getElementById("ocrModal");
    const inst = bootstrap.Modal.getInstance(modalEl);
    if (inst) inst.hide();

    if (window.SB) SB.toast("OCR 인식 결과를 폼에 적용했습니다. 진위확인 후 등록해주세요.", "ok");
  }

  function init() {
    const fileEl   = document.getElementById("ocrFile");
    const applyBtn = document.getElementById("ocrApplyBtn");
    const modalEl  = document.getElementById("ocrModal");
    const grpEl    = document.getElementById("ocrIndustryGrpCode");
    if (!fileEl || !applyBtn || !modalEl) return; // 모달이 없는 페이지에서는 조용히 종료

    fileEl.addEventListener("change", handleOcrFile);
    applyBtn.addEventListener("click", applyOcrToForm);
    modalEl.addEventListener("hidden.bs.modal", resetOcrModal);
    if (grpEl) grpEl.addEventListener("change", () => IndustryCode.renderOptions("ocrIndustryGrpCode", "ocrIndustryCode", ""));
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init);
  } else {
    init();
  }
})();