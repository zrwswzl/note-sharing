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
import com.project.login.model.entity.VerificationTokenEntity;
import com.project.login.model.vo.RemarkVO;
import com.project.login.repository.RemarkLikeRepository;
import com.project.login.repository.RemarkRepository;
import com.project.login.service.login.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
    private final JwtService jwtService;
    private RemarkVO transferDO2VO(RemarkDO remarkDO, Long loginUserId) {
        RemarkVO cur = remarkConvert.toVO(remarkDO);
        Long userId = remarkDO.getUserId();
        String receiveToRemarkId = remarkDO.getReceiveToRemarkId();
        Optional<RemarkDO> receiveTORemarkOptional = remarkRepository.findBy_id(receiveToRemarkId);
        RemarkDO receiveTORemark = receiveTORemarkOptional
                .orElseThrow(() -> new RuntimeException("Receive remark not found for ID: " + receiveToRemarkId));
        UserDO receiveUser = userMapper.selectById(receiveTORemark.getUserId());
        // 处理当前用户
        UserDO user = userMapper.selectById(userId);

        // 设置是否已点赞
        cur.setLikedOrNot(remarkLikeRepository.existsByRemarkIdAndUserId(
                cur.get_id(),
                loginUserId));

        // 设置用户名和回复用户名
        cur.setUsername(user.getUsername());
        cur.setReplyToUsername(receiveUser.getUsername());

        return cur;
    }
    @Transactional
    public List<RemarkVO> SelectRemark(RemarkSelectByNoteDTO remarkSelectByNoteDTO, VerificationTokenEntity token){
        List<RemarkDO> remarkDOList=remarkRepository.findRemarksByNoteIdAndIsReceiveFalse(remarkSelectByNoteDTO.getNoteId());
        List<RemarkVO> remarkVOList=new ArrayList<>();
        Long loginUserId=jwtService.extractUserId(token.getToken());
        for(RemarkDO remarkDO : remarkDOList){
            RemarkVO curFirst=transferDO2VO(remarkDO,loginUserId);
            List<RemarkDO> remarkReplyDOS=remarkRepository.findRemarksByParentIdAndIsReceiveTrue(curFirst.getParentId());
            List<RemarkVO> remarkReplyVOs=new ArrayList<>();
            for(RemarkDO remarkDO1 : remarkReplyDOS){
                RemarkVO curSec=transferDO2VO(remarkDO1,loginUserId);
                remarkReplyVOs.add(curSec);
            }
            curFirst.setReplies(remarkReplyVOs);
            remarkVOList.add(curFirst);
        }
        return remarkVOList;
    }
    @Transactional
    public List<RemarkVO> selectRemarkByUserId(VerificationTokenEntity token){
        Long loginUserId=jwtService.extractUserId(token.getToken());
        List<RemarkDO> remarkDOList=remarkRepository.findByUserId(loginUserId);
        List<RemarkVO> remarkVOList=new ArrayList<>();
        for(RemarkDO remarkDO : remarkDOList){
            RemarkVO curFirst=transferDO2VO(remarkDO,loginUserId);
            List<RemarkDO> remarkReplyDOS=remarkRepository.findRemarksByParentIdAndIsReceiveTrue(curFirst.getParentId());
            List<RemarkVO> remarkReplyVOs=new ArrayList<>();
            for(RemarkDO remarkDO1 : remarkReplyDOS){
                RemarkVO curSec=transferDO2VO(remarkDO1,loginUserId);
                remarkReplyVOs.add(curSec);
            }
            curFirst.setReplies(remarkReplyVOs);
            remarkVOList.add(curFirst);
        }
        return remarkVOList;
    }
    @Transactional
    public Boolean insertRemark(RemarkInsertDTO remarkInsertDTO) {
        try {
            // 转换 DTO 到 DO
            RemarkDO remarkDO = remarkConvert.toDO(remarkInsertDTO);

            // 保存数据
            remarkRepository.save(remarkDO);

            // 如果插入成功，返回 true
            return true;
        } catch (Exception e) {
            // 如果发生异常，打印日志并抛出运行时异常，事务将回滚
            throw new RuntimeException("Failed to insert remark", e);
        }
    }
    @Transactional
    public Boolean deleteRemark(RemarkDeleteDTO remarkDeleteDTO) {
        try {
            // 1. 查找要删除的评论
            RemarkDO remarkDO = remarkRepository.findBy_id(remarkDeleteDTO.getId())
                    .orElseThrow(() -> new RuntimeException(
                            "Remark not found for ID: " + remarkDeleteDTO.getId()));

            // 2. 如果是接收的评论，先删除其子评论及子评论的点赞记录
            if (Boolean.TRUE.equals(remarkDO.getIsReceive())) {
                String parentId = remarkDO.getParentId();

                // 查找所有子评论
                List<RemarkDO> childRemarks = remarkRepository.findRemarksByParentIdAndIsReceiveTrue(parentId);

                // 删除每个子评论的点赞记录
                for (RemarkDO child : childRemarks) {
                    remarkLikeRepository.deleteByRemarkId(child.get_id());
                }

                // 删除子评论
                remarkRepository.deleteByParentId(parentId);
            }

            // 3. 删除该评论的点赞记录
            remarkLikeRepository.deleteByRemarkId(remarkDO.get_id());

            // 4. 删除评论本身
            remarkRepository.deleteById(remarkDO.get_id());

            return Boolean.TRUE;
        } catch (Exception e) {
            // 打印错误日志（可换成你项目的日志框架）
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
