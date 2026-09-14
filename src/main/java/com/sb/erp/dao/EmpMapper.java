package com.sb.erp.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.sb.erp.dto.EmpAuthDto;
import com.sb.erp.dto.EmpDto;
import com.sb.erp.dto.EmpSearchDto;

@Mapper
public interface EmpMapper {
	
	// ─── 사원 정보 ────────────────────────────────────
	
    // 상세조회
    public EmpDto selectByEmpId(@Param("empId") int empId, @Param("comId") int comId);

    // 검색 필터 + 페이징(limit 추가)
    public List<EmpDto> search(EmpSearchDto dto);

    //사원 등록
    public int insert(EmpDto dto);

    //정보 수정
    public int update(EmpDto dto);
    
    //이메일 중복 유무
    public int countByEmpEmail(String empEmail);
    
    //연락처 중복 유무
    public int countByEmpMobile(String empMobile);
    
    //사번 중복 유무
    public int countByEmpNo(@Param("empNo")String empNo, @Param("comId") int comId);
    
    /*	 paging		*/
	public int selectCnt(EmpSearchDto dto); 

    // 비밀번호 찾기시 해당하는 사원 정보가 있는지 확인
	public EmpDto selectForVerify(EmpDto dto);

	// 비밀번호 재설정
	public int updatePassByEmpId(EmpDto dto);

	// 이메일을 기준으로 사용자 정보 확인
	public EmpDto selectByEmpEmail(String empEmail);

	// 아이디를 기준으로 패스워드 확인
	public String selectPassById(int empId);

	// 아이디를 기준으로 사원 정보 확인
	public List<EmpDto> selectByDeptId(int deptId);
	
	// 회사 아이디를 기준으로 권한 정보와 엮여있는 사원 정보 확인
	public List<EmpAuthDto> selectAuthByComId(int comId);

	// 비밀번호 분실 - session(empId) 기반, 본인확인 후에만 진입 가능
	public EmpDto selectAuthByEmpId(int empId);

	// 비밀번호 분실시 본인 확인 - EMP_ID로만 조회해서 업데이트
	public int updatePassByEmpIdOnly(EmpDto dto);
}