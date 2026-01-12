package com.project.login.model.dataobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection="Conversations")
public class ConversationDO {

    @Id
    private String conversationId; // 会话主键

    private List<Long> participants; // 会话双方 userId 列表

    private List<Message> messages; // 消息列表

    private Date lastTime; // 最后消息时间

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Message {
        private Long senderId;       // 发送者 userId
        private String content;      // 消息内容
        private Date createdAt;      // 消息发送时间
    }
}
