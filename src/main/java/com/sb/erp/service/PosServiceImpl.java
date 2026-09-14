package com.sb.erp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sb.erp.dao.PosMapper;
import com.sb.erp.dto.PosDto;
import com.sb.erp.util.SecurityUtil;

@Service
public class PosServiceImpl implements PosService {

    @Autowired PosMapper dao;

    // ─── 조회 ────────────────────────────
    @Override
    public List<PosDto> selectAll() {
        return dao.selectAll(SecurityUtil.getCurrentComId());
    }

    @Override
    public PosDto selectOneById(int posId) {
        return dao.selectOneById(posId, SecurityUtil.getCurrentComId());
    }

    // ─── 등록 ────────────────────────────
    @Override
    public int insert(PosDto dto) {
        dto.setComId(SecurityUtil.getCurrentComId());
        return dao.insert(dto);
    }

    // ─── 수정 ────────────────────────────
    @Override
    public int update(PosDto dto) {
        dto.setComId(SecurityUtil.getCurrentComId());
        return dao.update(dto);
    }

    // ─── 삭제 ────────────────────────────
    // 직급을 사용중인 사원이 있으면 차단(-1), 사용 사원이 없으면 삭제(1)
    @Override
    public int delete(int posId) {
        int comId = SecurityUtil.getCurrentComId();

        // 사용중인 사원 확인
        int usingCount = dao.countEmpUsing(posId, comId);
        if (usingCount > 0) return -1;

        // 삭제 진행
        PosDto dto = new PosDto();
        dto.setPosId(posId);
        dto.setComId(comId);
        return dao.delete(dto);
    }

    // ─── 중복 검사 ────────────────────────
    @Override
    public boolean isPosCodeDuplicate(String posCode, Integer excludePosId) {
        return dao.countByPosCode(posCode, SecurityUtil.getCurrentComId(), excludePosId) > 0;
    }
}