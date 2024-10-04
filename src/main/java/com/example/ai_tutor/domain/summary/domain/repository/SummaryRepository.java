package com.example.ai_tutor.domain.summary.domain.repository;

import com.example.ai_tutor.domain.summary.domain.Summary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SummaryRepository extends JpaRepository<Summary, Long> {

}
