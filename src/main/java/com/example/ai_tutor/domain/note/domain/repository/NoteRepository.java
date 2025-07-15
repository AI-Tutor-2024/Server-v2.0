package com.example.ai_tutor.domain.note.domain.repository;

import com.example.ai_tutor.domain.folder.domain.Folder;
import com.example.ai_tutor.domain.note.domain.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findAllByFolderOrderByCreatedAtDesc(Folder folder);

    boolean existsByCode(String code);

    Optional<Object> findByCode(String code);

    List<Note> findAllByFolderAndSummaryIsNotNullOrderByCreatedAtDesc(Folder folder);
}