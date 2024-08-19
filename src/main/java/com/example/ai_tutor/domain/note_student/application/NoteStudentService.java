package com.example.ai_tutor.domain.note_student.application;

import com.example.ai_tutor.domain.note_student.domain.NoteStudent;
import com.example.ai_tutor.domain.note_student.domain.repository.NoteStudentRepository;
import com.example.ai_tutor.domain.practice.domain.Practice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoteStudentService {

    private final NoteStudentRepository noteStudentRepository;

    public int getCorrectCount(Long noteStudentId) {
        NoteStudent noteStudent = noteStudentRepository.findById(noteStudentId).orElseThrow(() -> new IllegalArgumentException("해당 학생의 문제지가 존재하지 않습니다."));
        return noteStudent.getScore();
    }

}
