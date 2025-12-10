package com.project.login.model.response.notebook;

import com.project.login.model.entity.NotebookEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotebookAdminVO {
    private Long id;
    private String name;
    private Long spaceId;
    private String tag;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static NotebookAdminVO from(NotebookEntity e) {
        return new NotebookAdminVO(
                e.getId(),
                e.getName(),
                e.getSpace() != null ? e.getSpace().getId() : null,
                e.getTag() != null ? e.getTag().getName() : null,
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}