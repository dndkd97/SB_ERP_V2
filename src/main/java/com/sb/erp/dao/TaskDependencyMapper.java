package com.sb.erp.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import com.sb.erp.dto.TaskDto;

@Mapper
public interface TaskDependencyMapper {
	
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
   
   //병목 탐색기(추후 사용 예정)
   public List<TaskDto> findCriticalPath(int proId);
   
   //동일 프로젝트의 태스크를 수정하는 동안 다른 사용자의 동시 수정을 방지하기 위해 행 잠금 획득
   public List<Integer> lockProjectTasks(int taskId);
	
}
