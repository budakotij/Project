package com.company.api.exception;

public class DuplicateEntityException extends RuntimeException {
  public DuplicateEntityException(String message) {
    super(message);
  }
}
