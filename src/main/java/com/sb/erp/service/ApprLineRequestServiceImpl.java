package com.sb.erp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sb.erp.dao.ApprLineMapper;
import com.sb.erp.dao.ApprLineRequestMapper;
import com.sb.erp.dao.ApprLogMapper;
import com.sb.erp.dto.ApprLineDto;
import com.sb.erp.dto.ApprLineRequestDto;
import com.sb.erp.dto.ApprLogDto;

@Service
public class ApprLineRequestServiceImpl implements ApprLineRequestService{

	@Autowired ApprLineRequestMapper reqDao;
	@Autowired ApprLogMapper logDao;
	@Autowired ApprLineMapper lineDao;
	
	@Override
	public int requestChange(ApprLineRequestDto dto) {
		return reqDao.insertRequest(dto);
	}

	@Override
	public List<ApprLineRequestDto> getPendingRequests() {
		return reqDao.selectPendingRequest();
	}

	@Override
	public int countPending() {
		return reqDao.countPending();
	}

	@Override
	@Transactional
	public boolean approveRequest(int reqId, int newEmpId, int proEmpId) {
		ApprLineRequestDto req = reqDao.selectRequestById(reqId);
		
		// 이미 처리됐거나 존재하지 않을때
		if(req == null || !"REQ".equals(req.getReqStatus())) {
			return false;
		}
		
		ApprLineRequestDto updateDto = new ApprLineRequestDto();
		updateDto.setReqId(reqId);
		updateDto.setReqStatus("APP");
		updateDto.setNewEmpId(newEmpId);
		updateDto.setProEmpId(null);
		
		int updated = reqDao.updateRequestStatus(updateDto);
		
		// 동시 처리시 방어
		if(updated == 0) { return false; }
		
		// 결재선 담당자 교체
		ApprLineDto lineDto = new ApprLineDto();
		lineDto.setLinId(req.getLinId());
		lineDto.setEmpId(newEmpId);
		lineDao.updateLineStatus(lineDto);
		
		// 위임 승인 로그
		ApprLogDto log = new ApprLogDto();
		log.setDocId(req.getDocId());
		log.setOriEmpId(req.getOriEmpId());
		log.setActEmpId(newEmpId);
		log.setPerEmpId(proEmpId);
		logDao.insertLog(log);
		
		return true;
	}

	@Override
	@Transactional
	public boolean rejectRequest(int reqId, int proEmpId) {
		ApprLineRequestDto updateDto = new ApprLineRequestDto();
		updateDto.setReqId(reqId);
		updateDto.setReqStatus("REJ");
		updateDto.setProEmpId(proEmpId);
		return reqDao.updateRequestStatus(updateDto) > 0;
	}
	
}
