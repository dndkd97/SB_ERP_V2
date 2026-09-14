package com.sb.erp.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.sb.erp.dto.ApprDocDto;
import com.sb.erp.dto.ApprDocInitResponseDto;
import com.sb.erp.dto.ApprFormDto;
import com.sb.erp.dto.ApprLineDto;

@Mapper
public interface ApprDocMapper {
	
	// 문서 작성 파트
	public List<ApprFormDto> findForm(ApprDocDto dto);
	public ApprDocInitResponseDto initResponse(ApprDocDto dto);
	public int insertDoc(ApprDocDto dto);
	public ApprFormDto getForm(Map<String,Object> map);
	
	// 문서 조회 파트
	public Map<String, Object> selectDocCnt(ApprDocDto dto);
	public List<Map<String, Object>> selectMyHistoryDocs(ApprDocDto dto);
	public List<Map<String, Object>> selectMyTodoDocs(ApprDocDto dto);
	public ApprDocDto selectDocDetail(int docId);
	
	// 결재선 관련 파트
	public List<ApprLineDto> approversByEmpId(ApprDocDto dto);
	public int updateDocStatus(ApprDocDto dto);
	public List<ApprLineDto> selectDeptEmpsForLines(int deptId); 
	public int cntApprovers(@Param("deptId") int deptId,
							@Param("empId") int empId);
	
	// 페이징
	public int selectMyHistoryDocsCnt(ApprDocDto dto);
	public int selectMyTodoDocsCnt(ApprDocDto dto);
	
	// 동시성 검증용
	public int getRevision(int docId);
	public int chkDocRevision(ApprDocDto dto);
}
