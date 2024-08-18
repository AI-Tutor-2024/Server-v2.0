package com.example.ai_tutor.domain.note.presentation;

import com.example.ai_tutor.domain.note.application.StudentNoteService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/student/note")
@Tag(name = "Student Note", description = "학생의 강의 노트 관련 API입니다.")
public class StudentNoteController {

    private final StudentNoteService studentNoteService;

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
