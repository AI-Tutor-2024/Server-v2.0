package com.example.ai_tutor.domain.answer.domain;

import com.example.ai_tutor.domain.common.BaseEntity;
import com.example.ai_tutor.domain.note_student.domain.NoteStudent;
import com.example.ai_tutor.domain.practice.domain.Practice;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="Answer")
@NoArgsConstructor
@Getter
public class Answer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="answer_id", updatable = false, nullable = false, unique = true)
    private Long answerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="practice_id")
    private Practice practice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="note_student_id")
    private NoteStudent noteStudent;

    private String memo;

    private String content;

    private int studentPoint;

    @Builder
    public Answer(Practice practice, String memo, NoteStudent noteStudent, String content, int studentPoint) {
        this.practice = practice;
        this.memo = memo;
        this.content = content;
        this.studentPoint = studentPoint;
        this.noteStudent = noteStudent;
    }
}
