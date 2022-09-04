package ru.practicum.shareit.booking;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exeptions.BookingUnsupportedTypeException;
import ru.practicum.shareit.exeptions.ItemIsNotAvailableException;

import java.nio.file.AccessDeniedException;
import java.rmi.AccessException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public Booking addBooking(@RequestBody BookingRequest bookingRequest,
                              @RequestHeader(value = "X-Sharer-User-Id") long userId)
            throws ItemIsNotAvailableException, NoSuchElementException, IllegalArgumentException, AccessException {
        return bookingService.addBooking(bookingRequest, userId);
    }

    @PatchMapping("/{bookingId}")
    public Booking setApproved(@PathVariable long bookingId,
                               @RequestHeader(value = "X-Sharer-User-Id") long userId,
                               @RequestParam boolean approved)
            throws AccessDeniedException {
        return bookingService.setApproved(bookingId, userId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBookingById(@PathVariable long bookingId,
                                     @RequestHeader(value = "X-Sharer-User-Id") long userId)
            throws NoSuchElementException, AccessDeniedException {
        return BookingMapper.toBookingDto(bookingService.getBookingById(bookingId, userId));
    }

    @GetMapping
    public List<BookingDto> getAllForUSer(@RequestHeader(value = "X-Sharer-User-Id") long userId,
                                          @RequestParam(required = false, defaultValue = "ALL") String state)
            throws BookingUnsupportedTypeException {
        return bookingService.getAllForUser(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingDto> getBookingsByOwner(
            @RequestParam(value = "state", required = false, defaultValue = "ALL") String state,
            @RequestHeader("X-Sharer-User-Id") Long userId) throws BookingUnsupportedTypeException {
        return bookingService.getBookingsByOwner(userId, state);
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handleNoSuchElement(final NoSuchElementException e) {
        return new ResponseEntity<>(
                Map.of("message", e.getMessage()),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handleItemIsNotAvailable(final ItemIsNotAvailableException e) {
        return new ResponseEntity<>(
                Map.of("message", e.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handleIllegalArgument(final IllegalArgumentException e) {
        return new ResponseEntity<>(
                Map.of("message", e.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handleAccessDenied(final AccessDeniedException e) {
        return new ResponseEntity<>(
                Map.of("message", e.getMessage()),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handleAccess(final AccessException e) {
        return new ResponseEntity<>(
                Map.of("message", e.getMessage()),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handleAccess(final BookingUnsupportedTypeException e) {
        return new ResponseEntity<>(
                Map.of("error", e.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }
}