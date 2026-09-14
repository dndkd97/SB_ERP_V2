package com.sb.erp.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.sb.erp.dto.DeptTransferLogDto;
import com.sb.erp.dto.DeptTransferLogSearchDto;

@Mapper
public interface DeptTransferLogMapper {

	// 로그 삽입
	int insertTransferLog(DeptTransferLogDto logDto);

	// 부서 이관 이력 조회
	List<DeptTransferLogDto> searchTransferLogs(@Param("comId") Integer comId, @Param("search") DeptTransferLogSearchDto search);

	int listTotal(@Param("comId") Integer comId, @Param("search") DeptTransferLogSearchDto search);
}
