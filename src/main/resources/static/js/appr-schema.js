/* json 스키마 파싱 */
function renderSchema(field, editable){
	const req = field.required ? '<span class="text-danger">*</span>' : '';
	let inputHtml = '';
	
	switch(field.type){
	case 'date':
		inputHtml = editable
		? `<input type="date" class="form-control" id="dyn_${field.key}"/>`
		: `<div class="view-val view-val-empty">YYYY-MM-DD</div>`;
		break;
	case 'number':
		inputHtml = editable
		? `<input type="number" class="form-control" id="dyn_${field.key}"/>`
		: `<div class="view-val view-val-empty">숫자</div>`;
		break;
	case 'select':
		const opts = (field.options || []).map(o => `<option>${o}</option>`).join('');
		inputHtml = editable
		? `<select class="form-select" id="dyn_${field.key}"><option value="">선택</option>${opts}</select>`
		: `<div class="view-val view-val-empty" style="min-height:60px;"></div>`;
		break;
	case 'textarea':
		inputHtml = editable
			? `<textarea class="form-control" rows="3" id="dyn_${field.key}"></textarea>`
			: `<div class="view-val view-val-empty" style="min-height:60px;"></div>`;
		break;
	// text
	default:
		inputHtml = editable
			? `<input type="text" class="form-control" id="dyn_${field.key}"/>`
			: `<div class="view-val view-val-empty">텍스트</div>`;
	}
	
	return `<div class="appr-dyn-field">
		<label>${field.label} ${req}</label>
		${inputHtml}
	</div>`;
}