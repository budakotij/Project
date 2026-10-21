package com.company.dal.sql.repository;

import com.company.dal.sql.annotation.Bind;
import com.company.dal.sql.annotation.UseClasspathSql;
import com.company.dal.sql.schema.model.User;
import com.company.dal.sql.schema.model.helper.LoginUserDetails;

@UseClasspathSql
public interface UserRepository extends BaseRepository<User> {

  public LoginUserDetails getLoginUserDetailsByUserName(@Bind String userName);
}
