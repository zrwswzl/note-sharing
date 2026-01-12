package com.project.login.repository;

import com.project.login.model.dataobject.ConversationDO;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ConversationRepository extends MongoRepository<ConversationDO, String> {

    // 根据会话主键查询会话内容
    Optional<ConversationDO> findByConversationId(String conversationId);
    @Query("{ 'ConversationId': ?0 }")
    @Update("{ '$push': { 'messages': ?1 }, '$set': { 'lastTime': ?2 } }")
    void appendMessage(String conversationId, ConversationDO.Message message, LocalDateTime lastTime);

}
