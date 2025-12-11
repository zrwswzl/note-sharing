package com.project.login.model.dataobject;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@Document(collection = "user_qa_records")
public class UserQARecordDO {

    @Id
    private String id;
    private Long userId;        // 用户ID
    private String questionId;        // 笔记ID
    private QAType type;    //提问或者回答
    private LocalDateTime timestamp;  // 操作时间
}