package com.example.ai_tutor.domain.tutor.domain.repository;

import com.example.ai_tutor.domain.note.domain.Note;
import com.example.ai_tutor.domain.tutor.domain.Tutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TutorRepository extends JpaRepository<Tutor, Long> {
    List<Tutor> findByNoteOrderByCreatedAt(Note note);
}
