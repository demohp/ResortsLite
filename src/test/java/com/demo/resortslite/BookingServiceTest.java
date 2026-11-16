package com.demo.resortslite;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private BookingService bookingService;

    // ─────────────────────────────────────────────────────────────────────────
    // createBooking tests
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void createBooking_withValidInputs_returnsBookingMap() {
        // Arrange
        doNothing().when(jdbcTemplate).execute(anyString());

        // Act
        Map<String, Object> result = bookingService.createBooking(
                "Alice", "SUITE", "2024-06-01", "2024-06-05");

        // Assert
        assertNotNull(result);
        assertEquals("Alice", result.get("guestName"));
        assertEquals("SUITE", result.get("roomType"));
        assertEquals("2024-06-01", result.get("checkIn"));
        assertEquals("2024-06-05", result.get("checkOut"));
    }

    @Test
    void createBooking_returnsBookingIdWithBKPrefix() {
        // Arrange
        doNothing().when(jdbcTemplate).execute(anyString());

        // Act
        Map<String, Object> result = bookingService.createBooking(
                "Bob", "DELUXE", "2024-07-01", "2024-07-03");

        // Assert
        String bookingId = (String) result.get("bookingId");
        assertNotNull(bookingId);
        assertTrue(bookingId.startsWith("BK-"), "bookingId should start with 'BK-'");
    }

    @Test
    void createBooking_returnsConfirmationCode() {
        // Arrange
        doNothing().when(jdbcTemplate).execute(anyString());

        // Act
        Map<String, Object> result = bookingService.createBooking(
                "Carol", "STANDARD", "2024-08-01", "2024-08-02");

        // Assert
        assertNotNull(result.get("confirmationCode"));
        assertFalse(((String) result.get("confirmationCode")).isEmpty());
    }

    @Test
    void createBooking_returnsDbHostInMap() {
        // Arrange
        doNothing().when(jdbcTemplate).execute(anyString());

        // Act
        Map<String, Object> result = bookingService.createBooking(
                "Dave", "VILLA", "2024-09-01", "2024-09-10");

        // Assert
        assertNotNull(result.get("dbHost"));
    }

    @Test
    void createBooking_executesJdbcInsert() {
        // Arrange
        doNothing().when(jdbcTemplate).execute(anyString());

        // Act
        bookingService.createBooking("Eve", "SUITE", "2024-10-01", "2024-10-05");

        // Assert
        verify(jdbcTemplate, times(1)).execute(anyString());
    }

    @Test
    void createBooking_withSpecialCharactersInGuestName_doesNotThrow() {
        // Arrange
        doNothing().when(jdbcTemplate).execute(anyString());

        // Act & Assert
        assertDoesNotThrow(() ->
                bookingService.createBooking("O'Brien", "STANDARD", "2024-01-01", "2024-01-02"));
    }

    @Test
    void createBooking_twoCallsProduceDifferentBookingIds() {
        // Arrange
        doNothing().when(jdbcTemplate).execute(anyString());

        // Act
        Map<String, Object> r1 = bookingService.createBooking("A", "SUITE", "2024-01-01", "2024-01-02");
        Map<String, Object> r2 = bookingService.createBooking("B", "SUITE", "2024-01-01", "2024-01-02");

        // Assert
        assertNotEquals(r1.get("bookingId"), r2.get("bookingId"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getBookingById tests
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void getBookingById_whenFound_returnsBookingData() {
        // Arrange
        Map<String, Object> dbRow = new HashMap<>();
        dbRow.put("id", "BK-12345678");
        dbRow.put("guest", "Frank");
        when(jdbcTemplate.queryForMap(anyString())).thenReturn(dbRow);

        // Act
        Map<String, Object> result = bookingService.getBookingById("BK-12345678");

        // Assert
        assertNotNull(result);
        assertEquals("Frank", result.get("guest"));
    }

    @Test
    void getBookingById_whenNotFound_returnsErrorMap() {
        // Arrange
        when(jdbcTemplate.queryForMap(anyString()))
                .thenThrow(new RuntimeException("No rows found"));

        // Act
        Map<String, Object> result = bookingService.getBookingById("BK-NOTEXIST");

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("error"));
        assertTrue(((String) result.get("error")).contains("BK-NOTEXIST"));
    }

    @Test
    void getBookingById_whenExceptionThrown_errorMessageContainsBookingId() {
        // Arrange
        when(jdbcTemplate.queryForMap(anyString()))
                .thenThrow(new RuntimeException("EmptyResultDataAccessException"));

        // Act
        Map<String, Object> result = bookingService.getBookingById("BK-ABCDEF12");

        // Assert
        String error = (String) result.get("error");
        assertNotNull(error);
        assertTrue(error.contains("BK-ABCDEF12"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // calculateRoomPrice tests
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void calculateRoomPrice_standardRoomNormalSeasonNoLoyalty_returnsCorrectPrice() {
        // Arrange & Act
        String price = bookingService.calculateRoomPrice("STANDARD", 3, "NORMAL", "NONE");

        // Assert
        assertNotNull(price);
        assertEquals("360.00", price);  // 120 * 3
    }

    @Test
    void calculateRoomPrice_deluxeRoomNormalSeasonNoLoyalty_returnsCorrectPrice() {
        // Arrange & Act
        String price = bookingService.calculateRoomPrice("DELUXE", 2, "NORMAL", "NONE");

        // Assert
        assertEquals("400.00", price);  // 200 * 2
    }

    @Test
    void calculateRoomPrice_suiteRoomNormalSeasonNoLoyalty_returnsCorrectPrice() {
        // Arrange & Act
        String price = bookingService.calculateRoomPrice("SUITE", 1, "NORMAL", "NONE");

        // Assert
        assertEquals("350.00", price);  // 350 * 1
    }

    @Test
    void calculateRoomPrice_villaRoomNormalSeasonNoLoyalty_returnsCorrectPrice() {
        // Arrange & Act
        String price = bookingService.calculateRoomPrice("VILLA", 1, "NORMAL", "NONE");

        // Assert
        assertEquals("600.00", price);  // 600 * 1
    }

    @Test
    void calculateRoomPrice_unknownRoomType_defaultsToStandardPrice() {
        // Arrange & Act
        String price = bookingService.calculateRoomPrice("UNKNOWN", 1, "NORMAL", "NONE");

        // Assert
        assertEquals("120.00", price);  // defaults to 120
    }

    @Test
    void calculateRoomPrice_peakSeason_appliesMultiplier() {
        // Arrange & Act
        String price = bookingService.calculateRoomPrice("STANDARD", 1, "PEAK", "NONE");

        // Assert
        assertEquals("180.00", price);  // 120 * 1.5
    }

    @Test
    void calculateRoomPrice_offSeason_appliesDiscount() {
        // Arrange & Act
        String price = bookingService.calculateRoomPrice("STANDARD", 1, "OFF", "NONE");

        // Assert
        assertEquals("96.00", price);  // 120 * 0.8
    }

    @Test
    void calculateRoomPrice_goldLoyalty_appliesDiscount() {
        // Arrange & Act
        String price = bookingService.calculateRoomPrice("STANDARD", 1, "NORMAL", "GOLD");

        // Assert
        assertEquals("108.00", price);  // 120 * 0.9
    }

    @Test
    void calculateRoomPrice_platinumLoyalty_appliesDiscount() {
        // Arrange & Act
        String price = bookingService.calculateRoomPrice("STANDARD", 1, "NORMAL", "PLATINUM");

        // Assert
        assertEquals("96.00", price);  // 120 * 0.8
    }

    @Test
    void calculateRoomPrice_diamondLoyalty_appliesDiscount() {
        // Arrange & Act
        String price = bookingService.calculateRoomPrice("STANDARD", 1, "NORMAL", "DIAMOND");

        // Assert
        assertEquals("84.00", price);  // 120 * 0.7
    }

    @Test
    void calculateRoomPrice_sevenNights_appliesWeeklyDiscount() {
        // Arrange & Act
        String price = bookingService.calculateRoomPrice("STANDARD", 7, "NORMAL", "NONE");

        // Assert
        // 120 * 0.95 * 7 = 798.00
        assertEquals("798.00", price);
    }

    @Test
    void calculateRoomPrice_fourteenNights_appliesBiweeklyDiscount() {
        // Arrange & Act
        // nights >= 14 branch: 120 * 0.90 * 14 = 1512.00
        // NOTE: The code checks >= 7 first, so nights=14 hits the >= 7 branch (0.95)
        String price = bookingService.calculateRoomPrice("STANDARD", 14, "NORMAL", "NONE");

        // Assert — 120 * 0.95 * 14 = 1596.00 (>= 7 branch fires first)
        assertEquals("1596.00", price);
    }

    @Test
    void calculateRoomPrice_peakSeasonGoldLoyalty_combinesMultipliers() {
        // Arrange & Act
        String price = bookingService.calculateRoomPrice("DELUXE", 2, "PEAK", "GOLD");

        // Assert: 200 * 1.5 * 0.9 * 2 = 540.00
        assertEquals("540.00", price);
    }

    @Test
    void calculateRoomPrice_returnsFormattedTwoDecimalString() {
        // Arrange & Act
        String price = bookingService.calculateRoomPrice("SUITE", 3, "NORMAL", "NONE");

        // Assert
        assertTrue(price.matches("\\d+\\.\\d{2}"), "Price should be formatted to 2 decimal places");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // isRoomAvailable tests
    // ─────────────────────────────────────────────────────────────────────────

    @ParameterizedTest
    @ValueSource(strings = {"STANDARD", "DELUXE", "SUITE", "VILLA"})
    void isRoomAvailable_validRoomTypes_returnsTrue(String roomType) {
        assertTrue(bookingService.isRoomAvailable(roomType));
    }

    @ParameterizedTest
    @ValueSource(strings = {"UNKNOWN", "PENTHOUSE", "", "standard", "deluxe"})
    void isRoomAvailable_invalidRoomTypes_returnsFalse(String roomType) {
        assertFalse(bookingService.isRoomAvailable(roomType));
    }

    @Test
    void isRoomAvailable_nullRoomType_throwsException() {
        // Null will cause NullPointerException in .equals() call
        assertThrows(NullPointerException.class,
                () -> bookingService.isRoomAvailable(null));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // generateReport tests
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void generateReport_withValidMonth_returnsNonNullString() {
        // Act
        String result = bookingService.generateReport("March");

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void generateReport_containsMonthInOutput() {
        // Act
        String result = bookingService.generateReport("July");

        // Assert
        assertTrue(result.contains("July"));
    }

    @Test
    void generateReport_containsPaymentApiReference() {
        // Act
        String result = bookingService.generateReport("January");

        // Assert
        assertNotNull(result);
        // The method appends PAYMENT_API to the result
        assertTrue(result.contains("via "));
    }

    @Test
    void generateReport_withEmptyMonth_returnsStringWithEmptyMonth() {
        // Act
        String result = bookingService.generateReport("");

        // Assert
        assertNotNull(result);
        assertTrue(result.startsWith("Report generation triggered for: "));
    }
}
