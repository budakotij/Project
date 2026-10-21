package com.company.api.controller;

import com.company.api.service.UserService;
import com.company.dal.sql.schema.model.User;
import java.util.*;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {

  private final UserService userService;

  @GetMapping
  public List<User> listUsers() {
    return null;
  }

  @PostMapping
  public int createUser(@RequestBody User user) {
    return userService.createUser(user);
  }

  @GetMapping("/{userName}")
  public User getUser(@PathVariable String userName) {
    return null;
  }
}
