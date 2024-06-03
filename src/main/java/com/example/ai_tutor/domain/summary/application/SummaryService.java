package com.example.ai_tutor.domain.summary.application;

import com.example.ai_tutor.domain.Folder.domain.Folder;
import com.example.ai_tutor.domain.note.domain.Note;
import com.example.ai_tutor.domain.note.domain.repository.NoteRepository;
import com.example.ai_tutor.domain.summary.domain.Summary;
import com.example.ai_tutor.domain.summary.domain.repository.SummaryRepository;
import com.example.ai_tutor.domain.summary.dto.request.SummaryUpdateReq;
import com.example.ai_tutor.domain.user.domain.User;
import com.example.ai_tutor.domain.user.domain.repository.UserRepository;
import com.example.ai_tutor.global.DefaultAssert;
import com.example.ai_tutor.global.config.security.token.UserPrincipal;
import com.example.ai_tutor.global.payload.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SummaryService {
    private final UserRepository userRepository;
    private final SummaryRepository summaryRepository;
    private final NoteRepository noteRepository;

    @Transactional
    public ResponseEntity<?> updateSummary(
            UserPrincipal userPrincipal,
            Long noteId, Long summaryId,
            SummaryUpdateReq summaryUpdateReq) {
        User user = userRepository.findById(userPrincipal.getId()).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Note note = noteRepository.findById(noteId).orElseThrow(() -> new IllegalArgumentException("노트를 찾을 수 없습니다."));
        DefaultAssert.isTrue(note.getUser().equals(user), "해당 폴더에 접근할 수 없습니다.");

        String newSummary = summaryUpdateReq.getSummary();
        Summary summary = summaryRepository.findById(summaryId).orElseThrow(() -> new IllegalArgumentException("요약문을 찾을 수 없습니다."));
        summary.updateSummary(newSummary);

        ApiResponse apiResponse=ApiResponse.builder()
                .check(true)
                .information("요약문 수정 성공")
                .build();

        return ResponseEntity.ok(apiResponse);
    }
}
