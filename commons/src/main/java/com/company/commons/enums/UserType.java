package com.company.commons.enums;

import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
public enum UserType {
    ADMIN(Collections.emptyList()),
    USER(List.of(UserRoles.DEFAULT_USER_ROLE, UserRoles.BROWSE_TRIP, UserRoles.REGISTER_FOR_TRIP)),
    ORGANISER(List.of(UserRoles.DEFAULT_USER_ROLE, UserRoles.BROWSE_TRIP, UserRoles.REGISTER_FOR_TRIP, UserRoles.CREATE_TRIP, UserRoles.POST_TRIP_NOTIFICATION)),
    ORGANISER_STAFF(List.of(UserRoles.DEFAULT_USER_ROLE, UserRoles.BROWSE_TRIP, UserRoles.REGISTER_FOR_TRIP, UserRoles.POST_TRIP_NOTIFICATION));

    final List<UserRoles> roles;

    UserType(List<UserRoles> roles) {
        this.roles = roles;
    }
}
