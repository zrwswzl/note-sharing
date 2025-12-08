package com.project.login.model.dataobject;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection="remark_count")
public class RemarkCountDO {
    @Id
    @Field("remark_id")
    private String remarkId;

    @Field("remark_like_count")
    private Long remarkLikeCount;

    @Field("version")
    private Long version;

}
