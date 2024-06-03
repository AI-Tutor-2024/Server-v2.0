package com.example.ai_tutor.domain.text.domain.repository;

import com.example.ai_tutor.domain.note.domain.Note;
import com.example.ai_tutor.domain.text.domain.Text;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TextRepository extends JpaRepository<Text, Long>{
    List<Text> findAllByNote(Note note);
}
