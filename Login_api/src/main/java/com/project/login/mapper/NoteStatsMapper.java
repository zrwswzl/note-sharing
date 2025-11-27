package com.project.login.mapper;

import com.project.login.model.dataobject.NoteStatsDO;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface NoteStatsMapper {

    @Select("SELECT * FROM note_stats WHERE note_id = #{noteId}")
    NoteStatsDO getById(@Param("noteId") Long noteId);

    @Insert("INSERT INTO note_stats(note_id, views, likes, favorites, comments, last_activity_at, version) " +
            "VALUES(#{noteId}, #{views}, #{likes}, #{favorites}, #{comments}, #{lastActivityAt}, #{version})")
    void insert(NoteStatsDO noteStats);

    @Update("UPDATE note_stats SET views=#{views}, likes=#{likes}, favorites=#{favorites}, comments=#{comments}, " +
            "last_activity_at=#{lastActivityAt}, version=version+1 WHERE note_id=#{noteId} AND version=#{version}")
    int updateOptimistic(NoteStatsDO noteStats);

    @Select("SELECT * FROM note_stats ORDER BY last_activity_at DESC LIMIT #{limit}")
    List<NoteStatsDO> getRecentUpdated(@Param("limit") int limit);

    @Select({
            "<script>",
            "SELECT * FROM note_stats WHERE note_id IN ",
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>",
            "</script>"
    })
    List<NoteStatsDO> getByIds(@Param("ids") List<Long> ids);

    @Delete("DELETE FROM note_stats WHERE note_id = #{noteId}")
    void deleteById(@Param("noteId") Long noteId);
}

