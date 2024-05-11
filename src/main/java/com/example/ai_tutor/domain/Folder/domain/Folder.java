package com.example.ai_tutor.domain.Folder.domain;

import com.example.ai_tutor.domain.common.BaseEntity;
import com.example.ai_tutor.domain.note.domain.Note;
import com.example.ai_tutor.domain.practice.domain.Practice;
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
@Table(name="Folder")
@NoArgsConstructor
@Getter
public class Folder extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="folder_id", updatable = false, nullable = false, unique = true)
    private Long folderId;

    @Column(name="folder_name")
    private String folderName;

    @Column(name="professor")
    private String professor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id")
    private User user;

    @OneToMany(mappedBy = "folder")
    private List<Text> texts=new ArrayList<>();

    @OneToMany(mappedBy = "folder")
    private List<Note> notes=new ArrayList<>();

    @OneToMany(mappedBy = "folder")
    private List<Tutor> tutors=new ArrayList<>();

    @OneToMany(mappedBy = "folder")
    private List<Summary> summaries=new ArrayList<>();

    @OneToMany(mappedBy = "folder")
    private List<Practice> practices=new ArrayList<>();


    @Builder
    public Folder(User user, String folderName, String professor){
        this.user = user;
        this.folderName = folderName;
        this.professor = professor;
    }

    public void updateFolder(String folderName, String professor) {
        this.folderName = folderName;
        this.professor = professor;
    }
}
