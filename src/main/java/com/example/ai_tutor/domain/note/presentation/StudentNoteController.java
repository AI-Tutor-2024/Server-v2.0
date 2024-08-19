package com.example.ai_tutor.domain.note.presentation;

import com.example.ai_tutor.domain.note.application.StudentNoteService;

import com.example.ai_tutor.domain.note.dto.request.NoteAccessReq;
import com.example.ai_tutor.domain.note.dto.request.NoteCreateReq;
import com.example.ai_tutor.domain.note.dto.request.NoteStepUpdateReq;
import com.example.ai_tutor.domain.note.dto.response.NoteAccessRes;
import com.example.ai_tutor.domain.note.dto.response.NoteListRes;
import com.example.ai_tutor.domain.note.dto.response.StepOneListRes;
import com.example.ai_tutor.global.config.security.token.CurrentUser;
import com.example.ai_tutor.global.config.security.token.UserPrincipal;
import com.example.ai_tutor.global.payload.ErrorResponse;
import com.example.ai_tutor.global.payload.Message;


import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/student/note")
@Tag(name = "Student Note", description = "학생의 강의 노트 관련 API입니다.")
public class StudentNoteController {

    private final StudentNoteService studentNoteService;

    // 노트 코드를 통해 특정 노트에 접근하여 문제 풀이 수행
    @Operation(summary = "노트 코드로 노트 접근 API", description = "노트 코드를 통해 특정 문제지에 접근하여 문제 풀이를 수행하는 API입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "노트 코드를 통해 문제지 접근 성공", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = NoteAccessRes.class) ) } ),
            @ApiResponse(responseCode = "400", description = "노트 코드를 통해 문제지 접근 실패", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class) ) } ),
    })
    @PostMapping("/")
    public ResponseEntity<?> accessNoteByCode(
            @Parameter(description = "Schemas의 NoteAccessReq를 참고해주세요", required = true) @RequestBody NoteAccessReq noteAccessReq
    ) {
        return studentNoteService.accessNoteByCode(noteAccessReq);
    }


//    @Operation(summary = "노트 목록 조회 API", description = "강의 노트 목록을 조회하는 API입니다.")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "강의 노트 목록 조회 성공", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = NoteListRes.class) ) } ),
//            @ApiResponse(responseCode = "400", description = "강의 노트 목록 조회 실패", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class) ) } ),
//    })
//    @GetMapping("/{folderId}")
//    public ResponseEntity<?> getAllNotes(
//            @Parameter @CurrentUser UserPrincipal userPrincipal,
//            @PathVariable Long folderId

//    ) {
//        return studentNoteService.getAllNotes(userPrincipal, folderId);
//    }

}
