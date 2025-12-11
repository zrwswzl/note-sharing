package com.project.login.service.rabbitmq.consumer;

import com.project.login.model.dataobject.UserQARecordDO;
import com.project.login.repository.UserQARecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class QAMongodbConsumer {

    private final UserQARecordRepository userQARecordRepository;

    private static final String QA_Record_MQ_QUEUE = "qa_record.queue";  // 设置 MQ 队列

    @RabbitListener(queues = QA_Record_MQ_QUEUE)
    @Async
    public void storeQARecordInMongoDB(UserQARecordDO record) {
        try {
            UserQARecordDO existing = userQARecordRepository.findByUserIdAndTypeAndQuestionId(record.getUserId(),
                    record.getType(), record.getQuestionId());
            if(existing == null || existing.getQuestionId().equals(record.getQuestionId())) {
                record.setTimestamp(LocalDateTime.now());  // 设置操作时间
                userQARecordRepository.save(record);  // 异步存储到 MongoDB
            }
        } catch (Exception e) {
            log.error("Error storing behavior record for userId: {} and noteId: {}", record.getUserId(), record.getQuestionId(), e);
        }
    }
}
