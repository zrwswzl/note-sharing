package com.project.login.model.dataobject;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFollowDO {

    private Long id;

    /** 关注者 */
    private Long followerId;

    /** 被关注者 */
    private Long followeeId;

    /** 关注时间 */
    private LocalDateTime followTime;

}
