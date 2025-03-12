package com.example.ai_tutor.domain.openAPI.clova;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClovaService {

    private final WebClient clovaWebClient;

    @Value("${openapi.clova.secret}")
    private String secret;

    @Value("${openapi.clova.url}")
    private String clovaUrl;

    public Mono<JsonNode> processSpeechToText(MultipartFile file) {
        // 멀티파트 데이터로 전송할 Map 생성
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("media", file.getResource());  // 파일을 리소스로 전달
        body.add("params", "{ \"language\": \"enko\", \"completion\": \"sync\" }");  // 파라미터는 JSON 문자열로 전달

        return clovaWebClient.post()
                .uri(clovaUrl + "/recognizer/upload")
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header("X-CLOVASPEECH-API-KEY", secret)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(body)  // 멀티파트 데이터 전송
                .retrieve()
                .bodyToMono(JsonNode.class)
                .doOnSuccess(response -> log.info("Clova Speech API 응답 성공: {}", response))
                .doOnError(error -> log.error("Clova Speech API 호출 중 오류 발생: {}", error.getMessage()))
                .onErrorResume(error -> {
                    log.error("Clova Speech API 호출 중 예외 처리 발생: {}", error.getMessage());
                    return Mono.error(new RuntimeException("Clova Speech API 호출 실패", error));
                });
    }
}
