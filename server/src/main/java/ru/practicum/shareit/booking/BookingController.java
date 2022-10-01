package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.pageable.OffsetLimitPageable;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bookings")
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public Booking saveNewBooking(@RequestHeader("X-Sharer-User-Id") long userId,
                                  @RequestBody @Valid BookingDto bookingDto) {
        return bookingService.createBooking(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public Booking updateExistingBooking(@RequestHeader("X-Sharer-User-Id") long userId,
                                         @PathVariable long bookingId,
                                         @RequestParam boolean approved) {
        return bookingService.updateBooking(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public Booking getBookingById(@RequestHeader("X-Sharer-User-Id") long userId,
                                  @PathVariable long bookingId) {
        return bookingService.getBooking(userId, bookingId);
    }

    @GetMapping
    public List<Booking> getAllBookings(@RequestHeader("X-Sharer-User-Id") long userId,
                                        @RequestParam(required = false, defaultValue = "ALL") String state,
                                        @RequestParam(required = false, defaultValue = "0") long from,
                                        @RequestParam(required = false, defaultValue = "20") long size) {
        return bookingService.getAllBookings(userId, state, OffsetLimitPageable.of((int) from, (int) size));
    }

    @GetMapping("/owner")
    public List<Booking> getAllBookingsForOwner(@RequestHeader("X-Sharer-User-Id") long userId,
                                                @RequestParam(required = false, defaultValue = "ALL") String state,
                                                @RequestParam(required = false, defaultValue = "0") long from,
                                                @RequestParam(required = false, defaultValue = "20") long size) {
        return bookingService.getAllBookingsForOwner(userId, state, OffsetLimitPageable.of((int) from, (int) size));
    }
}
