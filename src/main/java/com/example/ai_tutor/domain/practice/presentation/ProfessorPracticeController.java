package com.example.ai_tutor.domain.practice.presentation;

import com.example.ai_tutor.domain.practice.application.ProfessorPracticeService;
import com.example.ai_tutor.domain.practice.dto.request.CreatePracticeReq;
import com.example.ai_tutor.domain.practice.dto.request.SavePracticeListReq;
import com.example.ai_tutor.domain.practice.dto.request.SavePracticeReq;
import com.example.ai_tutor.domain.practice.dto.request.UpdatePracticeReq;
import com.example.ai_tutor.domain.practice.dto.response.CreatePracticeListRes;
import com.example.ai_tutor.domain.practice.dto.response.PracticeSaveApiResponse;
import com.example.ai_tutor.domain.practice.dto.response.ProfessorPracticeListRes;
import com.example.ai_tutor.domain.practice.dto.response.ProfessorPracticeRes;
import com.example.ai_tutor.global.config.security.token.UserPrincipal;
import com.example.ai_tutor.global.payload.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
            summary = "요약본에 대한 문제 생성 후 저장",
            security = { @SecurityRequirement(name = "BearerAuth") },
            description = "요약본 데이터를 기반으로 문제를 생성하고, 생성된 문제를 DB에 곧바로 저장합니다. " +
                    "기존 두 API(generate + save)를 하나로 통합한 API입니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Practice 문제 생성 및 저장 성공",
                            content = @Content(schema = @Schema(implementation = com.example.ai_tutor.global.payload.ApiResponse.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청",
                            content = @Content(schema = @Schema(implementation = com.example.ai_tutor.global.payload.ApiResponse.class))),
                    @ApiResponse(responseCode = "500", description = "서버 오류",
                            content = @Content(schema = @Schema(implementation = com.example.ai_tutor.global.payload.ApiResponse.class)))
        })
    @PostMapping(value = "/{noteId}/generate-and-save")
    public Mono<ResponseEntity<com.example.ai_tutor.global.payload.ApiResponse<List<ProfessorPracticeRes>>>> generateAndSavePractice(
            @Parameter(description = "Access Token을 입력해주세요.", required = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal,

            @Parameter(description = "노트의 ID를 입력해주세요", required = true)
            @PathVariable Long noteId,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Schemas의 CreatePracticeReq를 참고해주세요",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreatePracticeReq.class))
            )
            @RequestBody CreatePracticeReq createPracticeReq
    ) {
        return professorPracticeService.generateAndSavePractice(userPrincipal, noteId, createPracticeReq);
    }

    // 문제 저장
    @Operation(
            summary = "선택된 문제를 저장하는 문제 저장 API 입니다.",
            security = { @SecurityRequirement(name = "BearerAuth") },
            description = "문제 생성 API를 통해 요청했던 문제들 중 교수님이 원하는 문제들만 선택하여 저장하는 API 입니다.",
            responses = {
                    @ApiResponse(
                        responseCode = "200",
                        description = "Practice 문제 저장 성공",
                        content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PracticeSaveApiResponse.class)
                        )),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청",
                            content = @Content(schema = @Schema(implementation = com.example.ai_tutor.global.payload.ApiResponse.class))),
                    @ApiResponse(responseCode = "500", description = "서버 오류",
                            content = @Content(schema = @Schema(implementation = com.example.ai_tutor.global.payload.ApiResponse.class)))
            })

    @PostMapping("/{noteId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> savePractice(
            @Parameter(description = "Access Token을 입력해주세요.", required = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "SavePracticeReq 배열을 전달해주세요",
                    required = true,
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = SavePracticeReq.class)))
            )
            @RequestBody List<SavePracticeReq> savePracticeReqs,

            @Parameter(description = "note의 id를 입력해주세요", required = true)
            @PathVariable Long noteId
    ) {
        return professorPracticeService.savePractice(userPrincipal, noteId, savePracticeReqs);
    }


    // 저장된 문제 수정 메서드
    @PatchMapping("/{noteId}/{practiceId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "문제 수정",
            security = { @SecurityRequirement(name = "BearerAuth") },
            description = "저장된 문제의 내용‧답안‧해설 등을 부분 수정합니다. additionalResults 필드는 신경쓰지마시고 필드에서 제외하고 요청해주셔도 됩니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Practice 문제 저장 성공",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = PracticeSaveApiResponse.class)
                            )),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청",
                            content = @Content(schema = @Schema(implementation = com.example.ai_tutor.global.payload.ApiResponse.class))),
                    @ApiResponse(responseCode = "500", description = "서버 오류",
                            content = @Content(schema = @Schema(implementation = com.example.ai_tutor.global.payload.ApiResponse.class)))
            })
    public ResponseEntity<?> updatePractice(
            @Parameter(description = "Access Token을 입력해주세요.", required = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "note의 id를 입력해주세요", required = true)
            @PathVariable Long noteId,
            @Parameter(description = "practice의 id를 입력해주세요", required = true)
            @PathVariable Long practiceId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "수정할 필드만 포함한 UpdatePracticeReq",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UpdatePracticeReq.class))
            )
            @RequestBody UpdatePracticeReq updateReq
    ) {
        return professorPracticeService.updatePractice(
                userPrincipal, noteId, practiceId, updateReq);
    }

    // 문제 조회
    @Operation(summary = "문제 조회", description = "생성된 문제, 답안, 해설을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ProfessorPracticeListRes.class)) } ),
            @ApiResponse(responseCode = "400", description = "조회 실패", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class) ) } ),
    })
    @GetMapping("/{noteId}")
    public ResponseEntity<?> findPractices(
            @Parameter(description = "Access Token을 입력해주세요.", required = true) @AuthenticationPrincipal UserPrincipal userPrincipal,
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
