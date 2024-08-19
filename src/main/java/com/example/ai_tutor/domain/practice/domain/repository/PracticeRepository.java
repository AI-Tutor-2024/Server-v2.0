package com.example.ai_tutor.domain.practice.domain.repository;

import com.example.ai_tutor.domain.note.domain.Note;
import com.example.ai_tutor.domain.practice.domain.Practice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PracticeRepository extends JpaRepository<Practice, Long> {

    //Practice findByNoteAndSequence(Note note, int number);

    List<Practice> findAllByNoteOrderByPracticeId(Note note);

    List<Practice> findByNote(Note note);

    int countByNote(Note note);

    List<Practice> findByNoteOrderBySequenceAsc(Note note);

}
