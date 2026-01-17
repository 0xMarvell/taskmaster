package com.marv.taskmaster.models.DTO.response.auth;

import com.marv.taskmaster.models.DTO.response.user.UserDetailResponse;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private String token;
    private UserDetailResponse user;
}