package com.sb.erp.service;

import java.util.List;

import com.sb.erp.dto.MyWeeklyReportDto;
import com.sb.erp.dto.TaskDto;
import com.sb.erp.dto.TaskSearchDto;

public interface TaskService {
	
	//태스크 추가
	public int insert(TaskDto dto);
	
	//태스크 참여 명단 조회
	public List<TaskDto> selectAll(TaskSearchDto search);
	
	//태스크 개수 카운트
	public int selectCnt(int proId);
	
	//태스크 삭제
	public int delete(int taskId);
	
	//태스크 수정
	public int update(TaskDto dto);
	
	//태스크 상세 조회
	public TaskDto select(int taskId);
	
	//태스크 수정뷰
	public TaskDto taskEditView(int taskId);
	
	//태스크 주간 보고서
	public MyWeeklyReportDto myWeeklyReport(int empId);
	
	//지연 태스크 목록
	public List<String>delayedTaskNames(int empId);
	
	//pdf보고서 생성
	public byte[] createMyWeeklyReport(MyWeeklyReportDto dto);
	
	//내 태스크 목록조회
	public List<TaskDto> selectMyTasks(TaskSearchDto search);
	public int selectMyTasksCount(TaskSearchDto search);
	
	//태스크 목록
	public List<TaskDto> selectTaskList(int proId);
}
