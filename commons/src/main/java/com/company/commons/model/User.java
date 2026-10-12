package com.company.commons.model;

import com.company.commons.enums.UserRoles;
import com.company.commons.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
public class User {
    private final String name;
    private final String userName;
    private final String passwordHash;
    private final String email;
    private final boolean emailVerified;
    private final Instant registrationDate;
    private final String profileImage;
    private final UserType userType;
    private final List<UserRoles> roles;

    @Override
    public int hashCode() {
        return userName.hashCode();
    }

    @Override
    public boolean equals(Object user) {
        return user instanceof User && ((User)user).userName.equals(this.userName);
    }
}
