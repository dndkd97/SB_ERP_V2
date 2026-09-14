package com.sb.erp.service;

import java.util.List;

import com.sb.erp.dto.DeptDto;
import com.sb.erp.dto.DeptTransferExecuteForm;
import com.sb.erp.dto.DeptTransferImpactDto;
import com.sb.erp.dto.DeptTransferLogDto;
import com.sb.erp.dto.DeptTransferLogSearchDto;
import com.sb.erp.dto.PendingDeptDto;

public interface DeptTransferService {

	//영향도 조회
	DeptTransferImpactDto getImpact(Integer comId, Integer deptId) throws IllegalAccessException;

	//이관 취소
	int cancelTransfer(Integer comId, Integer deptId) throws IllegalAccessException;

	//이관 최종 실행
	void executeTransfer(DeptTransferExecuteForm form, Integer empId) throws IllegalAccessException;

	//이관 대기(PENDING_DELETE) 부서 목록 조회
	List<PendingDeptDto> getPendingTransferDepts(Integer comId, String keyword);

	//부서 이관 이력 조회
	List<DeptTransferLogDto> searchTransferLogs(Integer comId, DeptTransferLogSearchDto searchForm);

	//부서 이관 이력 전체 갯수
	int listTotal(Integer comId, DeptTransferLogSearchDto search);
} 
