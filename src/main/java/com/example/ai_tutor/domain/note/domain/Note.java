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

    // 제한시간
    private int limitTime;

    // 마감시간
    private LocalDateTime endDate;

    // code
    private String code;

    // 총점
    // private int total;

    // 평균
    private double average = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="folder_id")
    private Folder folder;

       // @OneToMany(mappedBy = "note")
    // private List<Note> notes= new ArrayList<>();

//    public void updateStatus(NoteStatus noteStatus) {
//        this.noteStatus = noteStatus;
//    }

    @Builder
    public Note(Folder folder, String title, int limitTime, LocalDateTime endDate, String code){
        this.folder = folder;
        this.title = title;
        this.limitTime = limitTime;
        this.endDate = endDate;
        this.code = code;
    }
}
