package com.project.login.service.history;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HistoryCleaner {
    private final HistoryService historyService;
    /**
     * 每天凌晨3点执行历史记录清理
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanHistoryJob() {
        historyService.cleanOldHistory();
    }
}

