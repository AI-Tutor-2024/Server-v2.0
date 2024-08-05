package com.example.ai_tutor.domain.practice.domain;

import com.example.ai_tutor.domain.Folder.domain.Folder;
import com.example.ai_tutor.domain.common.BaseEntity;
import com.example.ai_tutor.domain.note.domain.Note;
import com.example.ai_tutor.domain.user.domain.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name= "Practice")
@NoArgsConstructor
@Getter
public class Practice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="practice_id", updatable = false, nullable = false)
    private Long practiceId;

    @Column(name="content")
    private String content;

    @Column(name="solution")
    private String solution;

    @Column(name="sequence")
    private Integer sequence;

    @Column(name="point")
    private Integer score;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="note_id")
    private Note note;

    @Builder
    public Practice(Note note, String content,  String solution, Integer score, Integer sequence){
        this.note = note;
        this.content = content;
        this.solution = solution;
        this.score = score;
        this.sequence = sequence;
    }

    // public void updateUserAnswer(String userAnswer) { this.userAnswer = userAnswer; }
}
