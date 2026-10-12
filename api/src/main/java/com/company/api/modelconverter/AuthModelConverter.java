package com.company.api.modelconverter;

import com.company.api.generated.model.RegisterUserRequest;
import com.company.commons.enums.UserRoles;
import com.company.commons.enums.UserType;
import com.company.commons.model.User;
import lombok.NonNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AuthModelConverter {

    public static User.UserBuilder convert(@NonNull RegisterUserRequest request) {
        UserType userType = UserType.valueOf(request.getUserType().name());
        return User.builder()
                .userName(request.getUserName())
                .name(request.getName())
                .email(request.getEmail())
                .emailVerified(false)
                .profileImage(request.getProfileImage())
                .userType(userType)
                .roles(userType.getRoles())
                .registrationDate(Instant.now());
    }
}
