package com.company.api.service;

import com.company.api.exception.InternalException;
import com.company.api.generated.model.CreateTripRequest;
import com.company.api.generated.model.TripModel;
import com.company.api.modelconverter.TripModelConverter;
import com.company.dal.sql.repository.TripRepository;
import com.company.dal.sql.schema.model.Trip;
import java.util.Arrays;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TripService {

  private final TripRepository tripRepository;

  public TripModel createTripAndGet(CreateTripRequest request) {
    Trip newTrip = TripModelConverter.convert(request);
    try {
      tripRepository.insert(newTrip);
    } catch (Exception ex) {
      ex.printStackTrace();
      throw new InternalException("Unable to create trip");
    }
    newTrip = tripRepository.getTripById(newTrip.getId(), List.of(Trip.Fields.ID));
    return TripModelConverter.convert(newTrip);
  }

  public TripModel getTripById(String id) {
    Trip trip = tripRepository.getTripById(id, Arrays.stream(Trip.Fields.values()).toList());
    return TripModelConverter.convert(trip);
  }
}
