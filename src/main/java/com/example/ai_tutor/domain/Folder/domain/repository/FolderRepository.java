package com.example.ai_tutor.domain.Folder.domain.repository;

import com.example.ai_tutor.domain.Folder.domain.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long>{

}
