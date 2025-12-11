package com.project.login.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoryNoteVO {
    private Long id;

    private String title;

    private String filename;

    private String fileType;

    private Long notebookId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
