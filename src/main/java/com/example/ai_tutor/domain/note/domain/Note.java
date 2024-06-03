package com.example.ai_tutor.domain.note.domain;


import com.example.ai_tutor.domain.Folder.domain.Folder;
import com.example.ai_tutor.domain.common.BaseEntity;
import com.example.ai_tutor.domain.common.Status;
import com.example.ai_tutor.domain.summary.domain.Summary;
import com.example.ai_tutor.domain.text.domain.Text;
import com.example.ai_tutor.domain.tutor.domain.Tutor;
import com.example.ai_tutor.domain.user.domain.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="Note")
@NoArgsConstructor
@Getter
public class Note extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="note_id", updatable = false)
    private Long noteId;

    @Column(name="length")
    private int length;

    @Column(name="title")
    private String title;

    @Column(name="step")
    private int step;

    @Column(name="record_url")
    private String recordUrl;

//    @Enumerated(EnumType.STRING)
//    @Column(name="note_status")
//    private NoteStatus noteStatus=NoteStatus.IN_PROGRESS;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="folder_id")
    private Folder folder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id")
    private User user;

    @OneToMany(mappedBy = "note")
    private List<Text> texts= new ArrayList<>();

    // 원본 텍스트
    @Lob
    @Column(name="original_text", columnDefinition = "TEXT")
    private String originalText;

    // @OneToMany(mappedBy = "note")
    // private List<Note> notes= new ArrayList<>();

    @OneToMany(mappedBy = "note")
    private List<Summary> summaries= new ArrayList<>();

//    public void updateStatus(NoteStatus noteStatus) {
//        this.noteStatus = noteStatus;
//    }

    @Builder
    public Note(User user, Folder folder, String title, int length, int step, String recordUrl, String originalText){
        this.user = user;
        this.folder = folder;
        this.title = title;
        this.length = length;
        this.step = step;
        this.recordUrl = recordUrl;
        this.originalText = originalText;
    }

    public void updateStep(int step) {
        this.step = step;
    }
}
