package com.example.ai_tutor.domain.note.presentation;

import com.example.ai_tutor.domain.note.application.NoteService;
import com.example.ai_tutor.domain.note.dto.request.NoteCreateReq;
import com.example.ai_tutor.domain.note.dto.request.NoteDeleteReq;
import com.example.ai_tutor.domain.note.dto.request.NoteStepUpdateReq;
import com.example.ai_tutor.domain.note.dto.response.NoteListRes;
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
@RequestMapping("/api/v1/note")
@Tag(name = "Note", description = "강의 노트 관련 API입니다.")
public class NoteController {

    private final NoteService noteService;

    @Operation(summary = "새 노트 생성 API", description = "새 강의 노트를 생성하는 API입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "강의 노트 생성 성공", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Message.class) ) } ),
            @ApiResponse(responseCode = "400", description = "강의 노트 생성 실패", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class) ) } ),
    })
    @PostMapping("/{folderId}")
    public ResponseEntity<?> createNewNote(
            @Parameter @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long folderId,
            @RequestBody NoteCreateReq noteCreateReq,
            @RequestParam MultipartFile note
    ) {
        // AI APi 호출 로직 추가해야 함.
        return noteService.createNewNote(userPrincipal, folderId, noteCreateReq, note);
    }


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
        return noteService.getAllNotes(userPrincipal, folderId);
    }

    @Operation(summary = "노트 삭제 API", description = "특정 강의 노트를 삭제하는 API입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "강의 노트 삭제 성공", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Message.class) ) } ),
            @ApiResponse(responseCode = "400", description = "강의 노트 삭제 실패", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class) ) } ),
    })
    @DeleteMapping("/{/noteId}")
    public ResponseEntity<?> deleteNote(
            @Parameter @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long noteId,
            @RequestBody NoteDeleteReq noteDeleteReq
    ) {
        return noteService.deleteNoteById(userPrincipal, noteDeleteReq, noteId);
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
        return noteService.updateNoteStep(userPrincipal, noteId, noteStepUpdateReq);
    }

    @Operation(summary = "1단계 학습 API", description = "텍스트 원문과 요약문을 타임스탬프에 따라 조회하는 API입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "텍스트 원문/요약문 조회 성공", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Message.class) ) } ),
            @ApiResponse(responseCode = "400", description = "텍스트 원문/요약문 조회 실패", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class) ) } ),
    })
    @GetMapping("/{noteId}/step/1")
    public ResponseEntity<?> getStepOne(
            @Parameter @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long noteId
    ) {
        return noteService.getStepOne(userPrincipal, noteId);
    }


}
