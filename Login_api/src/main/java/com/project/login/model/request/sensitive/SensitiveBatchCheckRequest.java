package com.project.login.model.request.sensitive;

import lombok.Data;

import java.util.List;

@Data
public class SensitiveBatchCheckRequest {
    private List<Long> noteIds;
    private Integer concurrency;
    private Boolean full;
}
