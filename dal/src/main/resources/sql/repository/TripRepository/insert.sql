INSERT INTO TRIPS
(
 ID, ORGANISER, TRIP_TAGS, TRIP_STATUS, POSTED_ON,
 PICKUP_ON, DROP_ON, PICKUP_AT, PICKUP_LOCATION,
 DROP_AT, DROP_LOCATION, DESCRIPTION, DESTINATION, DESTINATION_LOCATION,
 ITINERARY, IMAGES, MAXIMUM_OCCUPANCY, AVAILABLE_OCCUPANCY
)
VALUES
(
 :id, :organiser, :tripTags, :tripStatus, :postedOn,
 :pickupOn, :dropOn, :pickupAt,
 ST_GeogFromText(:pickupLocation),
 :dropAt,
 ST_GeogFromText(:dropLocation),
 :description, :destination,
 ST_GeogFromText(:destinationLocation),
 :itinerary::jsonb, :images,
 :maximumOccupancy, :availableOccupancy
)