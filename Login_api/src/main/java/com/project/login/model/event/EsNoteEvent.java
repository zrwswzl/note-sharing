package com.project.login.model.event;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

@Data
public class EsNoteEvent {

    private Long noteId;

    private NoteActionType action; // CREATE / UPDATE / DELETE

    private String title;
    private String contentSummary; // ES 搜索用摘要

    private String authorName;

    // --- 统计信息 ---
    private Integer viewCount;
    private Integer likeCount;
    private Integer favoriteCount;
    private Integer commentCount;

    // 更新时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedAt;
}
