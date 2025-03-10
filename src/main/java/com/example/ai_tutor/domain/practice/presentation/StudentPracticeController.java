package com.example.ai_tutor.domain.practice.presentation;

import com.example.ai_tutor.domain.practice.application.StudentPracticeService;
import com.example.ai_tutor.domain.practice.dto.request.AnswerReq;
import com.example.ai_tutor.domain.practice.dto.request.UpdateAnswersReq;
import com.example.ai_tutor.domain.practice.dto.response.PracticeRes;
import com.example.ai_tutor.domain.practice.dto.response.PracticeResultsRes;
import com.example.ai_tutor.global.config.security.token.CurrentUser;
import com.example.ai_tutor.global.config.security.token.UserPrincipal;
import com.example.ai_tutor.global.payload.ErrorResponse;
import com.example.ai_tutor.global.payload.Message;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Student Practice", description = "학생 유저의 학습 점검 및 결과 관련 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/student/practice")
public class StudentPracticeController {

    private final StudentPracticeService studentPracticeService;

//     @Operation(summary = "문제 조회", description = "2단계 학습 점검 단계 문제를 조회합니다.")
//     @ApiResponses(value = {
//             @ApiResponse(responseCode = "200", description = "조회 성공", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = PracticeRes.class) ) } ),
//             @ApiResponse(responseCode = "400", description = "조회 실패", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class) ) } ),
//     })
//     @GetMapping("")
//     public ResponseEntity<?> findQuestion(
//             @Parameter(description = "Access Token을 입력해주세요.", required = true) @CurrentUser UserPrincipal userPrincipal,
//             @Parameter(description = "노트의 Id를 입력해주세요.", required = true) @RequestParam Long noteId,
//             @Parameter(description = "문제 번호를 입력해주세요. 기본 값은 1로 지정됩니다.", required = true) @RequestParam(defaultValue = "1") int number) {
//         return studentPracticeService.getQuestion(userPrincipal, noteId, number);
//     }

//     @Operation(summary = "답변 작성", description = "사용자가 답변을 작성합니다.")
//     @ApiResponses(value = {
//             @ApiResponse(responseCode = "200", description = "답변 저장 성공", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Message.class) ) } ),
//             @ApiResponse(responseCode = "400", description = "답변 저장 실패", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class) ) } ),
//     })
//     @PostMapping("")
//     public ResponseEntity<?> writeAnswer(
//             @Parameter(description = "Access Token을 입력해주세요.", required = true) @CurrentUser UserPrincipal userPrincipal,
//             @Parameter(description = "Schemas의 AnswerReq를 참고해주세요.", required = true) @Valid @RequestBody AnswerReq answerReq) {
//         return studentPracticeService.registerAnswer(userPrincipal, answerReq);
//     }

//     @Operation(summary = "문제 및 나의 답변, 튜터 답변 조회", description = "학습결과 보기에서 문제와 내 답변, 튜터 답변을 조회합니다.")
//     @ApiResponses(value = {
//             @ApiResponse(responseCode = "200", description = "조회 성공", content = { @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = PracticeResultsRes.class)) ) } ),
//             @ApiResponse(responseCode = "400", description = "조회 실패", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class) ) } ),
//     })
//     @GetMapping("/result")
//     public ResponseEntity<?> findPracticeResult(
//             @Parameter(description = "Access Token을 입력해주세요.", required = true) @CurrentUser UserPrincipal userPrincipal,
//             @Parameter(description = "노트의 Id를 입력해주세요.", required = true) @RequestParam Long noteId
//     ) {
//             return studentPracticeService.getQuestionsAndAnswers(userPrincipal, noteId);
//     }

//     @Operation(summary = "나의 답변 수정", description = "학습결과 보기에서 내 답변을 수정합니다.")
//     @ApiResponses(value = {
//             @ApiResponse(responseCode = "200", description = "수정 성공", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Message.class) ) } ),
//             @ApiResponse(responseCode = "400", description = "수정 실패", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class) ) } ),
//     })
//     @PatchMapping("/result")
//     public ResponseEntity<?> rewriteAnswers(
//             @Parameter(description = "Access Token을 입력해주세요.", required = true) @CurrentUser UserPrincipal userPrincipal,
//             @Parameter(description = "Schemas의 UpdateAnswersReq를 확인해주세요.", required = true) @RequestBody List<UpdateAnswersReq> updateAnswersReqs
//             ) {
//         return studentPracticeService.updateMyAnswers(userPrincipal, updateAnswersReqs);
//     }

}
