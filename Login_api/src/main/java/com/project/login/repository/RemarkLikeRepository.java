package com.project.login.repository;

import com.project.login.model.dataobject.RemarkDO;
import com.project.login.model.dataobject.RemarkLikeDO;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RemarkLikeRepository extends MongoRepository<RemarkLikeDO, String> {

    // 根据评论 ID 和用户 ID 查询点赞记录
    Boolean existsByRemarkIdAndUserId(String remarkId, Long userId);
    // 根据 remarkId 删除点赞记录
    void deleteByRemarkId(String remarkId);
    void deleteByRemarkIdAndUserId(String remarkId, Long userId);
}
