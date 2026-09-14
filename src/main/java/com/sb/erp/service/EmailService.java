package com.sb.erp.service;

import com.sb.erp.dto.EmpDto;
import com.sb.erp.dto.WelcomeMailTargetDto;

/**
 * 온보딩 이메일 발송 서비스.
 * - EmailApi(SMTP)를 감싸서 템플릿 처리 + 발송 로그 + 비동기 처리 담당
 * - 발송 시작 시 email_send_log에 'P'로 기록, 결과에 따라 'S'/'F' 업데이트
 */

public interface EmailService {

    // 환영 메일 (사원 등록 직후).
    // @Async: 트랜잭션 커밋 이후 별도 스레드에서 실행
    // 예외 발생 시 로그만 남김 (등록 자체는 영향 없음)

    void sendWelcomeMailAsync(EmpDto emp);

    // 3일 차 적응 확인 메일.
     //스케줄러 또는 관리자 수동 트리거에서 호출
    void sendFollowup3DayMailAsync(WelcomeMailTargetDto target);
}
