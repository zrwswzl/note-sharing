package com.project.login.service.remark;


import com.project.login.convert.RemarkConvert;
import com.project.login.mapper.NoteMapper;
import com.project.login.mapper.UserMapper;
import com.project.login.model.dataobject.RemarkCountDO;
import com.project.login.model.dataobject.RemarkDO;
import com.project.login.model.dataobject.RemarkLikeByUsersDO;
import com.project.login.model.dataobject.UserDO;
import com.project.login.model.dto.remark.RemarkDeleteDTO;
import com.project.login.model.dto.remark.RemarkInsertDTO;
import com.project.login.model.dto.remark.RemarkSelectByNoteDTO;
import com.project.login.model.vo.RemarkVO;
import com.project.login.repository.RemarkCountRepository;
import com.project.login.repository.RemarkLikeByUsersRepository;
import com.project.login.repository.RemarkRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RemarkService {
    private final RemarkRepository remarkRepository;
    private final RemarkLikeByUsersRepository remarkLikeByUsersRepository;
    private final RemarkConvert remarkConvert;
    private final NoteMapper noteMapper;
    private final UserMapper userMapper;
    private final RemarkCountRepository remarkCountRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final String remarkLikeCountKey = "remark_count:";
    private final String remarkIdKey = "remark:";
    private final String NoteIdKey = "note_id_of_remark_list:";
    private final String replyToIdKey = "reply_to";
    private final String userLikeRemarkListKey = "remark:user_like:";

    private RemarkVO transferDO2VO(RemarkDO remarkDO, UserDO user) {
        //初始转化与变量准备
        RemarkVO cur = remarkConvert.toVO(remarkDO);
        Long loginUserId = remarkDO.getUserId();
        String receiveToRemarkId = remarkDO.getReceiveToRemarkId();

        Optional<RemarkDO> receiveTORemarkOptional = remarkRepository.findById(receiveToRemarkId);
        RemarkDO receiveTORemark = receiveTORemarkOptional
                .orElseThrow(() -> new RuntimeException("Receive remark not found for ID: " + receiveToRemarkId));
        // 设置是否已点赞
        // ------ 设置是否已点赞 ------
        String redisKey = userLikeRemarkListKey + remarkDO.get_id();
        String userIdStr = loginUserId.toString();
        boolean liked = false;

        if (redisTemplate.hasKey(redisKey)) {
            // Redis 有数据 → 直接判断
            liked = Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(redisKey, userIdStr));

        } else {
            // Redis 无数据 → 从数据库加载
            Optional<RemarkLikeByUsersDO> likeRecord = remarkLikeByUsersRepository.findById(remarkDO.get_id());

            if (likeRecord.isPresent()) {
                Set<String> userList = likeRecord.get().getUserList();

                if (userList != null && !userList.isEmpty()) {
                    // 判断是否点赞
                    liked = userList.contains(userIdStr);

                    // 同步整个列表到 Redis
                    redisTemplate.opsForSet().add(redisKey, userList.toArray(new String[0]));
                }
            }
        }
        // ------ 加载点赞数量 ------
        Long likedCount = 0L;
        String countKey = remarkLikeCountKey + remarkDO.get_id();

        if (redisTemplate.hasKey(countKey)) {

            Object countObj = redisTemplate.opsForHash().get(countKey, "remarkLikeCount");
            if (countObj instanceof Long l) {
                likedCount = l;
            } else if (countObj instanceof Integer i) {
                likedCount = i.longValue();
            }

        } else {

            // Redis 无计数 → 从数据库加载
            RemarkCountDO remarkCount = remarkCountRepository.findById(remarkDO.get_id())
                    .orElse(null);

            if (remarkCount != null) {
                likedCount = remarkCount.getRemarkLikeCount();
                // 同步到 Redis（hash）
                redisTemplate.opsForHash().put(countKey, "remarkLikeCount", remarkCount.getRemarkLikeCount());
                redisTemplate.opsForHash().put(countKey, "version", remarkCount.getVersion());
            }
        }

        cur.setLikedOrNot(liked);
        cur.setLikeCount(likedCount); // <--- 记得 VO 里要有该字段

        return cur;
    }

    @Transactional
    public List<RemarkVO> SelectRemark(RemarkSelectByNoteDTO remarkSelectByNoteDTO, Long loginUserId) {
// 获取当前用户信息
        UserDO user = userMapper.selectById(loginUserId);
        Long noteId = remarkSelectByNoteDTO.getNoteId();
        String noteKey = NoteIdKey + noteId;
        List<String> remarkFirstLayerList = new ArrayList<>();
        List<RemarkVO> result = new ArrayList<>();

// --- 一级评论列表处理 ---
        if (redisTemplate.hasKey(noteKey)) {
            // 从Redis读取一级评论ID列表
            List<Object> tmpList = redisTemplate.opsForList().range(noteKey, 0, -1);
            if (tmpList != null) {
                remarkFirstLayerList = tmpList.stream().map(Object::toString).toList();
            }
        } else {
            // Redis中没有则从MongoDB读取
            List<RemarkDO> tmpRemarkDOList = remarkRepository.findRemarksByNoteIdAndIsReceiveFalse(noteId);
            for (RemarkDO cur : tmpRemarkDOList) {
                if (cur != null && cur.get_id() != null) {
                    remarkFirstLayerList.add(cur.get_id());
                }
            }
            // 将一级评论ID列表写入Redis
            if (!remarkFirstLayerList.isEmpty()) {
                redisTemplate.opsForList().rightPushAll(noteKey, remarkFirstLayerList, Duration.ofHours(1));
            }
        }

// --- 遍历一级评论 ---
        for (String cur : remarkFirstLayerList) {
            RemarkDO curDO = null;
            // 尝试从Redis获取一级评论对象
            if (redisTemplate.hasKey(remarkIdKey + cur)) {
                curDO = (RemarkDO) redisTemplate.opsForValue().get(remarkIdKey + cur);
            }
            if (curDO == null) {
                // Redis没有则从MongoDB获取
                curDO = remarkRepository.findById(cur).orElse(null);
                if (curDO != null) {
                    redisTemplate.opsForValue().set(remarkIdKey + cur, curDO, Duration.ofHours(1));
                }
            }

            // 如果一级评论对象为空，跳过
            if (curDO == null) continue;

            // 将DO转换为VO
            RemarkVO curVO = transferDO2VO(curDO, user);

            // --- 二级评论列表处理 ---
            List<String> remarkSecondLayerList = new ArrayList<>();
            String replyKey = replyToIdKey + cur;
            if (redisTemplate.hasKey(replyKey)) {
                // 从Redis获取二级评论ID列表
                List<Object> tmpReplyList = redisTemplate.opsForList().range(replyKey, 0, -1);
                if (tmpReplyList != null) {
                    remarkSecondLayerList = tmpReplyList.stream().map(Object::toString).toList();
                }
            } else {
                // Redis没有则从MongoDB获取
                List<RemarkDO> replyList = remarkRepository.findRemarksByParentIdAndIsReceiveTrue(cur);
                for (RemarkDO reply : replyList) {
                    if (reply != null && reply.get_id() != null) {
                        redisTemplate.opsForValue().set(remarkIdKey + reply.get_id(), reply, Duration.ofHours(1));
                        remarkSecondLayerList.add(reply.get_id());
                    }
                }
            }

            // --- 二级评论VO构建 ---
            List<RemarkVO> replies = new ArrayList<>();

            for (String replyId : remarkSecondLayerList) {

                RemarkDO replyDO = null;

                // 先查 Redis
                String redisKey = remarkIdKey + replyId;
                if (redisTemplate.hasKey(redisKey)) {
                    replyDO = (RemarkDO) redisTemplate.opsForValue().get(redisKey);
                }

                //  Redis 中没有 → 查 MongoDB
                if (replyDO == null) {
                    replyDO = remarkRepository.findById(replyId)
                            .orElse(null);

                    //  查到了 → 写回 Redis，提升下次命中率
                    if (replyDO != null) {
                        redisTemplate.opsForValue().set(redisKey, replyDO, Duration.ofHours(1));
                    }
                }

                //  查不到就跳过
                if (replyDO != null) {
                    RemarkVO replyVO = transferDO2VO(replyDO, user);
                    replies.add(replyVO);
                }
            }

            curVO.setReplies(replies);
        }
        return result;
    }

    @Transactional
    public List<RemarkVO> selectRemarkByUserId(UserDO user) {
        Long loginUserId = user.getId();
        List<RemarkVO> remarkVOList = new ArrayList<>();

// 从数据库获取该用户所有一级评论
        List<RemarkDO> remarkDOList = remarkRepository.findByUserId(loginUserId);

        for (RemarkDO curDO : remarkDOList) {
            if (curDO == null || curDO.get_id() == null) continue;

            // --- 一级评论对象处理 ---
            String curKey = remarkIdKey + curDO.get_id();
            RemarkDO cachedCurDO = null;
            if (redisTemplate.hasKey(curKey)) {
                cachedCurDO = (RemarkDO) redisTemplate.opsForValue().get(curKey);
            }
            if (cachedCurDO == null) {
                // Redis没有则使用数据库对象，并写入Redis
                cachedCurDO = curDO;
                redisTemplate.opsForValue().set(curKey, curDO);
            }

            // 转换DO为VO
            RemarkVO curVO = transferDO2VO(cachedCurDO, user);

            // --- 二级评论对象处理 ---
            List<String> remarkSecondLayerList = new ArrayList<>();
            String replyKey = replyToIdKey + curDO.get_id();
            if (redisTemplate.hasKey(replyKey)) {
                List<Object> tmpReplyList = redisTemplate.opsForList().range(replyKey, 0, -1);
                if (tmpReplyList != null) {
                    remarkSecondLayerList = tmpReplyList.stream().map(Object::toString).toList();
                }
            } else {
                // Redis没有则从数据库加载二级评论，并写入Redis
                List<RemarkDO> replyList = remarkRepository.findRemarksByParentIdAndIsReceiveTrue(curDO.get_id());
                for (RemarkDO reply : replyList) {
                    if (reply != null && reply.get_id() != null) {
                        redisTemplate.opsForValue().set(remarkIdKey + reply.get_id(), reply, Duration.ofHours(1));
                        remarkSecondLayerList.add(reply.get_id());
                    }
                }
            }

            // --- 构建二级评论VO ---
            List<RemarkVO> replies = new ArrayList<>();
            for (String replyId : remarkSecondLayerList) {
                RemarkDO replyDO = null;
                String replyRedisKey = remarkIdKey + replyId;
                if (redisTemplate.hasKey(replyRedisKey)) {
                    replyDO = (RemarkDO) redisTemplate.opsForValue().get(replyRedisKey);
                }
                if (replyDO == null) {
                    replyDO = remarkRepository.findById(replyId).orElse(null);
                    if (replyDO != null) {
                        redisTemplate.opsForValue().set(replyRedisKey, replyDO, Duration.ofHours(1));
                    }
                }
                if (replyDO != null) {
                    RemarkVO replyVO = transferDO2VO(replyDO, user);
                    replies.add(replyVO);
                }
            }
            curVO.setReplies(replies);

            // 将一级评论VO加入结果列表
            remarkVOList.add(curVO);
        }

        return remarkVOList;

    }

    @Transactional
    public Boolean insertRemark(RemarkInsertDTO remarkInsertDTO) {
        try {
            // 1. 转换 DTO 到 DO
            RemarkDO remarkDO = remarkConvert.toDO(remarkInsertDTO);

            // 2. 保存到数据库
            remarkRepository.save(remarkDO);

            // 3. 删除相关缓存（第一次删除）
            if (!remarkDO.getIsReceive()) {
                redisTemplate.delete(NoteIdKey + remarkDO.getNoteId());
            } else {
                redisTemplate.delete(replyToIdKey + remarkDO.getParentId());
            }

            // 4. 延时再删除一次（延时双删）
            new Thread(() -> {
                try {
                    Thread.sleep(50); // 延时 50ms，可根据实际并发调整
                    if (!remarkDO.getIsReceive()) {
                        redisTemplate.delete(NoteIdKey + remarkDO.getNoteId());
                    } else {
                        redisTemplate.delete(replyToIdKey + remarkDO.getParentId());
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();

            return true;
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert remark", e);
        }
    }

    @Transactional
    public Boolean deleteRemark(RemarkDeleteDTO remarkDeleteDTO) {
        try {
            // 1. 查找要删除的评论
            RemarkDO remarkDO = remarkRepository.findById(remarkDeleteDTO.getId())
                    .orElseThrow(() -> new RuntimeException(
                            "Remark not found for ID: " + remarkDeleteDTO.getId()));

            // 2. 如果是接收的评论，先删除其子评论及点赞
            if (Boolean.FALSE.equals(remarkDO.getIsReceive())) {
                String parentId = remarkDO.get_id();

                // 从 Redis 获取子评论列表
                String replyKey = replyToIdKey + parentId;
                List<String> childIds = new ArrayList<>();
                if (redisTemplate.hasKey(replyKey)) {
                    List<Object> tmpList = redisTemplate.opsForList().range(replyKey, 0, -1);
                    if (tmpList != null) {
                        childIds = tmpList.stream().map(Object::toString).toList();
                    }
                }

                // Redis 没有则从数据库加载
                if (childIds.isEmpty()) {
                    List<RemarkDO> childRemarks = remarkRepository.findRemarksByParentIdAndIsReceiveTrue(parentId);
                    for (RemarkDO child : childRemarks) {
                        if (child != null && child.get_id() != null) {
                            childIds.add(child.get_id());
                        }
                    }
                }

                // 删除子评论点赞和缓存
                for (String childId : childIds) {
                    remarkLikeByUsersRepository.deleteById(childId);
                    remarkCountRepository.deleteById(childId);
                    redisTemplate.delete(remarkLikeCountKey + childId);
                    redisTemplate.delete(userLikeRemarkListKey + childId);
                    redisTemplate.delete(remarkIdKey + childId);
                }

                redisTemplate.delete(replyKey);
                remarkRepository.deleteByParentId(parentId);
            }

            // 3. 删除该评论的点赞记录和数据库记录
            remarkLikeByUsersRepository.deleteById(remarkDO.get_id());
            remarkRepository.deleteById(remarkDO.get_id());

            // 4. 删除 Redis 缓存（第一次删除）
            redisTemplate.delete(remarkIdKey + remarkDO.get_id());
            if (!remarkDO.getIsReceive()) {
                redisTemplate.delete(NoteIdKey + remarkDO.getNoteId());
            } else {
                redisTemplate.delete(replyToIdKey + remarkDO.getParentId());
            }

            // 5. 延时再删除一次缓存（延时双删）
            new Thread(() -> {
                try {
                    Thread.sleep(50); // 延时 50ms
                    redisTemplate.delete(remarkIdKey + remarkDO.get_id());
                    if (!remarkDO.getIsReceive()) {
                        redisTemplate.delete(NoteIdKey + remarkDO.getNoteId());
                    } else {
                        redisTemplate.delete(replyToIdKey + remarkDO.getParentId());
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();

            return Boolean.TRUE;
        } catch (Exception e) {
            System.err.println("Failed to delete remark: " + e.getMessage());
            return Boolean.FALSE;
        }
    }


    @Transactional
    public Boolean likeRemark(String remarkId, Long userId) {

        String redisKey = userLikeRemarkListKey + remarkId;

        // 1. 检查用户是否已点赞
        Boolean exists = false;

        if (redisTemplate.hasKey(redisKey)) {
            exists = redisTemplate.opsForSet().isMember(redisKey, userId.toString());
        } else {
            // Redis 无数据，则从数据库加载
            Optional<RemarkLikeByUsersDO> likeRecord =
                    remarkLikeByUsersRepository.findById(remarkId);

            if (likeRecord.isPresent()) {

                Set<String> userList = likeRecord.get().getUserList();

                if (userList != null && !userList.isEmpty()) {
                    exists = userList.contains(userId.toString());

                    // 同步整个 Set 到 Redis
                    redisTemplate.opsForSet().add(redisKey, userList.toArray(new String[0]));
                } else {
                    exists = false;
                }
            } else {
                exists = false;
            }
        }

        if (Boolean.TRUE.equals(exists)) {
            return false; // 已经点赞过
        }

        // 3. 更新 Redis 缓存
        redisTemplate.opsForSet().add(redisKey, userId.toString());

        // 4. 更新评论的 likes 数
        String countKey = remarkLikeCountKey + remarkId;

        Long newCount;

        if (redisTemplate.hasKey(countKey)) {

            // Redis 已有点赞计数，直接自增
            newCount = redisTemplate.opsForHash().increment(countKey, "remarkLikeCount", 1);

        } else {

            // Redis 中没有，从 MongoDB 加载
            RemarkCountDO remarkCount = remarkCountRepository.findById(remarkId)
                    .orElseGet(() -> RemarkCountDO.builder()
                            .remarkId(remarkId)
                            .remarkLikeCount(0L)
                            .version(0L)
                            .build()
                    );

            // 同步到 Redis（作为 Hash）
            redisTemplate.opsForHash().put(countKey, "remarkLikeCount", remarkCount.getRemarkLikeCount());
            redisTemplate.opsForHash().put(countKey, "version", remarkCount.getVersion());

            // 自增一次
            redisTemplate.opsForHash().increment(countKey, "remarkLikeCount", 1);
        }


        return true;
    }

    @Transactional
    public Boolean cancelLikeRemark(String remarkId, Long userId) {

        String redisKey = userLikeRemarkListKey + remarkId;
        String userIdStr = userId.toString();

        // 1. 判断是否已点赞（Redis → MongoDB）
        Boolean exists = false;

        if (redisTemplate.hasKey(redisKey)) {
            exists = redisTemplate.opsForSet().isMember(redisKey, userIdStr);
        } else {
            // Redis 无数据 → MongoDB 加载
            Optional<RemarkLikeByUsersDO> likeRecord =
                    remarkLikeByUsersRepository.findById(remarkId);

            if (likeRecord.isPresent()) {
                Set<String> userList = likeRecord.get().getUserList();

                if (userList != null && !userList.isEmpty()) {
                    exists = userList.contains(userIdStr);

                    // 同步整个 Set 到 Redis
                    redisTemplate.opsForSet().add(redisKey, userList.toArray(new String[0]));
                } else {
                    exists = false;
                }
            } else {
                exists = false;
            }
        }

        if (!Boolean.TRUE.equals(exists)) {
            return false; // 根本没点赞过
        }


        // 2. Redis 取消点赞
        redisTemplate.opsForSet().remove(redisKey, userIdStr);


        // 3. MongoDB 中从 userList 移除用户
        remarkLikeByUsersRepository.findById(remarkId).ifPresent(record -> {
            Set<String> userList = record.getUserList();
            if (userList != null) {
                userList.remove(userIdStr);
            }
            remarkLikeByUsersRepository.save(record);
        });


        // 4. 更新点赞计数（Redis → MongoDB）
        String countKey = remarkLikeCountKey + remarkId;

        if (redisTemplate.hasKey(countKey)) {

            // Redis 已有计数，直接减 1（但不低于 0）
            Long current = (Long) redisTemplate.opsForHash().get(countKey, "remarkLikeCount");
            if (current == null || current <= 0) {
                redisTemplate.opsForHash().put(countKey, "remarkLikeCount", 0L);
            } else {
                redisTemplate.opsForHash().increment(countKey, "remarkLikeCount", -1);
            }
        } else {
            // Redis 无计数 → 从 DB 加载
            RemarkCountDO remarkCount = remarkCountRepository.findById(remarkId)
                    .orElse(null);
            if (remarkCount == null) {
                return false;
            }


            // 同步到 Redis
            redisTemplate.opsForHash().put(countKey, "remarkLikeCount", remarkCount.getRemarkLikeCount());
            redisTemplate.opsForHash().put(countKey, "version", remarkCount.getVersion());

            // 自减一次（不低于 0）
            if (remarkCount.getRemarkLikeCount() > 0) {
                redisTemplate.opsForHash().increment(countKey, "remarkLikeCount", -1);
            }
        }
        return true;
    }
}
