package com.company.dal.sql.schema.model.helper;

import com.company.dal.sql.schema.enums.UserRoles;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginUserDetails {

  private String userName;
  private String passwordHash;
  private List<UserRoles> roles;
}
