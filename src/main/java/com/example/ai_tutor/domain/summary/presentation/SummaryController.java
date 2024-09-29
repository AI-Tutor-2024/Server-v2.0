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
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Tag(name = "요약문", description = "요약문 관련 API")
@RequiredArgsConstructor
@RestController
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
    public ResponseEntity<String> getSummary(
            @RequestPart("file") MultipartFile file,  // 파일은 여전히 @RequestPart로 받음
            @RequestPart(value = "request", required = false) SummaryReq summaryReq) throws IOException {

        // summaryReq가 null일 때 기본 처리
        List<String> keywords = summaryReq != null ? summaryReq.getKeywords() : List.of();  // 기본값 빈 리스트
        String requirement = summaryReq != null ? summaryReq.getRequirements() : "";  // 기본값 빈 문자열

        // SummaryService의 createSummary 메서드 호출
        String summary = summaryService.createSummary(file, keywords, requirement);

        // 요약 결과 반환
        return ResponseEntity.ok(summary);
    }


}
