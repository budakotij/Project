package com.company.api.modelconverter;

import static com.company.api.modelconverter.CommonModelConverter.convertEnum;
import static com.company.api.modelconverter.CommonModelConverter.convertEnumList;

import com.company.api.generated.model.*;
import com.company.dal.sql.schema.enums.TripStatus;
import com.company.dal.sql.schema.enums.TripTags;
import com.company.dal.sql.schema.model.Trip;
import com.company.dal.sql.schema.model.internal.Itinerary;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

public class TripModelConverter {

  private static final GeometryFactory geometryFactory =
      new GeometryFactory(new PrecisionModel(), 4326);

  public static Trip convert(CreateTripRequest request) {
    Instant now = Instant.now();
    return Trip.builder()
        .id(UUID.randomUUID().toString())
        .organiser(request.getOrganiser())
        .tripTags(convertEnumList(TripTags.class, request.getTripTags()))
        .tripStatus(TripStatus.NEW)
        .postedOn(now)
        .pickupOn(request.getPickupOn().toInstant())
        .pickupAt(request.getPickupAt())
        .pickupLocation(
            geometryFactory.createPoint(
                new Coordinate(
                    request.getPickupLocation().getLongitude(),
                    request.getPickupLocation().getLatitude())))
        .dropOn(request.getDropOn().toInstant())
        .dropAt(request.getDropAt())
        .dropLocation(
            (geometryFactory.createPoint(
                new Coordinate(
                    request.getDropLocation().getLongitude(),
                    request.getDropLocation().getLatitude()))))
        .description(request.getDescription())
        .destination(request.getDestination())
        .destinationLocation(
            (geometryFactory.createPoint(
                new Coordinate(
                    request.getDropLocation().getLongitude(),
                    request.getDropLocation().getLatitude()))))
        .itinerary(convert(request.getItinerary()))
        .images(request.getImages())
        .maximumOccupancy(request.getMaximumOccupancy())
        .availableOccupancy(request.getMaximumOccupancy())
        .build();
  }

  public static TripModel convert(Trip trip) {
    return new TripModel()
        .id(trip.getId())
        .postedOn(
            (OffsetDateTime)
                CommonModelConverter.getIfNonNull(
                    trip.getPostedOn(), () -> trip.getPostedOn().atOffset(ZoneOffset.UTC)))
        .tripStatus(convertEnum(TripModel.TripStatusEnum.class, trip.getTripStatus()))
        .organiser(trip.getOrganiser())
        .tripTags(
            (List<TripModel.TripTagsEnum>)
                CommonModelConverter.getIfNonNull(
                    trip.getTripTags(),
                    () -> convertEnumList(TripModel.TripTagsEnum.class, trip.getTripTags())))
        .pickupOn(
            (OffsetDateTime)
                CommonModelConverter.getIfNonNull(
                    trip.getPickupOn(), () -> trip.getPickupOn().atOffset(ZoneOffset.UTC)))
        .pickupAt(trip.getPickupAt())
        .pickupLocation(
            (CreateTripRequestPickupLocation)
                CommonModelConverter.getIfNonNull(
                    trip.getPickupLocation(),
                    () ->
                        new CreateTripRequestPickupLocation()
                            .longitude(trip.getPickupLocation().getX())
                            .latitude(trip.getPickupLocation().getX())))
        .dropOn(
            (OffsetDateTime)
                CommonModelConverter.getIfNonNull(
                    trip.getDropOn(), () -> trip.getDropOn().atOffset(ZoneOffset.UTC)))
        .dropAt(trip.getDropAt())
        .dropLocation(
            (CreateTripRequestDropLocation)
                CommonModelConverter.getIfNonNull(
                    trip.getDropLocation(),
                    () ->
                        new CreateTripRequestDropLocation()
                            .longitude(trip.getDropLocation().getX())
                            .latitude(trip.getDropLocation().getX())))
        .description(trip.getDescription())
        .destination(trip.getDestination())
        .destinationLocation(
            (CreateTripRequestDestinationLocation)
                CommonModelConverter.getIfNonNull(
                    trip.getDestinationLocation(),
                    () ->
                        new CreateTripRequestDestinationLocation()
                            .longitude(trip.getDestinationLocation().getX())
                            .latitude(trip.getDestinationLocation().getX())))
        .itinerary(
            (List<CreateTripRequestItineraryInner>)
                CommonModelConverter.getIfNonNull(
                    trip.getItinerary(), () -> convertToApiModel(trip.getItinerary())))
        .images(trip.getImages())
        .maximumOccupancy(trip.getMaximumOccupancy())
        .availableOccupancy(trip.getAvailableOccupancy());
  }

  public static List<Itinerary> convert(List<CreateTripRequestItineraryInner> itineraryList) {
    return itineraryList.stream().map(TripModelConverter::convert).toList();
  }

  public static Itinerary convert(CreateTripRequestItineraryInner itinerary) {
    return Itinerary.builder()
        .title(itinerary.getTitle())
        .description(itinerary.getDescription())
        .timestamp(itinerary.getTimestamp().toInstant())
        .image(itinerary.getImage())
        .build();
  }

  public static List<CreateTripRequestItineraryInner> convertToApiModel(
      List<Itinerary> itineraryList) {
    return itineraryList.stream().map(TripModelConverter::convert).toList();
  }

  public static CreateTripRequestItineraryInner convert(Itinerary itinerary) {
    return new CreateTripRequestItineraryInner()
        .title(itinerary.getTitle())
        .description(itinerary.getDescription())
        .timestamp(itinerary.getTimestamp().atOffset(ZoneOffset.UTC))
        .image(itinerary.getImage());
  }
}
