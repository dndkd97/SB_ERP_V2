package com.sb.erp.service;

import java.util.List;

import com.sb.erp.dto.ApprLineRequestDto;

public interface ApprLineRequestService {
	public int requestChange(ApprLineRequestDto dto);
	public List<ApprLineRequestDto> getPendingRequests();
	public int countPending();
	public boolean approveRequest(int reqId, int newEmpId, int proEmpId);
	public boolean rejectRequest(int reqId, int proEmpId);
}
