package com.project.login.service.notestats;

import com.project.login.mapper.NoteStatsCompensationMapper;
import com.project.login.mapper.NoteStatsMapper;
import com.project.login.model.dataobject.NoteStatsCompensationDO;
import com.project.login.model.dataobject.NoteStatsDO;
import com.project.login.model.vo.NoteStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NoteStatsService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final NoteStatsMapper noteStatsMapper;
    private final NoteStatsCompensationMapper compensationMapper;
    private final RabbitTemplate rabbitTemplate;

    private static final String REDIS_KEY_PREFIX = "note_stats:";
    private static final String MQ_QUEUE = "note.redis.queue";

    /**
     * 高频写入（只写 Redis）
     */
    public NoteStatsVO changeField(Long noteId, String field, long delta) {
        String key = REDIS_KEY_PREFIX + noteId;
        HashOperations<String, Object, Object> ops = redisTemplate.opsForHash();

        ops.increment(key, field, delta);
        ops.put(key, "last_activity_at", LocalDateTime.now().toString());

        return getStats(noteId);
    }

    /**
     * 获取统计数据
     * 从 Redis 获取
     * 若 Redis 没有 → 从 MySQL 查并写回 Redis
     */
    public NoteStatsVO getStats(Long noteId) {
        String key = REDIS_KEY_PREFIX + noteId;
        HashOperations<String, Object, Object> ops = redisTemplate.opsForHash();

        Map<Object, Object> map = ops.entries(key);

        // --- Redis 无数据 → 查 MySQL ---
        if (map == null || map.isEmpty()) {
            NoteStatsDO db = noteStatsMapper.getById(noteId);

            if (db == null) {
                // 数据不存在，返回空值
                return emptyStats(noteId);
            }

            // 回写 Redis（预热）
            ops.put(key, "views", db.getViews());
            ops.put(key, "likes", db.getLikes());
            ops.put(key, "favorites", db.getFavorites());
            ops.put(key, "comments", db.getComments());
            ops.put(key, "last_activity_at", db.getLastActivityAt().toString());

            // 重新读取 Redis 映射为 VO
            map = ops.entries(key);
        }

        return toVO(noteId, map);
    }

    /** 将 Redis Map 转成 VO */
    private NoteStatsVO toVO(Long noteId, Map<Object, Object> map) {
        NoteStatsVO vo = new NoteStatsVO();
        vo.setNoteId(noteId);
        vo.setViews(Long.parseLong(map.getOrDefault("views", 0).toString()));
        vo.setLikes(Long.parseLong(map.getOrDefault("likes", 0).toString()));
        vo.setFavorites(Long.parseLong(map.getOrDefault("favorites", 0).toString()));
        vo.setComments(Long.parseLong(map.getOrDefault("comments", 0).toString()));
        return vo;
    }

    private NoteStatsVO emptyStats(Long noteId) {
        NoteStatsVO vo = new NoteStatsVO();
        vo.setNoteId(noteId);
        vo.setViews(0L);
        vo.setLikes(0L);
        vo.setFavorites(0L);
        vo.setComments(0L);
        return vo;
    }

    /** 批量 flush Redis → MQ */
    public void flushToMQ() {
        redisTemplate.keys(REDIS_KEY_PREFIX + "*").forEach(key -> {
            Map<Object, Object> map = redisTemplate.opsForHash().entries(key);
            rabbitTemplate.convertAndSend(MQ_QUEUE, map);
        });
    }

    /** 系统启动预热 Redis */
    @Async
    public void preloadRecent(int n) {
        noteStatsMapper.getRecentUpdated(n).forEach(item -> {
            String key = REDIS_KEY_PREFIX + item.getNoteId();
            HashOperations<String, Object, Object> ops = redisTemplate.opsForHash();

            ops.put(key, "views", item.getViews());
            ops.put(key, "likes", item.getLikes());
            ops.put(key, "favorites", item.getFavorites());
            ops.put(key, "comments", item.getComments());
            ops.put(key, "last_activity_at", item.getLastActivityAt().toString());
        });
    }

    /** MQ 消费者 → 批量落盘 + 冷数据删除 */
    public void persistFromMQ(Map<Object, Object> data) {
        NoteStatsDO doEntity = new NoteStatsDO();
        doEntity.setNoteId(Long.parseLong(data.get("noteId").toString()));
        doEntity.setViews(Long.parseLong(data.getOrDefault("views", "0").toString()));
        doEntity.setLikes(Long.parseLong(data.getOrDefault("likes", "0").toString()));
        doEntity.setFavorites(Long.parseLong(data.getOrDefault("favorites", "0").toString()));
        doEntity.setComments(Long.parseLong(data.getOrDefault("comments", "0").toString()));
        doEntity.setLastActivityAt(LocalDateTime.parse(data.get("last_activity_at").toString()));
        doEntity.setVersion(Long.parseLong(data.getOrDefault("version", "0").toString()));

        int updated = noteStatsMapper.updateOptimistic(doEntity);
        if (updated == 0) {
            // 乐观锁失败 -> 写入补偿表
            NoteStatsCompensationDO comp = new NoteStatsCompensationDO();
            comp.setNoteId(doEntity.getNoteId());
            comp.setViews(doEntity.getViews());
            comp.setLikes(doEntity.getLikes());
            comp.setFavorites(doEntity.getFavorites());
            comp.setComments(doEntity.getComments());
            comp.setLastActivityAt(doEntity.getLastActivityAt());
            comp.setStatus("PENDING");
            comp.setRetryCount(0);
            compensationMapper.insert(comp);
        } else {
            // 落盘成功 -> 删除冷门 Redis 数据
            String key = REDIS_KEY_PREFIX + doEntity.getNoteId();
            Object redisLastObj = redisTemplate.opsForHash().get(key, "last_activity_at");

            if (redisLastObj != null) {
                LocalDateTime redisLast = LocalDateTime.parse(redisLastObj.toString());
                if (redisLast.isBefore(doEntity.getLastActivityAt())) {
                    redisTemplate.delete(key);
                }
            }
        }
    }
}
