package com.project.login.model.vo;

import com.project.login.model.dataobject.ConversationDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationVO {
    private Long userId;
    private String conversationId;       // 会话ID
    private List<Long> participants;     // 参与者ID列表
    private List<ConversationDO.Message> messages;    // 消息列表
    private Date lastTime;               // 最后消息时间

}
