package com.example.ai_tutor.domain.Folder.domain;

import com.example.ai_tutor.domain.common.BaseEntity;
import com.example.ai_tutor.domain.note.domain.Note;
import com.example.ai_tutor.domain.professor.domain.Professor;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="professor_id")
    private Professor professor;

    @Column(name="professor_name")
    private String professorName;

    @OneToMany(mappedBy = "folder", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Note> notes = new ArrayList<>();

    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name="student_id")
    // private Student student;

    @Builder
    public Folder(String folderName, Professor professor, String professorName){
        this.folderName = folderName;
        this.professor = professor;
        this.professorName = professorName;
    }

    public void updateFolder(String folderName, String professorName){
        this.folderName = folderName;
        this.professorName = professorName;
    }
}
