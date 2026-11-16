package com.demo.resortslite;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import jakarta.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingControllerTest {

    @Mock
    private BookingService bookingService;

    @Mock
    private HttpSession session;

    @InjectMocks
    private BookingController bookingController;

    private Map<String, Object> sampleBooking;

    @BeforeEach
    void setUp() {
        sampleBooking = new HashMap<>();
        sampleBooking.put("bookingId", "BK-ABCD1234");
        sampleBooking.put("guestName", "Alice");
        sampleBooking.put("roomType", "SUITE");
        sampleBooking.put("checkIn", "2024-06-01");
        sampleBooking.put("checkOut", "2024-06-05");
        sampleBooking.put("confirmationCode", "abc123def456");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // createBooking tests
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void createBooking_withValidParams_returnsConfirmedStatus() {
        // Arrange
        when(bookingService.createBooking(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(sampleBooking);
        doNothing().when(session).setAttribute(anyString(), any());

        // Act
        Map<String, Object> response = bookingController.createBooking(
                "Alice", "SUITE", "2024-06-01", "2024-06-05", session);

        // Assert
        assertNotNull(response);
        assertEquals("confirmed", response.get("status"));
    }

    @Test
    void createBooking_withValidParams_returnsBookingInResponse() {
        // Arrange
        when(bookingService.createBooking(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(sampleBooking);
        doNothing().when(session).setAttribute(anyString(), any());

        // Act
        Map<String, Object> response = bookingController.createBooking(
                "Alice", "SUITE", "2024-06-01", "2024-06-05", session);

        // Assert
        assertNotNull(response.get("booking"));
        assertEquals(sampleBooking, response.get("booking"));
    }

    @Test
    void createBooking_setsSessionAttributes() {
        // Arrange
        when(bookingService.createBooking(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(sampleBooking);
        doNothing().when(session).setAttribute(anyString(), any());

        // Act
        bookingController.createBooking("Alice", "SUITE", "2024-06-01", "2024-06-05", session);

        // Assert
        verify(session, times(1)).setAttribute(eq("lastBooking"), eq(sampleBooking));
        verify(session, times(1)).setAttribute(eq("guestName"), eq("Alice"));
    }

    @Test
    void createBooking_callsBookingServiceCreateBooking() {
        // Arrange
        when(bookingService.createBooking("Bob", "DELUXE", "2024-07-01", "2024-07-03"))
                .thenReturn(sampleBooking);
        doNothing().when(session).setAttribute(anyString(), any());

        // Act
        bookingController.createBooking("Bob", "DELUXE", "2024-07-01", "2024-07-03", session);

        // Assert
        verify(bookingService, times(1))
                .createBooking("Bob", "DELUXE", "2024-07-01", "2024-07-03");
    }

    @Test
    void createBooking_responseContainsTwoKeys() {
        // Arrange
        when(bookingService.createBooking(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(sampleBooking);
        doNothing().when(session).setAttribute(anyString(), any());

        // Act
        Map<String, Object> response = bookingController.createBooking(
                "Carol", "VILLA", "2024-08-01", "2024-08-10", session);

        // Assert
        assertEquals(2, response.size());
        assertTrue(response.containsKey("status"));
        assertTrue(response.containsKey("booking"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getBookingStatus tests
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void getBookingStatus_withValidBookingId_returnsResultMap() {
        // Arrange
        when(session.getAttribute("guestName")).thenReturn("Alice");
        when(bookingService.getBookingById("BK-ABCD1234")).thenReturn(sampleBooking);

        // Act
        Map<String, Object> result = bookingController.getBookingStatus("BK-ABCD1234", session);

        // Assert
        assertNotNull(result);
        assertEquals("BK-ABCD1234", result.get("bookingId"));
    }

    @Test
    void getBookingStatus_returnsSessionGuestName() {
        // Arrange
        when(session.getAttribute("guestName")).thenReturn("Alice");
        when(bookingService.getBookingById(anyString())).thenReturn(sampleBooking);

        // Act
        Map<String, Object> result = bookingController.getBookingStatus("BK-ABCD1234", session);

        // Assert
        assertEquals("Alice", result.get("sessionGuest"));
    }

    @Test
    void getBookingStatus_whenSessionGuestIsNull_returnsNullSessionGuest() {
        // Arrange
        when(session.getAttribute("guestName")).thenReturn(null);
        when(bookingService.getBookingById(anyString())).thenReturn(sampleBooking);

        // Act
        Map<String, Object> result = bookingController.getBookingStatus("BK-ABCD1234", session);

        // Assert
        assertNull(result.get("sessionGuest"));
    }

    @Test
    void getBookingStatus_returnsDetailsFromService() {
        // Arrange
        when(session.getAttribute("guestName")).thenReturn("Bob");
        when(bookingService.getBookingById("BK-XYZ99999")).thenReturn(sampleBooking);

        // Act
        Map<String, Object> result = bookingController.getBookingStatus("BK-XYZ99999", session);

        // Assert
        assertEquals(sampleBooking, result.get("details"));
    }

    @Test
    void getBookingStatus_resultContainsThreeKeys() {
        // Arrange
        when(session.getAttribute("guestName")).thenReturn("Dave");
        when(bookingService.getBookingById(anyString())).thenReturn(sampleBooking);

        // Act
        Map<String, Object> result = bookingController.getBookingStatus("BK-ABCD1234", session);

        // Assert
        assertEquals(3, result.size());
        assertTrue(result.containsKey("bookingId"));
        assertTrue(result.containsKey("sessionGuest"));
        assertTrue(result.containsKey("details"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // checkAvailability tests
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void checkAvailability_withValidRoomType_returnsResponseMap() {
        // Arrange
        when(bookingService.isRoomAvailable("SUITE")).thenReturn(true);

        // Act
        Map<String, Object> response = bookingController.checkAvailability("SUITE");

        // Assert
        assertNotNull(response);
        assertEquals("SUITE", response.get("roomType"));
    }

    @Test
    void checkAvailability_whenRoomAvailable_returnsTrue() {
        // Arrange
        when(bookingService.isRoomAvailable("DELUXE")).thenReturn(true);

        // Act
        Map<String, Object> response = bookingController.checkAvailability("DELUXE");

        // Assert
        assertTrue((Boolean) response.get("available"));
    }

    @Test
    void checkAvailability_whenRoomNotAvailable_returnsFalse() {
        // Arrange
        when(bookingService.isRoomAvailable("UNKNOWN")).thenReturn(false);

        // Act
        Map<String, Object> response = bookingController.checkAvailability("UNKNOWN");

        // Assert
        assertFalse((Boolean) response.get("available"));
    }

    @Test
    void checkAvailability_responseContainsInventoryEndpoint() {
        // Arrange
        when(bookingService.isRoomAvailable(anyString())).thenReturn(true);

        // Act
        Map<String, Object> response = bookingController.checkAvailability("STANDARD");

        // Assert
        assertNotNull(response.get("inventoryEndpoint"));
        assertTrue(((String) response.get("inventoryEndpoint")).contains("inventory-service"));
    }

    @Test
    void checkAvailability_callsIsRoomAvailableOnService() {
        // Arrange
        when(bookingService.isRoomAvailable("VILLA")).thenReturn(true);

        // Act
        bookingController.checkAvailability("VILLA");

        // Assert
        verify(bookingService, times(1)).isRoomAvailable("VILLA");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // downloadReport tests
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void downloadReport_withValidMonth_returnsResponseMap() {
        // Arrange
        when(bookingService.generateReport("March")).thenReturn("Report generated for March");

        // Act
        Map<String, Object> response = bookingController.downloadReport("March");

        // Assert
        assertNotNull(response);
    }

    @Test
    void downloadReport_responseContainsReportPath() {
        // Arrange
        when(bookingService.generateReport(anyString())).thenReturn("Report generated");

        // Act
        Map<String, Object> response = bookingController.downloadReport("June");

        // Assert
        assertNotNull(response.get("reportPath"));
        assertTrue(((String) response.get("reportPath")).contains("June"));
    }

    @Test
    void downloadReport_reportPathContainsPdfExtension() {
        // Arrange
        when(bookingService.generateReport(anyString())).thenReturn("Report generated");

        // Act
        Map<String, Object> response = bookingController.downloadReport("December");

        // Assert
        String path = (String) response.get("reportPath");
        assertTrue(path.endsWith(".pdf"));
    }

    @Test
    void downloadReport_responseContainsMessage() {
        // Arrange
        String expectedMessage = "Report generation triggered for: July";
        when(bookingService.generateReport("July")).thenReturn(expectedMessage);

        // Act
        Map<String, Object> response = bookingController.downloadReport("July");

        // Assert
        assertEquals(expectedMessage, response.get("message"));
    }

    @Test
    void downloadReport_callsGenerateReportOnService() {
        // Arrange
        when(bookingService.generateReport("April")).thenReturn("done");

        // Act
        bookingController.downloadReport("April");

        // Assert
        verify(bookingService, times(1)).generateReport("April");
    }
}
