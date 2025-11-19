package com.project.login.mapper;

import com.project.login.model.dataobject.NotebookDO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface NotebookMapper {

    @Insert("INSERT INTO notebooks (name, space_id, tag_id, created_at, updated_at) " +
            "VALUES (#{name}, #{spaceId}, #{tagId}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(NotebookDO notebook);

    /**
     * 注册 Base ResultMap（必须挂在一个 SELECT 方法上）
     */
    @Select("SELECT id, name, space_id, tag_id, created_at, updated_at FROM notebooks WHERE id = #{id}")
    @Results(id = "NotebookBaseResultMap", value = {
            @Result(column = "id", property = "id", id = true),
            @Result(column = "name", property = "name"),
            @Result(column = "space_id", property = "spaceId"),
            @Result(column = "tag_id", property = "tagId"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "updated_at", property = "updatedAt")
    })
    NotebookDO selectById(Long id);

    @Select("SELECT id, name, space_id, tag_id, created_at, updated_at " +
            "FROM notebooks WHERE space_id = #{spaceId}")
    @ResultMap("NotebookBaseResultMap")
    List<NotebookDO> selectBySpaceId(Long spaceId);


    @Update({
            "UPDATE notebooks",
            "SET name = #{name},",
            "    tag_id = #{tagId},",
            "    space_id = #{spaceId},",
            "    updated_at = #{updatedAt}",
            "WHERE id = #{id}"
    })
    void update(NotebookDO notebook);

    @Delete("DELETE FROM notebooks WHERE id = #{id}")
    void deleteById(Long id);


    @Results(id = "NotebookVOResultMap", value = {
            @Result(column = "n_id", property = "id", id = true),
            @Result(column = "n_name", property = "name"),
            @Result(column = "space_id", property = "spaceId"),
            @Result(column = "tag_id", property = "tagId"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "updated_at", property = "updatedAt"),
            @Result(column = "s_name", property = "spaceName"),
            @Result(column = "t_name", property = "tagName")
    })
    @Select({
            "SELECT",
            "    n.id AS n_id, n.name AS n_name, n.space_id, n.tag_id, n.created_at, n.updated_at,",
            "    s.name AS s_name, t.name AS t_name",
            "FROM notebooks n",
            "JOIN note_spaces s ON n.space_id = s.id",
            "JOIN tags t ON n.tag_id = t.id",
            "WHERE n.space_id = #{spaceId}",
            "ORDER BY n.updated_at DESC"
    })
    List<NotebookDO> selectVOListBySpaceId(Long spaceId);
}
