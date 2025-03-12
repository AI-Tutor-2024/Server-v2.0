package com.example.ai_tutor.domain.practice.presentation;

import com.example.ai_tutor.domain.practice.application.ProfessorPracticeService;
import com.example.ai_tutor.domain.practice.dto.request.CreatePracticeReq;
import com.example.ai_tutor.domain.practice.dto.request.SavePracticeReq;
import com.example.ai_tutor.domain.practice.dto.response.ProfessorPracticeListRes;
import com.example.ai_tutor.global.config.security.token.CurrentUser;
import com.example.ai_tutor.global.config.security.token.UserPrincipal;
import com.example.ai_tutor.global.payload.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import java.util.List;

@Tag(name = "Professor Practice", description = "교수 사용자의 문제지 생성 및 수정 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/professor/practice")
public class ProfessorPracticeController {

    private final ProfessorPracticeService professorPracticeService;

    @Operation(
            summary = "요약본에 대한 문제 생성",
            description = "교수자가 제공한 강의를 저장해둔 데이터를 기반으로 요약본을 생성하고, 이를 통해 문제를 생성합니다." +
                    "이는 CreatePracticeReq를 참고하여 문제를 생성하고 파일을 업로드합니다. ",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Practice 문제 생성 성공",
                            content = @Content(schema = @Schema(implementation = com.example.ai_tutor.global.payload.ApiResponse.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청",
                            content = @Content(schema = @Schema(implementation = com.example.ai_tutor.global.payload.ApiResponse.class))),
                    @ApiResponse(responseCode = "500", description = "서버 오류",
                            content = @Content(schema = @Schema(implementation = com.example.ai_tutor.global.payload.ApiResponse.class)))
            })
    @PostMapping(value = "/{noteId}/new", consumes = "multipart/form-data")
    public Mono<ResponseEntity<com.example.ai_tutor.global.payload.ApiResponse>> generatePractice(
            @Parameter(description = "note의 id를 입력해주세요", required = true) @PathVariable Long noteId,
            @Parameter(description = "Schemas의 CreatePracticeReq를 참고해주세요", required = true)
            @RequestPart CreatePracticeReq createPracticeReq,

            @Parameter(description = "Multipart form-data", required = true,
                    schema = @Schema(type = "string", format = "binary"))
            @RequestPart MultipartFile file
    ) {
        return professorPracticeService.generatePractice(createPracticeReq, file, noteId)
                .map(apiResponse -> ResponseEntity.ok((com.example.ai_tutor.global.payload.ApiResponse) apiResponse));  // ApiResponse를 ResponseEntity로 감싸서 반환
    }


    // 문제 저장
    @PostMapping("/{noteId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> savePractice(

            @Parameter(description = "Access Token을 입력해주세요.", required = true) @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Schemas의 SavePracticeListReq를 참고해주세요", required = true) @RequestBody List<SavePracticeReq> savePracticeReqs,
            @Parameter(description = "note의 id를 입력해주세요", required = true) @PathVariable Long noteId

    ) {
        return professorPracticeService.savePractice(userPrincipal, noteId, savePracticeReqs);
    }

    // 문제 조회
    @Operation(summary = "문제 조회", description = "생성된 문제, 답안, 해설을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ProfessorPracticeListRes.class)) } ),
            @ApiResponse(responseCode = "400", description = "조회 실패", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class) ) } ),
    })
    @GetMapping("/{noteId}")
    public ResponseEntity<?> findPractices(
            @Parameter(description = "Access Token을 입력해주세요.", required = true) @CurrentUser UserPrincipal userPrincipal,
            @Parameter(description = "note의 id를 입력해주세요", required = true) @PathVariable Long noteId
    ) {
        return professorPracticeService.getPractices(userPrincipal, noteId);
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
