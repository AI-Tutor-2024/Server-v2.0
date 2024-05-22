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

    @Column(name="user_answer")
    private String userAnswer;

    @Column(name="ai_answer")
    private String aiAnswer;

    @Column(name="sequence")
    private Integer sequence;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="note_id")
    private Note note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="folder_id")
    private Folder folder;

    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn(name="user_id")
    private User user;

    @Builder
    public Practice(User user, Folder folder, Note note, String content, String userAnswer, String aiAnswer, Integer sequence){
        this.user = user;
        this.folder = folder;
        this.note = note;
        this.content = content;
        this.userAnswer = userAnswer;
        this.aiAnswer = aiAnswer;
        this.sequence = sequence;
    }

    public void updateUserAnswer(String userAnswer) { this.userAnswer = userAnswer; }
}
