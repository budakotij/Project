package com.company.commons.model.helper;

import com.company.commons.enums.UserRoles;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginUserDetails {

    private String userName;
    private String passwordHash;
    private List<UserRoles> roles;
}
