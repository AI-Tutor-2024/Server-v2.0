package com.example.ai_tutor.domain.note.domain.repository;

import com.example.ai_tutor.domain.Folder.domain.Folder;
import com.example.ai_tutor.domain.note.domain.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findAllByFolder(Folder folder);
}