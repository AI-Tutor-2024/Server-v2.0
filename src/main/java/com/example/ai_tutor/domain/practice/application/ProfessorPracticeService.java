package com.example.ai_tutor.domain.practice.application;

import com.example.ai_tutor.domain.Folder.domain.Folder;
import com.example.ai_tutor.domain.chatgpt.application.GptService;
import com.example.ai_tutor.domain.note.domain.Note;
import com.example.ai_tutor.domain.note.domain.repository.NoteRepository;
import com.example.ai_tutor.domain.practice.dto.request.CreatePracticeReq;
import com.example.ai_tutor.domain.practice.dto.response.CreatePracticeListRes;
import com.example.ai_tutor.domain.practice.dto.response.CreatePracticeRes;
import com.example.ai_tutor.domain.professor.domain.Professor;
import com.example.ai_tutor.domain.professor.domain.repository.ProfessorRepository;
import com.example.ai_tutor.domain.user.domain.User;
import com.example.ai_tutor.domain.user.domain.repository.UserRepository;
import com.example.ai_tutor.global.config.security.token.UserPrincipal;
import com.example.ai_tutor.global.payload.ApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfessorPracticeService {

    private final UserRepository userRepository;
    private final NoteRepository noteRepository;
    private ProfessorRepository professorRepository;

    private final GptService gptService;

    // 문제 생성
    public ResponseEntity<?> generatePractice(UserPrincipal userPrincipal, CreatePracticeReq createPracticeReq) throws JsonProcessingException {
        User user = userRepository.findById(userPrincipal.getId()).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Professor professor = professorRepository.findByUser(user).orElseThrow(() -> new IllegalArgumentException("교수자를 찾을 수 없습니다."));

        // 요약문 (파일 기반으로 생성하는 예시)
        // 요약문 생성 메소드 여기 연결해주세요!
        String summary = "1949년 10월 1일 제정된「국경일에 관한 법률」에 의거, 광복절이 국경일로 제정되었다. 이 날은 경축행사를 전국적으로 거행하는데 중앙경축식은 서울에서, 지방경축행사는 각 시·도 단위별로 거행한다.\n" +
                "\n" +
                "이 날의 의의를 고양하고자 전국의 모든 가정은 국기를 달아 경축하며, 정부는 이 날 저녁에 각계각층의 인사와 외교사절을 초청하여 경축연회를 베푼다.\n" +
                "\n" +
                "광복회원을 위한 우대조치의 하나로, 광복회원 및 동반가족에 대하여 전국의 철도·시내버스 및 수도권전철의 무임승차와 고궁 및 공원에 무료입장할 수 있도록 하고 있다.\n" +
                "[네이버 지식백과] 광복절 [光復節] (한국민족문화대백과, 한국학중앙연구원)";

        // 문제 개수 및 유형
        int practiceSize = createPracticeReq.getPracticeSize();
        List<CreatePracticeRes> practices = new ArrayList<>();

        if ("BOTH".equalsIgnoreCase(createPracticeReq.getType())) {
            // 문제 개수에 따라 OX 문제와 객관식 문제를 생성
            int oxCount = practiceSize % 2 == 0 ? practiceSize / 2 : practiceSize / 2 + 1;
            int multipleCount = practiceSize - oxCount;

            List<CreatePracticeRes> oxPractices = gptService.generateQuestions(summary, oxCount, "OX", 1);
            List<CreatePracticeRes> multiplePractices = gptService.generateQuestions(summary, multipleCount, "MULTIPLE", oxCount + 1);

            practices.addAll(oxPractices);
            practices.addAll(multiplePractices);
        } else {
            // 지정된 문제 유형에 맞는 문제 생성
            practices = gptService.generateQuestions(summary, practiceSize, createPracticeReq.getType(), 1);
        }

        CreatePracticeListRes createPracticeListRes = CreatePracticeListRes.builder()
                .practiceResList(practices)
                .build();

        ApiResponse apiResponse = ApiResponse.builder()
                .check(true)
                .information(createPracticeListRes)
                .build();
        return ResponseEntity.ok(apiResponse);
    }





}
