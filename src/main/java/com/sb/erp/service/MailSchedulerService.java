package com.sb.erp.service;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.sb.erp.dao.EmailSendLogMapper;
import com.sb.erp.dto.WelcomeMailTargetDto;

@Service
public class MailSchedulerService {

    @Autowired EmailSendLogMapper logMapper;
    @Autowired EmailService emailService;

    private final AtomicBoolean followup3DayRunning = new AtomicBoolean(false);
    private final AtomicBoolean welcomeOrphanRunning = new AtomicBoolean(false);


    // ─── 3일 차 안부 메일 (평일 오전 9시에만) ───
    @Scheduled(cron = "0 0 9 * * MON-FRI", zone = "Asia/Seoul")
    public void sendFollowup3DayEmails() {
        if (!followup3DayRunning.compareAndSet(false, true)) {
            System.out.println("[MailScheduler] 3일 메일 배치 이미 실행 중 - 스킵");
            return;
        }
        try {
            System.out.println("[MailScheduler] === 3일 차 안부 메일 배치 시작 ===");
            List<WelcomeMailTargetDto> targets = logMapper.selectFollowup3DayTargets();

            if (targets == null || targets.isEmpty()) {
                System.out.println("[MailScheduler] 3일 메일 대상자 없음");
                return;
            }

            System.out.println("[MailScheduler] 3일 메일 대상자 " + targets.size() + "명");
            for (WelcomeMailTargetDto target : targets) {
                // 개별 발송은 EmailService @Async로 위임 → 스레드 풀에서 병렬 처리
                emailService.sendFollowup3DayMailAsync(target);
            }

            System.out.println("[MailScheduler] === 3일 차 안부 메일 배치 종료 (예약 완료) ===");
        } finally {
            followup3DayRunning.set(false);
        }
    }


    // ─── 환영 메일 안전망 (매일 01:30) ───

    /**
     * afterCommit 콜백 실행 실패로 누락된 환영 메일을 복구 발송.
     * 예: DB 커밋은 성공했으나 그 직후 서버 다운 → 비동기 스레드 실행 안 됨.
     */
    @Scheduled(cron = "0 30 1 * * ?", zone = "Asia/Seoul")
    public void sendWelcomeOrphans() {
        if (!welcomeOrphanRunning.compareAndSet(false, true)) {
            System.out.println("[MailScheduler] 환영 메일 안전망 배치 이미 실행 중 - 스킵");
            return;
        }
        try {
            System.out.println("[MailScheduler] === 환영 메일 안전망 배치 시작 ===");
            List<WelcomeMailTargetDto> orphans = logMapper.selectWelcomeOrphans();

            if (orphans == null || orphans.isEmpty()) {
                System.out.println("[MailScheduler] 환영 메일 누락자 없음");
                return;
            }

            System.out.println("[MailScheduler] 환영 메일 누락자 " + orphans.size() + "명");
            for (WelcomeMailTargetDto orphan : orphans) {
                // EmpDto가 아니라 WelcomeMailTargetDto지만, 필드 구성이 같아 EmailService에서 활용 가능
                // → 별도 오버로드로 호출 (구현체 참조)
                com.sb.erp.dto.EmpDto emp = new com.sb.erp.dto.EmpDto();
                emp.setEmpId(orphan.getEmpId());
                emp.setEmpName(orphan.getEmpName());
                emp.setEmpEmail(orphan.getEmpEmail());
                emp.setComName(orphan.getComName());
                emailService.sendWelcomeMailAsync(emp);
            }

            System.out.println("[MailScheduler] === 환영 메일 안전망 배치 종료 (예약 완료) ===");
        } finally {
            welcomeOrphanRunning.set(false);
        }
    }


    // ─── 수동 트리거용 공개 메서드 (Controller에서 호출) ───

    /** 관리자 강제 실행: 3일 메일 배치. */
    public void triggerFollowup3Day() { sendFollowup3DayEmails(); }

    /** 관리자 강제 실행: 환영 메일 안전망 배치. */
    public void triggerWelcomeOrphans() { sendWelcomeOrphans(); }
    
}
