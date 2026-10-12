package com.company.dal.sql.repository;

import com.company.commons.model.User;
import com.company.commons.model.helper.LoginUserDetails;
import com.company.dal.sql.annotation.UseClasspathSql;

@UseClasspathSql
public interface UserRepository extends BaseRepository<User> {

    public LoginUserDetails getLoginUserDetailsByUserName(String userName);
}
