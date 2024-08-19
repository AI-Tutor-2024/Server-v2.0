package com.example.ai_tutor.domain.answer.domain.repository;

import com.example.ai_tutor.domain.answer.domain.Answer;
import com.example.ai_tutor.domain.practice.domain.Practice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    boolean existsByPractice(Practice practice);
}
