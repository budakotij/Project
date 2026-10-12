package com.company.dal.sql.factory;

import lombok.Data;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Data
public class JdbcRepositoryFactoryBean<T> implements FactoryBean<T> {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private Class<T> repositoryInterface;
    private T repository;

    public JdbcRepositoryFactoryBean(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public T getObject() {
        if (repository == null) {
            repository = SqlInterfaceFactory.createRepository(
                    repositoryInterface,
                    jdbcTemplate
            );
        }

        return repository;
    }

    @Override
    public Class<?> getObjectType() {
        return repositoryInterface;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}