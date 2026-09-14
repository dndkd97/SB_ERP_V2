package com.sb.erp.service;

import java.util.List;
import java.util.Map;

import com.sb.erp.dto.ResSearchDto;
import com.sb.erp.dto.ResDto;

public interface ResourceService {

	// 회사내 자원 조회
    List<ResDto> getResourceList(ResSearchDto search);

    // 전체 자원 갯수 조회
    int getResourceCount(ResSearchDto search);

    ResDto getResourceDetail(int resId);

    int insertResource(ResDto resDto);

    int updateResource(ResDto resDto);

    int deleteResource(int resId);

    // 자원코드 중복 체크
	ResDto isDuplicateResCode(ResDto resDto);

	// 예약 할 수 있는 회사의 자원 정보
	List<ResDto> getResListForResv(ResSearchDto search);
}
