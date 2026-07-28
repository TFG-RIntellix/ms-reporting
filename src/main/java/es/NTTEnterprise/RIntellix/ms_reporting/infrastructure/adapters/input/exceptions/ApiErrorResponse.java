package es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.adapters.input.exceptions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standardized API Error Response Data Transfer Object.
 * Encapsulates error details returned to the client when exceptions occur.
 *
 * @author Lucía Fernández Mancebo
 * @Date 07-28-2026
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
}
