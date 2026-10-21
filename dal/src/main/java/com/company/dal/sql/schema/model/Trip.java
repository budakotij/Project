package com.company.dal.sql.schema.model;

import com.company.dal.sql.annotation.JsonColumn;
import com.company.dal.sql.schema.enums.TripStatus;
import com.company.dal.sql.schema.enums.TripTags;
import com.company.dal.sql.schema.model.internal.Itinerary;
import jakarta.persistence.Column;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.locationtech.jts.geom.Point;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldNameConstants(asEnum = true)
public class Trip {
  private String id;
  private String organiser;
  private List<TripTags> tripTags;
  private TripStatus tripStatus;
  private Instant postedOn;
  private Instant pickupOn;
  private String pickupAt;

  @Column(columnDefinition = "geography(Point, 4326)")
  private Point pickupLocation;

  private Instant dropOn;
  private String dropAt;

  @Column(columnDefinition = "geography(Point, 4326)")
  private Point dropLocation;

  private String description;
  private String destination;

  @Column(columnDefinition = "geography(Point, 4326)")
  private Point destinationLocation;

  @JsonColumn private List<Itinerary> itinerary;
  private List<String> images;
  private Integer maximumOccupancy;
  private Integer availableOccupancy;
}
