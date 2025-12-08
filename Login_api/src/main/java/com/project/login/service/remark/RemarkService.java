package com.project.login.service.remark;


import com.project.login.convert.RemarkConvert;
import com.project.login.mapper.NoteMapper;
import com.project.login.mapper.UserMapper;
import com.project.login.model.dataobject.RemarkDO;
import com.project.login.model.dataobject.RemarkLikeDO;
import com.project.login.model.dataobject.UserDO;
import com.project.login.model.dto.remark.RemarkDeleteDTO;
import com.project.login.model.dto.remark.RemarkInsertDTO;
import com.project.login.model.dto.remark.RemarkSelectByNoteDTO;
import com.project.login.model.vo.RemarkVO;
import com.project.login.repository.RemarkCountRepository;
import com.project.login.repository.RemarkLikeRepository;
import com.project.login.repository.RemarkRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RemarkService {
    private final RemarkRepository remarkRepository;
    private final RemarkLikeRepository remarkLikeRepository;
    private final RemarkConvert remarkConvert;
    private final NoteMapper noteMapper;
    private final UserMapper userMapper;
    private final RemarkCountRepository remarkCountRepository;
    private final RedisTemplate<String,Object> redisTemplate;
    private final String remarkCountKey="remark_count:";
    private final String remarkIdKey ="remark:";
    private final String NoteIdKey="note_id_of_remark_list:";
    private final String replyToIdKey="reply_to";
    private final String 
    private RemarkVO transferDO2VO(RemarkDO remarkDO, UserDO user) {
        //初始转化与变量准备
        RemarkVO cur = remarkConvert.toVO(remarkDO);
        Long loginUserId = remarkDO.getUserId();
        String receiveToRemarkId = remarkDO.getReceiveToRemarkId();

        Optional<RemarkDO> receiveTORemarkOptional = remarkRepository.findById(receiveToRemarkId);
        RemarkDO receiveTORemark = receiveTORemarkOptional
                .orElseThrow(() -> new RuntimeException("Receive remark not found for ID: " + receiveToRemarkId));
        // 设置是否已点赞
        // ------
        cur.setLikedOrNot(remarkLikeRepository.existsByRemarkIdAndUserId(
                cur.get_id(),
                loginUserId));
        // ------
        // 设置用户名和回复用户名
        cur.setUsername(user.getUsername());
        cur.setReplyToUsername(remarkDO.getReplyToUsername());

        return cur;
    }
    @Transactional
    public List<RemarkVO> SelectRemark(RemarkSelectByNoteDTO remarkSelectByNoteDTO, Long loginUserId){
// 获取当前用户信息
        UserDO user = userMapper.selectById(loginUserId);
        Long noteId = remarkSelectByNoteDTO.getNoteId();
        String noteKey = NoteIdKey + noteId;
        List<String> remarkFirstLayerList = new ArrayList<>();
        List<RemarkVO> result = new ArrayList<>();

// --- 一级评论列表处理 ---
        if(redisTemplate.hasKey(noteKey)){
            // 从Redis读取一级评论ID列表
            List<Object> tmpList = redisTemplate.opsForList().range(noteKey, 0, -1);
            if(tmpList != null){
                remarkFirstLayerList = tmpList.stream().map(Object::toString).toList();
            }
        } else {
            // Redis中没有则从MongoDB读取
            List<RemarkDO> tmpRemarkDOList = remarkRepository.findRemarksByNoteIdAndIsReceiveFalse(noteId);
            for(RemarkDO cur : tmpRemarkDOList){
                if(cur != null && cur.get_id() != null){
                    remarkFirstLayerList.add(cur.get_id());
                }
            }
            // 将一级评论ID列表写入Redis
            if(!remarkFirstLayerList.isEmpty()){
                redisTemplate.opsForList().rightPushAll(noteKey, remarkFirstLayerList);
            }
        }

// --- 遍历一级评论 ---
        for(String cur : remarkFirstLayerList){
            RemarkDO curDO = null;
            // 尝试从Redis获取一级评论对象
            if(redisTemplate.hasKey(remarkIdKey + cur)){
                curDO = (RemarkDO) redisTemplate.opsForValue().get(remarkIdKey + cur);
            }
            if(curDO == null){
                // Redis没有则从MongoDB获取
                curDO = remarkRepository.findById(cur).orElse(null);
                if(curDO != null){
                    redisTemplate.opsForValue().set(remarkIdKey + cur, curDO);
                }
            }

            // 如果一级评论对象为空，跳过
            if(curDO == null) continue;

            // 将DO转换为VO
            RemarkVO curVO = transferDO2VO(curDO, user);

            // --- 二级评论列表处理 ---
            List<String> remarkSecondLayerList = new ArrayList<>();
            String replyKey = replyToIdKey + cur;
            if(redisTemplate.hasKey(replyKey)){
                // 从Redis获取二级评论ID列表
                List<Object> tmpReplyList = redisTemplate.opsForList().range(replyKey, 0, -1);
                if(tmpReplyList != null){
                    remarkSecondLayerList = tmpReplyList.stream().map(Object::toString).toList();
                }
            } else {
                // Redis没有则从MongoDB获取
                List<RemarkDO> replyList = remarkRepository.findRemarksByParentIdAndIsReceiveTrue(cur);
                for(RemarkDO reply : replyList){
                    if(reply != null && reply.get_id() != null){
                        redisTemplate.opsForValue().set(remarkIdKey + reply.get_id(), reply);
                        remarkSecondLayerList.add(reply.get_id());
                    }
                }
            }

            // --- 二级评论VO构建 ---
            List<RemarkVO> replies = new ArrayList<>();
            for(String replyId : remarkSecondLayerList){
                RemarkDO replyDO = null;
                if(redisTemplate.hasKey(remarkIdKey + replyId)){
                    replyDO = (RemarkDO) redisTemplate.opsForValue().get(remarkIdKey + replyId);
                }
                if(replyDO != null){
                    RemarkVO replyVO = transferDO2VO(replyDO, user);
                    replies.add(replyVO);
                }
            }
            curVO.setReplies(replies);

            // 将一级评论VO加入结果列表
            result.add(curVO);
        }
        return result;
    }

    @Transactional
    public List<RemarkVO> selectRemarkByUserId(UserDO user){
        Long loginUserId = user.getId();
        List<RemarkVO> remarkVOList = new ArrayList<>();

// 从数据库获取该用户所有一级评论
        List<RemarkDO> remarkDOList = remarkRepository.findByUserId(loginUserId);

        for(RemarkDO curDO : remarkDOList){
            if(curDO == null || curDO.get_id() == null) continue;

            // --- 一级评论对象处理 ---
            String curKey = remarkIdKey + curDO.get_id();
            RemarkDO cachedCurDO = null;
            if(redisTemplate.hasKey(curKey)){
                cachedCurDO = (RemarkDO) redisTemplate.opsForValue().get(curKey);
            }
            if(cachedCurDO == null){
                // Redis没有则使用数据库对象，并写入Redis
                cachedCurDO = curDO;
                redisTemplate.opsForValue().set(curKey, curDO);
            }

            // 转换DO为VO
            RemarkVO curVO = transferDO2VO(cachedCurDO, user);

            // --- 二级评论对象处理 ---
            List<String> remarkSecondLayerList = new ArrayList<>();
            String replyKey = replyToIdKey + curDO.get_id();
            if(redisTemplate.hasKey(replyKey)){
                List<Object> tmpReplyList = redisTemplate.opsForList().range(replyKey, 0, -1);
                if(tmpReplyList != null){
                    remarkSecondLayerList = tmpReplyList.stream().map(Object::toString).toList();
                }
            } else {
                // Redis没有则从数据库加载二级评论，并写入Redis
                List<RemarkDO> replyList = remarkRepository.findRemarksByParentIdAndIsReceiveTrue(curDO.get_id());
                for(RemarkDO reply : replyList){
                    if(reply != null && reply.get_id() != null){
                        redisTemplate.opsForValue().set(remarkIdKey + reply.get_id(), reply);
                        remarkSecondLayerList.add(reply.get_id());
                    }
                }
            }

            // --- 构建二级评论VO ---
            List<RemarkVO> replies = new ArrayList<>();
            for(String replyId : remarkSecondLayerList){
                RemarkDO replyDO = null;
                String replyRedisKey = remarkIdKey + replyId;
                if(redisTemplate.hasKey(replyRedisKey)){
                    replyDO = (RemarkDO) redisTemplate.opsForValue().get(replyRedisKey);
                }
                if(replyDO == null){
                    replyDO = remarkRepository.findById(replyId).orElse(null);
                    if(replyDO != null){
                        redisTemplate.opsForValue().set(replyRedisKey, replyDO);
                    }
                }
                if(replyDO != null){
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
                    remarkLikeRepository.deleteByRemarkId(childId);
                    redisTemplate.delete(remarkIdKey + childId);
                }

                redisTemplate.delete(replyKey);
                remarkRepository.deleteByParentId(parentId);
            }

            // 3. 删除该评论的点赞记录和数据库记录
            remarkLikeRepository.deleteByRemarkId(remarkDO.get_id());
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
        // 1. 检查用户是否已点赞
        Boolean exists = remarkLikeRepository.existsByRemarkIdAndUserId(remarkId, userId);
        if (Boolean.TRUE.equals(exists)) {
            return false; // 已经点赞过
        }

        // 2. 插入点赞记录
        RemarkLikeDO like = RemarkLikeDO.builder()
                .remarkId(remarkId)
                .userId(userId)
                .createdAt(LocalDateTime.now())
                .build();
        remarkLikeRepository.save(like);

        // 3. 更新评论的 likes 数
        RemarkDO remark = remarkRepository.findBy_id(remarkId)
                .orElseThrow(() -> new RuntimeException("Remark not found for ID: " + remarkId));
        remark.setLikes(remark.getLikes() == null ? 1 : remark.getLikes() + 1);
        remarkRepository.save(remark);

        return true;
    }

    @Transactional
    public Boolean cancelLikeRemark(String remarkId, Long userId) {
        // 1. 检查点赞记录是否存在
        Boolean exists = remarkLikeRepository.existsByRemarkIdAndUserId(remarkId, userId);
        if (!Boolean.TRUE.equals(exists)) {
            return false; // 没有点赞过
        }

        // 2. 删除点赞记录// 注意：如果一条评论可能有多个用户点赞，这里应改为删除指定用户
        remarkLikeRepository.deleteByRemarkIdAndUserId(remarkId, userId);

        // 3. 更新评论的 likes 数
        RemarkDO remark = remarkRepository.findBy_id(remarkId)
                .orElseThrow(() -> new RuntimeException("Remark not found for ID: " + remarkId));
        remark.setLikes(remark.getLikes() == null || remark.getLikes() <= 0 ? 0 : remark.getLikes() - 1);
        remarkRepository.save(remark);

        return true;
    }
}
