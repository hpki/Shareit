package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.pageable.OffsetLimitPageable;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bookings")
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public Booking addBooking(@RequestHeader("X-Sharer-User-Id") long userId,
                                  @RequestBody @Valid BookingDto bookingDto) {
        return bookingService.addBooking(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public Booking editBooking(@RequestHeader("X-Sharer-User-Id") long userId,
                                         @PathVariable long bookingId,
                                         @RequestParam boolean approved) {
        return bookingService.setApproved(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public Booking getBookingById(@RequestHeader("X-Sharer-User-Id") long userId,
                                  @PathVariable long bookingId) {
        return bookingService.getBookingById(userId, bookingId);
    }

    @GetMapping
    public List<Booking> getAllBookings(@RequestHeader("X-Sharer-User-Id") long userId,
                                        @RequestParam(required = false, defaultValue = "ALL") String state,
                                        @RequestParam(required = false, defaultValue = "0") long from,
                                        @RequestParam(required = false, defaultValue = "20") long size) {
        return bookingService.getAll(userId, state, OffsetLimitPageable.of((int) from, (int) size));
    }

    @GetMapping("/owner")
    public List<Booking> getAllBookingsByOwner(@RequestHeader("X-Sharer-User-Id") long userId,
                                                @RequestParam(required = false, defaultValue = "ALL") String state,
                                                @RequestParam(required = false, defaultValue = "0") long from,
                                                @RequestParam(required = false, defaultValue = "20") long size) {
        return bookingService.getAllBookingsByOwner(userId, state, OffsetLimitPageable.of((int) from, (int) size));
    }
}
