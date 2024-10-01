package com.example.ai_tutor.domain.summary.presentation;

import com.example.ai_tutor.domain.summary.application.SummaryService;
import com.example.ai_tutor.domain.summary.dto.response.SummaryReq;
import com.example.ai_tutor.global.config.security.token.CurrentUser;
import com.example.ai_tutor.global.config.security.token.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;

@Tag(name = "요약문", description = "요약문 관련 API")
@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/api/v1/professor/summary")
public class SummaryController {

    private final SummaryService summaryService;

//    // STT 테스트용 API
//    @PostMapping("/stt")
//    public ResponseEntity<?> tts(
//            @CurrentUser UserPrincipal userPrincipal,
//            @RequestParam("file") MultipartFile file
//    ) throws IOException {
//        return ResponseEntity.ok(summaryService.createSummary(userPrincipal, file));
//    }



    @Operation(summary = "파일 및 키워드를 기반으로 요약 생성", description = "파일, 키워드 목록 및 요구사항을 기반으로 요약을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요약 생성 성공", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = String.class)) }),
            @ApiResponse(responseCode = "400", description = "요약 생성 실패", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)) })
    })
    @PostMapping(value = "/get-summary", consumes = "multipart/form-data")
    public Mono<ResponseEntity<String>> getSummary(
            @RequestPart("file") MultipartFile file,  // 파일을 처리하는 부분은 그대로 유지
            @RequestPart(value = "request", required = false) SummaryReq summaryReq) throws IOException {

        // summaryReq가 null일 때 기본 처리
        String keywords = summaryReq != null ? summaryReq.getKeywords() : "";  // 기본값 빈 리스트
        String requirement = summaryReq != null ? summaryReq.getRequirement() : "";  // 기본값 빈 문자열

        // SummaryService의 processSttAndSummary 메서드 호출
        return summaryService.processSttAndSummary(file, keywords, requirement)
                .map(summary -> ResponseEntity.ok().body(summary))  // 성공 시 200 응답 반환
                .onErrorResume(error -> {
                    // 에러가 발생하면 로그를 남기고 400 응답을 반환
                    log.error("요약 생성 중 오류 발생: {}", error.getMessage());
                    return Mono.just(ResponseEntity.badRequest().body("요약 생성 실패: " + error.getMessage()));
                });
    }



}
