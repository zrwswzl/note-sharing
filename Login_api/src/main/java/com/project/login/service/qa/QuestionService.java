package com.project.login.service.qa;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.login.convert.QuestionConvert;
import com.project.login.model.dataobject.QAType;
import com.project.login.model.dataobject.QuestionDO;
import com.project.login.model.dto.qa.*;
import com.project.login.model.event.QuestionEvent;
import com.project.login.model.vo.qa.AnswerVO;
import com.project.login.model.vo.qa.CommentVO;
import com.project.login.model.vo.qa.QuestionVO;
import com.project.login.model.vo.qa.ReplyVO;
import com.project.login.repository.QuestionRepository;

import com.project.login.service.history.UserQARecordService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository repo;
    private final QuestionConvert convert;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final UserQARecordService userQARecordService;
    @Resource
    private StringRedisTemplate redis;


    private void updateRedisCacheIfExists(String questionId, QuestionDO updated) {
        String key = "question:detail:" + questionId;

        Boolean exists = redis.hasKey(key);
        if (Boolean.TRUE.equals(exists)) {
            try {
                String json = objectMapper.writeValueAsString(updated);
                redis.opsForValue().set(key, json, Duration.ofHours(2));
            } catch (Exception e) {
                log.error("更新 Redis 缓存失败 key={}", key, e);
            }
        }
    }


    /** 创建问题并异步发送到 Elasticsearch */
    public QuestionVO createQuestion(QuestionCreateDTO dto) {
        // 1. 构建 QuestionDO 并保存到 MongoDB
        QuestionDO q = new QuestionDO();
        q.setAuthorId(dto.getAuthorId());
        q.setTitle(dto.getTitle());
        q.setContent(dto.getContent());
        q.setTags(dto.getTags());
        q.setCreatedAt(LocalDateTime.now());

        // 保存问题，Mongo 自动生成 _id
        repo.save(q);

        // 2. 异步发送到 Elasticsearch
        String tagsStr = String.join(",", q.getTags()); // 标签列表转成字符串
        QuestionEvent event = new QuestionEvent(
                q.getQuestionId(),
                q.getTitle(),
                q.getContent(),
                tagsStr,
                QuestionEvent.EventType.CREATE
        );
        rabbitTemplate.convertAndSend("question.es.queue", event);

        //3. 创建记录
        try {
            userQARecordService.sendQARecord(dto.getAuthorId(),q.getQuestionId(), QAType.QUESTION);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return convert.toQuestionVO(q);
    }


    /** 创建回答 */
    public AnswerVO createAnswer(AnswerCreateDTO dto) {
        QuestionDO q = repo.findByQuestionId(dto.getQuestionId());
        if (q == null) return null;

        QuestionDO.AnswerDO a = new QuestionDO.AnswerDO();
        a.setAnswerId(System.currentTimeMillis());
        a.setAuthorId(dto.getAuthorId());
        a.setContent(dto.getContent());
        a.setCreatedAt(LocalDateTime.now());

        q.getAnswers().add(a);
        repo.save(q);

        updateRedisCacheIfExists(dto.getQuestionId(), q);

        //3. 创建记录
        try {
            userQARecordService.sendQARecord(dto.getAuthorId(),dto.getQuestionId(),QAType.ANSWER);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return convert.toAnswerVO(a);
    }

    /** 创建评论 */
    public CommentVO createComment(CommentCreateDTO dto) {
        QuestionDO q = repo.findByQuestionId(dto.getQuestionId());
        if (q == null) return null;

        // 查找指定回答
        QuestionDO.AnswerDO answer = q.getAnswers().stream()
                .filter(a -> a.getAnswerId().equals(dto.getAnswerId()))
                .findFirst()
                .orElse(null);

        if (answer == null) return null;

        // 创建新的评论
        QuestionDO.CommentDO comment = new QuestionDO.CommentDO();
        comment.setCommentId(System.currentTimeMillis()); // 可以换成自动生成的ID
        comment.setAuthorId(dto.getAuthorId());
        comment.setContent(dto.getContent());
        comment.setCreatedAt(LocalDateTime.now());

        // 将评论添加到对应回答的评论列表
        answer.getComments().add(comment);

        // 保存更新后的问题对象
        repo.save(q);

        updateRedisCacheIfExists(dto.getQuestionId(), q);

        // 转换为 VO 对象并返回
        return convert.toCommentVO(comment);
    }


    /** 创建回复（二级评论） */
    public ReplyVO createReply(ReplyCreateDTO dto) {
        // 查找问题
        QuestionDO q = repo.findByQuestionId(dto.getQuestionId());
        if (q == null) return null; // 如果问题不存在，返回 null

        // 查找指定的回答
        QuestionDO.AnswerDO answer = q.getAnswers().stream()
                .filter(a -> a.getAnswerId().equals(dto.getAnswerId()))
                .findFirst()
                .orElse(null);
        if (answer == null) return null; // 如果回答不存在，返回 null

        // 查找指定的评论
        QuestionDO.CommentDO comment = answer.getComments().stream()
                .filter(c -> c.getCommentId().equals(dto.getCommentId()))
                .findFirst()
                .orElse(null);
        if (comment == null) return null; // 如果评论不存在，返回 null

        // 创建新的回复
        QuestionDO.ReplyDO reply = new QuestionDO.ReplyDO();
        reply.setReplyId(System.currentTimeMillis()); // 可以换成自动生成的ID
        reply.setAuthorId(dto.getAuthorId());
        reply.setContent(dto.getContent());
        reply.setCreatedAt(LocalDateTime.now());

        // 将回复添加到评论的回复列表中
        comment.getReplies().add(reply);

        // 保存更新后的问题对象
        repo.save(q);

        updateRedisCacheIfExists(dto.getQuestionId(), q);

        // 转换为 VO 对象并返回
        return convert.toReplyVO(reply);
    }

    /** 点赞问题 */
    public void likeQuestion(Long userId, String questionId) {
        QuestionDO q = repo.findByQuestionId(questionId);
        if (q == null) return;

        if (q.getLikes().contains(userId))
            q.getLikes().remove(userId);
        else
            q.getLikes().add(userId);

        repo.save(q);

        updateRedisCacheIfExists(questionId, q);

    }

    /** 点赞回答 */
    public void likeAnswer(Long userId, String questionId, Long answerId) {
        QuestionDO q = repo.findByQuestionId(questionId);
        if (q == null) return;

        // 查找指定回答
        QuestionDO.AnswerDO answer = q.getAnswers().stream()
                .filter(a -> a.getAnswerId().equals(answerId))
                .findFirst()
                .orElse(null);

        if (answer == null) return; // 如果没有找到该回答

        // 判断用户是否已点赞
        if (answer.getLikes().contains(userId)) {
            answer.getLikes().remove(userId); // 如果已点赞，则移除
        } else {
            answer.getLikes().add(userId);    // 如果未点赞，则添加
        }

        // 保存更新后的问题
        repo.save(q);

        updateRedisCacheIfExists(questionId, q);
    }

    /** 点赞评论 */
    public void likeComment(Long userId, String questionId, Long answerId, Long commentId) {
        QuestionDO q = repo.findByQuestionId(questionId);
        if (q == null) return;

        // 查找指定回答
        QuestionDO.AnswerDO answer = q.getAnswers().stream()
                .filter(a -> a.getAnswerId().equals(answerId))
                .findFirst()
                .orElse(null);

        if (answer == null) return; // 如果没有找到该回答

        // 查找指定评论
        QuestionDO.CommentDO comment = answer.getComments().stream()
                .filter(c -> c.getCommentId().equals(commentId))
                .findFirst()
                .orElse(null);

        if (comment == null) return; // 如果没有找到该评论

        // 判断用户是否已点赞
        if (comment.getLikes().contains(userId)) {
            comment.getLikes().remove(userId); // 如果已点赞，则移除
        } else {
            comment.getLikes().add(userId);    // 如果未点赞，则添加
        }

        // 保存更新后的问题
        repo.save(q);

        updateRedisCacheIfExists(questionId, q);
    }

    /** 点赞回复（二级评论） */
    public void likeReply(Long userId, String questionId, Long answerId, Long commentId, Long replyId) {
        QuestionDO q = repo.findByQuestionId(questionId);
        if (q == null) return;

        // 查找指定回答
        QuestionDO.AnswerDO answer = q.getAnswers().stream()
                .filter(a -> a.getAnswerId().equals(answerId))
                .findFirst()
                .orElse(null);

        if (answer == null) return; // 如果没有找到该回答

        // 查找指定评论
        QuestionDO.CommentDO comment = answer.getComments().stream()
                .filter(c -> c.getCommentId().equals(commentId))
                .findFirst()
                .orElse(null);

        if (comment == null) return; // 如果没有找到该评论

        // 查找指定回复
        QuestionDO.ReplyDO reply = comment.getReplies().stream()
                .filter(r -> r.getReplyId().equals(replyId))
                .findFirst()
                .orElse(null);

        if (reply == null) return; // 如果没有找到该回复

        // 判断用户是否已点赞
        if (reply.getLikes().contains(userId)) {
            reply.getLikes().remove(userId); // 如果已点赞，则移除
        } else {
            reply.getLikes().add(userId);    // 如果未点赞，则添加
        }

        // 保存更新后的问题
        repo.save(q);

        updateRedisCacheIfExists(questionId, q);
    }

    /** 收藏问题 */
    public void favoriteQuestion(Long userId, String questionId) {
        QuestionDO q = repo.findByQuestionId(questionId);
        if (q == null) return;

        if (q.getFavorites().contains(userId))
            q.getFavorites().remove(userId);
        else
            q.getFavorites().add(userId);

        repo.save(q);

        updateRedisCacheIfExists(questionId, q);
    }

    /** 删除提问并异步发送到 Elasticsearch */
    public void deleteQuestion(String questionId) {
        // 1. 查询问题，确保获取到标题、内容、标签等信息
        QuestionDO q = repo.findByQuestionId(questionId);
        if (q == null) return;

        // 2. 删除 MongoDB 中的记录
        repo.deleteByQuestionId(questionId);

        redis.delete("question:detail:" + questionId);

        // 3. 异步发送删除事件到 Elasticsearch
        String tagsStr = String.join(",", q.getTags());
        QuestionEvent event = new QuestionEvent(
                q.getQuestionId(),
                q.getTitle(),
                q.getContent(),
                tagsStr,
                QuestionEvent.EventType.DELETE
        );
        rabbitTemplate.convertAndSend("question.es.queue", event);

        //4. 删除记录
        try {
            userQARecordService.deleteQARecord(q.getAuthorId(),q.getQuestionId(),QAType.QUESTION);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** 删除回答 */
    public void deleteAnswer(String questionId, Long answerId) throws Exception {
        QuestionDO q = repo.findByQuestionId(questionId);
        if (q == null) return;

        q.getAnswers().removeIf(a -> a.getAnswerId().equals(answerId));
        repo.save(q);

        updateRedisCacheIfExists(questionId, q);

        userQARecordService.deleteQARecord(q.getAuthorId(),questionId,QAType.ANSWER);
    }

    /** 删除评论 */
    public void deleteComment(String questionId, Long answerId, Long commentId) {
        QuestionDO q = repo.findByQuestionId(questionId);
        if (q == null) return;

        q.getAnswers().stream()
                .filter(a -> a.getAnswerId().equals(answerId))
                .findFirst()
                .ifPresent(a ->
                        a.getComments().removeIf(c -> c.getCommentId().equals(commentId))
                );

        repo.save(q);

        updateRedisCacheIfExists(questionId, q);
    }

    /** 删除回复（二级评论） */
    public void deleteReply(String questionId, Long answerId, Long commentId, Long replyId) {
        QuestionDO q = repo.findByQuestionId(questionId);
        if (q == null) return;

        q.getAnswers().stream()
                .filter(a -> a.getAnswerId().equals(answerId))
                .flatMap(a -> a.getComments().stream())
                .filter(c -> c.getCommentId().equals(commentId))
                .findFirst()
                .ifPresent(c ->
                        c.getReplies().removeIf(r -> r.getReplyId().equals(replyId))
                );

        repo.save(q);

        updateRedisCacheIfExists(questionId, q);
    }

    /** 查询详情（带 Redis 缓存） */
    public QuestionVO getQuestion(String questionId) {
        String key = "question:detail:" + questionId;

        // 1. 查 Redis
        String cacheJson = redis.opsForValue().get(key);
        if (cacheJson != null) {
            try {
                QuestionDO q = objectMapper.readValue(cacheJson, QuestionDO.class);
                return convert.toQuestionVO(q);
            } catch (Exception e) {
                log.warn("Redis 缓存 JSON 解析失败，降级读取 Mongo，key={}", key, e);
            }
        }

        // 2. Mongo 查询
        QuestionDO q = repo.findByQuestionId(questionId);
        if (q == null) return null;

        // 3. 同步写入 Redis，设置生存时间 1 小时
        try {
            String json = objectMapper.writeValueAsString(q);

            redis.opsForValue().set(
                    key,
                    json,
                    Duration.ofHours(2)        // ← TTL = 2 小时
            );

        } catch (Exception e) {
            log.error("写入 Redis 缓存失败 questionId={}", questionId, e);
        }

        return convert.toQuestionVO(q);
    }

    // 获取提问记录
    public List<QuestionVO> getQuestionRecordsByAuthorId(Long authorId) throws Exception {
        List<String> questionIds = userQARecordService.getQARecordsByUserIdAndType(authorId, QAType.QUESTION);
        List<QuestionVO> vos = new ArrayList<>();
        for (String questionId : questionIds) {
            QuestionVO q = getQuestion(questionId);
            if (q != null) {
                vos.add(q);
            }
        }
        return vos;
    }

    // 获取回答记录
    public List<QuestionVO> getAnswerRecordsByAuthorId(Long authorId) throws Exception {
        // 获取用户回答的 questionId 列表，并去重
        List<String> questionIds = new ArrayList<>(new HashSet<>(userQARecordService.getQARecordsByUserIdAndType(authorId, QAType.ANSWER)));

        // 如果没有回答记录，直接返回空列表
        if (questionIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 查询问题详情并组装成 QuestionVO 列表
        List<QuestionVO> questionVOList = new ArrayList<>();
        for (String questionId : questionIds) {
            QuestionVO questionVO = getQuestion(questionId);
            if (questionVO != null) {
                questionVOList.add(questionVO);
            }
        }

        return questionVOList;
    }

}
