package com.project.login.model.dataobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection="remark_likes")
public class RemarkLikeDO {
    @Id
    @Field("_id")
    private String _id; // MongoDB 自动生成唯一 _id

    @Field("remark_id")
    @Indexed // 为了快速查询某条评论的点赞数
    private String remarkId; // 评论 ID

    @Field("user_id")
    @Indexed(unique = true) // (remarkId, userId) 组合唯一可通过复合索引实现
    private Long userId;   // 点赞用户 ID

    @Field("created_at")
    private LocalDateTime createdAt; // 点赞时间
}
