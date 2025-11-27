package com.project.login.mapper;

import com.project.login.model.dataobject.NoteStatsCompensationDO;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface NoteStatsCompensationMapper {

    @Insert("INSERT INTO note_stats_compensation(note_id, views, likes, favorites, comments, last_activity_at, status) " +
            "VALUES(#{noteId}, #{views}, #{likes}, #{favorites}, #{comments}, #{lastActivityAt}, #{status})")
    void insert(NoteStatsCompensationDO compensation);

    @Select("SELECT * FROM note_stats_compensation WHERE status IN ('PENDING','FAILED')")
    List<NoteStatsCompensationDO> getPendingOrFailed();

    @Update("UPDATE note_stats_compensation SET status=#{status}, retry_count=#{retryCount}, updated_at=NOW() WHERE id=#{id}")
    void updateStatus(NoteStatsCompensationDO compensation);
}
