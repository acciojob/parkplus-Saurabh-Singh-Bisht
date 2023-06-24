package com.driver.services.impl;

import com.driver.model.*;
import com.driver.repository.ParkingLotRepository;
import com.driver.repository.ReservationRepository;
import com.driver.repository.SpotRepository;
import com.driver.repository.UserRepository;
import com.driver.services.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReservationServiceImpl implements ReservationService {
    @Autowired
    UserRepository userRepository3;
    @Autowired
    SpotRepository spotRepository3;
    @Autowired
    ReservationRepository reservationRepository3;
    @Autowired
    ParkingLotRepository parkingLotRepository3;
    @Override
    public Reservation reserveSpot(Integer userId, Integer parkingLotId, Integer timeInHours, Integer numberOfWheels) throws Exception {
        //Reserve a spot in the given parkingLot such that the total price is minimum.
        // Note that the price per hour for each spot is different
        //Note that the vehicle can only be parked in a spot having
        // a type equal to or larger than given vehicle
        //If parkingLot is not found, user is not found, or no spot is available,
        // throw "Cannot make reservation" exception.
        try {
            if(!userRepository3.findById(userId).isPresent() ||
            !parkingLotRepository3.findById(parkingLotId).isPresent()){
                throw new Exception("Cannot make reservation");
            }
            User user = userRepository3.findById(userId).get();
            ParkingLot parkingLot = parkingLotRepository3.findById(parkingLotId).get();
            List<Spot> spotList = parkingLot.getSpotList();

            boolean checkSpot = false;
            for (Spot spot: spotList){
                if(spot.getOccupied() == false){
                    checkSpot =true;
                    break;
                }
            }
            if(!checkSpot){
                throw new Exception("Cannot make reservation");
            }
            SpotType reqSpotType;
            if(numberOfWheels > 4){
                reqSpotType = SpotType.OTHERS;
            }
            else if(numberOfWheels > 2){
                reqSpotType = SpotType.FOUR_WHEELER;
            }
            else {
                reqSpotType = SpotType.TWO_WHEELER;
            }

            int minPrice = Integer.MAX_VALUE;
            checkSpot = false;
            Spot minPriceSpot = null;
            for (Spot spot:spotList){
                if (reqSpotType.equals(SpotType.OTHERS) && spot.getSpotType().equals(SpotType.OTHERS)) {
                    if(!spot.getOccupied() && spot.getPricePerHour()*timeInHours < minPrice){
                        checkSpot = true;
                        minPrice = spot.getPricePerHour() * timeInHours;
                        minPriceSpot = spot;
                    }
                } else if (reqSpotType.equals(SpotType.FOUR_WHEELER) && (spot.getSpotType().equals(SpotType.FOUR_WHEELER) || spot.getSpotType().equals(SpotType.OTHERS))) {
                    if(!spot.getOccupied() && spot.getPricePerHour()*timeInHours < minPrice){
                        checkSpot = true;
                        minPrice = spot.getPricePerHour()*timeInHours;
                        minPriceSpot = spot;
                    }
                } else if (reqSpotType.equals(SpotType.TWO_WHEELER) && (spot.getSpotType().equals(SpotType.TWO_WHEELER)
                        || spot.getSpotType().equals(SpotType.FOUR_WHEELER)
                        || spot.getSpotType().equals(SpotType.OTHERS))) {
                    if(!spot.getOccupied() && spot.getPricePerHour()*timeInHours < minPrice){
                        checkSpot = true;
                        minPrice = spot.getPricePerHour()*timeInHours;
                        minPriceSpot = spot;
                    }
                }
            }
            if (!checkSpot){
                throw new Exception("Cannot make reservation");
            }
            minPriceSpot.setOccupied(Boolean.TRUE);

            Reservation reservation = new Reservation();
            reservation.setSpot(minPriceSpot);
            reservation.setUser(user);
            reservation.setNumberOfHours(timeInHours);

            reservationRepository3.save(reservation);

            minPriceSpot.getReservationList().add(reservation);
            user.getReservationList().add(reservation);

            spotRepository3.save(minPriceSpot);
            userRepository3.save(user);

            return reservation;
        }
        catch (Exception ex){
            return null;
        }
    }
}
