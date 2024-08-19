package com.example.ai_tutor.domain.student.domain;

import com.example.ai_tutor.domain.common.BaseEntity;
import com.example.ai_tutor.domain.user.domain.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="Student")
@NoArgsConstructor
@Getter
public class Student extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="student_id", updatable = false, nullable = false, unique = true)
    private Long studentId;

    private String studentNumber;

    private String name;

    @Builder
    public Student(Long studentId, String studentNumber, String name) {
        this.studentId = studentId;
        this.studentNumber = studentNumber;
        this.name = name;
    }
}
