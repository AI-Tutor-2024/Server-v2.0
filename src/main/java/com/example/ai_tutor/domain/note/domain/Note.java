package com.example.ai_tutor.domain.note.domain;


import com.example.ai_tutor.domain.Folder.domain.Folder;
import com.example.ai_tutor.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @Column(name="title")
    private String title;

    @Column(name="step")
    private int step;

    // 제한시간
    private LocalDateTime limit;

    // 마감시간
    private LocalDate endDate;

    // code
    private String code;

    // 총점
    private int total;

    // 평균
    private double average = 0;

    // 응시 상태
    @Enumerated(EnumType.STRING)
    private NoteStatus noteStatus = NoteStatus.INCOMPLETED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="folder_id")
    private Folder folder;

       // @OneToMany(mappedBy = "note")
    // private List<Note> notes= new ArrayList<>();

//    public void updateStatus(NoteStatus noteStatus) {
//        this.noteStatus = noteStatus;
//    }

    @Builder
    public Note(Folder folder, String title, int step, LocalDateTime limit, LocalDate endDate, String code, int total){
        this.folder = folder;
        this.title = title;
        this.step = step;
        this.limit = limit;
        this.endDate = endDate;
        this.code = code;
        this.total = total;
    }

    public void updateStep(int step) {
        this.step = step;
    }
}
