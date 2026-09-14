package com.sb.erp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.sb.erp.dao.NoticeMapper;
import com.sb.erp.dto.NoticeDto;
import com.sb.erp.dto.NoticeSearchDto;
import com.sb.erp.util.FileUploadDto;
import com.sb.erp.util.FileUploadType;
import com.sb.erp.util.FileUploadUtil;

@Service
public class NoticeServiceImpl implements NoticeService{
	
	    @Autowired NoticeMapper noticeMapper;

	    @Override // 공지등록
	    public int insert(NoticeDto dto , MultipartFile file) {
	        if (dto.getBcontent() != null && dto.getBcontent().contains("긴급")) {
	            int urgentCount = noticeMapper.countUrgentNotices(dto.getComId());
	            if (urgentCount >= 5) {
	                throw new IllegalStateException("긴급 공지는 최대 5개까지만 등록할 수 있습니다. 기존 긴급 공지를 해제한 후 다시 시도해주세요.");
	            }
	        }
			if (file != null && !file.isEmpty()) {
				FileUploadDto uploaded = FileUploadUtil.upload(file, FileUploadType.NOTICE_ATTACH);
				dto.setBfile(uploaded.getFileUrl()); // DB에는 웹 접근용 URL을 저장 (/upload/notice/attach/xxxx.pdf)
			}
			// 첨부파일이 없으면 bfile은 null로 두고, insert 쿼리의 <if>가 알아서 컬럼을 생략한다.
	        return noticeMapper.insert(dto);
	    }

	    @Override // 공지수정
	    public int update(NoticeDto dto, MultipartFile file) {
	        NoticeDto origin = noticeMapper.select(dto.getBno());
	        boolean wasUrgent = origin != null && origin.getBcontent() != null && origin.getBcontent().contains("긴급");
	        boolean willBeUrgent = dto.getBcontent() != null && dto.getBcontent().contains("긴급");

	        // 원래 긴급이 아니었는데 수정으로 긴급이 되려는 경우만 체크 (이미 긴급이었으면 카운트 안 늘어나니 통과)
	        if (willBeUrgent && !wasUrgent) {
	            int urgentCount = noticeMapper.countUrgentNotices(dto.getComId());
	            if (urgentCount >= 5) {
	                throw new IllegalStateException("긴급 공지는 최대 5개까지만 등록할 수 있습니다. 기존 긴급 공지를 해제한 후 다시 시도해주세요.");
	            }
	        }
	    	
			if (file != null && !file.isEmpty()) {
				// 새 파일을 올리기 전에 기존 첨부파일 URL을 먼저 확보해둔다. (교체 후 정리용)
				String oldFileUrl = origin != null ? origin.getBfile() : null;

				FileUploadDto uploaded = FileUploadUtil.upload(file, FileUploadType.NOTICE_ATTACH);
				dto.setBfile(uploaded.getFileUrl());

				if (oldFileUrl != null && !oldFileUrl.isEmpty()) {
					FileUploadUtil.delete(FileUploadUtil.resolveDiskPath(oldFileUrl)); // 교체된 옛 파일 정리
				}
			}
			// 새 파일이 없으면 dto.getBfile()은 null → update 쿼리의 <if>가 기존 값을 그대로 유지시켜준다.
	        return noticeMapper.update(dto);
	    }

	    @Override // 공지삭제
	    public int delete(int bno) {
			NoticeDto origin = noticeMapper.select(bno);
			if (origin != null && origin.getBfile() != null && !origin.getBfile().isEmpty()) {
				FileUploadUtil.delete(FileUploadUtil.resolveDiskPath(origin.getBfile())); // 게시글과 함께 첨부파일도 정리
			}
	        return noticeMapper.delete(bno);
	    }

	    @Override // 공지 상세 조회
	    public NoticeDto select(int bno) { return noticeMapper.select(bno); }

	    @Override // 공지 조회수 증가
	    public int updateHit(int bno) { return noticeMapper.updateHit(bno); }

	    @Override  // 페이징
	    public List<NoticeDto> selectAll(NoticeSearchDto search) {
	    	search.setPstartno((search.getPstartno()-1)*search.getOnepagelist());
	        return noticeMapper.selectAll(search);
	    }

	    @Override // 전체 카운트
	    public int selectCount(NoticeSearchDto search) { return noticeMapper.selectCount(search); }

		/* 이거 안쓰는거같은데
		 * @Override // 검색+페이징 public List<NoticeDto> selectNoticeList(NoticeSearchDto
		 * search) { return noticeMapper.selectNoticeList(search); }
		 */

	    @Override // 검색 결과 카운트
	    public long selectCountNoticeList(NoticeSearchDto search) { return noticeMapper.selectCountNoticeList(search); }

		@Override // 긴급 공지 리스트
		public List<NoticeDto> selectUrgentNotices(NoticeSearchDto search) {  return noticeMapper.selectUrgentNotices(search); }

		@Override // 긴급 공지 갯수 체크용
		public int countUrgentNotices(Integer comId) {  return noticeMapper.countUrgentNotices(comId); }

		@Override  //긴급 5개 상단 고정 + 나머지 일반 목록 리스트
		public List<NoticeDto> getNoticeListWithUrgent(NoticeSearchDto search) {
		    // 1. 긴급 top5 먼저 조회 (pstartno 변형 전에 해야 함, urgent 쿼리는 offset 안 쓰니 순서 상관은 없지만 명확하게 먼저)
		    List<NoticeDto> urgentList = noticeMapper.selectUrgentNotices(search);

		    // 2. pinnedBnos 세팅 (selectAll, selectCountNoticeList 둘 다 이 값 참조함)
		    search.setPinnedBnos(urgentList.stream().map(NoticeDto::getBno).toList());

		    // 3. selectAll 호출 -> 이 안에서 pstartno가 offset으로 변형됨
		    List<NoticeDto> pagedList = selectAll(search);

		    List<NoticeDto> result = new java.util.ArrayList<>();
		    result.addAll(urgentList);
		    result.addAll(pagedList);
		    return result;
		}
	    
	    
	    
	    
}