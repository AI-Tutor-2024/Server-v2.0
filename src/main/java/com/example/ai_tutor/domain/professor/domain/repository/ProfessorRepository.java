package com.example.ai_tutor.domain.professor.domain.repository;

import com.example.ai_tutor.domain.professor.domain.Professor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfessorRepository extends JpaRepository<Professor, Long> {
}
