package com.sb.erp.api;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sb.erp.api.ReportApi.ReportSections;
import com.sb.erp.dto.MyWeeklyReportDto;
import com.sb.erp.dto.ProjectAnalysisDto;
import com.sb.erp.dto.WeeklyReportDto;

@Service
public class OpenAiGpt {
	
	@Value("${openai.api.key}")private String apikey;
	@Value("${kjy.openai.api.key}") private String kjyapikey;
	
	private static final String API_URL="https://api.openai.com/v1/chat/completions";
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final RestClient restClient;
	 
	public OpenAiGpt(RestClient.Builder restClientBuilder) {
		this.restClient = restClientBuilder.baseUrl(API_URL).build();
	}
	
	private String callOpenAi(String prompt) {
	    Map<String, Object> body = Map.of(
	        "model", "gpt-4o-mini",
	        "messages", List.of(
	            Map.of("role", "user", "content", prompt)
	        )
	    );

	    String response = restClient.post()
	            .contentType(MediaType.APPLICATION_JSON)
	            .header("Authorization", "Bearer " + apikey)
	            .body(body)
	            .retrieve()
	            .body(String.class);

	    try {
	        JsonNode root = objectMapper.readTree(response);
	        return root.path("choices").get(0).path("message").path("content").asText();
	    } catch (Exception e) {
	        throw new RuntimeException("OpenAI 응답 실패",e);
	    }
	}
	
	//리스크 분석
	public String analyzeProject(ProjectAnalysisDto dto) {
		String prompt=String.format("""
									당신은 10년 이상의 경력을 가진 IT 프로젝트 관리자(PM)입니다
									아래 프로젝트 정보를 분석하여 프로젝트의 위험도를 판단해주세요.
									프로젝트명:%s
									진행률:%d%%
									전체태스크:%d
									TODO:%d
									DOING:%d
									DONE:%d
									지연태스크:%d
									마감일까지 남은기간:%d일
									다음 형식으로만 답변하세요.
									프로젝트명:
									위험도:
									(HIGH/MEDIUM,LOW중 하나)
									리스크 분석:
									권장 조치:
									다른 설명은 하지 마세요.
									""",
				 dto.getProjectName(),dto.getProgressRate(),dto.getTotalTask(),dto.getTodoCount()
				,dto.getDoingCount(),dto.getDoneCount(),dto.getDelayCount(),dto.getRemainDays());

		return callOpenAi(prompt);
	
	}
	
	// KJY
	public String formSchema(String userPrompt) {
		
		
		String systemPrompt = """
			당신은 사내 전자결재 시스템의 양식 설계 도우미입니다.
			사용자가 원하는 결재 양식을 설명하면, 필요한 입력 필드들을 설계해주세요.
			
			각 필드는 다음 구조를 따릅니다:
			{
				"key": "필드의 영문 식별자 (snake_case)",
				"label": "필드의 한글 라벨",
				"type": "text, textarea, date, number, select 중 하나",
				"required": true 또는 false,
				"options": ["select 타입일 때만 존재하는 선택지 배열"]
			}
			
			반드시 아래 형식의 JSON 객체만 응답하세요. 다른 설명은 절대 하지 마세요.
			{
				"title": "양식 제목 추천",
				"fields": [...]
			}		
			""";
		
		Map<String, Object> body = Map.of(
				"model", "gpt-4o-mini",
				"messages", List.of(
					Map.of("role", "system", "content", systemPrompt),
					Map.of("role", "user", "content", userPrompt)
				),
				"response_format", Map.of("type", "json_object")
		);
		
		try {
			String responseBody = restClient.post()
					.contentType(MediaType.APPLICATION_JSON)
					.header("Authorization", "Bearer " + kjyapikey)
					.body(body)
					.retrieve()
					.body(String.class);
			JsonNode root = objectMapper.readTree(responseBody);
			return root.path("choices").get(0).path("message").path("content").asText();
		} catch (Exception e) {
			throw new RuntimeException("openAi 양식 생성 실패", e);
		}
		
	}
	
	//관리자용 주간보고서
	public ReportSections weeklyReportSections(WeeklyReportDto dto) {
		String prompt=String.format("""
									당신은 10년 이상의 경력을 가진 IT 프로젝트 관리자(PM)입니다.
									전체 일정 리스크와 거시적인 진행 상황 위주로 작성하세요.
									아래는 저번 주 프로젝트 정량 데이터입니다.
									프로젝트명:%s
									마감일:%s
									전체 태스크:%d
									완료된 태스크:%d
									미완료 태스크:%d
									지난주 완료 태스크:%d
									지연 태스크:%d
									전체 진행률:%d%%
									평균 작업일:%.1f일
									평균 지연일:%d일
									남은 기간:%d일(음수면 마감을 이미 초과한 상태)
									위 데이터를 근거로 아래 4개 항목을 각각 2~3문장으로 작성하세요.
									반드시 아래 JSON 형식으로만 응답하세요. 다른 설명은 절대 하지 마세요.
									{
									  "summary": "이번주 업무 요약",
									  "risks": "주요 리스크",
									  "priorities": "다음주 우선순위",
									  "recommendation": "권장사항"
									}
									""",
									dto.getProjectName(), dto.getEndDate(), dto.getTotalTask(),dto.getDoneTaskCount(),dto.getNotDoneTaskCount(),
									dto.getCompletedThisWeek(), dto.getDelayTaskCount(), dto.getProgressRate(), dto.getAvgTaskDays(), dto.getAvgDelayDays()
								   ,dto.getRemainDays());
	
		 try {
		        String content = callOpenAi(prompt);
		        JsonNode sections = objectMapper.readTree(content);
		        return new ReportSections(
		                sections.path("summary").asText(),
		                sections.path("risks").asText(),
		                sections.path("priorities").asText(),
		                sections.path("recommendation").asText(),
		                sections.path("techIssues").asText()
		        );
		    } catch (Exception e) {
		        throw new RuntimeException("OpenAI 주간보고서 생성 실패", e);
		    }
	}
	// 개발자용 주간보고서
		public ReportSections myWeeklyReportSections(MyWeeklyReportDto dto) {
			String delayedListText = dto.getDelayedTaskNames() == null || dto.getDelayedTaskNames().isEmpty()
					? "없음"
					: String.join(", ", dto.getDelayedTaskNames());

			String prompt=String.format("""
										당신은 10년 이상의 경력을 가진 백엔드 개발자입니다.
										담당자가 이번 주에 직접 해결해야 할 기술적 이슈와 다음 주 작업 위주로 작성하세요.
										아래는 한 개발자가 여러 프로젝트에 걸쳐 담당하고 있는
										전체 태스크에 대한 지난주 정량 데이터입니다.
										(특정 프로젝트 하나가 아니라 담당자 개인 기준 집계이니, 특정 프로젝트를 단정 짓지 마세요.)
										담당자명:%s
										전체 태스크:%d
										완료된 태스크:%d
										미완료 태스크:%d
										지난주 완료:%d
										지연 태스크:%d
										전체 진행률:%d%%
										평균 작업일:%.1f일
										평균 지연일:%d일
										지연 태스크 목록:%s
										위 데이터를 근거로 아래 4개 항목을 각각 2~3문장으로 작성하세요.
										반드시 아래 JSON 형식으로만 응답하세요. 다른 설명은 절대 하지 마세요.
										{
										  "summary": "지난주 업무 요약",
										  "risks": "주요 리스크",
										  "priorities": "이번주 우선순위",
										  "techIssues": "이번주 해결해야 할 기술 이슈"
										}
										""",
										dto.getEmpName(), dto.getTotalTask(), dto.getDoneTaskCount(), dto.getNotDoneTaskCount(),
										dto.getCompletedThisWeek(), dto.getDelayTaskCount(), dto.getProgressRate(),
										dto.getAvgTaskDays(), dto.getAvgDelayDays(), delayedListText);

			try {
				String content = callOpenAi(prompt);
				JsonNode sections = objectMapper.readTree(content);
				return new ReportSections(
						sections.path("summary").asText(),
						sections.path("risks").asText(),
						sections.path("priorities").asText(),
						"",
						sections.path("techIssues").asText()
				);
			} catch (Exception e) {
				throw new RuntimeException("OpenAI 개인 주간보고서 생성 실패", e);
			}
		}
}
