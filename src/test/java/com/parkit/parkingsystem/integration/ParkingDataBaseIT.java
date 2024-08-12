package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.apache.logging.log4j.core.config.Order;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    public static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();

    }

    @BeforeEach
    public void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    public static void tearDown(){

    }

    @Test
    public void testParkingACar(){

        //GIVEN
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);


        //WHEN
        parkingService.processIncomingVehicle();
        //TODO: check that a ticket is actualy saved in DB and Parking table is updated with availability

        //THEN
        Ticket resultTicket  = ticketDAO.getTicket("ABCDEF");

        assertEquals(1, resultTicket.getId());
        assertEquals(1, resultTicket.getParkingSpot().getId());
        assertEquals(0, resultTicket.getPrice());
        assertEquals(null,resultTicket.getOutTime());

    }

    @Test
    public void testParkingLotExit() throws InterruptedException, Exception {
        //GIVEN
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        parkingService.processIncomingVehicle();

        Thread.sleep(3000);
        //WHEN
        parkingService.processExitingVehicle();

        //TODO: check that the fare generated and out time are populated correctly in the database
        // THEN
        Ticket resultTicket = ticketDAO.getTicket("ABCDEF");

        assertEquals(1, resultTicket.getId());
        assertEquals(1, resultTicket.getParkingSpot().getId());
        assertEquals(0, resultTicket.getPrice());
        assertNotNull(resultTicket.getOutTime());
    }

   @Test
    public void testParkingLotExitRecurringUser() throws InterruptedException, Exception {

        LocalDateTime indateTime = LocalDateTime.of(2019, 8, 5, 14, 0);
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime inZonedDateTime = indateTime.atZone(zoneId);
        Timestamp intimestamp = Timestamp.from(inZonedDateTime.toInstant());

        LocalDateTime outDateTime = LocalDateTime.of(2019, 8, 5, 16, 0);
        ZonedDateTime outZonedDateTime = outDateTime.atZone(zoneId);
        Timestamp outDtimestamp = Timestamp.from(outZonedDateTime.toInstant());

        Ticket ancientTicket = new Ticket();
        ancientTicket.setId(1);
        ancientTicket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, false));
        ancientTicket.setVehicleRegNumber("ABCDEF");
        ancientTicket.setInTime(intimestamp);
        ancientTicket.setOutTime(outDtimestamp);
        ancientTicket.setPrice(62620.19999999999);


        ticketDAO.saveTicket(ancientTicket);
        /*ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);
        parkingSpotDAO.updateParking(parkingSpot);*/

     //   ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        parkingService.processIncomingVehicle();

        Thread.sleep(3000);
        //WHEN
        parkingService.processExitingVehicle();

        //TODO: check that the fare generated and out time are populated correctly in the database
        // THEN
        Ticket resultTicket = ticketDAO.getTicket("ABCDEF");

        assertEquals(2, resultTicket.getId());
        assertEquals(1, resultTicket.getParkingSpot().getId());
        assertEquals(0, resultTicket.getPrice());
        assertNotNull(resultTicket.getOutTime());




    }
}
