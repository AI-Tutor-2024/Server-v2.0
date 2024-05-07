package com.example.ai_tutor.domain.user.domain;

import com.example.ai_tutor.domain.Folder.domain.Folder;
import com.example.ai_tutor.domain.common.BaseEntity;
import com.example.ai_tutor.domain.note.domain.Note;
import com.example.ai_tutor.domain.practice.domain.Practice;
import com.example.ai_tutor.domain.summary.domain.Summary;
import com.example.ai_tutor.domain.text.domain.Text;
import com.example.ai_tutor.domain.tutor.domain.Tutor;
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

    @Enumerated(EnumType.STRING)
    private Provider provider;

    private String providerId;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Folder> folders=new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Note> notes=new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Tutor> tutors=new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Text> texts=new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Practice> practices=new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Summary> summaries=new ArrayList<>();

    @Builder
    public User(String name, String email, Provider provider, String providerId){
        this.name = name;
        this.email = email;
        this.provider = provider;
        this.providerId = providerId;
        this.role = Role.USER;
    }

    public void updateName(String name) { this.name = name; }

}
