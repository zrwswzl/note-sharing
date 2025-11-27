CREATE TABLE IF NOT EXISTS note_stats (
                                          note_id          BIGINT PRIMARY KEY,
                                          views            BIGINT NOT NULL DEFAULT 0,
                                          likes            BIGINT NOT NULL DEFAULT 0,
                                          favorites        BIGINT NOT NULL DEFAULT 0,
                                          comments         BIGINT NOT NULL DEFAULT 0,
                                          last_activity_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                          version          BIGINT NOT NULL DEFAULT 0, -- 用于乐观锁,减少并发冲突时的覆盖风险
                                          updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                          CONSTRAINT fk_notestats_note FOREIGN KEY (note_id) REFERENCES notes(id) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 补偿表：当批量落盘重试耗尽时写入
CREATE TABLE IF NOT EXISTS note_stats_compensation (
                                                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                       note_id BIGINT NOT NULL,
                                                       views BIGINT NOT NULL DEFAULT 0,
                                                       likes BIGINT NOT NULL DEFAULT 0,
                                                       favorites BIGINT NOT NULL DEFAULT 0,
                                                       comments BIGINT NOT NULL DEFAULT 0,
                                                       last_activity_at TIMESTAMP NULL,
                                                       status VARCHAR(32) NOT NULL DEFAULT 'PENDING', -- PENDING / PROCESSING / DONE / FAILED
    retry_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX (note_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
