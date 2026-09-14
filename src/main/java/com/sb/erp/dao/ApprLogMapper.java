package com.sb.erp.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.sb.erp.dto.ApprLogDto;


@Mapper
public interface ApprLogMapper {
	public int insertLog(ApprLogDto dto);
	public List<ApprLogDto> selectLogsByDocId(int docId);
}
