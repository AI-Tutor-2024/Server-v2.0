package com.example.ai_tutor.domain.user.domain;

import com.example.ai_tutor.domain.common.BaseEntity;
import com.example.ai_tutor.domain.professor.domain.Professor;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="User")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="user_id", updatable = false, nullable = false, unique = true)
    private Long userId;

    @Column(name="name")
    private String name;

    @Column(name="email")
    private String email;

    @Column(name="password")
    private String password;

    private String provider;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name="professor_id")
    private Professor professor;

    private String providerId;

    @Enumerated(EnumType.STRING)
    private Role role = Role.PROFESSOR;

    public void updateProfessor(Professor professor) { this.professor = professor; }

    public void updateName(String name) { this.name = name; }

}
