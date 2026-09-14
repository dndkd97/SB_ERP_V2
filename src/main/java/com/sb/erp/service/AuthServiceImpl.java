package com.sb.erp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sb.erp.dao.AuthMapper;
import com.sb.erp.dto.AuthPermDto;
import com.sb.erp.dto.EmpAuthDto;
import com.sb.erp.util.SecurityUtil;

@Service
public class AuthServiceImpl implements AuthService {
	@Autowired AuthMapper dao;

	// ─── 권한 관리 ────────────────
	@Override
	public List<AuthPermDto> selectAll() {
		return dao.selectAll(SecurityUtil.getCurrentComId());
	}

	@Override
	public AuthPermDto selectOneById(int autId) {
		return dao.selectOneById(autId, SecurityUtil.getCurrentComId());
	}

	@Override
	public int insert(AuthPermDto dto) {
		dto.setComId(SecurityUtil.getCurrentComId());
		return dao.insert(dto);
	}

	@Override
	public int update(AuthPermDto dto) {
		dto.setComId(SecurityUtil.getCurrentComId());
		return dao.update(dto);
	}

	@Override
	public int delete(int autId) {
		AuthPermDto dto = new AuthPermDto();
		dto.setAutId(autId);
		dto.setComId(SecurityUtil.getCurrentComId());
		return dao.delete(dto);
	}

	
	
	// ─── 사원-권한 매핑 ───────────────
	@Override
	public List<EmpAuthDto> selectEmpsByAuthId(int autId) {
		return dao.selectEmpsByAuthId(autId, SecurityUtil.getCurrentComId());
	}

	@Override
	public List<EmpAuthDto> selectAuthsByEmpId(int empId) {
		return dao.selectAuthsByEmpId(empId, SecurityUtil.getCurrentComId());
	}

	// 권한 부여
	@Override
	public int grantAuth(EmpAuthDto dto) {
		// 권한이 현재 회사 소속인지 확인
		AuthPermDto auth = dao.selectOneById(dto.getAutId(), SecurityUtil.getCurrentComId());
		if (auth == null)
			return 0;

		// 대상 사원이 현재 회사 소속인지 확인은 컨트롤러 진입 시 EmpService 조회로 커버
		return dao.grantAuth(dto);
	}

	// 권한 회수
	@Override
	public int revokeAuth(EmpAuthDto dto) {
		return dao.revokeAuth(dto);
	}

}
