package com.company.api.modelconverter;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class CommonModelConverter {

  public static Object getIfNonNull(Object o, Supplier<Object> supplier) {
    return Objects.nonNull(o) ? supplier.get() : null;
  }

  public static <T extends Enum<T>> List<T> convertEnumList(
      Class<T> clazz, List<? extends Enum<?>> enumList) {
    return enumList.stream()
        .map(enumValue -> convertEnum(clazz, enumValue))
        .filter(Objects::nonNull)
        .toList();
  }

  public static <T extends Enum<T>> T convertEnum(Class<T> clazz, Enum<?> enumValue) {
    try {
      return Enum.valueOf(clazz, enumValue.name());
    } catch (Exception ex) {
      return null;
    }
  }
}
