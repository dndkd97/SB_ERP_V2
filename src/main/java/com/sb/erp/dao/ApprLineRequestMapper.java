package com.sb.erp.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.sb.erp.dto.ApprLineDto;
import com.sb.erp.dto.ApprLineRequestDto;

@Mapper
public interface ApprLineRequestMapper {
	public int insertRequest(ApprLineRequestDto dto);
	public List<ApprLineRequestDto> selectPendingRequest();
	public ApprLineRequestDto selectRequestById(int reqId);
	public int updateRequestStatus(ApprLineRequestDto dto);
	public int countPending();
}
