package com.project.login.service.noting;

import com.project.login.model.dataobject.NoteSpaceDO;
import com.project.login.model.dataobject.TagDO;
import com.project.login.model.dto.notespace.*;
import com.project.login.model.dataobject.NoteSpaceDO;
import com.project.login.model.vo.NoteSpaceVO;
import com.project.login.mapper.NoteSpaceMapper;
import com.project.login.mapper.TagMapper;
import com.project.login.mapper.UserMapper;
import com.project.login.convert.NoteSpaceConvert;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoteSpaceService {

    private final NoteSpaceMapper noteSpaceMapper;
    private final UserMapper userMapper;
    private final TagMapper tagMapper;
    @Qualifier("noteSpaceConvert")
    private final NoteSpaceConvert convert;

    // --- 创建笔记空间 ---
    @Transactional
    public NoteSpaceVO createSpace(NoteSpaceCreateDTO dto) {
        if (userMapper.selectById(dto.getUserId()) == null) {
            throw new RuntimeException("用户不存在");
        }

        Long tagId = tagMapper.selectIdByName(dto.getTag());
        if (tagId == null) {
            TagDO newTag = TagDO.builder()
                    .name(dto.getTag())
                    .build();
            tagMapper.insertTag(newTag);
            tagId = newTag.getId();
        }

        NoteSpaceDO doObj = NoteSpaceDO.builder()
                .name(dto.getName())
                .userId(dto.getUserId())
                .tagId(tagId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        noteSpaceMapper.insertNoteSpace(doObj);

        return convert.toVO(doObj);
    }

    // --- 修改笔记空间 ---
    @Transactional
    public NoteSpaceVO renameSpace(NoteSpaceUpdateDTO dto) {
        NoteSpaceDO existing = noteSpaceMapper.selectById(dto.getId());
        if (existing == null) throw new RuntimeException("笔记空间不存在");

        Long tagId = tagMapper.selectIdByName(dto.getTag());
        if (tagId == null) {
            TagDO newTag = TagDO.builder()
                    .name(dto.getTag())
                    .build();
            tagMapper.insertTag(newTag);
            tagId = newTag.getId();
        }

        existing.setName(dto.getName());
        existing.setTagId(tagId);
        existing.setUpdatedAt(LocalDateTime.now());

        noteSpaceMapper.updateNoteSpace(existing);

        NoteSpaceDO updated = noteSpaceMapper.selectById(dto.getId());
        return convert.toVO(updated);
    }

    // --- 删除笔记空间 ---
    @Transactional
    public void deleteSpace(NoteSpaceDeleteDTO dto) {
        NoteSpaceDO existing = noteSpaceMapper.selectById(dto.getId());
        if (existing == null) throw new RuntimeException("笔记空间不存在");

        noteSpaceMapper.deleteNoteSpace(dto.getId());
    }


    @Transactional(readOnly = true)
    public List<NoteSpaceVO> getSpacesByUserId(NoteSpaceByUserDTO dto) {
        if (userMapper.selectById(dto.getUserId()) == null) throw new RuntimeException("用户不存在");

        List<NoteSpaceDO> doList = noteSpaceMapper.selectByUser(dto.getUserId());
        return doList.stream().map(convert::toVO).collect(Collectors.toList());
    }
}
