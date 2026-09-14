package com.sb.erp.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.sb.erp.dto.DeptDto;
import com.sb.erp.dto.DeptSearchDto;
import com.sb.erp.dto.StatsDeptDto;

@Mapper
public interface DeptMapper {

	// 필요한 메서드 선언
	List<DeptDto> selectAll(int comId);

	int countActiveDepts(int comId);

	DeptDto selectOneById(int deptId);

	int maxSortOrder(@Param("parentId")int parentId, @Param("comId")int comId);

	int insert(DeptDto dto);

	int delete(int deptId);

	int countChildren(int deptId);

	int update(DeptDto dto);

	List<Integer> selectAllChildIds(int deptId);

	StatsDeptDto selectStats(int comId);

	int countByDept(int deptId);

	int softDelete(int deptId);

	List<DeptDto> selectAllDeptsByComId(Integer comId);

	DeptDto selectDeptCode(DeptSearchDto search);

}
