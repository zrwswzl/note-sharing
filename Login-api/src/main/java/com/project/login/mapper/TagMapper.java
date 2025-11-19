package com.project.login.mapper;

import com.project.login.model.dataobject.TagDO;
import org.apache.ibatis.annotations.*;

@Mapper
public interface TagMapper {

    @Select("SELECT id FROM tags WHERE name = #{name}")
    Long selectIdByName(@Param("name") String name);

    @Insert("INSERT INTO tags (name) VALUES (#{name})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertTag(TagDO tag);
}