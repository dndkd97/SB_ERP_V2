package com.sb.erp.service;

import java.util.List;
import com.sb.erp.dto.PosDto;

public interface PosService {
	
	// ─── 조회 ────────────────────────────
    List<PosDto> selectAll();
    PosDto selectOneById(int posId);

    // ─── 등록 / 수정 / 삭제 ──────────────
    int insert(PosDto dto);
    int update(PosDto dto);
    // 반환값: 1(성공), 0(실패), -1(사용중인 사원 존재)
    int delete(int posId);

    // ─── 중복 검사 (AJAX) ────────────────
    // 수정 시 자신 제외
    boolean isPosCodeDuplicate(String posCode, Integer excludePosId);
    
}
