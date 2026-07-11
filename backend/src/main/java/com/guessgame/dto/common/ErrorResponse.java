package com.guessgame.dto.common;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String message,
        String path,
        List<FieldErrorResponse> fieldErrors
) {
}
