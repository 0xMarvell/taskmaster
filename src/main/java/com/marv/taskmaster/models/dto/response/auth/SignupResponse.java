package com.marv.taskmaster.models.dto.response.auth;

import com.marv.taskmaster.models.dto.response.user.UserDetailResponse;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SignupResponse {
    private UserDetailResponse user;
}