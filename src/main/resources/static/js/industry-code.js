/* ==========================================================================
   SBerp — industry-code.js
   업종 대분류(industryGrpCode) <-> 세부업종(industryCode) 공용 매핑/렌더링 유틸.

   사용처: com/form.html(등록), com/edit.html(수정), com-ocr.js(OCR 자동입력 모달)
   위 세 곳에서 각자 들고 있던 INDUSTRY_CODE_MAP / render 함수를 이 파일 하나로 통합했습니다.
   이 스크립트는 각 페이지의 인라인 스크립트보다 먼저 로드되어야 합니다.

   [참고] DB(company 테이블)에는 industry_code에 대한 별도 마스터 테이블이 없어
   여기 있는 목록은 프론트에서 예시로 구성한 참고용입니다. 실 운영 환경에서는
   통계청 표준산업분류(KSIC) 코드를 별도 테이블로 관리하는 것을 권장합니다.
   ========================================================================== */
(function (window) {
    "use strict";

    /* 업종 대분류 한글 라벨 <-> 코드 (OCR 인식 텍스트 매칭용) */
    var GRP_TEXT_MAP = {
        "제조업": "C",
        "건설업": "F",
        "도매 및 소매업": "G",
        "운수 및 창고업": "H",
        "숙박 및 음식점업": "I",
        "정보통신업": "J",
        "금융 및 보험업": "K",
        "전문, 과학 및 기술 서비스업": "M",
        "사업시설관리, 사업지원 및 임대 서비스업": "N",
        "협회 및 단체, 수리 및 기타 개인 서비스업": "S"
    };

    /* 업종 대분류 -> 세부업종(코드/이름) 목록 */
    var CODE_MAP = {
        C: [
            { code: '10120', name: '육류 가공 및 저장 처리업' },
            { code: '20421', name: '계면활성제 제조업' },
            { code: '26290', name: '기타 전자부품 제조업' }
        ],
        F: [
            { code: '41221', name: '단독주택 건설업' },
            { code: '42111', name: '도로 건설업' }
        ],
        G: [
            { code: '45111', name: '국산 승용차 판매업' },
            { code: '46900', name: '상품 종합 도매업' },
            { code: '47190', name: '기타 종합 소매업' }
        ],
        H: [
            { code: '49110', name: '철도 운송업' },
            { code: '52941', name: '소화물 전문 운송업(택배)' }
        ],
        I: [
            { code: '55101', name: '호텔업' },
            { code: '56111', name: '한식 음식점업' }
        ],
        J: [
            { code: '58222', name: '응용 소프트웨어 개발 및 공급업' },
            { code: '62010', name: '컴퓨터 프로그래밍 서비스업' },
            { code: '63910', name: '뉴스 제공업' }
        ],
        K: [
            { code: '64121', name: '은행업' },
            { code: '64992', name: '신탁업 및 집합투자업' }
        ],
        M: [
            { code: '70112', name: '지주회사' },
            { code: '71531', name: '기타 엔지니어링서비스업' },
            { code: '72911', name: '사업시설 유지관리 서비스업' }
        ],
        N: [
            { code: '74110', name: '인력 공급 및 고용 알선업' },
            { code: '75291', name: '여행사업' }
        ],
        S: [
            { code: '94120', name: '산업 및 전문가 단체' },
            { code: '95120', name: '컴퓨터 및 주변기기 수리업' }
        ]
    };

    /* 외부(OCR 등) 입력 텍스트를 innerHTML에 넣기 전 이스케이프 */
    function escapeHtml(str) {
        var div = document.createElement("div");
        div.textContent = str == null ? "" : str;
        return div.innerHTML;
    }

    /** OCR 등에서 인식한 업종 대분류 텍스트 -> 코드(C/F/G ...) */
    function matchGrpFromText(text) {
        if (!text) return "";
        if (GRP_TEXT_MAP[text]) return GRP_TEXT_MAP[text];
        var key = Object.keys(GRP_TEXT_MAP).find(function (k) {
            return text.includes(k) || k.includes(text);
        });
        return key ? GRP_TEXT_MAP[key] : "";
    }

    /** OCR 등에서 인식한 세부업종(종목) 텍스트 -> grp에 해당하는 코드 */
    function matchCodeFromText(grp, text) {
        if (!grp || !text) return "";
        var list = CODE_MAP[grp] || [];
        var found = list.find(function (item) {
            return text.includes(item.name) || item.name.includes(text);
        });
        return found ? found.code : "";
    }

    /**
     * grpSelectId select의 현재 값에 맞춰 codeSelectId select의 옵션을 다시 그림.
     * - selectedCode가 있으면 그 값으로 선택 상태를 맞춤
     * - 목록에 없는 기존 저장값(더미데이터 등)은 유실되지 않도록 "코드 (extraLabel)" 형태로
     *   별도 옵션을 추가해서 보존함
     *
     * @param {string} grpSelectId   업종 대분류 select의 id
     * @param {string} codeSelectId  세부업종 select의 id
     * @param {string} selectedCode  선택해 둘 세부업종 코드 (없으면 '')
     * @param {string} [extraLabel]  목록에 없는 값 표시용 라벨 (기본: '현재 저장된 값')
     */
    function renderOptions(grpSelectId, codeSelectId, selectedCode, extraLabel) {
        var grpEl  = document.getElementById(grpSelectId);
        var codeEl = document.getElementById(codeSelectId);
        if (!grpEl || !codeEl) return;

        var grp   = grpEl.value;
        var list  = CODE_MAP[grp] || [];
        var label = extraLabel || "현재 저장된 값";

        if (!grp) {
            codeEl.innerHTML = '<option value="">-- 업종을 먼저 선택하세요 --</option>';
            codeEl.disabled = true;
            codeEl.classList.remove("is-valid", "is-invalid");
            return;
        }

        codeEl.disabled = false;
        codeEl.innerHTML = '<option value="">-- 세부업종 선택 --</option>';

        list.forEach(function (item) {
            var opt = document.createElement("option");
            opt.value = item.code;
            opt.textContent = item.code + " · " + item.name;
            codeEl.appendChild(opt);
        });

        if (selectedCode && !list.some(function (i) { return i.code === selectedCode; })) {
            var extraOpt = document.createElement("option");
            extraOpt.value = selectedCode;
            extraOpt.textContent = selectedCode + " (" + label + ")";
            codeEl.appendChild(extraOpt);
        }

        codeEl.value = selectedCode || "";
    }

    window.IndustryCode = {
        MAP: CODE_MAP,
        GRP_TEXT_MAP: GRP_TEXT_MAP,
        escapeHtml: escapeHtml,
        matchGrpFromText: matchGrpFromText,
        matchCodeFromText: matchCodeFromText,
        renderOptions: renderOptions
    };
})(window);