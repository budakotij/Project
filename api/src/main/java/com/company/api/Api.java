package com.company.api;

import com.company.dal.sql.annotation.EnableJdbcRepositories;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableJdbcRepositories(basePackages = "com.company.dal.sql.repository")
public class Api {
  public static void main(String[] args) {
    SpringApplication.run(Api.class, args);
  }
}
