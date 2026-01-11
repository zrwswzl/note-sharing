package com.project.login.mapper;

import com.project.login.model.dataobject.ConversationRelationDO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ConversationRelationMapper {

    /** 插入新会话关系 */
    @Insert("INSERT INTO conversation_relation(conversation_id, user1_id, user2_id, create_time) " +
            "VALUES(#{conversationId}, #{user1Id}, #{user2Id}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ConversationRelationDO conversation);

    /** 根据 userId 查会话（任意一方） */
    @Select("SELECT * FROM conversation_relation " +
            "WHERE user1_id = #{userId} OR user2_id = #{userId} " +
            "ORDER BY create_time DESC")
    List<ConversationRelationDO> selectByUserId(@Param("userId") Long userId);

    /** 根据用户对查单条会话（保证 user1 < user2） */
    @Select("SELECT * FROM conversation_relation " +
            "WHERE user1_id = #{user1Id} AND user2_id = #{user2Id} LIMIT 1")
    ConversationRelationDO selectByUserPair(@Param("user1Id") Long user1Id,
                                            @Param("user2Id") Long user2Id);

    /** 根据 conversationId 查会话 */
    @Select("SELECT * FROM conversation_relation WHERE conversation_id = #{conversationId}")
    ConversationRelationDO selectByConversationId(@Param("conversationId") String conversationId);
}
