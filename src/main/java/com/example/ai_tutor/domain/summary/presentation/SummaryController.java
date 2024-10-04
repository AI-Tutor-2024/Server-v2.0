package com.example.ai_tutor.domain.summary.presentation;

import com.example.ai_tutor.domain.summary.application.SummaryService;
import com.example.ai_tutor.domain.summary.dto.request.SummaryReq;
import com.example.ai_tutor.global.config.security.token.CurrentUser;
import com.example.ai_tutor.global.config.security.token.UserPrincipal;
import com.example.ai_tutor.global.payload.ErrorResponse;
import com.example.ai_tutor.global.payload.Message;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Tag(name = "요약문", description = "요약문 관련 API")
@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/api/v1/professor/summary")
public class SummaryController {

    private final SummaryService summaryService;

    // 문제지를 푼 학생들의 결과 및 정보 조회
    @GetMapping("/{noteId}")
    public ResponseEntity<?> getSummary(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long noteId
    ) {
        return summaryService.getSummary(noteId);
    }



}
