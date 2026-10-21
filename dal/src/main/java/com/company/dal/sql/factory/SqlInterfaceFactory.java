package com.company.dal.sql.factory;

import com.company.dal.sql.Exceptions.IncorrectUseOfDefineAnnotationException;
import com.company.dal.sql.annotation.Bind;
import com.company.dal.sql.annotation.Define;
import com.company.dal.sql.annotation.JsonColumn;
import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;
import org.postgresql.jdbc.PgArray;
import org.postgresql.util.PGobject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.SqlArrayValue;
import org.springframework.util.FileCopyUtils;
import tools.jackson.databind.ObjectMapper;

public class SqlInterfaceFactory {

  private static final ObjectMapper objectMapper = new ObjectMapper();
  private static final WKTWriter wktWriter = new WKTWriter();

  @SuppressWarnings("unchecked")
  public static <T> T createRepository(
      Class<T> interfaceType, NamedParameterJdbcTemplate jdbcTemplate) {
    return (T)
        Proxy.newProxyInstance(
            interfaceType.getClassLoader(),
            new Class<?>[] {interfaceType},
            (proxy, method, args) -> {

              // 1. Locate the .sql file on the classpath
              String path =
                  String.format(
                      "sql/repository/%s/%s.sql", interfaceType.getSimpleName(), method.getName());
              ClassPathResource resource = new ClassPathResource(path);
              if (!resource.exists()) {
                throw new IllegalStateException("Missing SQL file at classpath: " + path);
              }
              String sql =
                  new String(
                      FileCopyUtils.copyToByteArray(resource.getInputStream()),
                      StandardCharsets.UTF_8);

              // 2. Advanced Parameter Binding Engine
              MapSqlParameterSource multiParamSource = new MapSqlParameterSource();

              if (args != null) {
                Parameter[] methodParameters = method.getParameters();
                for (int i = 0; i < methodParameters.length; i++) {
                  Parameter param = methodParameters[i];
                  String paramName = param.getName();
                  Object paramValue = args[i];

                  if (param.isAnnotationPresent(Define.class)) {
                    Define define = param.getAnnotation(Define.class);
                    String literal = "<" + define.value() + ">";
                    String replacement = "";
                    if (paramValue instanceof List<?> paramValueList) {
                      if (paramValueList.isEmpty()) {
                        continue;
                      }
                      Class<?> paramValueItemType = paramValueList.getFirst().getClass();
                      if (paramValueItemType.isEnum()) {
                        replacement =
                            paramValueList.stream()
                                .map(value -> ((Enum<?>) value).name())
                                .collect(Collectors.joining(", "));
                      } else if (paramValueList.getFirst() instanceof String) {
                        replacement =
                            paramValueList.stream()
                                .map(value -> (String) value)
                                .collect(Collectors.joining(", "));
                      } else if (paramValueList.getFirst() instanceof Number) {
                        replacement =
                            paramValueList.stream()
                                .map(String::valueOf)
                                .collect(Collectors.joining(", "));
                      } else {
                        throw new IncorrectUseOfDefineAnnotationException(paramValueItemType, true);
                      }
                    } else if (paramValue instanceof String) {
                      replacement = path;
                    } else if (paramValue instanceof Number) {
                      replacement = String.valueOf(paramValue);
                    } else {
                      throw new IncorrectUseOfDefineAnnotationException(
                          paramValue.getClass(), false);
                    }
                    sql = sql.replace(literal, replacement);
                  } else if (param.isAnnotationPresent(Bind.class)) {
                    performBinding(multiParamSource, paramName, paramValue);
                  }
                }
              }

              // 3. Execution logic
              return executeQuery(jdbcTemplate, sql, multiParamSource, method);
            });
  }

  private static void performBinding(
      MapSqlParameterSource multiParamSource, String paramName, Object paramValue) {
    if (paramValue == null) {
      multiParamSource.addValue(paramName, null);
    } else if (paramValue instanceof Enum<?> paramEnumValue) {
      multiParamSource.addValue(paramName, paramEnumValue.name());
    } else if (paramValue instanceof Instant paramInstantValue) {
      multiParamSource.addValue(paramName, Timestamp.from(paramInstantValue));
    } else if (paramValue instanceof List<?> paramValueList) {
      addListParam(multiParamSource, paramName, paramValueList);
    } else if (isCustomObject(paramValue.getClass())) {
      // Unpack the properties of the custom object (e.g., User record)
      Class<?> clazz = paramValue.getClass();
      for (Field field : clazz.getDeclaredFields()) {
        String propName = field.getName();
        if (!propName.equals("class")) { // Skip getClass() property
          Object propValue;
          try {
            field.setAccessible(true);
            propValue = field.get(paramValue);
          } catch (IllegalAccessException ex) {
            ex.printStackTrace();
            continue;
          }
          if (field.isAnnotationPresent(JsonColumn.class)) {
            multiParamSource.addValue(propName, objectMapper.writeValueAsString(propValue));
          } else if (propValue instanceof Enum<?> propEnumValue) {
            multiParamSource.addValue(propName, propEnumValue.name());
          } else if (propValue instanceof Instant propInstantValue) {
            multiParamSource.addValue(propName, Timestamp.from(propInstantValue));
          } else if (propValue instanceof List<?> propValueList) {
            addListParam(multiParamSource, propName, propValueList);
          } else if (propValue instanceof Point propValuePoint) {
            multiParamSource.addValue(propName, wktWriter.write(propValuePoint));
          } else {
            multiParamSource.addValue(propName, propValue);
          }
        }
      }
    } else {
      // Standard primitive/wrapper types (String, Long, int, etc.)
      multiParamSource.addValue(paramName, paramValue);
    }
  }

  private static void addListParam(
      MapSqlParameterSource multiParamSource, String paramName, List<?> paramValueList) {
    if (paramValueList.isEmpty()) {
      multiParamSource.addValue(paramName, new Object[] {});
    }
    Object firstValue = paramValueList.getFirst();
    if (paramValueList.getFirst().getClass().isEnum()) {
      Object[] paramValueStringList =
          paramValueList.stream().map(value -> ((Enum<?>) value).name()).toArray();
      multiParamSource.addValue(paramName, new SqlArrayValue("text", paramValueStringList));
    } else if (firstValue instanceof String) {
      Object[] paramValueStringList = paramValueList.toArray();
      multiParamSource.addValue(paramName, new SqlArrayValue("text", paramValueStringList));
    } else if (firstValue instanceof Number) {
      Object[] paramValueStringList = paramValueList.toArray();
      multiParamSource.addValue(paramName, new SqlArrayValue("integer", paramValueStringList));
    }
  }

  // Helper to determine if a type is a custom domain model / object
  private static boolean isCustomObject(Class<?> type) {
    return !type.isPrimitive()
        && !type.getName().startsWith("java.lang.")
        && !type.getName().startsWith("java.util.")
        && !type.getName().startsWith("java.math.");
  }

  private static Object executeQuery(
      NamedParameterJdbcTemplate jdbcTemplate,
      String sql,
      MapSqlParameterSource params,
      Method method) {
    Class<?> returnType = method.getReturnType();
    if (returnType.equals(Void.TYPE)) {
      jdbcTemplate.update(sql, params);
      return null;
    }
    // Handle database mutation queries returning counts
    String trimmedSql = sql.trim().toUpperCase();
    if (trimmedSql.startsWith("UPDATE")
        || trimmedSql.startsWith("DELETE")
        || trimmedSql.startsWith("INSERT")) {
      return jdbcTemplate.update(sql, params);
    }
    return jdbcTemplate.queryForObject(sql, params, new CustomPgRowMapper<>(returnType));
  }

  static class CustomPgRowMapper<T> extends BeanPropertyRowMapper<T> {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final WKBReader wkbReader = new WKBReader();
    private static final WKTReader wktReaderLocal = new WKTReader();

    public CustomPgRowMapper(Class<T> mappedClass) {
      super(mappedClass);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected Object getColumnValue(ResultSet rs, int index, PropertyDescriptor pd)
        throws SQLException {
      Object obj = rs.getObject(index);
      Class<?> targetType = pd.getPropertyType();
      System.out.println(pd.getName());
      Field field;
      try {
        field = getMappedClass().getDeclaredField(pd.getName());
      } catch (NoSuchFieldException e) {
        throw new RuntimeException(e);
      }

      // Process only if it is a PostgreSQL Array
      if (obj instanceof PgArray pgArray) {
        Object rawArray = pgArray.getArray();
        if (rawArray == null) return null;

        // Check if the destination target Java field is a List
        if (List.class.isAssignableFrom(targetType)) {

          // Extract the generic Type argument using Spring's provided PropertyDescriptor
          Type genericType =
              (pd.getWriteMethod() != null)
                  ? pd.getWriteMethod().getGenericParameterTypes()[0]
                  : pd.getReadMethod().getGenericReturnType();

          if (genericType instanceof ParameterizedType pt) {
            Type[] typeArguments = pt.getActualTypeArguments();

            if (typeArguments.length > 0 && typeArguments[0] instanceof Class<?> genericClass) {

              // If it's a List of Enums, clean map them
              if (genericClass.isEnum()) {
                String[] stringArray = (String[]) rawArray;
                return Arrays.stream(stringArray)
                    .map(str -> Enum.valueOf((Class<Enum>) genericClass, str))
                    .collect(Collectors.toList());
              }
            }
          }

          // If it's a standard List (e.g., List<String>, List<Integer>)
          return Arrays.asList((Object[]) rawArray);
        }
      } else if (field.isAnnotationPresent(JsonColumn.class) && obj instanceof PGobject pGobject) {
        String objString = pGobject.getValue();
        return objectMapper.readValue(
            objString, objectMapper.getTypeFactory().constructType(field.getGenericType()));
      } else if (Point.class.isAssignableFrom(pd.getPropertyType())
          && obj instanceof PGobject pgObject) {
        String geoValue = pgObject.getValue();
        if (geoValue != null) {
          Point point = null;
          try {
            // Scenario A: If Postgres returns standard hex WKB (most common)
            byte[] aux = WKBReader.hexToBytes(geoValue);
            point = (Point) wkbReader.read(aux);
          } catch (Exception e) {
            try {
              // Scenario B: Fallback to text parsing if SQL uses ST_AsText() or returns WKT
              point = (Point) wktReaderLocal.read(geoValue);
            } catch (Exception ex) {
              throw new SQLException("Failed to parse geometry for field: " + pd.getName(), ex);
            }
          }
          return point;
        }
      }

      // Fall back to Spring's default behavior for standard fields
      return super.getColumnValue(rs, index, pd);
    }
  }
}
