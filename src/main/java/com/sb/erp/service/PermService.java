package com.sb.erp.service;

import com.sb.erp.dto.AuthPermDto;

public interface PermService {

	AuthPermDto selectByEmpId(int empId);
}
