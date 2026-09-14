package com.sb.erp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.sb.erp.security.CustomUserDetails;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	// http 경로 설정
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http, PasswordEncoder passEncoder) throws Exception {

		http// 1. 허용경로
				.authorizeHttpRequests(auth -> auth
						// ─── 정적 리소스 + 인증 관련 ───────────
						.requestMatchers("/css/**", "/js/**", "/images/**", 
								"/api/**", "/auth/login", "/auth/confirm",
								"/auth/resetPass", "/auth/forgotResetPass","/auth/updatePass").permitAll()
						// ─── ROOT 전용 ────────────────────────
						.requestMatchers("/root/**").hasAuthority("ROOT")
						// ─── ADMIN 이상 ───────────────────────
						.requestMatchers("/admin/**").hasAnyAuthority("ROOT", "ROLE_ADMIN")
						// ─── 로그인만 하면 접근 가능 ───────────
						.requestMatchers("/", "/emp/list", "/emp/detail", "/emp/edit",
								"/emp/editPass", "/com/**", "/dept/**", "/appr/**", "/res/**", "/resv/**", 
								"/proj/**", "/notice/**", "/eval/report/detail", "/eval/report/my","/report/**",
								"/upload/**").authenticated()
						// ─── 사원/직급/권한/평가 관리 (ADMIN 전용) ────────────────
						.requestMatchers("/emp/add", "/emp/resetPass", "/emp/checkEmail", "/emp/checkMobile",
								"/emp/checkEmpNo", "/perm/**", "/pos/**", "/dept/transfer/pending",
								"/dept/transfer/list", "/dept/transfer/log", "/eval/**").hasRole("ADMIN")
					
						// ─── 그 외 ────────────────
						// .anyRequest().permitAll()
				)
				// 권한이 없는 페이지(403)에 접근했을 경우
				.exceptionHandling(ex -> ex.accessDeniedHandler((request, response, accessDeniedException) -> {
					request.getSession().setAttribute("accessDeniedMsg", "접근 권한이 없습니다.");
					response.sendRedirect(request.getContextPath() + "/");
				}))
				// 2. 로그인처리
				.formLogin(form -> form.loginPage("/auth/login")
						.loginProcessingUrl("/auth/login")
						.successHandler(authenticationSuccessHandler(passEncoder))
						.failureUrl("/auth/login?error")
						.permitAll())
				// 3. 로그아웃
				.logout(logout -> logout.logoutUrl("/auth/logout")
										.logoutSuccessUrl("/auth/login")
										.invalidateHttpSession(true)
										.clearAuthentication(true)
										.permitAll())
				// 4. csrf 예외처리
				.csrf(csrf -> csrf.ignoringRequestMatchers("/auth/login", "/auth/update", "/auth/delete"));

		return http.build();
	}

	// 로그인 성공 후 이동 경로 분기
	// - ROOT 권한: 비밀번호 그대로 사용 -> 메인("/")으로 이동
	// - 그 외 권한: 최초 로그인(pwd_change_yn = 'N')인 경우에만 비밀번호 재설정 페이지로 이동
	// 이미 비밀번호를 변경한 적이 있으면 정상적으로 메인으로 이동
	@Bean
	public AuthenticationSuccessHandler authenticationSuccessHandler(PasswordEncoder passEncoder) {
		return (request, response, authentication) -> {
			CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();

			boolean isRoot = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROOT"));

			// 세션에 사원 id/회사 id 저장 (기존 화면들이 session의 empId/comId를 참조하는 경우 대비)
			request.getSession().setAttribute("empId", principal.getUser().getEmpId());
			request.getSession().setAttribute("comId", principal.getUser().getComId());

			if (isRoot) {
				response.sendRedirect(request.getContextPath() + "/");
				return;
			} else {
				if (passEncoder.matches(principal.getEmpNo(), principal.getPassword())) {
					response.sendRedirect(request.getContextPath() + "/auth/resetPass");
					return;
				}
			}
			response.sendRedirect(request.getContextPath() + "/");
		};
	}

	// AuthenticationManager 설정
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}
}
