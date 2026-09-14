package com.sb.erp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sb.erp.dao.ProjectMemberMapper;
import com.sb.erp.dto.EmpDto;
import com.sb.erp.dto.ProjectMemberDto;

@Service
public class ProjectMemberServiceImpl implements ProjectMemberService{
	@Autowired ProjectMemberMapper dao;
	
	//프로젝트 멤버 추가
	@Override public int insert(ProjectMemberDto dto) { 
	    if (dao.existsMember(dto) > 0) {
	        throw new IllegalArgumentException("이미 프로젝트에 참여 중인 사원입니다.");
	    }
		return dao.insert(dto); }
	
	//프로젝트 멤버 삭제
	@Override public int delete(int pmId) {  return dao.delete(pmId); }
	
	//프로젝트 참여 인원 조회
	@Override public List<ProjectMemberDto> select(int proId) { return dao.select(proId); }
	
	//해당 프로젝트에 참여 중인 멤버 목록
	@Override public List<ProjectMemberDto> selectByproject(int projectProId) {  return dao.selectByproject(projectProId); }
	
	//프로젝트 등록-메버 이름,유효성 체크
	@Override public ProjectMemberDto selectOne(int pmId) {  return dao.selectOne(pmId); }
	
	//사원 이름,번호 조회
	@Override public List<EmpDto> searchEmpForProject(int comId, String keyword) { return dao.searchEmpForProject(comId, keyword); }

}
