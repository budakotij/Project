package com.company.dal.sql.schema.model;

import com.company.dal.sql.schema.enums.Gender;
import com.company.dal.sql.schema.enums.UserRoles;
import com.company.dal.sql.schema.enums.UserType;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class User {
  private final String name;
  private final String userName;
  private final Gender gender;
  private final LocalDate dateOfBirth;
  private final String passwordHash;
  private final String email;
  private final boolean emailVerified;
  private final String phoneNumber;
  private final boolean phoneNumberVerified;
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
    return user instanceof User && ((User) user).userName.equals(this.userName);
  }
}
