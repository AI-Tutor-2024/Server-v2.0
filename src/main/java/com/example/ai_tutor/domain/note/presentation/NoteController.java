package com.example.ai_tutor.domain.note.presentation;

import com.example.ai_tutor.domain.note.application.ProfessorNoteService;
import com.example.ai_tutor.domain.note.dto.request.NoteCreateReq;
import com.example.ai_tutor.domain.note.dto.response.NoteAccessRes;
import com.example.ai_tutor.domain.note.dto.response.NoteCodeRes;
import com.example.ai_tutor.domain.note.dto.response.NoteListRes;
import com.example.ai_tutor.domain.practice.dto.request.SavePracticeListReq;
import com.example.ai_tutor.domain.summary.application.SummaryService;
import com.example.ai_tutor.domain.summary.dto.response.SummaryRes;
import com.example.ai_tutor.global.config.security.token.UserPrincipal;
import com.example.ai_tutor.global.payload.ErrorResponse;
import com.example.ai_tutor.global.payload.Message;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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


    @Operation(
            summary = "새 빈 노트 생성 API", security = { @SecurityRequirement(name = "BearerAuth") },
            description = "새 비어있는 강의 노트를 생성하는 API입니다. 특정 folder ID에 title(강의 제목) 값만 요청하면 됩니다. 이때 폴더 ID 는 로그인한 회원이 생성한 폴더만 노트가 생성 가능합니다. (타인 계정으로 불가능)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "강의 노트 생성 성공", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = NoteAccessRes.class) ) } ),
            @ApiResponse(responseCode = "400", description = "강의 노트 생성 실패", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class) ) } ),
    })
    @PostMapping()
    public ResponseEntity<?> createNewNote(
            @Parameter(description = "Access Token을 입력해주세요.", required = true) @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "folder의 id를 입력해주세요", required = true) @PathVariable Long folderId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Schemas의 NoteCreateReq를 참고해주세요",
                    required = true,
                    content = @Content(schema = @Schema(implementation = NoteCreateReq.class))
            ) @RequestBody NoteCreateReq noteCreateReq
    ) {
        return professorNoteService.createNewNote(userPrincipal, folderId, noteCreateReq);
    }



    // 노트 목록 조회
    @Operation(summary = "노트 목록 조회 API", security = { @SecurityRequirement(name = "BearerAuth") }, description = "로그인한 유저가 만든 요청한 폴더에 대한 강의 노트 목록을 조회하는 API입니다.")
    @ApiResponses(value = {

            @ApiResponse(responseCode = "200", description = "강의 노트 목록 조회 성공", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = NoteListRes.class) ) } ),
            @ApiResponse(responseCode = "400", description = "강의 노트 목록 조회 실패", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class) ) } ),
    })
    @GetMapping()
    public ResponseEntity<?> getAllNotes(
            @Parameter(description = "Access Token을 입력해주세요.", required = true) @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "folder의 id를 입력해주세요") @PathVariable Long folderId

    ) {
        return professorNoteService.getAllNotesByFolder(userPrincipal, folderId);
    }

    @Operation(summary = "노트 단일 조회 API", security = { @SecurityRequirement(name = "BearerAuth") }, description = "로그인한 유저가 만든 특정 강의 노트 목록을 조회하는 API입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "강의 노트 목록 조회 성공", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = NoteListRes.class) ) } ),
            @ApiResponse(responseCode = "400", description = "강의 노트 목록 조회 실패", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class) ) } ),
    })
    @GetMapping("/{noteId}")
    public ResponseEntity<?> getNote(
            @Parameter(description = "Access Token을 입력해주세요.", required = true) @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "삭제하려는 note의 id를 입력해주세요", required = true) @PathVariable Long noteId
    ) {
        return professorNoteService.getNote(userPrincipal, noteId);
    }

    @Operation(summary = "노트 삭제 API", security = { @SecurityRequirement(name = "BearerAuth") }, description = "특정 강의 노트를 삭제하는 API입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "강의 노트 삭제 성공", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Message.class) ) } ),
            @ApiResponse(responseCode = "400", description = "강의 노트 삭제 실패", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class) ) } ),
    })
    @DeleteMapping("/{noteId}")
    public ResponseEntity<?> deleteNote(
            @Parameter(description = "Access Token을 입력해주세요.", required = true) @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "삭제하려는 note의 id를 입력해주세요", required = true) @PathVariable Long noteId
    ) {
        return professorNoteService.deleteNoteById(userPrincipal, noteId);
    }



    @Operation(summary = "1. 노트 STT 변환 API", security = { @SecurityRequirement(name = "BearerAuth") }, description = "노트의 강의 영상을 CLOVA API를 활용하여 STT 변환하는 API입니다. 처음 영상을 올리는 것이라면 필수적으로 이 API를 요청하여 영상을 TEXT로 변환하여야 합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "STT 변환 성공", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Message.class)) }),
            @ApiResponse(responseCode = "400", description = "STT 변환 실패", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)) }),
    })
    @PostMapping(value = "/{noteId}/stt", consumes = "multipart/form-data")
    public ResponseEntity<?> convertSpeechToText(
//            @Parameter(description = "Access Token을 입력해주세요.", required = false) @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "노트 ID", required = true) @PathVariable Long noteId,
            @Parameter(description = "STT 변환을 위한 강의 영상 파일", required = true,
                    schema = @Schema(type = "string", format = "binary"))
            @RequestPart("file") MultipartFile file
    ) {
        try {
            boolean success = professorNoteService.convertSpeechToText(noteId, file);
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
    @Operation(summary = "2. 노트 요약 생성", security = { @SecurityRequirement(name = "BearerAuth") }, description = "저장된 STT 데이터를 기반으로 노트 요약을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "노트 요약 생성 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SummaryRes.class))),
            @ApiResponse(responseCode = "400", description = "노트 요약 생성 실패",
                    content = @Content(mediaType = "application/json"))
    })
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{noteId}/summaries")
    public Mono<ResponseEntity<SummaryRes>> createSummary(
            @Parameter(description = "note id를 입력해주세요", required = true) @PathVariable Long noteId,
            @Parameter(description = "folder의 id를 입력해주세요", required = true) @PathVariable Long folderId,
            @RequestParam(required = false) String keywords,
            @RequestParam(required = false) String requirement) {

        return summaryService.processSummaryFromSavedStt(noteId, keywords, requirement)
                .map(ResponseEntity::ok)
                .onErrorResume(error -> Mono.just(ResponseEntity
                        .badRequest()
                        .build()));
    }


    @Operation(summary = "노트 요약 조회", description = "노트에 대한 요약본을 조회합니다.")
    @GetMapping("/{noteId}/summaries")
    public ResponseEntity<?> getSummary(
//            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "note id를 입력해주세요", required = true) @PathVariable Long noteId,
            @Parameter(description = "folder의 id를 입력해주세요", required = true) @PathVariable Long folderId) {
        return summaryService.getSummary(noteId);
    }


    // ==== deprecated ====
    @Operation(summary = "문제 랜덤 코드 생성 API", description = "특정 문제지의 고유한 랜덤 코드를 생성하는 API입니다.", tags = "Deprecated")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "문제 랜덤 코드 생성 성공", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = NoteCodeRes.class) ) } ),
            @ApiResponse(responseCode = "400", description = "문제 랜덤 코드 생성 실패", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class) ) } ),
    })
    @PostMapping("/{noteId}/quiz")
    @Deprecated
    public ResponseEntity<?> createRandomCode(
            @Parameter(description = "Access Token을 입력해주세요.", required = true) @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "note id를 입력해주세요", required = true) @PathVariable Long noteId,
            @Parameter(description = "folder의 id를 입력해주세요", required = true) @PathVariable Long folderId
    ) {
        return professorNoteService.createRandomCode(userPrincipal, noteId);
    }

    // 문제지를 푼 학생들의 결과 및 정보 조회
    @Operation(summary = "문제지 결과 조회 API", description = "특정 문제지를 푼 학생들의 결과 및 정보를 조회하는 API입니다.", tags = "Deprecated")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "문제지 결과 조회 성공", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Message.class) ) } ),
            @ApiResponse(responseCode = "400", description = "문제지 결과 조회 실패", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class) ) } ),
    })
    @GetMapping("/{noteId}/quiz/result")
    @Deprecated
    public ResponseEntity<?> getNoteResult(
            @Parameter(description = "Access Token을 입력해주세요.", required = true) @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "note id를 입력해주세요", required = true) @PathVariable Long noteId,
            @Parameter(description = "folder의 id를 입력해주세요", required = true) @PathVariable Long folderId
    ) {
        return professorNoteService.getNoteResult(userPrincipal, noteId);
    }


}
