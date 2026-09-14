package com.sb.erp.util;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.sb.erp.security.CustomUserDetails;

public class SecurityUtil {

	// 현재 로그인한 유저의 comId
	public static int getCurrentComId() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated())
			return 0;
		Object principal = auth.getPrincipal();
		if (principal instanceof CustomUserDetails) {
			return ((CustomUserDetails) principal).getUser().getComId();
		}
		return 0;
	}

	// 현재 로그인한 유저의 empId
	public static int getCurrentEmpId() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated())
			return 0;
		Object principal = auth.getPrincipal();
		if (principal instanceof CustomUserDetails) {
			return ((CustomUserDetails) principal).getUser().getEmpId();
		}
		return 0;
	}

	// 현재 로그인 사용자가 ROOT 또는 ROLE_ADMIN 권한을 가졌는지 확인
	public static boolean isAdminOrRoot(Authentication auth) {
	    if (auth == null || !auth.isAuthenticated()) return false;
	    return auth.getAuthorities().stream()
	        .anyMatch(a -> a.getAuthority().equals("ROOT")
	                    || a.getAuthority().equals("ROLE_ADMIN"));
	}

	public static boolean isAdmin() {
	    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    return isAdminOrRoot(auth);
	}
	
	// 대상 데이터(targetComId)가 현재 로그인 유저의 회사와 같은지 검증.
	// 관리자/ROOT는 회사 상관없이 통과, 아니면 comId 불일치 시 예외 발생.
	public static void checkComIdAccess(Integer targetComId) {

	    int currentComId = getCurrentComId();
	    if (targetComId == null || !targetComId.equals(currentComId)) {
	        throw new AccessDeniedException("접근 권한이 없습니다.");
	    }
	}
	
}