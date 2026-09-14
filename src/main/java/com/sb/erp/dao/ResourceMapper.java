package com.sb.erp.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.sb.erp.dto.ResSearchDto;
import com.sb.erp.dto.ResDto;

@Mapper
public interface ResourceMapper {

    List<ResDto> selectResourceList(ResSearchDto search);

    int selectResourceCount(ResSearchDto search);

    ResDto selectResourceDetail(int resId);

    int insertResource(ResDto resourceDto);

    int updateResource(ResDto resourceDto);

    int deleteResource(int resId);

    // 자원코드 중복 체크
	ResDto selectByResCode(ResDto resDto);

	// 예약 할 수 있는 회사의 자원 정보
	List<ResDto> selectResListForResv(ResSearchDto search);
}
