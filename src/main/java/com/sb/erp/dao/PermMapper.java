package com.sb.erp.dao;

import org.apache.ibatis.annotations.Mapper;

import com.sb.erp.dto.AuthPermDto;

@Mapper
public interface PermMapper {

	AuthPermDto selectByEmpId(int empId);

}
