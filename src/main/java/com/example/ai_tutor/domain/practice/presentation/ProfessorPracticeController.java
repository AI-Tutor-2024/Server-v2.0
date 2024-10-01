package com.example.ai_tutor.domain.practice.presentation;

import com.example.ai_tutor.domain.practice.application.ProfessorPracticeService;
import com.example.ai_tutor.domain.practice.dto.request.CreatePracticeReq;
import com.example.ai_tutor.domain.practice.dto.request.SavePracticeListReq;
import com.example.ai_tutor.domain.practice.dto.request.SavePracticeReq;
import com.example.ai_tutor.domain.practice.dto.request.UpdateLimitAndEndReq;
import com.example.ai_tutor.domain.practice.dto.response.CreatePracticeRes;
import com.example.ai_tutor.domain.practice.dto.response.PracticeRes;
import com.example.ai_tutor.domain.practice.dto.response.ProfessorPracticeListRes;
import com.example.ai_tutor.global.config.security.token.CurrentUser;
import com.example.ai_tutor.global.config.security.token.UserPrincipal;
import com.example.ai_tutor.global.payload.ErrorResponse;
import com.example.ai_tutor.global.payload.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Tag(name = "[교수자] 문제지", description = "문제지 생성 및 수정 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/professor/practice")
public class ProfessorPracticeController {

    private final ProfessorPracticeService professorPracticeService;

    @Operation(summary = "문제 생성", description = "파일, 문제 유형 및 개수를 기반으로 문제를 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "생성 성공", content = { @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = CreatePracticeRes.class))) } ),
            @ApiResponse(responseCode = "400", description = "생성 실패", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class) ) } ),
    })
    @PostMapping("")
    public ResponseEntity<?> generatePractice(
            //@Parameter(description = "Access Token을 입력해주세요.", required = true) @CurrentUser UserPrincipal userPrincipal,
            @Parameter(description = "Schemas의 CreatePracticeReq를 참고해주세요", required = true) @RequestPart CreatePracticeReq createPracticeReq,
            @Parameter(description = "Multipart form-data", required = true) @RequestPart MultipartFile file
    ) throws IOException, JsonProcessingException {
        return professorPracticeService.generatePractice(createPracticeReq, file);
    }

    // 문제 저장
    @Operation(summary = "문제 저장", description = "생성된 문제를 저장합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "생성 성공", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Message.class)) } ),
            @ApiResponse(responseCode = "400", description = "생성 실패", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class) ) } ),
    })
    @PostMapping("/{noteId}")
    public ResponseEntity<?> savePractice(
            //@Parameter(description = "Access Token을 입력해주세요.", required = true) @CurrentUser UserPrincipal userPrincipal,
            @Parameter(description = "Schemas의 SavePracticeListReq를 참고해주세요", required = true) @RequestBody List<SavePracticeReq> savePracticeReqs,
            @Parameter(description = "note의 id를 입력해주세요", required = true) @PathVariable Long noteId
    ) {
        return professorPracticeService.savePractice(noteId, savePracticeReqs);
    }

    // 문제 조회
    @Operation(summary = "문제 조회", description = "생성된 문제, 답안, 해설을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ProfessorPracticeListRes.class)) } ),
            @ApiResponse(responseCode = "400", description = "조회 실패", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class) ) } ),
    })
    @GetMapping("/{noteId}")
    public ResponseEntity<?> findPractices(
            //@Parameter(description = "Access Token을 입력해주세요.", required = true) @CurrentUser UserPrincipal userPrincipal,
            @Parameter(description = "note의 id를 입력해주세요", required = true) @PathVariable Long noteId
    ) {
        return professorPracticeService.getPractices(noteId);
    }

//    @Operation(summary = "제한 시간, 마감 기간 수정", description = "제한 시간, 마감 기간을 수정합니다.")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "조회 성공", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ProfessorPracticeListRes.class)) } ),
//            @ApiResponse(responseCode = "400", description = "조회 실패", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class) ) } ),
//    })
//    @PatchMapping("/{noteId}")
//    public ResponseEntity<?> updateLimitAndEnd(
//            //@Parameter(description = "Access Token을 입력해주세요.", required = true) @CurrentUser UserPrincipal userPrincipal,
//            @Parameter(description = "note의 id를 입력해주세요", required = true) @PathVariable Long noteId,
//            @Parameter(description = "Schemas의 UpdateLimitAndEndReq를 참고해주세요", required = true) @RequestBody UpdateLimitAndEndReq updateLimitAndEndReq
//    ) {
//        return professorPracticeService.updateLimitTimeAndEndDate(noteId, updateLimitAndEndReq);
//    }

}
