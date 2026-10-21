package com.company.dal.sql.schema.model;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class TripTravellers {
  private final String id;
  private final String tripId;
  private final String travellerUserName;
  private final Instant registeredOn;
}
