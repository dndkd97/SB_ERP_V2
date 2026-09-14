package com.sb.erp.api;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.sb.erp.dto.BizNoVerifyDto;

@Service
public class BizNoVerifyApi {
	@Value("${nts.bizno.api}") private String apiKey;

	private final RestClient restClient;

	public BizNoVerifyApi(RestClient.Builder restClientBuilder) {
		super();
		// [신규] connect/read 타임아웃 설정
		// -> 설정이 없으면 국세청(api.odcloud.kr)이 응답을 지연시킬 때
		//    우리 서버 스레드가 응답이 올 때까지 무한정 대기하게 되어
		//    "너무 오래 걸린다"는 체감이 발생합니다.
		//    connect 3초 / read 5초로 상한을 두고, 넘으면 예외로 빠르게 실패 처리합니다.
		ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.defaults()
				.withConnectTimeout(Duration.ofSeconds(3))
				.withReadTimeout(Duration.ofSeconds(5));
		this.restClient = restClientBuilder
				.requestFactory(ClientHttpRequestFactoryBuilder.detect().build(settings))
				.build();
	}

	/**
	 * 국세청 사업자등록정보 진위확인 (POST /v1/validate)
	 * 요청 body 예시:
	 * { "businesses": [ { "b_no": "1234567890", "start_dt": "20240101", "p_nm": "홍길동" } ] }
	 */
	public String getResponse(BizNoVerifyDto dto) {
		// https 시작하지 않아도 OK /  인코딩문제관련 처리
		URI uri = UriComponentsBuilder
				      .fromUriString("https://api.odcloud.kr/api/nts-businessman/v1/validate")
				      .queryParam("serviceKey", apiKey)
				      .queryParam("returnType", "JSON")
				      .build(true)
				      .toUri();

		Map<String, Object> business = Map.of(
				"b_no", normalizeBizNo(dto.getBizNo()),
				"start_dt", normalizeDate(dto.getStartDt()),
				"p_nm", dto.getCeoName() == null ? "" : dto.getCeoName()
		);
		Map<String, Object> requestBody = Map.of("businesses", List.of(business));

		try {
			return restClient.post()
			         .uri(uri)
			         .contentType(MediaType.APPLICATION_JSON)
			         .body(requestBody)
			         .retrieve()
			         .body(String.class);
		} catch (ResourceAccessException e) {
			// [신규] connect/read timeout 은 대부분 이 예외로 올라옵니다.
			// 500 에러를 그대로 던지면 프런트 fetch(res.json())가 깨지므로,
			// 프런트가 기대하는 형식(status_code)에 맞춰 폴백 JSON을 내려줍니다.
			return "{\"status_code\":\"TIMEOUT\",\"message\":\"국세청 서버 응답이 지연되고 있습니다. 잠시 후 다시 시도해주세요.\"}";
		} catch (Exception e) {
			throw new RuntimeException("사업자번호 진위확인 API 호출중 오류 발생", e);
		}
	}

	/** "123-45-67890" -> "1234567890" (국세청 API는 '-' 없는 10자리 숫자를 요구) */
	private String normalizeBizNo(String raw) {
		return raw == null ? "" : raw.replaceAll("[^0-9]", "");
	}

	/** "2024-01-01" -> "20240101" (국세청 API는 yyyyMMdd 형식을 요구) */
	private String normalizeDate(String raw) {
		return raw == null ? "" : raw.replaceAll("[^0-9]", "");
	}
}