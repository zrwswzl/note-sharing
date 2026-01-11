package com.project.login.mapper;

import com.project.login.model.dataobject.UserFollowDO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserFollowMapper {

    /** 插入关注关系 */
    @Insert("""
        INSERT INTO user_follow (follower_id, followee_id, follow_time)
        VALUES (#{followerId}, #{followeeId}, NOW())
    """)
    int insert(UserFollowDO follow);


    /** 删除关注关系（取关） */
    @Delete("""
        DELETE FROM user_follow
        WHERE follower_id = #{followerId}
          AND followee_id = #{followeeId}
    """)
    int delete(@Param("followerId") Long followerId,
               @Param("followeeId") Long followeeId);


    /** 判断是否已关注（返回 0 / 1） */
    @Select("""
        SELECT COUNT(1)
        FROM user_follow
        WHERE follower_id = #{followerId}
          AND followee_id = #{followeeId}
    """)
    int exists(@Param("followerId") Long followerId,
               @Param("followeeId") Long followeeId);


    /** 查询我关注了谁 */
    @Select("""
        SELECT id, follower_id, followee_id, follow_time
        FROM user_follow
        WHERE follower_id = #{followerId}
        ORDER BY follow_time DESC
    """)
    @Results(id = "UserFollowMap", value = {
            @Result(column = "id", property = "id"),
            @Result(column = "follower_id", property = "followerId"),
            @Result(column = "followee_id", property = "followeeId"),
            @Result(column = "follow_time", property = "followTime")
    })
    List<UserFollowDO> selectFollowings(@Param("followerId") Long followerId);


    /** 查询谁关注了我 */
    @Select("""
        SELECT id, follower_id, followee_id, follow_time
        FROM user_follow
        WHERE followee_id = #{followeeId}
        ORDER BY follow_time DESC
    """)
    @ResultMap("UserFollowMap")
    List<UserFollowDO> selectFollowers(@Param("followeeId") Long followeeId);
}
