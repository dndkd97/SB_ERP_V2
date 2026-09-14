/* 안내 메세지 */
(function(){
	const ICONS = {
	success: 'bi-check-circle-fill',
	error: 'bi-x-circle-fill',
	info: 'bi-info-circle-fill'
	};
	const COLORS = {
		success: 'text-bg-success',
		error: 'text-bg-danger',
		info: 'text-bg-primary'
	};

	window.showToast = function(type, message){
		const container = document.getElementById("toastContainer");

		const iconClass = ICONS[type] || ICONS.info;
		const colorClass = COLORS[type] || COLORS.info;

		const toastEl = document.createElement("div");
		toastEl.className = `toast align-items-center ${colorClass} border-0`;
		toastEl.setAttribute("role", "alert");
		toastEl.innerHTML = `
			<div class="d-flex">
				<div class="toast-body">
					<i class="bi ${iconClass} me-2"></i>${message}
				</div>
				<button type="button" class="btn-close btn-close-white me-2 m-auto"
					data-bs-dismiss="toast"></button>
			</div>
		`;

		container.appendChild(toastEl);

		const bsToast = new bootstrap.Toast(toastEl, { delay: 3500 });
		bsToast.show();

		// 닫힌뒤 DOM에서 제거
		toastEl.addEventListener("hidden.bs.toast", () => toastEl.remove());
	};

	// 서버 리다이렉트로 넘어온 메세지 출력
	document.addEventListener("DOMContentLoaded", function(){
		const flashData = document.getElementById("flashToastData");
		if(flashData){
			const type = flashData.getAttribute("data-type") || "info";
			const msg = flashData.getAttribute("data-msg");
			if(msg) window.showToast(type, msg);
		}
	});
})();