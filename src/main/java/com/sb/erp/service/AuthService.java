package com.sb.erp.service;

import java.util.List;

import com.sb.erp.dto.AuthPermDto;
import com.sb.erp.dto.EmpAuthDto;

public interface AuthService {

	// ─── 권한 관리 ───────────────
	List<AuthPermDto> selectAll();

	AuthPermDto selectOneById(int autId);

	int insert(AuthPermDto dto);

	int update(AuthPermDto dto);

	int delete(int autId);
	

	// ─── 사원-권한 매핑 ────────────
	List<EmpAuthDto> selectEmpsByAuthId(int autId);

	List<EmpAuthDto> selectAuthsByEmpId(int empId);

	int grantAuth(EmpAuthDto dto);

	int revokeAuth(EmpAuthDto dto);

}
