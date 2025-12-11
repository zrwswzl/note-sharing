package com.project.login.controller;

import com.project.login.model.response.StandardResponse;
import com.project.login.model.dataobject.NoteDO;
import com.project.login.model.vo.HistoryNoteVO;
import com.project.login.model.vo.HistoryRemarkVO;
import com.project.login.model.vo.RemarkVO;
import com.project.login.service.history.HistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@Tag(name = "History", description = "User's history for remark and note")
@RestController
@RequestMapping("/api/v1/history")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;

    @Operation(summary = "Get user's note history with pagination")
    @GetMapping("/note/list")
    public StandardResponse<Page<HistoryNoteVO>> getNoteHistory(
            @RequestParam Long loginUserId,
            @RequestParam @Min(1) int pageNum,
            @RequestParam @Min(1) int pageSize
    ) {
        Page<HistoryNoteVO> page = historyService.getNoteHistory(loginUserId, pageNum, pageSize);
        return StandardResponse.success(page);
    }

    @Operation(summary = "Get user's remark history with pagination")
    @GetMapping("/remark/list")
    public StandardResponse<Page<HistoryRemarkVO>> getRemarkHistory(
            @RequestParam Long loginUserId,
            @RequestParam @Min(1) int pageNum,
            @RequestParam @Min(1) int pageSize
    ) {
        Page<HistoryRemarkVO> page = historyService.getRemarkHistory(loginUserId, pageNum, pageSize);
        return StandardResponse.success(page);
    }
}
