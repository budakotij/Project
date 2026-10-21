package com.company.dal.sql.schema.enums;

import java.util.Collections;
import java.util.List;
import lombok.Getter;

@Getter
public enum UserType {
  ADMIN(Collections.emptyList()),
  USER(List.of(UserRoles.DEFAULT_USER_ROLE, UserRoles.BROWSE_TRIP, UserRoles.REGISTER_FOR_TRIP)),
  ORGANISER(
      List.of(
          UserRoles.DEFAULT_USER_ROLE,
          UserRoles.BROWSE_TRIP,
          UserRoles.REGISTER_FOR_TRIP,
          UserRoles.CREATE_TRIP,
          UserRoles.POST_TRIP_ANNOUNCEMENTS)),
  ORGANISER_STAFF(
      List.of(
          UserRoles.DEFAULT_USER_ROLE,
          UserRoles.BROWSE_TRIP,
          UserRoles.REGISTER_FOR_TRIP,
          UserRoles.POST_TRIP_ANNOUNCEMENTS));

  final List<UserRoles> roles;

  UserType(List<UserRoles> roles) {
    this.roles = roles;
  }
}
