package com.example.ai_tutor.domain.text.domain;


import com.example.ai_tutor.domain.Folder.domain.Folder;
import com.example.ai_tutor.domain.common.BaseEntity;
import com.example.ai_tutor.domain.note.domain.Note;
import com.example.ai_tutor.domain.user.domain.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity
@Table(name="Text")
@NoArgsConstructor
@Getter
public class Text extends BaseEntity {
    @Id
    @Column(name="text_id", updatable = false, nullable = false)
    private Long textId;

//    @Column(name="timestamp")
//    private Timestamp timestamp;

    @Column(name="content")
    private String content;

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
    public Text(User user, Folder folder, Note note, String content){
        this.user = user;
        this.folder = folder;
        this.note = note;
        this.content = content;
    }
}
