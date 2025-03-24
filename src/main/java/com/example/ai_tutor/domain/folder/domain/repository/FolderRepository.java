package com.example.ai_tutor.domain.folder.domain.repository;

import com.example.ai_tutor.domain.folder.domain.Folder;
import com.example.ai_tutor.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long>{
    List<Folder> findAllByUser(User user);
}
