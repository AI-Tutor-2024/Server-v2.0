package com.example.ai_tutor.domain.note.presentation;

import com.example.ai_tutor.domain.note.application.ProfessorNoteService;
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
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/note/professor")
@Tag(name = "Professor Note", description = "교수자의 강의 노트 관련 API입니다.")
public class ProfessorNoteController {

    private final ProfessorNoteService professorNoteService;

    // @Operation(summary = "새 노트 생성 API", description = "새 강의 노트를 생성하는 API입니다.")
    // @ApiResponses(value = {
    //         @ApiResponse(responseCode = "200", description = "강의 노트 생성 성공", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Message.class) ) } ),
    //         @ApiResponse(responseCode = "400", description = "강의 노트 생성 실패", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class) ) } ),
    // })
    // @PostMapping("/{folderId}")
    // public ResponseEntity<?> createNewNote(
    //         @Parameter @CurrentUser UserPrincipal userPrincipal,
    //         @PathVariable Long folderId,
    //         @RequestBody NoteCreateReq noteCreateReq,
    //         @RequestParam MultipartFile note
    // ) {
    //     return professorNoteService.createNewNote(userPrincipal, folderId, noteCreateReq, note);
    // }

    // 노트 목록 조회

    @Operation(summary = "노트 삭제 API", description = "특정 강의 노트를 삭제하는 API입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "강의 노트 삭제 성공", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Message.class) ) } ),
            @ApiResponse(responseCode = "400", description = "강의 노트 삭제 실패", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class) ) } ),
    })
    @DeleteMapping("/{noteId}")
    public ResponseEntity<?> deleteNote(
            @Parameter(description = "Access Token을 입력해주세요.", required = true) @CurrentUser UserPrincipal userPrincipal,
            @Parameter(description = "삭제하려는 note의 id를 입력해주세요", required = true) @PathVariable Long noteId
    ) {
        return professorNoteService.deleteNoteById(userPrincipal, noteId);
    }

}
