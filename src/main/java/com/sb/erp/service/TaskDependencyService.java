package com.sb.erp.service;

import java.util.List;
import com.sb.erp.dto.TaskDto;

public interface TaskDependencyService {
	
	   //선행 태스크를 지정하여 태스크 생성
	   public int insertWithParent(TaskDto dto);
	   
	   //태스크 의존성 트리 조회
	   public List<TaskDto> selectTaskDependencies(int proId);
	   
	   //태스크 일정 및 선행 태스크 수정
	   public int updateTaskSchedule(TaskDto dto);
	   
	   //벌크 연쇄 업데이트
	   public void updateBatchTaskSchedule(List<TaskDto> list);
	   
	   //후속 작업
	   public List<TaskDto> selectImpactTasks(int taskId);

}
