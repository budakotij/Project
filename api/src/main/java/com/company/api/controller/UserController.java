package com.company.api.controller;

import com.company.commons.model.User;
import com.company.api.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

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
