package com.example.ai_tutor.domain.summary.domain.repository;

import com.example.ai_tutor.domain.note.domain.Note;
import com.example.ai_tutor.domain.summary.domain.Summary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SummaryRepository extends JpaRepository<Summary, Long> {
    List<Summary> findAllByNote(Note note);
}
