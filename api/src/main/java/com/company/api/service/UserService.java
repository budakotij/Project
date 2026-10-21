package com.company.api.service;

import com.company.dal.sql.repository.UserRepository;
import com.company.dal.sql.schema.model.User;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public int createUser(User user) {
    return userRepository.insert(user);
  }
}
