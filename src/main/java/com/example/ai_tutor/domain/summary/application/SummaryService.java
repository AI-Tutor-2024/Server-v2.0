package com.example.ai_tutor.domain.summary.application;
import com.example.ai_tutor.domain.summary.dto.response.SttRes;
import com.example.ai_tutor.domain.user.domain.User;
import com.example.ai_tutor.domain.user.domain.repository.UserRepository;
import com.example.ai_tutor.global.config.security.token.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class SummaryService {

    private final UserRepository userRepository;
    private final WebClient webClient;

    public String createSummary(UserPrincipal userPrincipal, MultipartFile file) throws IOException {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        // Create MultipartBodyBuilder
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new ByteArrayResource(file.getBytes()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "form-data; name=file; filename=" + file.getOriginalFilename());

        // Send the file using WebClient and retrieve the summary field from the response
        Mono<String> response = webClient.post()
                .uri("/stt-summary/")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(SttRes.class)
                .map(SttRes::getSummary);

        // Get the summary text
        return response.block();
    }


}