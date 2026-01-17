package com.marv.taskmaster.models.DTO.response.user;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UserDetailResponse {
    private UUID id;
    private String firstname;
    private String lastname;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}