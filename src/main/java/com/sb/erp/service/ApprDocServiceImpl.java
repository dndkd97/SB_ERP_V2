package com.sb.erp.service;

import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sb.erp.dao.ApprDocMapper;
import com.sb.erp.dao.ApprLineMapper;
import com.sb.erp.dto.ApprDocDto;
import com.sb.erp.dto.ApprDocInitResponseDto;
import com.sb.erp.dto.ApprFormDto;
import com.sb.erp.dto.ApprLineDto;
import com.sb.erp.dto.DeptDto;

@Service
public class ApprDocServiceImpl implements ApprDocService{

	@Autowired ApprDocMapper dao;
	@Autowired ApprLineMapper lineDao;
	@Autowired DeptService deptDao;
	
	
	// 작성하려는 사용자의 회사 양식
	@Override
	public List<ApprFormDto> findForm(ApprDocDto dto) {
		return dao.findForm(dto);
	}
	
	// 양식 값 가져오기
	@Override
	public ApprFormDto getForm(Map<String,Object> map) {
		return dao.getForm(map);
	}

	// 결재 양식 작성하려는 사용자의 인적사항
	@Override
	public ApprDocInitResponseDto initResponse(ApprDocDto dto) {
		return dao.initResponse(dto);
	}

	// 결재 문서 작성
	@Override
	public int insertDoc(ApprDocDto dto) {

		return dao.insertDoc(dto);
	}

	// 대시보드용 통계
	@Override
	public Map<String, Object> selectDocCnt(ApprDocDto dto) {
		return dao.selectDocCnt(dto);
	}

	// 내가 결재 했던 모든 문서
	@Override
	public List<Map<String, Object>> selectMyHistoryDocs(ApprDocDto dto) {
		return dao.selectMyHistoryDocs(dto);
	}

	// 내가 결재 해야할 모든 문서
	@Override
	public List<Map<String, Object>> selectMyTodoDocs(ApprDocDto dto) {
		return dao.selectMyTodoDocs(dto);
	}

	// 상사들 다 가져오기
	@Override
	public List<ApprLineDto> approversByEmpId(ApprDocDto dto) {
		return dao.approversByEmpId(dto);
	}

	// 결재선 insert
	@Override
	@Transactional
	public boolean insertLines(ApprDocDto dto) {
		
		// 문서 데이터 넣기
		dao.insertDoc(dto);
		
		// 새로 채번된 문서 id 막기
		int docId = dto.getDocId();
		
		// 폼에서 넘어온 결재선 데이터 추출
		List<ApprLineDto> lineList = dto.getApprLines();
		
		// 결재선 순차 적재
		if(lineList != null && !lineList.isEmpty()) {
			for(ApprLineDto line : lineList) {
				line.setDocId(docId);
				lineDao.insertLine(line);
			}
		}
		return true;
	}

	// 모든 문서 카운트
	@Override
	public int selectMyHistoryDocsCnt(ApprDocDto dto) {
		return dao.selectMyHistoryDocsCnt(dto);
	}

	// 본인차례 결재 문서 카운트
	@Override
	public int selectMyTodoDocsCnt(ApprDocDto dto) {
		return dao.selectMyTodoDocsCnt(dto);
	}

	// 상세 페이지 docId 로 데이터 가져오기 
	@Override
	public ApprDocDto selectDocDetail(int docId) {
		return dao.selectDocDetail(docId);
	}

	// docId로 결재선 가져오기
	@Override
	public List<ApprLineDto> selectLinesByDocId(int docId) {
		return lineDao.selectLinesByDocId(docId);
	}

	// 결재 처리 로직
	@Override
	@Transactional
	public void processLine(int docId, int empId, String action) {
		
		// 검증
		ApprDocDto checkDoc = new ApprDocDto();
		checkDoc.setDocRevision(dao.getRevision(docId));
		checkDoc.setDocId(docId);
		
		// 테스트용 강제 오류내기
		//checkDoc.setDocRevision(22);
		
		int check = dao.chkDocRevision(checkDoc);
		if(check == 0) {
			throw new ConcurrentModificationException("이미 결재가 진행되었거나 수정된 문서입니다");
		}
		
		// 결재선 업데이트
		ApprLineDto myLine = new ApprLineDto();
		myLine.setDocId(docId);
		myLine.setEmpId(empId);
		myLine.setLinStatus(action);
		lineDao.updateLineStatus(myLine);
		
		// 반려했을시 처리
		if("REJ".equals(action)) {
			ApprDocDto doc = new ApprDocDto();
			doc.setDocId(docId);
			doc.setDocStatus("REJ");
			dao.updateDocStatus(doc);
			return;
		}
		
		// 승인했을시 다음 순서 있는지 확인
		// 전체 결재선 라인 가져옴
		List<ApprLineDto> lines = lineDao.selectLinesByDocId(docId);
		// 현재 결재한 사용자 정보
		ApprLineDto current = lines.stream()
				// 결재선 데이터의 empId와 로그인한 사용자의 empId가 일치하는 데이터만 남김
				.filter(l -> l.getEmpId() == empId)
				// 위에서 남긴 데이터의 첫번째 데이터를 가져옴
				.findFirst().orElseThrow();
		
		// 다음 결재자 정보
		ApprLineDto next = lines.stream()
				// 결재 순서가 내 순서보다 1 더 큰 데이터를 가져옴 
				.filter(l -> l.getLinOrder() == current.getLinOrder() + 1)
				// 조건에 맞는 결재자 정보 가져옴 / 없으면 null 담기
				.findFirst().orElse(null);
		
		// 다음순서 있는지 검증
		if(next != null) { // 있는경우
			ApprLineDto nextLine = new ApprLineDto();
			nextLine.setDocId(docId);
			nextLine.setEmpId(next.getEmpId());
			nextLine.setLinStatus("WAI");
			lineDao.updateLineStatus(nextLine);
		}
		else { // 없는경우, 문서 최종 승인
			ApprDocDto doc = new ApprDocDto();
			doc.setDocId(docId);
			doc.setDocStatus("APP");
			dao.updateDocStatus(doc);
		}
	}

	// 결재선
	@Override
	public List<ApprLineDto> selectDeptEmpsForLines(int deptId) {
		return dao.selectDeptEmpsForLines(deptId);
	}

	// 결재선 선택가능 인원 카운트
	@Override
	public List<DeptDto> cntApprovers(int deptId, int empId) {
		List<DeptDto> chain = deptDao.selectAncestorDepts(deptId);
		for (DeptDto d : chain) {
			int cnt = dao.cntApprovers(d.getDeptId(), empId);
			d.setEmpCount(cnt); // 부서 총인원 값을 선택가능 인원으로 씀
		}
		return chain;
	}
}
