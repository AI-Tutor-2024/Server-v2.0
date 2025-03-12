package com.example.ai_tutor.domain.note.presentation;

import com.example.ai_tutor.domain.note.application.ProfessorNoteService;
import com.example.ai_tutor.domain.note.dto.request.NoteCreateReq;
import com.example.ai_tutor.domain.note.dto.response.FolderInfoRes;
import com.example.ai_tutor.domain.note.dto.response.NoteCodeRes;
import com.example.ai_tutor.domain.note.dto.response.NoteListRes;
import com.example.ai_tutor.domain.summary.application.SummaryService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/folders/{folderId}/notes")
@Tag(name = "Professor Note", description = "노트 관련 API입니다.")
public class NoteController {

    private final ProfessorNoteService professorNoteService;
    private final SummaryService summaryService;

    @Operation(summary = "새 노트 생성 API", description = "새 강의 노트를 생성하는 API입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "강의 노트 생성 성공", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Message.class) ) } ),
            @ApiResponse(responseCode = "400", description = "강의 노트 생성 실패", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class) ) } ),
    })
    @PostMapping()
    public ResponseEntity<?> createNewNote(
            @Parameter(description = "Access Token을 입력해주세요.", required = true) @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "folder의 id를 입력해주세요", required = true) @PathVariable Long folderId,
            @Parameter(description = "Schemas의 NoteCreateReq를 참고해주세요", required = true) @RequestBody NoteCreateReq noteCreateReq
    ) {
        return professorNoteService.createNewNote(userPrincipal, folderId, noteCreateReq);
    }

    // 노트 목록 조회
    @Operation(summary = "노트 목록 조회 API", description = "강의 노트 목록을 조회하는 API입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "강의 노트 목록 조회 성공", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = NoteListRes.class) ) } ),
            @ApiResponse(responseCode = "400", description = "강의 노트 목록 조회 실패", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class) ) } ),
    })
    @GetMapping()
    public ResponseEntity<?> getAllNotes(
            @Parameter(description = "Access Token을 입력해주세요.", required = true) @CurrentUser UserPrincipal userPrincipal,
            @Parameter(description = "folder의 id를 입력해주세요", required = true) @PathVariable Long folderId

    ) {
        return professorNoteService.getAllNotesByFolder(userPrincipal, folderId);
    }

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

    @Operation(summary = "노트 STT 변환 API", description = "노트의 강의 영상을 CLOVA API를 활용하여 STT 변환하는 API입니다. 처음 영상을 올리는 것이라면 이 API를 활용하여 영상을 TEXT로 변환하여야 합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "STT 변환 성공", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Message.class)) }),
            @ApiResponse(responseCode = "400", description = "STT 변환 실패", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)) }),
    })
    @PostMapping(value = "/{noteId}/stt", consumes = "multipart/form-data")
    public ResponseEntity<?> convertSpeechToText(
            @Parameter(description = "Access Token을 입력해주세요.", required = false) @CurrentUser UserPrincipal userPrincipal,
            @Parameter(description = "노트 ID", required = true) @PathVariable Long noteId,
            @Parameter(description = "STT 변환을 위한 강의 영상 파일", required = true,
                    schema = @Schema(type = "string", format = "binary"))
            @RequestPart MultipartFile file
    ) {
        try {
            boolean success = professorNoteService.convertSpeechToText(userPrincipal, noteId, file);
            if (success) {
                return ResponseEntity.ok("STT 변환이 완료되었습니다.");
            } else {
                return ResponseEntity.ok("이미 변환된 STT 데이터가 존재합니다.");
            }
        } catch (Exception e) {
            log.error("STT 변환 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("STT 변환 실패: " + e.getMessage());
        }
    }


    // ===============================
    // 📑 노트 요약 생성 & 조회
    // ===============================

    @Operation(summary = "노트 요약 생성", description = "저장된 STT 데이터를 기반으로 노트 요약을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "노트 요약 생성 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "노트 요약 생성 실패",
                    content = @Content(mediaType = "application/json"))
    })
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{noteId}/summaries")
    public Mono<ResponseEntity<String>> createSummary(
            @PathVariable Long noteId,
            @RequestParam(required = false) String keywords,
            @RequestParam(required = false) String requirement) {

        return summaryService.processSummaryFromSavedStt(noteId, keywords, requirement)
                .map(ResponseEntity::ok)
                .onErrorResume(error -> {
                    return Mono.just(ResponseEntity
                            .badRequest()
                            .body(error.getMessage()));
                });
    }


    @Operation(summary = "노트 요약 조회", description = "노트에 대한 요약본을 조회합니다.")
    @GetMapping("/{noteId}/summaries")
    public ResponseEntity<?> getSummary(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long noteId) {
        return summaryService.getSummary(userPrincipal, noteId);
    }

    @Operation(summary = "문제 랜덤 코드 생성 API", description = "특정 문제지의 고유한 랜덤 코드를 생성하는 API입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "문제 랜덤 코드 생성 성공", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = NoteCodeRes.class) ) } ),
            @ApiResponse(responseCode = "400", description = "문제 랜덤 코드 생성 실패", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class) ) } ),
    })
    @PostMapping("/{noteId}/quiz")
    public ResponseEntity<?> createRandomCode(
            @Parameter(description = "Access Token을 입력해주세요.", required = true) @CurrentUser UserPrincipal userPrincipal,
            @Parameter(description = "code를 생성하려는 note의 id를 입력해주세요", required = true) @PathVariable Long noteId
    ) {
        return professorNoteService.createRandomCode(userPrincipal, noteId);
    }

    // 문제지를 푼 학생들의 결과 및 정보 조회
    @Operation(summary = "문제지 결과 조회 API", description = "특정 문제지를 푼 학생들의 결과 및 정보를 조회하는 API입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "문제지 결과 조회 성공", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Message.class) ) } ),
            @ApiResponse(responseCode = "400", description = "문제지 결과 조회 실패", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class) ) } ),
    })
    @GetMapping("/{noteId}/quiz/result")
    public ResponseEntity<?> getNoteResult(
            @Parameter(description = "Access Token을 입력해주세요.", required = true) @CurrentUser UserPrincipal userPrincipal,
            @Parameter(description = "결과를 조회하려는 note의 id를 입력해주세요", required = true) @PathVariable Long noteId
    ) {
        return professorNoteService.getNoteResult(userPrincipal, noteId);
    }

}
