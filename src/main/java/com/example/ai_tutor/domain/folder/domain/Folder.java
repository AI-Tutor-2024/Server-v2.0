package com.example.ai_tutor.domain.folder.domain;

import com.example.ai_tutor.domain.common.BaseEntity;
import com.example.ai_tutor.domain.folder.dto.request.FolderReq;
import com.example.ai_tutor.domain.note.domain.Note;
import com.example.ai_tutor.domain.user.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name="Folder")
@NoArgsConstructor
@Getter
@Builder @AllArgsConstructor
public class Folder extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="folder_id", updatable = false, nullable = false, unique = true)
    private Long folderId;

    @Column(nullable = false, unique = true)
    private UUID uuid;

    @Column(name="folder_name")
    private String folderName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "folder", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Note> notes = new ArrayList<>();

    // createFolder
    public static Folder create(FolderReq.FolderCreateReq folderCreateReq){
        return Folder.builder()
                .uuid(UUID.randomUUID())
                .folderName(folderCreateReq.getFolderName())
                .build();
    }

    public void updateFolder(String folderName){
        this.folderName = folderName;
    }
}
