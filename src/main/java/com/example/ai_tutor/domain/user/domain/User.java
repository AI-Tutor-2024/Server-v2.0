package com.example.ai_tutor.domain.user.domain;

import com.example.ai_tutor.domain.Folder.domain.Folder;
import com.example.ai_tutor.domain.common.BaseEntity;
import com.example.ai_tutor.domain.note.domain.Note;
import com.example.ai_tutor.domain.practice.domain.Practice;
import com.example.ai_tutor.domain.text.domain.Text;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="User")
@NoArgsConstructor
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

    @Enumerated(EnumType.STRING)
    private Provider provider;

    private String providerId;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Builder
    public User(String name, String email, Provider provider, String providerId, String password, Role role){
        this.name = name;
        this.email = email;
        this.password = password;
        this.provider = provider;
        this.providerId = providerId;
        this.role = role;
    }

    public void updateName(String name) { this.name = name; }

}
