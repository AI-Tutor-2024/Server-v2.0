package com.example.ai_tutor.domain.summary.presentation;

import com.example.ai_tutor.domain.summary.application.SummaryService;
import com.example.ai_tutor.domain.summary.dto.request.SummaryUpdateReq;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/note")
@Tag(name = "Summary", description = "요약문 관련 API입니다.")
public class SummaryController {

    private final SummaryService summaryService;

    @Operation(summary = "요약문 수정 API", description = "특정 강의 노트의 요약문 문단 하나를 수정하는 API입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "학습 단계 업데이트 성공", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Message.class) ) } ),
            @ApiResponse(responseCode = "400", description = "학습 단계 업데이트 실패", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class) ) } ),
    })
    @PatchMapping("/{noteId}/step/1/{summaryId}")
    public ResponseEntity<?> updateStepOne(
            @Parameter @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long noteId,
            @PathVariable Long summaryId,
            @RequestBody SummaryUpdateReq summaryUpdateReq
            ) {
        return summaryService.updateSummary(userPrincipal, noteId, summaryId, summaryUpdateReq);
    }
}
