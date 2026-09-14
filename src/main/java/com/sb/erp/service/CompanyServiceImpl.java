package com.sb.erp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sb.erp.dao.CompanyMapper;
import com.sb.erp.dao.DeptMapper;
import com.sb.erp.dto.ComSearchDto;
import com.sb.erp.dto.CompanyDto;
import com.sb.erp.dto.StatsComDto;

@Service
public class CompanyServiceImpl implements CompanyService {
	@Autowired CompanyMapper dao;
	@Autowired DeptMapper deptDao;

	@Override
	public List<CompanyDto> list(ComSearchDto dto) {
		// keyword, onepagelist, (pstarValue-1)*onepagelist
		dto.setPstartno((dto.getPstartno()-1)*dto.getOnepagelist());
		return dao.selectAll(dto);
	}

	@Override
	public int add(CompanyDto dto) {
		if(dto.getBizNo() != null && dao.selectByBizNo(dto.getBizNo()) != null) {
			throw new IllegalArgumentException("중복된 사업자 번호");
		}
		return dao.insert(dto);
	}

	@Override
	public CompanyDto isDuplicateBizNo(String bizNo) {
		return dao.selectByBizNo(bizNo);
	}

	@Override
	public CompanyDto selectOneById(int comId) {
		return dao.selectOneById(comId);
	}

	@Override
	public int update(CompanyDto dto) {
		return dao.update(dto);
	}

	@Override
	public int delete(int comId) {
		if(deptDao.countActiveDepts(comId) > 0) {
			throw new IllegalArgumentException("하위 부서가 존재하는 경우, 회사 삭제 불가능");
		}
		return dao.delete(comId);
	}

	@Override
	public List<CompanyDto> getSuggest(String keyword) {
		return dao.selectSuggest(keyword);
	}

	@Override
	public int listTotal(ComSearchDto search) {
		return dao.listTotal(search);

	}

	@Override
	public StatsComDto selectStats() {
		return dao.selectStats();
	}

	@Override
	public CompanyDto selectOneByEmpId(int empId) {
		return dao.selectOneByEmpId(empId);
	}

}
