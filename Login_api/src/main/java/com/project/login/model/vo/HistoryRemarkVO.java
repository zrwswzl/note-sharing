package com.project.login.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoryRemarkVO {

    private String _id;

    private Long noteId;

    private Long userId;

    private String username;

    private String content;

    private String createdAt;

    private String parentId;

    private Boolean isReply;

    private String replyToRemarkId;

    private String replyToUsername;
}
