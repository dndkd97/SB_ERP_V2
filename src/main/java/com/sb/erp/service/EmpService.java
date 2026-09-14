package com.sb.erp.service;

import java.util.List;

import com.sb.erp.dto.EmpAuthDto;
import com.sb.erp.dto.EmpDto;
import com.sb.erp.dto.EmpSearchDto;

public interface EmpService {
	
	
	// ─── 조회 ────────────────────────
	// 상세 보기
	EmpDto selectByEmpId(int empId);
	
	// 이메일을 기준으로 사용자 정보 확인
	EmpDto selectByEmpEmail(String empEmail);

	// 사원 목록(검색)
	List<EmpDto> search(EmpSearchDto dto);
	
	// paging
	int selectCnt(EmpSearchDto dto);
	
	// 부서 id를 통해 사원정보 조회
	List<EmpDto> selectByDeptId(int deptId);
	
	
	// ─── 등록/수정 ─────────────────────
	// 사원 등록
	int insert(EmpDto dto);

	// 사원 정보 수정
	int update(EmpDto dto);
	
	
	// ─── 중복 검사 ─────────────────────
	boolean isEmailDuplicate(String empEmail);
	boolean isMobileDuplicate(String empMobile);
	boolean isEmpNoDuplicate(String empNo);
	
	
	// ─── 비밀번호 ──────────────────────
	// 비밀번호 재설정
	int updatePassByEmpId(EmpDto dto);
	
	// 관리자 초기화 (사번으로)
	int resetPassByEmpNo(int empId); 
	
	// 본인 변경
	int changePassword(int empId, String currentPass, String newPass);
	
	// 기존 비밀번호와 일치 확인
	boolean matchPassword(EmpDto dto);
	
	// 비밀번호 찾기시 해당하는 사원 정보가 있는지 확인
	EmpDto selectForVerify(EmpDto dto);
		
	
	// ─── 권한 표시 ─────────────────────
	// 회사 아이디를 기준으로 권한 정보와 엮여있는 사원 정보 확인
	List<EmpAuthDto> selectAuthByComId();
	
	// 비밀번호 분실시 본인 확인 - session(empId)기반
	Object selectAuthByEmpId(int empId);

	// 비밀번호 분실시 본인 확인 - EMP_ID로만 조회해서 업데이트
	int updatePassByEmpIdOnly(EmpDto dto);
}