package com.sb.erp.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.sb.erp.dao.AuthMapper;
import com.sb.erp.dao.EmpMapper;
import com.sb.erp.dto.AuthUserDto;
import com.sb.erp.dto.EmpDto;
import com.sb.erp.service.EmpService;

@Service
public class CustomUserDetailsService   implements UserDetailsService{
	@Autowired EmpMapper dao;
	@Autowired AuthMapper authDao;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//		EmpDto dto = new EmpDto(); 
//		dto.setEmpEmail(username);
		AuthUserDto authDto =  authDao.readAuth(username); //  권한들: username, password, List<AuthDto>
		
		EmpDto empDto = dao.selectByEmpEmail(username); // 사용자 정보들
		return new CustomUserDetails(empDto, authDto);// 사용자정보, 사용자 로그인 정보
	}


}