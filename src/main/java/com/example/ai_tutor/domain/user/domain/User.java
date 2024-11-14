package com.example.ai_tutor.domain.user.domain;

import com.example.ai_tutor.domain.Folder.domain.Folder;
import com.example.ai_tutor.domain.common.BaseEntity;
import com.example.ai_tutor.domain.note.domain.Note;
import com.example.ai_tutor.domain.practice.domain.Practice;
import com.example.ai_tutor.domain.professor.domain.Professor;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.cloud.aws.cache.config.annotation.CacheClusterConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name="User")
@NoArgsConstructor
@Getter
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="user_id", updatable = false, nullable = false, unique = true)
    private Long userId;

    @Column(name="uuid")
    private UUID userUUID;

    @Column(name="name")
    private String name;

    @Column(name="email")
    private String email;

    @Column(name="password")
    private String password;

    @Enumerated(EnumType.STRING)
    private Provider provider;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="professor_id")
    private Professor professor;

    private String providerId;

    @Enumerated(EnumType.STRING)
    private Role role = Role.PROFESSOR;


    @Builder
    public User(String name, String email, String password, Provider provider, Professor professor, String providerId, Role role){
        this.name = name;
        this.email = email;
        this.password = password;
        this.provider = provider;
        this.professor = professor;
        this.providerId = providerId;
        this.role = Role.PROFESSOR;
    }

    public void updateProfessor(Professor professor) { this.professor = professor; }

    public void updateName(String name) { this.name = name; }

}
