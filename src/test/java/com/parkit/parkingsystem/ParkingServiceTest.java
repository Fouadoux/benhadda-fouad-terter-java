package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.service.UpdateTicketException;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import java.util.Date;

import static junit.framework.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    private static ParkingService parkingService;

    @Mock
    public static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;







    @BeforeEach
    public void setUpPerTest() {


        try {
               lenient().when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

           /*ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
            Ticket ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber("ABCDEF");
            when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
            when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);*/


            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }

    @Test
    public void processExitingVehicleTest() throws Exception{

        //GIVEN

        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");
        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
        when(ticketDAO.getNbTicket(anyString())).thenReturn(false);
       // parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        //WHEN
        parkingService.processExitingVehicle();

        //THEN
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
    }

    @Test
    public void processIncomingVehicle() {
        //GIVEN

        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
        when(inputReaderUtil.readSelection()).thenReturn(1);
       // parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        //WHEN

        parkingService.processIncomingVehicle();

        //THEN

        verify(ticketDAO).saveTicket(any(Ticket.class));
        verify(parkingSpotDAO).getNextAvailableSlot(ParkingType.CAR);

    }


    @Test
    public void processExitingVehicleTestUnableUpdate() throws UpdateTicketException{
        //GIVEN

        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);
        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);

        //WHEN & THEN
        Exception thrown = assertThrows(UpdateTicketException.class, () -> parkingService.processExitingVehicle());
        assertTrue(thrown.getMessage().contains("Unable to update ticket information. Error occurred"));
    }



   @Test
    public void getNextParkingNumberIfAvailable() throws Exception {
       //GIVEN
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
        ParkingSpot parkingSpot =new ParkingSpot(1,ParkingType.CAR,true);
       //WHEN
       parkingService.getNextParkingNumberIfAvailable();

       //THEN
       assertEquals(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR),1);
       assertEquals(parkingSpot.isAvailable(), true);
    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() {

        //GIVEN
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(-1);
        ParkingSpot parkingSpot =new ParkingSpot(1,ParkingType.CAR,false);

        //WHEN & THEN
        Exception thrown = assertThrows(Exception.class, () -> parkingService.getNextParkingNumberIfAvailable());
        assertTrue(thrown.getMessage().contains("Error fetching parking number from DB. Parking slots might be full"));

    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument()  {
        //GIVEN
        when(inputReaderUtil.readSelection()).thenReturn(3);

        //WHEN & THEN

        Exception thrown = assertThrows(IllegalArgumentException.class, () -> parkingService.getNextParkingNumberIfAvailable());
        assertTrue(thrown.getMessage().contains("Entered input is invalid"));

    }

}


