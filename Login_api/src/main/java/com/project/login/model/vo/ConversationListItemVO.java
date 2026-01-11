package com.project.login.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationListItemVO {
    private Long peerId;          // 对方用户 ID
    private String conversationId;
    private String lastMessage;
    private Date lastTime;
}
