package dev.ograh.dynamicforms.submission.dto;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record SubmissionDto(
        UUID id,
        String submittedBy,
        LocalDateTime submittedAt,
        Map<String, Object> data
) {}
