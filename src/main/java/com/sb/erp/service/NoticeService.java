package com.sb.erp.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.sb.erp.dto.NoticeDto;
import com.sb.erp.dto.NoticeSearchDto;

public interface NoticeService {
	
	// 공지 등록
	public int insert(NoticeDto dto, MultipartFile file);
	// 공지 수정
	public int update(NoticeDto dto, MultipartFile file);
	// 공지 삭제
	public int delete(int bno);
	// 상세 조회
	public NoticeDto select(int bno);
	// 조회수 증가
	public int updateHit(int bno);
	// 일반 목록   
	public List<NoticeDto> selectAll(NoticeSearchDto search);
	// 전체 카운트
	public int selectCount(NoticeSearchDto search);
	// 페이징 계산용 카운트    
	//public List<NoticeDto> selectNoticeList(NoticeSearchDto search);
	// 검색 결과 카운트
	public long selectCountNoticeList(NoticeSearchDto search);
    // 긴급 공지 리스트
    public List<NoticeDto> selectUrgentNotices(NoticeSearchDto search);
    // 긴급 공지 갯수 체크용
    public int countUrgentNotices(Integer comId);
    //긴급 5개 상단 고정 + 나머지 일반 목록 리스트
    public List<NoticeDto> getNoticeListWithUrgent(NoticeSearchDto search);
    
   
	
	}
	


