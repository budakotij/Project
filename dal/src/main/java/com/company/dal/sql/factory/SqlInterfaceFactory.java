package com.company.dal.sql.factory;

import org.postgresql.jdbc.PgArray;
import org.postgresql.util.PGTimestamp;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.SqlArrayValue;
import org.springframework.util.FileCopyUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SqlInterfaceFactory {

    @SuppressWarnings("unchecked")
    public static <T> T createRepository(Class<T> interfaceType, NamedParameterJdbcTemplate jdbcTemplate) {
        return (T) Proxy.newProxyInstance(
                interfaceType.getClassLoader(),
                new Class<?>[]{interfaceType},
                (proxy, method, args) -> {

                    // 1. Locate the .sql file on the classpath
                    String path = String.format("sql/repository/%s/%s.sql", interfaceType.getSimpleName(), method.getName());
                    ClassPathResource resource = new ClassPathResource(path);
                    if (!resource.exists()) {
                        throw new IllegalStateException("Missing SQL file at classpath: " + path);
                    }
                    String sql = new String(FileCopyUtils.copyToByteArray(resource.getInputStream()), StandardCharsets.UTF_8);

                    // 2. Advanced Parameter Binding Engine
                    MapSqlParameterSource multiParamSource = new MapSqlParameterSource();

                    if (args != null) {
                        Parameter[] methodParameters = method.getParameters();
                        for (int i = 0; i < methodParameters.length; i++) {
                            String paramName = methodParameters[i].getName();
                            Object paramValue = args[i];

                            if (paramValue == null) {
                                multiParamSource.addValue(paramName, null);
                            } else if(paramValue instanceof Enum<?> paramEnumValue) {
                                multiParamSource.addValue(paramName, paramEnumValue.name());
                            } else if(paramValue instanceof Instant paramInstantValue) {
                                multiParamSource.addValue(paramName, Timestamp.from(paramInstantValue));
                            } else if(paramValue instanceof List<?> paramValueList) {
                                addListParam(multiParamSource, paramName, paramValueList);
                            } else if (isCustomObject(paramValue.getClass())) {
                                // Unpack the properties of the custom object (e.g., User record)
                                BeanPropertySqlParameterSource objectSource = new BeanPropertySqlParameterSource(paramValue);
                                for (String propName : objectSource.getReadablePropertyNames()) {
                                    if (!propName.equals("class")) { // Skip getClass() property
                                        Object propValue = objectSource.getValue(propName);
                                        if(propValue instanceof Enum<?> propEnumValue) {
                                            multiParamSource.addValue(propName, propEnumValue.name());
                                        } else if(propValue instanceof Instant propInstantValue) {
                                            multiParamSource.addValue(propName, Timestamp.from(propInstantValue));
                                        } else if(propValue instanceof List<?> propValueList) {
                                            addListParam(multiParamSource, propName, propValueList);
                                        } else {
                                            multiParamSource.addValue(propName, objectSource.getValue(propName));
                                        }
                                    }
                                }
                            } else {
                                // Standard primitive/wrapper types (String, Long, int, etc.)
                                multiParamSource.addValue(paramName, paramValue);
                            }
                        }
                    }

                    // 3. Execution logic
                    return executeQuery(jdbcTemplate, sql, multiParamSource, method);
                }
        );
    }

    private static void addListParam(MapSqlParameterSource multiParamSource, String paramName, List<?> paramValueList) {
        if(paramValueList.isEmpty()) {
            multiParamSource.addValue(paramName, new Object[]{});
        }
        Object firstValue = paramValueList.getFirst();
        if(paramValueList.getFirst().getClass().isEnum()) {
            Object[] paramValueStringList = paramValueList.stream()
                    .map(value -> ((Enum<?>)value).name())
                    .toArray();
            multiParamSource.addValue(paramName, new SqlArrayValue("text", paramValueStringList));
        } else if(firstValue instanceof String) {
            Object[] paramValueStringList = paramValueList.toArray();
            multiParamSource.addValue(paramName, new SqlArrayValue("text", paramValueStringList));
        } else if(firstValue instanceof Number) {
            Object[] paramValueStringList = paramValueList.toArray();
            multiParamSource.addValue(paramName, new SqlArrayValue("integer", paramValueStringList));
        }
    }

    // Helper to determine if a type is a custom domain model / object
    private static boolean isCustomObject(Class<?> type) {
        return !type.isPrimitive() &&
                !type.getName().startsWith("java.lang.") &&
                !type.getName().startsWith("java.util.") &&
                !type.getName().startsWith("java.math.");
    }

    private static Object executeQuery(NamedParameterJdbcTemplate jdbcTemplate, String sql, MapSqlParameterSource params, Method method) {
        Class<?> returnType = method.getReturnType();
        if (returnType.equals(Void.TYPE)) {
            jdbcTemplate.update(sql, params);
            return null;
        }
        // Handle database mutation queries returning counts
        String trimmedSql = sql.trim().toUpperCase();
        if (trimmedSql.startsWith("UPDATE") || trimmedSql.startsWith("DELETE") || trimmedSql.startsWith("INSERT")) {
            return jdbcTemplate.update(sql, params);
        }
        return jdbcTemplate.queryForObject(sql, params, new CustomPgRowMapper<>(returnType));
    }

    static class CustomPgRowMapper<T> extends BeanPropertyRowMapper<T> {

        public CustomPgRowMapper(Class<T> mappedClass) {
            super(mappedClass);
        }

        @Override
        protected Object getColumnValue(ResultSet rs, int index, Class<?> paramType) throws SQLException {
            Object obj = rs.getObject(index);

            // Process only if it is a PostgreSQL Array
            if (obj instanceof PgArray pgArray) {
                Object rawArray = pgArray.getArray();
                if (rawArray == null) return null;

                // Check if the destination target Java field is a List
                if (paramType.isAssignableFrom(List.class)) {
                    // Pinpoint the exact field name from the current column metadata
                    String columnName = rs.getMetaData().getColumnLabel(index);
                    Class<?> enumClass = getGenericEnumClass(columnName);

                    if (enumClass != null) {
                        // It's a List of Enums! Cleanly map strings to Enum instances
                        String[] stringArray = (String[]) rawArray;
                        return Arrays.stream(stringArray)
                                .map(str -> Enum.valueOf((Class<Enum>) enumClass, str))
                                .collect(Collectors.toList());
                    } else {
                        // It's a standard List (e.g., List<String>, List<Integer>)
                        return Arrays.asList((Object[]) rawArray);
                    }
                }
            } else if(obj instanceof PGTimestamp pgTimestamp) {
                return pgTimestamp.toInstant();
            }

            return super.getColumnValue(rs, index, paramType);
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        @Override
        protected Object getColumnValue(ResultSet rs, int index, PropertyDescriptor pd) throws SQLException {
            Object obj = rs.getObject(index);

            // Process only if it is a PostgreSQL Array
            if (obj instanceof PgArray pgArray) {
                Object rawArray = pgArray.getArray();
                if (rawArray == null) return null;

                Class<?> targetType = pd.getPropertyType();

                // Check if the destination target Java field is a List
                if (List.class.isAssignableFrom(targetType)) {

                    // Extract the generic Type argument using Spring's provided PropertyDescriptor
                    Type genericType = (pd.getWriteMethod() != null)
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
            }

            // Fall back to Spring's default behavior for standard fields
            return super.getColumnValue(rs, index, pd);
        }

        private Class<?> getGenericEnumClass(String columnName) {
            // Convert snake_case column (e.g., user_roles) to camelCase property (e.g., userRoles)
            String propertyName = java.beans.Introspector.decapitalize(
                    Arrays.stream(columnName.split("_"))
                            .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
                            .collect(Collectors.joining())
            );

            PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(getMappedClass(), propertyName);
            if (pd != null && pd.getReadMethod() != null) {
                Type genericType = pd.getReadMethod().getGenericReturnType();
                if (genericType instanceof ParameterizedType pt) {
                    Type[] actualTypeArguments = pt.getActualTypeArguments();
                    if (actualTypeArguments.length > 0 && actualTypeArguments[0] instanceof Class<?> rawType) {
                        if (rawType.isEnum()) {
                            return rawType;
                        }
                    }
                }
            }
            return null;
        }
    }
}

