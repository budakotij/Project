package com.company.api.service;

import com.company.api.config.JwtProperties;
import com.company.api.exception.DuplicateEntityException;
import com.company.api.exception.InternalException;
import com.company.api.exception.InvalidUserCredentialsException;
import com.company.api.generated.model.*;
import com.company.api.modelconverter.AuthModelConverter;
import com.company.dal.sql.repository.UserRepository;
import com.company.dal.sql.schema.enums.UserRoles;
import com.company.dal.sql.schema.model.User;
import com.company.dal.sql.schema.model.helper.LoginUserDetails;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthService {

  private final PasswordEncoder passwordEncoder;
  private final UserRepository userRepository;
  private final JwtEncoder jwtEncoder;
  private final JwtProperties jwtProperties;

  public RegisterUser201Response registerUser(RegisterUserRequest request) {
    User user =
        AuthModelConverter.convert(request)
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .build();
    try {
      userRepository.insert(user);
    } catch (DuplicateKeyException ex) {
      System.out.println(ex.getMessage());
      if (ex.getMessage().contains("users_pkey")) {
        throw new DuplicateEntityException(
            "Username " + request.getUserName() + " is already taken");
      } else {
        throw new DuplicateEntityException(
            "Email " + request.getEmail() + " is already registered");
      }
    } catch (Exception ex) {
      // ToDo: Replace with log
      ex.printStackTrace();
      throw new InternalException(
          "Unable to register user at the moment. Please try again after some time.");
    }
    String accessToken = createAccessToken(user.getUserName(), user.getRoles());
    return new RegisterUser201Response(
        accessToken,
        "Bearer",
        Instant.now().plus(jwtProperties.getAccessTokenTtl()).atOffset(ZoneOffset.UTC));
  }

  public LoginUser200Response loginUser(LoginUserRequest request) {
    LoginUserDetails user;
    try {
      user = userRepository.getLoginUserDetailsByUserName(request.getUserName());
      if (Objects.isNull(user)
          || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
        throw new InvalidUserCredentialsException("Incorrect username or password.");
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      throw new InternalException(
          "Unable to login at the moment. Please try again after some time.");
    }

    String accessToken = createAccessToken(request.getUserName(), user.getRoles());
    return new LoginUser200Response(
        accessToken,
        "Bearer",
        Instant.now().plus(jwtProperties.getAccessTokenTtl()).atOffset(ZoneOffset.UTC));
  }

  public RefreshToken200Response refreshToken(String userName) {
    LoginUserDetails user;
    try {
      user = userRepository.getLoginUserDetailsByUserName(userName);
      if (Objects.isNull(user)) {
        throw new InvalidUserCredentialsException(
            "User not found. Unable to refresh access token.");
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      throw new InternalException("Unable to refresh access token");
    }

    String accessToken = createAccessToken(userName, user.getRoles());
    return new RefreshToken200Response(
        accessToken,
        "Bearer",
        Instant.now().plus(jwtProperties.getAccessTokenTtl()).atOffset(ZoneOffset.UTC));
  }

  public String createAccessToken(String userName, List<UserRoles> roles) {
    Instant now = Instant.now();

    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .subject(userName)
            .issuedAt(now)
            .expiresAt(now.plus(jwtProperties.getAccessTokenTtl()))
            .claim("roles", roles.stream().map(Enum::name).toList())
            .build();

    JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();

    return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
  }
}
