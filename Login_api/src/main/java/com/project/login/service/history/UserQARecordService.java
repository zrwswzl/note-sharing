package com.project.login.service.history;

import com.project.login.model.dataobject.QAType;
import com.project.login.model.dataobject.UserQARecordDO;
import com.project.login.repository.UserQARecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserQARecordService {

    private final RabbitTemplate rabbitTemplate;
    private final UserQARecordRepository userQARecordRepository;


    private static final String QA_Record_MQ_QUEUE = "qa_record.queue";  // 设置 MQ 队列

    public void sendQARecord(Long userId, String questionId, QAType type) throws Exception {

        UserQARecordDO record = UserQARecordDO.builder()
                .userId(userId)
                .questionId(questionId)
                .type(type)
                .timestamp(LocalDateTime.now())  // 操作时间
                .build();

        rabbitTemplate.convertAndSend(QA_Record_MQ_QUEUE, record);  // 发送到 MQ 队列
    }

    // 查找用户问答记录
    public List<String> getQARecordsByUserIdAndType(Long userId, QAType type) throws Exception {
        // 获取用户的提问记录（或回答记录）
        List<UserQARecordDO> records = userQARecordRepository.findByUserIdAndType(userId, type);

        if (records == null || records.isEmpty()) {
            return Collections.emptyList();  // 如果没有记录，返回空列表
        }

        // 从记录中提取 questionId 列表
        return records.stream()
                .map(UserQARecordDO::getQuestionId)
                .toList();
    }


    // 删除提问记录
    public void deleteQARecord(Long userId, String questionId, QAType type) throws Exception {
        try {
            userQARecordRepository.deleteByUserIdAndQuestionIdAndType(userId, questionId, type);
        } catch (Exception e) {
            System.err.println("Error deleting qa record for userId: " + userId + ", questionId: " + questionId);
        }
    }
}
