package com.example.ai_tutor.domain.summary.domain;


import com.example.ai_tutor.domain.common.BaseEntity;
import com.example.ai_tutor.domain.note.domain.Note;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="Summary")
@NoArgsConstructor
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

    @Builder
    public Summary(String content, Note note) {
        this.content = content;
        this.note = note;
    }

}
