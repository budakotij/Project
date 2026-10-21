package com.company.dal.sql.schema.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class Announcements {
  private final String id;
  private final String tripId;
  private final String postedBy;
  private final String title;
  private final String description;
  private final List<String> images;
}
