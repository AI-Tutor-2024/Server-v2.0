package com.example.ai_tutor.domain.student.domain.repository;

import com.example.ai_tutor.domain.student.domain.Student;
import com.example.ai_tutor.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Student findByUser(User user);
}
