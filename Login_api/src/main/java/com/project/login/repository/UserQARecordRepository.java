package com.project.login.repository;

import com.project.login.model.dataobject.QAType;
import com.project.login.model.dataobject.UserQARecordDO;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UserQARecordRepository extends MongoRepository<UserQARecordDO, String> {
    // 根据用户 ID 和类型查找记录
    List<UserQARecordDO> findByUserIdAndType(Long userId, QAType type);

    // 根据用户 ID 和 类型 和 提问ID 查找记录
    default UserQARecordDO findByUserIdAndTypeAndQuestionId(Long userId, QAType type, String questionId) {
        return null;
    }

    // 根据用户 ID 和问题 ID 和 类型删除提问记录
    void deleteByUserIdAndQuestionIdAndType(Long userId, String questionId, QAType type);
}
