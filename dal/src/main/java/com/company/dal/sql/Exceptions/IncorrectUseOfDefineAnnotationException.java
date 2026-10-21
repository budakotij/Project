package com.company.dal.sql.Exceptions;

public class IncorrectUseOfDefineAnnotationException extends RuntimeException {
  public IncorrectUseOfDefineAnnotationException(Class<?> clazz, boolean isList) {
    super(
        "Unsupported data type "
            + clazz.getName()
            + (isList ? " inside List" : "")
            + " for define annotated param."
            + "Allowed type are Enum, String and Number.");
  }
}
