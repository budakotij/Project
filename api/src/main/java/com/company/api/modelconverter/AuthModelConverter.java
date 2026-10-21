package com.company.api.modelconverter;

import com.company.api.generated.model.RegisterUserRequest;
import com.company.dal.sql.schema.enums.Gender;
import com.company.dal.sql.schema.enums.UserType;
import com.company.dal.sql.schema.model.User;
import java.time.Instant;
import java.util.Objects;
import lombok.NonNull;

public class AuthModelConverter {

  public static User.UserBuilder convert(@NonNull RegisterUserRequest request) {
    UserType userType = UserType.valueOf(request.getUserType().name());
    return User.builder()
        .userName(request.getUserName())
        .name(request.getName())
        .dateOfBirth(request.getDateOfBirth())
        .gender(
            Objects.nonNull(request.getGender())
                ? CommonModelConverter.convertEnum(Gender.class, request.getGender())
                : Gender.NOT_SPECIFIED)
        .phoneNumber(request.getPhoneNumber())
        .phoneNumberVerified(false)
        .email(request.getEmail())
        .emailVerified(false)
        .profileImage(request.getProfileImage())
        .userType(userType)
        .roles(userType.getRoles())
        .registrationDate(Instant.now());
  }
}
