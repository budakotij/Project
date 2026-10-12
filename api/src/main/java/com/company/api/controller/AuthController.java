package com.company.api.controller;


import com.company.api.exception.InvalidUserCredentialsException;
import com.company.api.generated.AuthApi;
import com.company.api.generated.model.*;
import com.company.api.service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;

import java.util.Objects;

@Controller
@AllArgsConstructor
public class AuthController implements AuthApi {

    private final AuthService authService;

    @Override
    public ResponseEntity<RegisterUser201Response> registerUser(
            RegisterUserRequest registerUserRequest
    ) {
        RegisterUser201Response response = authService.registerUser(registerUserRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<LoginUser200Response> loginUser(
            LoginUserRequest loginUserRequest
    ) {
        LoginUser200Response response = authService.loginUser(loginUserRequest);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<RefreshToken200Response> refreshToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(Objects.isNull(authentication) || !StringUtils.hasLength(authentication.getName())) {
            throw new InvalidUserCredentialsException("User not found. Unable to refresh access token.");
        }
        RefreshToken200Response response = authService.refreshToken(authentication.getName());
        return ResponseEntity.ok(response);
    }
}
