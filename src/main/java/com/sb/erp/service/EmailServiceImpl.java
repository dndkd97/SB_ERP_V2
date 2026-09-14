package com.sb.erp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sb.erp.api.EmailApi;
import com.sb.erp.service.MailTemplates;
import com.sb.erp.dao.EmailSendLogMapper;
import com.sb.erp.dto.EmailSendLogDto;
import com.sb.erp.dto.EmpDto;
import com.sb.erp.dto.WelcomeMailTargetDto;

/**
 * 온보딩 이메일 발송 구현체.
 *
 * <p>발송 라이프사이클:
 * <pre>
 *   1) log.upsertProcessing → 상태 'P'
 *   2) emailApi.sendMail(...)
 *   3-a) 성공 → log.updateSuccess → 'S'
 *   3-b) 실패 → log.updateFail → 'F' + error_msg
 * </pre>
 *
 * <p>이 클래스의 메서드는 {@code mailExecutor} 스레드 풀에서 비동기 실행되며,
 * 예외를 절대 밖으로 던지지 않는다 (호출자 트랜잭션에 영향 없음).
 */

@Service
@Transactional
public class EmailServiceImpl implements EmailService {

    @Autowired EmailApi emailApi;
    @Autowired EmailSendLogMapper logMapper;

    // 회사명은 employee와 조인 없이는 얻기 어려우므로, insert 시점엔 fallback 사용
    private static final String DEFAULT_COM_NAME = "SBerp";


    // ─── 환영 메일 ─────────────────────────────
    @Override
    @Async("mailExecutor")
    public void sendWelcomeMailAsync(EmpDto emp) {
        int    empId    = emp.getEmpId();
        String empName  = emp.getEmpName();
        String empEmail = emp.getEmpEmail();
        String comName  = (emp.getComName() != null) ? emp.getComName() : DEFAULT_COM_NAME;

        if (empEmail == null || empEmail.isBlank()) {
            System.err.println("[EmailService] 환영 메일 스킵: empId=" + empId + " 이메일 없음");
            return;
        }
        
        try {
            logMapper.upsertProcessing(empId, EmailSendLogDto.TYPE_WELCOME);

            String subject = MailTemplates.welcomeSubject(comName, empName);
            String body    = MailTemplates.welcomeBody(comName, empName);

            emailApi.sendMail(subject, body, empEmail);

            logMapper.updateSuccess(empId, EmailSendLogDto.TYPE_WELCOME);
            System.out.println("[EmailService] 환영 메일 성공 empId=" + empId);

        } catch (Exception e) {
            String msg = truncate(e.getMessage(), 500);
            try {
                logMapper.updateFail(empId, EmailSendLogDto.TYPE_WELCOME, msg);
            } catch (Exception logEx) {
                System.err.println("[EmailService] 로그 업데이트 실패: " + logEx.getMessage());
            }
            System.err.println("[EmailService] 환영 메일 실패 empId=" + empId + " err=" + msg);
        }
    }


    // ─── 3일 차 적응 확인 메일 ──────────────────

    @Override
    @Async("mailExecutor")
    public void sendFollowup3DayMailAsync(WelcomeMailTargetDto target) {
        int    empId    = target.getEmpId();
        String empName  = target.getEmpName();
        String empEmail = target.getEmpEmail();
        String comName  = (target.getComName() != null) ? target.getComName() : DEFAULT_COM_NAME;

        if (empEmail == null || empEmail.isBlank()) {
            System.err.println("[EmailService] 3일 메일 스킵: empId=" + empId + " 이메일 없음");
            return;
        }

        try {
            logMapper.upsertProcessing(empId, EmailSendLogDto.TYPE_FOLLOWUP_3DAY);

            String subject = MailTemplates.followup3DaySubject(comName, empName);
            String body    = MailTemplates.followup3DayBody(comName, empName);

            emailApi.sendMail(subject, body, empEmail);

            logMapper.updateSuccess(empId, EmailSendLogDto.TYPE_FOLLOWUP_3DAY);
            System.out.println("[EmailService] 3일 메일 성공 empId=" + empId);

        } catch (Exception e) {
            String msg = truncate(e.getMessage(), 500);
            try {
                logMapper.updateFail(empId, EmailSendLogDto.TYPE_FOLLOWUP_3DAY, msg);
            } catch (Exception logEx) {
                System.err.println("[EmailService] 로그 업데이트 실패: " + logEx.getMessage());
            }
            System.err.println("[EmailService] 3일 메일 실패 empId=" + empId + " err=" + msg);
        }
    }


    // ─── util ──────────────────

    private String truncate(String s, int max) {
        if (s == null) return "unknown";
        return s.length() <= max ? s : s.substring(0, max);
    }
}
