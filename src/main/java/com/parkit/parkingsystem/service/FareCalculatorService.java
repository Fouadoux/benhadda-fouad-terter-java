package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

/**
 * class for caculate the price tikcet
 */
public class FareCalculatorService {


    public void calculateFare(Ticket ticket, boolean discount) {
        if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
            throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
        }

        double inHour = ticket.getInTime().getTime() / (1000 * 60 * 60);
        double outHour = ticket.getOutTime().getTime() / (1000 * 60 * 60);
        double inMinute = ticket.getInTime().getTime() / (1000 * 60);
        double outMinute = ticket.getOutTime().getTime() / (1000 * 60);
        double duration = 0D;

        if (outMinute - inMinute <= 30) {
            duration = 0;
        } else if (30 < (outMinute - inMinute) && (outMinute - inMinute) < 60) {
            duration = (outMinute - inMinute) / 60;
        } else {
            duration = (outHour - inHour);
        }

        if (discount == false) {
            switch (ticket.getParkingSpot().getParkingType()) {
                case CAR: {
                    ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR);
                    break;
                }
                case BIKE: {
                    ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR);
                    break;
                }
                default:
                    throw new IllegalArgumentException("Unkown Parking Type");
            }
        } else {
            switch (ticket.getParkingSpot().getParkingType()) {
                case CAR: {
                    ticket.setPrice(duration * Fare.CAR_DISCOUNT_RATE_PER_HOUR);
                    break;
                }
                case BIKE: {
                    ticket.setPrice(duration * Fare.BIKE_DISCOUNT_RATE_PER_HOUR);
                    break;
                }
                default:
                    throw new IllegalArgumentException("Unkown Parking Type");
            }
        }
    }
}