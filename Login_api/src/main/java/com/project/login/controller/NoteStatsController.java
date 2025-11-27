package com.project.login.controller;

import com.project.login.model.dto.notestats.NoteStatsDTO;
import com.project.login.model.response.StandardResponse;
import com.project.login.model.vo.NoteStatsVO;
import com.project.login.service.notestats.NoteStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Note Stats", description = "High-frequency note statistics")
@RestController
@RequestMapping("/api/v1/noting/note-stats")
@RequiredArgsConstructor
public class NoteStatsController {

    private final NoteStatsService noteStatsService;

    @Operation(summary = "Increment/Decrement a note statistic field")
    @PostMapping("/change")
    public StandardResponse<NoteStatsVO> change(
            @Valid @RequestBody NoteStatsDTO dto,
            @RequestParam String field,
            @RequestParam(defaultValue = "1") long delta) {

        NoteStatsVO vo = noteStatsService.changeField(dto.getNoteId(), field, delta);
        return StandardResponse.success(vo);
    }

    @Operation(summary = "Get note statistics")
    @GetMapping("/{noteId}")
    public StandardResponse<NoteStatsVO> get(@PathVariable Long noteId) {
        NoteStatsVO vo = noteStatsService.getStats(noteId);
        return StandardResponse.success(vo);
    }
}
