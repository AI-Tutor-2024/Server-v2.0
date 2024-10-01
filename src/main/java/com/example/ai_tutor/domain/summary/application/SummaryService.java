package com.example.ai_tutor.domain.summary.application;
import com.example.ai_tutor.domain.practice.dto.request.CreatePracticeReq;
import com.example.ai_tutor.domain.summary.dto.response.SttRes;
import com.example.ai_tutor.domain.summary.dto.response.SummaryReq;
import com.example.ai_tutor.domain.user.domain.User;
import com.example.ai_tutor.domain.user.domain.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SummaryService {

    private final UserRepository userRepository;
    private final WebClient webClient;


    public String createSummary(MultipartFile file, List<String> keywords, String requirement) throws IOException {
        // Multipart 요청 빌드
        MultipartBodyBuilder builder = buildMultipartBody(file, keywords, requirement);

        // WebClient를 통해 파일과 키워드, 요구사항 전송 및 응답 처리
        return webClient.post()
                .uri("/stt-summary/v3")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(SttRes.class)
                .map(SttRes::getSummary)
                .block(); // 비동기 응답을 동기적으로 대기
    }

    private MultipartBodyBuilder buildMultipartBody(MultipartFile file, List<String> keywords, String requirement) throws IOException {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();

        // 파일 파트 추가 (MP4 파일)
        builder.part("file", file.getResource())  // InputStreamResource 대신 직접 리소스 제공
                .header(HttpHeaders.CONTENT_DISPOSITION, "form-data; name=\"file\"; filename=\"" + file.getOriginalFilename() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, "video/mp4");  // MP4 파일에 적합한 Content-Type 설정

        // 키워드와 요구사항이 null일 경우 빈 리스트와 빈 문자열 처리
        keywords = keywords != null ? keywords : List.of();
        requirement = requirement != null ? requirement : "";

        // 키워드와 요구사항을 JSON 문자열로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("keywords", keywords);  // 키워드 배열 추가
        requestMap.put("requirement", requirement);  // 요구사항 추가

        // JSON 변환 후 multipart request에 추가
        String requestJson = objectMapper.writeValueAsString(requestMap);

        // JSON 데이터를 'request' 파트로 추가
        builder.part("request", requestJson);  // JSON 문자열 추가 (별도의 Content-Type 헤더 불필요)

        return builder;
    }

}