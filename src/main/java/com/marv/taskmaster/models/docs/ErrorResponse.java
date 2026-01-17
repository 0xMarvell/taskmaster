package com.marv.taskmaster.models.docs;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;


/**
 *  ONLY for Swagger/OpenAPI documentation purposes
 * to explicitly show that "data" is null in error responses.
 */
@Data
public class ErrorResponse {
    @Schema(example = "false")
    private boolean success;

    @Schema(example = "Error message description")
    private String message;

    // This forces the Schema to show "data": null
    @Schema(nullable = true, example = "null")
    private Object data;

    @Schema(example = "2026-01-17T10:00:00")
    private LocalDateTime timestamp;
}