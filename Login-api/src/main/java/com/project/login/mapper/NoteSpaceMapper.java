package com.project.login.mapper;

import com.project.login.model.dataobject.NoteSpaceDO;
import com.project.login.model.vo.NoteSpaceVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface NoteSpaceMapper {

    @Insert("INSERT INTO note_spaces (name, user_id, tag_id, created_at, updated_at) " +
            "VALUES (#{name}, #{userId}, #{tagId}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertNoteSpace(NoteSpaceDO noteSpaceDO);

    @Update("UPDATE note_spaces SET name = #{name}, tag_id = #{tagId}, updated_at = #{updatedAt} WHERE id = #{id}")
    void updateNoteSpace(NoteSpaceDO noteSpaceDO);

    @Delete("DELETE FROM note_spaces WHERE id = #{id}")
    void deleteNoteSpace(Long id);

    @Select("SELECT ns.id, ns.name, ns.user_id, ns.tag_id, " +
            "ns.created_at, ns.updated_at " +
            "FROM note_spaces ns " +
            "LEFT JOIN tags t ON ns.tag_id = t.id " +
            "WHERE ns.user_id = #{userId}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "name", column = "name"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "tagId", column = "tag_id"),
            @Result(property = "tagName", column = "tag_name"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    List<NoteSpaceDO> selectByUser(Long userId);

    @Select("SELECT ns.id, ns.name, ns.user_id, ns.tag_id, " +
            "ns.created_at, ns.updated_at " +
            "FROM note_spaces ns " +
            "LEFT JOIN tags t ON ns.tag_id = t.id " +
            "WHERE ns.id = #{id}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "name", column = "name"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "tagId", column = "tag_id"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    NoteSpaceDO selectById(Long id); // 移除了 @Param 注解
}