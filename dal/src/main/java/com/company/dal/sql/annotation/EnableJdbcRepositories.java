package com.company.dal.sql.annotation;

import com.company.dal.sql.config.JdbcRepositoryRegistrar;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(JdbcRepositoryRegistrar.class)
public @interface EnableJdbcRepositories {
  String[] basePackages();
}
