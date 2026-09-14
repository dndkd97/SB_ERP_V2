package com.sb.erp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sb.erp.dao.ApprFormMapper;
import com.sb.erp.dto.ApprFormDto;
import com.sb.erp.dto.ApprFormSearchDto;
import com.sb.erp.dto.CompanySearchDto;

@Service
public class ApprFormServiceImpl implements ApprFormService {
	@Autowired ApprFormMapper dao;  
	
	// 양식 content, schema 정상적으로 들어가있는지 방어코드
	private void contentXor(ApprFormDto dto) {
		boolean hasContent = dto.getForContent() != null &&
				!dto.getForContent().isBlank();
		boolean hasSchema = dto.getForSchema() != null &&
				!dto.getForSchema().isBlank();
		
		// 둘중 하나는 있어야 오류안뱉고 넘어감
		if(hasContent == hasSchema) {
			throw new IllegalArgumentException("양식 내용은 에디터 작성 또는 AI 생성중 하나만 선택해야 합니다.");
		}
	}
	
	@Override
	public String getCompanyName(int comId) {
		return dao.getCompanyName(comId);
	}
	
	@Override
	public List<CompanySearchDto> searchCompany(String keyword) {
		return dao.searchCompany(keyword);
	}

	@Override
	public int listFormCnt(ApprFormSearchDto dto) {
		return dao.listFormCnt(dto);
	}
	
	///////////////////////// 양식 관련 기능 //////////////////////////////////
	
	@Override
	public ApprFormDto selectFormAll(ApprFormDto dto) {
		return dao.selectFormAll(dto);
	}

	@Override
	public int insertForm(ApprFormDto dto) {
		
		// forStatus null로 들어왔을때 오류 방지
		if(dto.getForStatus() == null) {
			dto.setForStatus(false);
		}
		
		contentXor(dto);
		
		return dao.insertForm(dto);
	}

	@Override
	@Transactional
	public int updateForm(ApprFormDto dto) {
		
		contentXor(dto);
		
		// 원본 데이터 넣기
		ApprFormDto original = dao.selectFormAll(dto);
		
		// 공백 줄바꿈 이것저것 다빼고 순수 택스트만 비교해야 버전이 안올라감.....
		String origContent = original.getForContent() == null ? "" : original.getForContent().replaceAll("\\s+","");
		String dtoContent = dto.getForContent() == null ? "" :dto.getForContent().replaceAll("\\s+", "");
		
		String origTitle = original.getForTitle() == null ? "" : original.getForTitle().replaceAll("\\s+","");
		String dtoTitle = dto.getForTitle() == null ? "" :dto.getForTitle().replaceAll("\\s+", "");
		
		String origSchema = original.getForSchema() == null ? "" : original.getForSchema().replaceAll("\\s+", "");
		String dtoSchema = dto.getForSchema() == null ? "" : dto.getForSchema().replaceAll("\\s+", "");
		
		// insert, update 분기점 확인 / 데이터가 바뀌었는지 원본과 대조
		boolean test = !origContent.equals(dtoContent)
					|| !origTitle.equals(dtoTitle)
					|| !origSchema.equals(dtoSchema);
		
		// 확인후 처리
		if(test) { // 중요 데이터가 바뀌었을경우 version +1 처리
			dto.setCreatedAt(original.getCreatedAt());
			return dao.updateFormNewVersion(dto);
		}
		else { // 중요 데이터가 바뀌지 않았을경우 일반 update 처리
			return dao.updateForm(dto);
		}
	}
	
	
	@Override
	public int deleteForm(ApprFormDto dto) {
		return dao.deleteForm(dto);
	}

	@Override
	public List<ApprFormDto> selectFormList(ApprFormSearchDto dto) {
		return dao.selectFormList(dto);
	}

	@Override
	public String findByCode(ApprFormDto dto) {
		return dao.findByCode(dto);
	}
	
	@Override
	public List<ApprFormDto> selectFormVersions(int forId){
		return dao.selectFormVersions(forId);
	}

	///////////////////////// 양식 관련 기능 //////////////////////////////////
	
}
