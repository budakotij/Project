package com.company.dal.sql.schema.model.internal;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class Itinerary {
  private final String title;
  private final String description;
  private final Instant timestamp;
  private final String image;
}
