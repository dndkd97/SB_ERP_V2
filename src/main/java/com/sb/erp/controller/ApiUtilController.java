package com.sb.erp.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.sb.erp.api.ApiCoolSms;
import com.sb.erp.api.BizNoVerifyApi;
import com.sb.erp.api.OcrNaverApi;
import com.sb.erp.dto.BizNoVerifyDto;
import com.sb.erp.dto.OcrResultDto;

import net.nurigo.java_sdk.exceptions.CoolsmsException;

@Controller
@RequestMapping("/api/util")
public class ApiUtilController {
	@Autowired BizNoVerifyApi bizNoVerifyApi;
	/**
     * 사업자등록번호 진위확인
     * POST /api/util/bizno/verify
     * body: { 
     *     "bizNo": "123-45-67890", 
     *     "startDt": "2024-01-01", 
     *     "ceoName": "홍길동" 
     * }
     */
	@PostMapping(value="/bizno/verify" , produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String verify(@RequestBody BizNoVerifyDto bizNoVerify) {
        return bizNoVerifyApi.getResponse(bizNoVerify);
	}

	
	@Autowired OcrNaverApi ocrNaverApi;
	
	@PostMapping(value = "/ocr", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseBody
	public Map<String, Object> processOcr(@RequestParam("file") MultipartFile file) {

	    Map<String, Object> resultMap = new HashMap<>();
	    try {
	        OcrResultDto parsed = ocrNaverApi.executeOcr(file);
	        resultMap.put("status", "success");
	        resultMap.put("data", parsed); // 화면에서 필드별로 바로 사용

	    } catch (Exception e) {
	        resultMap.put("status", "error");
	        resultMap.put("message", e.getMessage());
	    }
	    return resultMap;
	}
}
