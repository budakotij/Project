package com.company.api.controller;

import com.company.api.generated.TripApi;
import com.company.api.generated.model.CreateTripRequest;
import com.company.api.generated.model.TripModel;
import com.company.api.service.TripService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
@AllArgsConstructor
public class TripController implements TripApi {

  private final TripService tripService;

  @Override
  public ResponseEntity<TripModel> createTrip(CreateTripRequest createTripRequest) {
    String systemUsername = System.getProperty("user.name");
    System.out.println("System User: " + systemUsername);
    TripModel newTrip = tripService.createTripAndGet(createTripRequest);
    return new ResponseEntity<>(newTrip, HttpStatus.CREATED);
  }

  @Override
  public ResponseEntity<TripModel> getTrip(String id) {
    TripModel trip = tripService.getTripById(id);
    return ResponseEntity.ok(trip);
  }
}
