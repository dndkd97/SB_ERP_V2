package com.sb.erp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sb.erp.service.MailSchedulerService;

/**
 * 관리자 수동 이메일 배치 트리거.
 * <ul>
 *   <li>실무 관례: 새벽 스케줄러 실패 시 관리자가 수동으로 재실행할 수 있게 열어둠</li>
 *   <li>ROOT/ADMIN만 접근 가능</li>
 * </ul>
 *
 * <p>curl 테스트:
 * <pre>
 *   curl -X POST http://localhost:8080/api/admin/mail/trigger-followup-3day
 *   curl -X POST http://localhost:8080/api/admin/mail/trigger-welcome-orphans
 * </pre>
 */
@RestController
@RequestMapping("/api/admin/mail")
public class AdminMailController {

    @Autowired MailSchedulerService mailSchedulerService;

    @PreAuthorize("hasAuthority('ROOT') or hasRole('ADMIN')")
    @PostMapping("/trigger-followup-3day")
    public ResponseEntity<String> triggerFollowup3Day() {
        mailSchedulerService.triggerFollowup3Day();
        return ResponseEntity.ok("3일 차 안부 메일 배치를 수동 실행했습니다.");
    }

    @PreAuthorize("hasAuthority('ROOT') or hasRole('ADMIN')")
    @PostMapping("/trigger-welcome-orphans")
    public ResponseEntity<String> triggerWelcomeOrphans() {
        mailSchedulerService.triggerWelcomeOrphans();
        return ResponseEntity.ok("환영 메일 안전망 배치를 수동 실행했습니다.");
    }
}
