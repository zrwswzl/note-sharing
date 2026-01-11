package com.project.login.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationListVO {
    private Long userId;                    // 当前用户 ID
    private List<ConversationListItemVO> conversations;  // 会话摘要列表
}
