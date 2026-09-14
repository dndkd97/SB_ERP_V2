package com.sb.erp.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.sb.erp.dto.ApprLineDto;

@Mapper
public interface ApprLineMapper {
	public int insertLine(ApprLineDto dto);
	public int updateLineStatus(ApprLineDto dto);
	public ApprLineDto selectLineByOrder(ApprLineDto dto);
	public List<ApprLineDto> selectLinesByDocId(int docId);
	public int updateLineEmp(ApprLineDto dto);
}
