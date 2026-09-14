package com.sb.erp.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.sb.erp.dto.ApprDocImpactDto;
import com.sb.erp.dto.ApprLineImpactDto;
import com.sb.erp.dto.DeptDto;
import com.sb.erp.dto.EmpTransferDto;
import com.sb.erp.dto.PendingDeptDto;
import com.sb.erp.dto.ResvImpactDto;

@Mapper
public interface DeptTransferMapper {

	int countDeptInCompany(@Param("deptId") Integer deptId, @Param("comId") Integer comId);
	
	DeptDto selectOneById(Integer deptId);
	
	List<EmpTransferDto> findEmployeesByDept(Integer deptId);

	List<ResvImpactDto> findPendingResvByDept(Integer deptId);

	List<ApprLineImpactDto> findPendingApprLineByDept(Integer deptId);

	List<ApprDocImpactDto> findPendingApprDocsByDept(Integer deptId);

	String findPendingApprDocTitles(Integer deptId);

	List<DeptDto> findCandidateDepartments(@Param("deptId") Integer deptId, @Param("comId") Integer comId);

	List<DeptDto> findActiveDeptsExcluding(@Param("deptId") Integer deptId, @Param("comId") Integer comId);

	int updateActiveById(Integer deptId);

	int markDeleted(Integer deptId);

	int updateEmployeeDept(@Param("empId") Integer empId, @Param("newDeptId") Integer newDeptId);

	List<PendingDeptDto> findPendingTransferDepts(@Param("comId") Integer comId, @Param("keyword") String keyword);

	List<DeptDto> findAllDeptsByCompany(Integer comId);

}
