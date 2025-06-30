package com.example.ai_tutor.global.util;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class ConvertToMp3 {

    private static final List<String> SUPPORTED_EXTENSIONS = List.of("mp4", "m4a", "wav");

    /**
     * 확장자 확인
     */
    private String getExtension(MultipartFile file) {
        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.contains(".")) {
            throw new IllegalArgumentException("파일 이름에 확장자가 없습니다.");
        }
        return originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase();
    }

    /**
     * 사용자가 업로드한 파일을 mp3로 변환하여 반환합니다.
     * @param inputFile 사용자로부터 업로드된 MultipartFile (.mp4, .m4a, .wav)
     * @return mp3로 변환된 File 객체
     */
    public File convert(MultipartFile inputFile) throws IOException, InterruptedException {
        String ext = getExtension(inputFile);
        if (!SUPPORTED_EXTENSIONS.contains(ext)) {
            throw new IllegalArgumentException("지원하지 않는 확장자입니다: " + ext);
        }

        // 임시 파일로 저장
        File inputTemp = File.createTempFile("input-", "." + ext);
        inputFile.transferTo(inputTemp);

        // 출력될 mp3 임시 파일
        File outputMp3 = File.createTempFile("converted-", ".mp3");

        // ffmpeg 명령어 실행
        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg", "-y",               // 덮어쓰기 허용
                "-i", inputTemp.getAbsolutePath(),
                "-vn",                        // 영상 제거 (오디오만)
                "-acodec", "libmp3lame",      // mp3 코덱
                outputMp3.getAbsolutePath()
        );

        pb.redirectErrorStream(true);
        Process process = pb.start();
        int exitCode = process.waitFor();

        // 임시 원본 파일은 삭제
        if (!inputTemp.delete()) {
            log.warn("임시 입력 파일 삭제 실패: {}", inputTemp.getAbsolutePath());
        }

        if (exitCode != 0) {
            if (!outputMp3.delete()) {
                log.warn("실패한 mp3 파일 삭제 실패: {}", outputMp3.getAbsolutePath());
            }
            throw new RuntimeException("ffmpeg 변환 실패 (exitCode=" + exitCode + ")");
        }

        return outputMp3;
    }
}