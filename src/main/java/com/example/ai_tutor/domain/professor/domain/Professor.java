package com.example.ai_tutor.domain.professor.domain;

import com.example.ai_tutor.domain.common.BaseEntity;
import com.example.ai_tutor.domain.user.domain.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="Professor")
@NoArgsConstructor
@Getter
public class Professor extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="professor_id", updatable = false, nullable = false, unique = true)
    private Long professorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id")
    private User user;

    @Builder
    public Professor(Long professorId, User user) {
        this.professorId = professorId;
        this.user = user;
    }
}
