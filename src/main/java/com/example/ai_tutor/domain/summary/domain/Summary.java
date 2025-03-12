package com.example.ai_tutor.domain.summary.domain;


import com.example.ai_tutor.domain.common.BaseEntity;
import com.example.ai_tutor.domain.note.domain.Note;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="Summary")
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Getter
public class Summary extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="summary_id", updatable = false)
    private Long summaryId;

    @Lob
    @Column(name="content", columnDefinition = "TEXT")
    private String content;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="note_id")
    private Note note;


    public static Summary create(String content, Note note) {
        return Summary.builder()
                .content(content)
                .note(note)
                .build();
    }

}
