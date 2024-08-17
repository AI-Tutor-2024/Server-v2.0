package com.example.ai_tutor.domain.practice.domain;

import com.example.ai_tutor.domain.Folder.domain.Folder;
import com.example.ai_tutor.domain.common.BaseEntity;
import com.example.ai_tutor.domain.note.domain.Note;
import com.example.ai_tutor.domain.user.domain.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

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
    private String content;   // 문제

    @Column(name="solution")
    private String solution;  // 해설

    @Column(name="sequence")
    private Integer sequence; // 문제 번호

    @Column(name="result")
    private String result;    // 정답

    // @Column(name="point")
    // private Integer score;

    @ElementCollection
    @CollectionTable(name = "choices", joinColumns = @JoinColumn(name = "practice_id"))
    @Column(name = "choice")
    private List<String> choices;    // 객관식의 경우에만 사용

    @Enumerated(EnumType.STRING)
    @Column(name = "practice_type")
    private PracticeType practiceType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="note_id")
    private Note note;

    @Builder
    public Practice(Note note, String content,  String solution, Integer sequence){
        this.note = note;
        this.content = content;
        this.solution = solution;
        this.sequence = sequence;
    }

    // public void updateUserAnswer(String userAnswer) { this.userAnswer = userAnswer; }
}
