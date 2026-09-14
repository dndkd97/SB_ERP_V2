package com.sb.erp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sb.erp.api.ReportApi;
import com.sb.erp.dao.TaskMapper;
import com.sb.erp.dto.MyWeeklyReportDto;
import com.sb.erp.dto.TaskDto;
import com.sb.erp.dto.TaskSearchDto;

@Service
public class TaskServiceImpl implements TaskService {
	@Autowired TaskMapper dao;
	@Autowired ReportApi reportApi;
		
	//태스크 추가
	@Override public int insert(TaskDto dto) {  return dao.insert(dto); }

	/*paging*/
	@Override public List<TaskDto> selectAll(TaskSearchDto search) {  return dao.selectAll(search); }
	@Override public int selectCnt(int proId) {  return dao.selectCnt(proId); }
	
	//태스크 삭제
	@Override public int delete(int taskId) {  return dao.delete(taskId); }
	
	//태스크 수정
	@Override public int update(TaskDto dto) {  return dao.update(dto); }
	
	//태스크 상세조회
	@Override public TaskDto select(int taskId) {  return dao.select(taskId); }
	
	//태스크 수정뷰
	@Override public TaskDto taskEditView(int taskId) { return dao.select(taskId); }

	//태스크 주간 보고서
	@Override public MyWeeklyReportDto myWeeklyReport(int empId) {  return dao.myWeeklyReport(empId); }

	//지연 태스크 목록
	@Override public List<String> delayedTaskNames(int empId) {  return dao.delayedTaskNames(empId); }
	
	//pdf보고서 생성
	@Override public byte[] createMyWeeklyReport(MyWeeklyReportDto dto) {
	    return reportApi.createMyWeeklyReport(dto); }
	
	//내 태스크 목록조회 paging
	@Override public List<TaskDto> selectMyTasks(TaskSearchDto search) {  return dao.selectMyTasks(search); }
	@Override public int selectMyTasksCount(TaskSearchDto search) {  return dao.selectMyTasksCount(search); }

	//태스크 목록
	@Override public List<TaskDto> selectTaskList(int proId) {  return dao.selectTaskList(proId); }


	
	
	
}
