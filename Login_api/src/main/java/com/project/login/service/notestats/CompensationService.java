package com.project.login.service.notestats;

import com.project.login.mapper.NoteStatsCompensationMapper;
import com.project.login.mapper.NoteStatsMapper;
import com.project.login.model.dataobject.NoteStatsCompensationDO;
import com.project.login.model.dataobject.NoteStatsDO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompensationService {

    private final NoteStatsCompensationMapper compensationMapper;
    private final NoteStatsMapper noteStatsMapper;
    private static final int MAX_RETRY = 3;

    public void retryCompensation() {
        List<NoteStatsCompensationDO> pendingList = compensationMapper.getPendingOrFailed();
        for (NoteStatsCompensationDO comp : pendingList) {
            try {
                NoteStatsDO doEntity = new NoteStatsDO();
                doEntity.setNoteId(comp.getNoteId());
                doEntity.setViews(comp.getViews());
                doEntity.setLikes(comp.getLikes());
                doEntity.setFavorites(comp.getFavorites());
                doEntity.setComments(comp.getComments());
                doEntity.setLastActivityAt(comp.getLastActivityAt() != null ? comp.getLastActivityAt() : LocalDateTime.now());
                doEntity.setVersion(0L);

                int updated = noteStatsMapper.updateOptimistic(doEntity);
                if (updated > 0) {
                    comp.setStatus("DONE");
                } else {
                    comp.setRetryCount(comp.getRetryCount() + 1);
                    comp.setStatus(comp.getRetryCount() >= MAX_RETRY ? "FAILED" : "PENDING");
                }
            } catch (Exception e) {
                comp.setRetryCount(comp.getRetryCount() + 1);
                if (comp.getRetryCount() >= MAX_RETRY) comp.setStatus("FAILED");
            }
            compensationMapper.updateStatus(comp);
        }
    }
}
