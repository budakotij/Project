package com.company.dal.sql.repository;

import com.company.dal.sql.annotation.Bind;

public interface BaseRepository<T> {
  public int insert(@Bind T t);
}
