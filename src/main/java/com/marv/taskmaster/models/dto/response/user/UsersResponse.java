package com.marv.taskmaster.models.dto.response.user;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class UsersResponse {
    private UUID id;
    private String firstname;
    private String lastname;
    private String email;
}