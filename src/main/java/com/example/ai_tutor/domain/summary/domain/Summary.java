package com.example.ai_tutor.domain.summary.domain;

import com.example.ai_tutor.domain.Folder.domain.Folder;
import com.example.ai_tutor.domain.common.BaseEntity;
import com.example.ai_tutor.domain.note.domain.Note;
import com.example.ai_tutor.domain.text.domain.Text;
import com.example.ai_tutor.domain.user.domain.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

//content
@Entity
@Table(name="Summary")
@NoArgsConstructor
@Getter

public class Summary extends BaseEntity {
    @Id
    @Column(name="summary_id", updatable = false, nullable = false)
    private Long summaryId;

    @Column(name="content")
    private String content;


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="text_id", nullable = false)
    private Text text;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="folder_id")
    private Folder folder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="note_id")
    private Note note;

    @Builder
    public Summary(Text text, User user, Folder folder, Note note, String content){
        this.text = text;
        this.user = user;
        this.folder = folder;
        this.note = note;
        this.content = content;
    }

    public void updateSummary(String newSummary) {
        this.content = newSummary;
    }
}
