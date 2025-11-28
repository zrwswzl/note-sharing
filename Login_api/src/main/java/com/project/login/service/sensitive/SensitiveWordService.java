package com.project.login.service.sensitive;

import com.project.login.model.vo.SensitiveCheckResult;

public interface SensitiveWordService {
    SensitiveCheckResult checkNote(Long noteId);
    SensitiveCheckResult checkNote(Long noteId, boolean full);
    SensitiveCheckResult checkText(String text);
}
