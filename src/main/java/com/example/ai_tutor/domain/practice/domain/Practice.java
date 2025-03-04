package com.example.ai_tutor.domain.practice.domain;

import com.example.ai_tutor.domain.common.BaseEntity;
import com.example.ai_tutor.domain.note.domain.Note;
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

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "additional_results", joinColumns = @JoinColumn(name = "practice_id"))
    @Column(name = "additional_answer")
    private List<String> additionalResults;    // 객관식의 경우에만 사용

    @Enumerated(EnumType.STRING)
    @Column(name = "practice_type")
    private PracticeType practiceType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="note_id")
    private Note note;

    @Builder
    public Practice(Note note, String content,  String solution, Integer sequence, String result, List<String> additionalResults, PracticeType practiceType){
        this.note = note;
        this.content = content;
        this.solution = solution;
        this.sequence = sequence;
        this.result = result;
        this.additionalResults = additionalResults;
        this.practiceType = practiceType;
    }

    // public void updateUserAnswer(String userAnswer) { this.userAnswer = userAnswer; }
}
