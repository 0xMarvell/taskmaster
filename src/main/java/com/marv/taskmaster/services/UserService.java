package com.marv.taskmaster.services;

import com.marv.taskmaster.models.dto.response.generic.PagedData;
import com.marv.taskmaster.models.dto.response.user.UserDetailResponse;
import com.marv.taskmaster.models.dto.response.user.UsersResponse;
import com.marv.taskmaster.models.entities.User;
import com.marv.taskmaster.models.security.CustomUserDetails;
import com.marv.taskmaster.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return new CustomUserDetails(user);
    }

    public PagedData<UsersResponse> getAllUsers(Pageable pageable) {
        Page<User> userPage = userRepository.findAll(pageable);
        Page<UsersResponse> responsePage = userPage.map(this::mapToUsersResponse);
        return new PagedData<>(responsePage);
    }

    public UserDetailResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + id));
        return mapToDetailResponse(user);
    }

    // --- Private Helpers ---

    private UserDetailResponse mapToDetailResponse(User user) {
        return UserDetailResponse.builder()
                .id(user.getId())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private UsersResponse mapToUsersResponse(User user) {
        return UsersResponse.builder()
                .id(user.getId())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .build();
    }
}