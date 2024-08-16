package com.example.ai_tutor.domain.note.presentation;

import com.example.ai_tutor.domain.note.application.StudentNoteService;
import com.example.ai_tutor.domain.note.dto.request.NoteCreateReq;
import com.example.ai_tutor.domain.note.dto.request.NoteDeleteReq;
import com.example.ai_tutor.domain.note.dto.request.NoteStepUpdateReq;
import com.example.ai_tutor.domain.note.dto.response.NoteListRes;
import com.example.ai_tutor.domain.note.dto.response.StepOneListRes;
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
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/note/student")
@Tag(name = "Student Note", description = "학생의 강의 노트 관련 API입니다.")
public class StudentNoteController {

    private final StudentNoteService studentNoteService;

    @Operation(summary = "노트 목록 조회 API", description = "강의 노트 목록을 조회하는 API입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "강의 노트 목록 조회 성공", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = NoteListRes.class) ) } ),
            @ApiResponse(responseCode = "400", description = "강의 노트 목록 조회 실패", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class) ) } ),
    })
    @GetMapping("/{folderId}")
    public ResponseEntity<?> getAllNotes(
            @Parameter @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long folderId

    ) {
        return studentNoteService.getAllNotes(userPrincipal, folderId);
    }

    @Operation(summary = "학습 단계 업데이트 API", description = "특정 강의 노트의 학습 단계를 업데이트하는 API입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "학습 단계 업데이트 성공", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Message.class) ) } ),
            @ApiResponse(responseCode = "400", description = "학습 단계 업데이트 실패", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class) ) } ),
    })
    @PatchMapping("/{noteId}")
    public ResponseEntity<?> updateNoteLevel(
            @Parameter @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long noteId,
            @RequestBody NoteStepUpdateReq noteStepUpdateReq
    ) {
        return studentNoteService.updateNoteStep(userPrincipal, noteId, noteStepUpdateReq);
    }

    // @Operation(summary = "1단계 학습 API", description = "텍스트 원문과 요약문을 타임스탬프에 따라 조회하는 API입니다.")
    // @ApiResponses(value = {
    //         @ApiResponse(responseCode = "200", description = "텍스트 원문/요약문 조회 성공", content = { @Content(mediaType = "application/json", schema = @Schema(implementation =  StepOneListRes.class) ) } ),
    //         @ApiResponse(responseCode = "400", description = "텍스트 원문/요약문 조회 실패", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class) ) } ),
    // })
    // @GetMapping("/{noteId}/step/1")
    // public ResponseEntity<?> getStepOne(
    //         @Parameter @CurrentUser UserPrincipal userPrincipal,
    //         @PathVariable Long noteId
    // ) {
    //     return studentNoteService.getStepOne(userPrincipal, noteId);
    // }


}
