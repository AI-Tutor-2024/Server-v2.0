package com.example.ai_tutor.domain.Folder.domain;

import com.example.ai_tutor.domain.common.BaseEntity;
import com.example.ai_tutor.domain.professor.domain.Professor;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name="student_id")
    // private Student student;

    @Builder
    public Folder(String folderName, Professor professor){
        this.folderName = folderName;
        this.professor = professor;
        // this.student = student;
    }

    public void updateFolder(String folderName, Professor professor) {
        this.folderName = folderName;
        this.professor = professor;
    }
}
