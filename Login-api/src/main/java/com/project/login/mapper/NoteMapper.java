package com.project.login.mapper;

import com.project.login.model.dataobject.NoteDO;
import com.project.login.model.entity.NoteEntity;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface NoteMapper {

    // ------------------ CRUD ------------------

    @Insert("INSERT INTO notes (title, filename, file_type, notebook_id, created_at, updated_at) " +
            "VALUES (#{title}, #{filename}, #{fileType}, #{notebookId}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(NoteDO note);

    // ------------------- SELECT -------------------

    @Select("SELECT id, title, filename, file_type, notebook_id, created_at, updated_at " +
            "FROM notes WHERE id = #{id}")
    @Results(id = "NoteBaseResultMap", value = {
            @Result(column = "id", property = "id", id = true),
            @Result(column = "title", property = "title"),
            @Result(column = "filename", property = "filename"),
            @Result(column = "file_type", property = "fileType"),
            @Result(column = "notebook_id", property = "notebookId"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "updated_at", property = "updatedAt")
    })
    NoteDO selectById(Long id);


    @Select("SELECT id, title, filename, file_type, notebook_id, created_at, updated_at " +
            "FROM notes WHERE notebook_id = #{notebookId} ORDER BY updated_at DESC")
    @ResultMap("NoteBaseResultMap")
    List<NoteDO> selectByNotebookId(Long notebookId);

    // ------------------- UPDATE -------------------

    @Update({
            "UPDATE notes",
            "SET title = #{title},",
            "    filename = #{filename},",
            "    file_type = #{fileType},",
            "    updated_at = #{updatedAt}",
            "WHERE id = #{id}"
    })
    void update(NoteEntity note);

    // ------------------- DELETE -------------------

    @Delete("DELETE FROM notes WHERE id = #{id}")
    void deleteById(Long id);

    @Delete("DELETE FROM notes WHERE notebook_id = #{notebookId}")
    void deleteByNotebookId(Long notebookId);


    // ------------------ 视图对象（VO）查询 ------------------

    @Select({
            "SELECT",
            "    n.id AS n_id, n.title, n.filename, n.file_type, n.notebook_id,",
            "    n.created_at, n.updated_at,",
            "    b.name AS notebook_name",
            "FROM notes n",
            "JOIN notebooks b ON n.notebook_id = b.id",
            "WHERE n.notebook_id = #{notebookId}",
            "ORDER BY n.updated_at DESC"
    })
    @Results(id = "NoteVOResultMap", value = {
            @Result(column = "n_id", property = "id", id = true),
            @Result(column = "title", property = "title"),
            @Result(column = "filename", property = "filename"),
            @Result(column = "file_type", property = "fileType"),
            @Result(column = "notebook_id", property = "notebookId"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "updated_at", property = "updatedAt")
    })
    List<NoteDO> selectVOListByNotebookId(Long notebookId);
}