package com.company.dal.sql.repository;

import com.company.dal.sql.annotation.Bind;
import com.company.dal.sql.annotation.Define;
import com.company.dal.sql.annotation.UseClasspathSql;
import com.company.dal.sql.schema.model.Trip;
import java.util.List;

@UseClasspathSql
public interface TripRepository extends BaseRepository<Trip> {

  Trip getTripById(@Bind String id, @Define("columns") List<Trip.Fields> columns);
}
