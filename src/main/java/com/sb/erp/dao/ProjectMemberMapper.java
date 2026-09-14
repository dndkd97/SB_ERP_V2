package com.sb.erp.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.sb.erp.dto.EmpDto;
import com.sb.erp.dto.ProjectMemberDto;

@Mapper
public interface ProjectMemberMapper {
	//프로젝트 멤버 추가
	public int insert(ProjectMemberDto dto);
	
	//프로젝트 멤버 삭제
	public int delete(int pmId);
	
	//프로젝트 참여 인원 조회
	public List<ProjectMemberDto> select(int proId);
	
	//해당 프로젝트에 참여 중인 멤버 목록
	public List<ProjectMemberDto> selectByproject(int projectProId);
	
	//프로젝트 등록-멤버 이름,유효성 체크
	public ProjectMemberDto selectOne(int pmId);
	
	//사원 이름,번호 조회
	public List<EmpDto> searchEmpForProject(@Param("comId") int comId, @Param("keyword") String keyword);
	
	// 프로젝트에 동일한 사원이 이미 참여 중인지 확인 
	public int existsMember(ProjectMemberDto dto);
}