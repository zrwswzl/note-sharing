package com.project.login.model.dataobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationRelationDO {
    private Long id;
    private String conversationId;   // 对应 MongoDB Conversation _id
    private Long user1Id;
    private Long user2Id;
    private LocalDateTime createTime;
}
