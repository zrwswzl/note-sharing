package com.project.login.service.rabbitmq.consumer;

import com.project.login.service.notestats.NoteStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class NoteStatsConsumer {

    private final NoteStatsService noteStatsService;

    @RabbitListener(queues = "note.redis.queue")
    public void handleMessage(Map<Object, Object> data) {
        noteStatsService.persistFromMQ(data);
    }
}
