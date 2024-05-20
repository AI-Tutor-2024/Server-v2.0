package com.example.ai_tutor.domain.note.presentation;

import com.example.ai_tutor.domain.note.application.NoteService;
import com.example.ai_tutor.domain.note.dto.request.NoteCreateReq;
import com.example.ai_tutor.domain.note.dto.request.NoteDeleteReq;
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
            @RequestBody NoteCreateReq noteCreateReq
    ) {
        return noteService.createNewNote(userPrincipal, folderId, noteCreateReq);
    }


    @Operation(summary = "노트 목록 조회 API", description = "강의 노트 목록을 조회하는 API입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "강의 노트 목록 조회 성공", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Message.class) ) } ),
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
    @DeleteMapping("/{noteId}")
    public ResponseEntity<?> deleteNote(
            @Parameter @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long noteId,
            @RequestBody NoteDeleteReq noteDeleteReq
    ) {
        return noteService.deleteNoteById(userPrincipal, noteDeleteReq, noteId);
    }


}
