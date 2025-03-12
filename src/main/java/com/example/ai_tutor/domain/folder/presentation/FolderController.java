package com.example.ai_tutor.domain.folder.presentation;

import com.example.ai_tutor.domain.folder.application.FolderService;
import com.example.ai_tutor.domain.folder.dto.request.FolderCreateReq;
import com.example.ai_tutor.domain.folder.dto.response.FolderListRes;
import com.example.ai_tutor.domain.folder.dto.response.FolderNameListRes;
import com.example.ai_tutor.domain.note.dto.response.FolderInfoRes;
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
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/folders")
@Tag(name = "Folder", description = "폴더 관련 API입니다.")
public class FolderController {

    private final FolderService folderService;

    @Operation(summary = "폴더 생성 API", description = "폴더를 생성하는 API입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "새 폴더 생성 성공", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Message.class) ) } ),
            @ApiResponse(responseCode = "400", description = "새 폴더 생성 실패", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class) ) } ),
    })
    @PostMapping
    public ResponseEntity<?> createNewFolder(
            @Parameter(description = "Access Token을 입력해주세요.", required = true) @CurrentUser UserPrincipal userPrincipal,
            @Parameter(description = "Schemas의 FolderCreateReq를 참고해주세요", required = true) @RequestBody FolderCreateReq folderCreateReq
            ) {
        return folderService.createNewFolder(userPrincipal, folderCreateReq);
    }

    // 교수자 - 폴더 목록 조회
    @Operation(summary = "폴더 목록 조회 API", description = "폴더 목록을 조회하는 API입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "폴더 목록 조회 성공", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = FolderListRes.class) ) } ),
            @ApiResponse(responseCode = "400", description = "폴더 목록 조회 실패", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class) ) } ),
    })
    @GetMapping
    public ResponseEntity<?> getAllFolders(
            @Parameter(description = "Access Token을 입력해주세요.", required = true) @CurrentUser UserPrincipal userPrincipal
    ) {
        return folderService.getAllFolders(userPrincipal);
    }


    @Operation(summary = "폴더 정보 조회 API", description = "노트를 생성하기 전 해당 노트의 폴더 - 강의명과 교수자명을 조회하는 API입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = FolderInfoRes.class) ) } ),
            @ApiResponse(responseCode = "400", description = "조회 실패", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class) ) } ),
    })
    @GetMapping("/{folderId}/info")
    public ResponseEntity<?> getFolderInfoBeforeCreatingNote(
            @Parameter(description = "Access Token을 입력해주세요.", required = true) @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "folder의 id를 입력해주세요", required = true) @PathVariable Long folderId
    ) {
        return folderService.getFolderInfo(userPrincipal, folderId);
    }


    @Operation(summary = "폴더 이름 목록 조회 API", description = "폴더 이름 목록을 조회하는 API입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "폴더 이름 목록 조회 성공", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = FolderNameListRes.class) ) } ),
            @ApiResponse(responseCode = "400", description = "폴더 이름 목록 조회 실패", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class) ) } ),
    })
    @GetMapping("/names")
    public ResponseEntity<?> getFolderNames(
            @Parameter(description = "Access Token을 입력해주세요.", required = true) @CurrentUser UserPrincipal userPrincipal
        ) {
        return folderService.getFolderNames(userPrincipal);
    }

    @Operation(summary = "폴더 정보 수정 API", description = "폴더 정보(강좌명/교수자명)를 수정하는 API입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "폴더 정보 수정 성공", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Message.class) ) } ),
            @ApiResponse(responseCode = "400", description = "폴더 정보 수정 실패", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class) ) } ),
    })
    @PatchMapping("/{folderId}")
    public ResponseEntity<?> updateFolder(
            @Parameter(description = "Access Token을 입력해주세요.", required = true) @CurrentUser UserPrincipal userPrincipal,
            @Parameter(description = "folder의 id를 입력해주세요", required = true) @PathVariable Long folderId,
            @Parameter(description = "Schemas의 FolderCreateReq를 참고해주세요", required = true) @RequestBody FolderCreateReq folderCreateReq
    ) {
        return folderService.updateFolder(userPrincipal, folderId, folderCreateReq);
    }

    @Operation(summary = "폴더 삭제 API", description = "특정 폴더를 삭제하는 API입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "폴더 정보 수정 성공", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Message.class) ) } ),
            @ApiResponse(responseCode = "400", description = "폴더 정보 수정 실패", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class) ) } ),
    })
    @DeleteMapping("/{folderId}")
    public ResponseEntity<?> deleteFolder(
            @Parameter(description = "Access Token을 입력해주세요.", required = true) @CurrentUser UserPrincipal userPrincipal,
            @Parameter(description = "folder의 id를 입력해주세요", required = true) @PathVariable Long folderId
    ) {
        return folderService.deleteFolder(userPrincipal, folderId);
    }

}
