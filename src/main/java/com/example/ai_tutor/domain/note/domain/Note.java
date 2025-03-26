package com.example.ai_tutor.domain.note.domain;


import com.example.ai_tutor.domain.folder.domain.Folder;
import com.example.ai_tutor.domain.common.BaseEntity;
import com.example.ai_tutor.domain.summary.domain.Summary;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "note")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class Note extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "note_id", updatable = false)
    private Long noteId;

    @Column(name="title")
    private String title;

    @Column(name = "code")
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")
    private Folder folder;

    @OneToOne(mappedBy = "note", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private Summary summary;

    @Lob
    @Column(name = "stt_text", columnDefinition = "LONGTEXT")
    private String sttText; // stt로 변환된 텍스트

    @Lob
    @Column(name = "stt_response", columnDefinition = "LONGTEXT")
    private String sttResponse; // stt로 변환된 텍스트

    /**
     * ✅ 생성자 대신 팩토리 메서드 사용
     */
    public static Note createNote(Folder folder, String title, String code, String sttResponse) {
        return Note.builder()
                .folder(folder)
                .title(title)
                .code(code)
                .sttResponse(sttResponse)
                .build();
    }


    /**
     * ✅ STT 변환 완료 후 업데이트 메서드
     */
    public void updateStt(String sttText, String sttResponse) {
        this.sttText = sttText;
        this.sttResponse = sttResponse;
    }

    public void markSttFailed() {
        this.sttText = null;  // 실패했으므로 변환된 텍스트 제거
        this.sttResponse = null;  // 응답 데이터도 초기화
    }

    public void updateCode(String code) {this.code = code;}
}
