package com.example.ai_tutor.domain.professor.domain.repository;

import com.example.ai_tutor.domain.professor.domain.Professor;
import com.example.ai_tutor.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfessorRepository extends JpaRepository<Professor, Long> {
    Optional<Professor> findByUser(User user);
}
