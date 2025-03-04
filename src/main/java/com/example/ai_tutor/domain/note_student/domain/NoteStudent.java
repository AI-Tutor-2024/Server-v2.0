package com.example.ai_tutor.domain.note_student.domain;

import com.example.ai_tutor.domain.common.BaseEntity;
import com.example.ai_tutor.domain.note.domain.Note;
import com.example.ai_tutor.domain.note.domain.NoteStatus;
import com.example.ai_tutor.domain.student.domain.Student;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="NoteStudent")
@NoArgsConstructor
@Getter
public class NoteStudent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="note_student_id", updatable = false)
    private Long noteStudentId;

    // 맞은 문제 수
    private int score;

    // 응시 상태
    @Enumerated(EnumType.STRING)
    private NoteStatus noteStatus = NoteStatus.INCOMPLETED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="note_id")
    private Note note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="student_id")
    private Student student;

    @Builder
    public NoteStudent(int score, NoteStatus noteStatus, Note note, Student student) {
        this.score = score;
        this.noteStatus = noteStatus;
        this.note = note;
        this.student = student;
    }


}
