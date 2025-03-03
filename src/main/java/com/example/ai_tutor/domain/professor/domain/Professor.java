package com.example.ai_tutor.domain.professor.domain;

import com.example.ai_tutor.domain.folder.domain.Folder;
import com.example.ai_tutor.domain.common.BaseEntity;
import com.example.ai_tutor.domain.user.domain.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="Professor")
@NoArgsConstructor
@Getter
public class Professor extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="professor_id", updatable = false, nullable = false, unique = true)
    private Long professorId;

    @Column(name="professor_name")
    private String professorName;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id")
    private User user;

    @OneToMany(mappedBy = "professor")
    private List<Folder> folders = new ArrayList<>();

    @Builder
    public Professor(String professorName, User user){
        this.professorName = professorName;
        this.user = user;
    }
}
