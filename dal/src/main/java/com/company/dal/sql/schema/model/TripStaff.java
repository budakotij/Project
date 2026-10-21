package com.company.dal.sql.schema.model;

import com.company.dal.sql.schema.enums.StaffType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class TripStaff {
  private final String id;
  private final String tripId;
  private final String staffUserName;
  private final StaffType staffType;
}
