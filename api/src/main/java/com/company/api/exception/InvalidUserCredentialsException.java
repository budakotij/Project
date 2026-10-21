package com.company.api.exception;

public class InvalidUserCredentialsException extends RuntimeException {
  public InvalidUserCredentialsException(String message) {
    super(message);
  }
}
