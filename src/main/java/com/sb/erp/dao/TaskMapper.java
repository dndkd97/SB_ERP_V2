package com.sb.erp.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.sb.erp.dto.MyWeeklyReportDto;
import com.sb.erp.dto.TaskDto;
import com.sb.erp.dto.TaskSearchDto;

@Mapper
public interface TaskMapper {
	//태스크 등록
	public int insert(TaskDto dto);
	
	//해당 프로젝트-태스크 페이징
	public List<TaskDto> selectAll(TaskSearchDto search);
	public int selectCnt (int proId);
	
	//태스크 삭제
	public int delete(int taskId);
	
	//태스크 삭제
	public int update(TaskDto dto);
	
	//태스크 상세
	public TaskDto select(int taskId);
	
	//태스크 주간 보고서
	public MyWeeklyReportDto myWeeklyReport(@Param("empId") int empId);
	
	//지연 태스크 목록
	public List<String>delayedTaskNames(@Param("empId")int empId);
	
	//pdf보고서 생성
	public byte[] createMyWeeklyReport(MyWeeklyReportDto dto);
	
	//내 태스크 목록조회
	public List<TaskDto> selectMyTasks(TaskSearchDto search);
	public int selectMyTasksCount(TaskSearchDto search);
	
	//태스크 목록
	public List<TaskDto> selectTaskList(int proId);
	
}
