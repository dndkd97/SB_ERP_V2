package com.sb.erp.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.sb.erp.dto.PosDto;

@Mapper
public interface PosMapper {

	// 회사별 직급 목록 (기존)
    List<PosDto> selectAll(int comId);

    // 직급 단건 조회 (수정 화면용, com_id 가드)
    PosDto selectOneById(@Param("posId") int posId, @Param("comId") int comId);

    // 직급 등록
    int insert(PosDto dto);

    // 직급 수정
    int update(PosDto dto);

    // 직급 삭제
    int delete(PosDto dto);

    // 사원 사용 여부 확인 (삭제 전 검증)
    int countEmpUsing(@Param("posId") int posId, @Param("comId") int comId);

    // 회사 내 직급 코드 중복 확인 (등록/수정)
    int countByPosCode(@Param("posCode") String posCode,
                       @Param("comId") int comId,
                       @Param("excludePosId") Integer excludePosId);
	
}
