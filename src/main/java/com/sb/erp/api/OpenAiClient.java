package com.sb.erp.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sb.erp.dto.openai.ChatMessage;
import com.sb.erp.dto.openai.ChatRequest;
import com.sb.erp.dto.openai.ChatResponse;
import com.sb.erp.dto.openai.ReportContent;

/* OpenAI Chat Completions API 저수준 호출 어댑터.
1. 이 클래스가 하는 일:
- ChatMessage 리스트 받아서 OpenAI에 요청
- 응답의 content 안의 JSON을 ReportContent record로 파싱
- 실패/이상 응답 시 null 리턴 (예외 안 던짐) — 호출자가 fallback 판단

2. 이 클래스가 하지 않는 일(서비스 계층 몫):
- 프롬프트 설계 
- fallback 판단
- DB 저장

도메인 지식이 없어야 다른 도메인(결재 요약 등)에서도 재사용 가능.
 */
@Component // Spring이 관리하는 일반 Bean
public class OpenAiClient {

    @Value("${openai.api.model}") private String model;
    // @Autowired로 필드 주입하는 대신 생성자 주입하기
    // 필드에 final을 붙일 수 있음 → 실수로 변경하지 않게
    private final RestClient openAiRestClient;
    private final ObjectMapper objectMapper;
    
    
    @Autowired
    public OpenAiClient( // 생성자 주입
            @Qualifier("openAiRestClient") RestClient openAiRestClient,
          //RestClient는 OpenAiConfig가 만들어놓은 "openAiRestClient" Bean을 지정해 받음
          //ObjectMapper는 Spring Boot가 기본으로 제공하는 걸 자동 주입
            ObjectMapper objectMapper) {
        this.openAiRestClient = openAiRestClient;
        this.objectMapper = objectMapper;
    }

    //GPT에게 메시지 리스트를 보내고 리포트 콘텐츠를 받아온다.
    //@param messages system + user 메시지 조합
    //@return 파싱된 ReportContent, 실패 시 null
    //리포트 생성
    public ReportContent generateReport(List<ChatMessage> messages) {
        try {
            // 1. 요청 조립 (JSON mode + temperature 0.3)
            ChatRequest request = new ChatRequest(
                model, // properties에서 주입한 "gpt-4o-mini"
                messages, // 서비스가 넘긴 프롬프트
                ChatRequest.ResponseFormat.jsonObject(), // JSON mode
                0.3 // temperature
            );

            // 2. HTTP 요청 및 응답 자동 파싱 (1번 파싱)
            ChatResponse response = openAiRestClient.post()
                .uri("/chat/completions") // baseUrl 뒤에 붙음 (config에서 세팅한 값)
                .body(request) // 요청 바디 (Jackson이 record → JSON 자동 변환)
                .retrieve() // 실행
                .body(ChatResponse.class); // 응답을 이 타입으로 자동 파싱

            // 3. content 문자열 안전하게 꺼내기
            String contentJson = (response == null) ? null : response.firstContent();
            if (contentJson == null || contentJson.isBlank()) {
                System.err.println("[OpenAiClient] 응답 content가 비어있음");
                return null;
            }

            // 4. content 안의 JSON을 ReportContent record로 파싱 (2번 파싱)
            return objectMapper.readValue(contentJson, ReportContent.class);
            // readValue(문자열, 타입) — Jackson의 파싱 메서드. 문자열을 지정한 타입 record로 변환.

        } catch (Exception e) {
            // API 호출 실패, 파싱 실패, 네트워크 오류 등 모든 예외 포함
            // 호출자가 null 확인 후 fallback (mock) 로직으로 넘어감
            System.err.println("[OpenAiClient] GPT 호출 실패: " + e.getMessage());
            return null;
        }
    }
}