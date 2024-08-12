package com.parkit.parkingsystem.dao;


import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.*;
import java.util.Date;

import static junit.framework.Assert.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class TicketDAOTest {

    @InjectMocks
    private TicketDAO ticketDAO;

    @Mock
    private DataBaseConfig dataBaseConfig;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;

    private Ticket ticket;

    @BeforeEach
    public void setUp() throws Exception {
        // Initialize a Ticket object
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);
        ticket = new Ticket();
        ticket.setId(1);
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");
        ticket.setPrice(10.0);
        ticket.setInTime(new java.util.Date());
        ticket.setOutTime(new java.util.Date());

        // Mock the behavior of DataBaseConfig to return a mocked connection
        when(dataBaseConfig.getConnection()).thenReturn(connection);

        // Mock the behavior of PreparedStatement
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);


    }

    @Test
    public void testSaveTicket() throws SQLException {
        //GIVEN
        when(preparedStatement.executeUpdate()).thenReturn(1);

        //WHEN
        boolean result = ticketDAO.saveTicket(ticket);

        //THEN
        verify(preparedStatement, times(1)).executeUpdate();
        assertTrue(result);
    }


    @Test
    void testGetTicket() throws SQLException {

        //GIVEN
        String vehicleRegNumber = "ABC123";
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        doReturn(1).when(resultSet).getInt(1); // PARKING_NUMBER
        doReturn(123).when(resultSet).getInt(2); // ID
        doReturn(15.0).when(resultSet).getDouble(3); // PRICE
        doReturn(new Timestamp(System.currentTimeMillis())).when(resultSet).getTimestamp(4); // IN_TIME
        doReturn(null).when(resultSet).getTimestamp(5); // OUT_TIME
        doReturn("CAR").when(resultSet).getString(6); // TYPE

        //WHEN
        Ticket ticketTest = ticketDAO.getTicket(vehicleRegNumber);

        //THEN
        assertNotNull(ticketTest);
        assertEquals(vehicleRegNumber, ticketTest.getVehicleRegNumber());
        assertEquals(1, ticketTest.getParkingSpot().getId());
        assertEquals(ParkingType.CAR, ticketTest.getParkingSpot().getParkingType());

    }

    @Test
    public void testGetTicketNoResult() throws Exception {

        //GIVEN
        String vehicleRegNumber = "ABC123";
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        //WHEN
        Ticket ticket = ticketDAO.getTicket(vehicleRegNumber);

        //THEN
        assertNull(ticket);
    }

}