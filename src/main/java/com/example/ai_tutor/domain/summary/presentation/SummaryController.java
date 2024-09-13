//package com.example.ai_tutor.domain.summary.presentation;
//
//import com.example.ai_tutor.domain.summary.application.SummaryService;
//import com.example.ai_tutor.global.config.security.token.CurrentUser;
//import com.example.ai_tutor.global.config.security.token.UserPrincipal;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//
//@Tag(name = "요약문", description = "요약문 관련 API")
//@RequiredArgsConstructor
//@RestController
//@RequestMapping("/api/v1/professor/summary")
//public class SummaryController {
//
//    private final SummaryService summaryService;
//
//    // STT 테스트용 API
//    @PostMapping("/stt")
//    public ResponseEntity<?> tts(
//            @CurrentUser UserPrincipal userPrincipal,
//            @RequestParam("file") MultipartFile file
//    ) throws IOException {
//        return ResponseEntity.ok(summaryService.createSummary(userPrincipal, file));
//    }
//
//
//}
