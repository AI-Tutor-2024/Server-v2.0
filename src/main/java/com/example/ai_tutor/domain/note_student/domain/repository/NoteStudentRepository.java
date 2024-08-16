package com.example.ai_tutor.domain.note_student.domain.repository;

import com.example.ai_tutor.domain.note.domain.Note;
import com.example.ai_tutor.domain.note.domain.NoteStatus;
import com.example.ai_tutor.domain.note_student.domain.NoteStudent;
import com.example.ai_tutor.domain.student.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteStudentRepository extends JpaRepository<NoteStudent, Long> {
    int countByNoteAndNoteStatus(Note note, NoteStatus noteStatus);

    List<NoteStudent> findByNote(Note note);

}
