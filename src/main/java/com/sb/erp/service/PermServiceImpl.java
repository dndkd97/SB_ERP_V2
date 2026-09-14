package com.sb.erp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sb.erp.dao.PermMapper;
import com.sb.erp.dto.AuthPermDto;

@Service
public class PermServiceImpl implements PermService {
	@Autowired PermMapper dao;
	
	@Override
	public AuthPermDto selectByEmpId(int empId) {
		return dao.selectByEmpId(empId);
	}

}
