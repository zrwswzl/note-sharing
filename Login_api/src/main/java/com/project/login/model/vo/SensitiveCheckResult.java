package com.project.login.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class SensitiveCheckResult {
    private String status;
    private String riskLevel;
    private Double score;
    private List<String> categories;
    private List<Finding> findings;
    private NoteMeta noteMeta;
    private String model;
    private String checkedAt;
    private Long durationMs;
    private TokenUsage tokenUsage;
    private String message;

    @Data
    public static class Finding {
        private String term;
        private String category;
        private Double confidence;
        private String snippet;
        private Integer startOffset;
        private Integer endOffset;
    }

    @Data
    public static class NoteMeta {
        private Long noteId;
        private String title;
    }

    @Data
    public static class TokenUsage {
        private Integer promptTokens;
        private Integer completionTokens;
        private Integer totalTokens;
    }
}
