package ru.practicum.shareit.booking;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;

import java.util.List;

public interface BookingService {
    Booking addBooking(long userId, BookingDto bookingDto);

    Booking editBooking(long userId, long bookingId, boolean approved);

    Booking getBooking(long userId, long bookingId);

    List<Booking> getAllBookings(long userId, String state, Pageable pageable);

    List<Booking> getAllBookingsByOwner(long userId, String state, Pageable pageable);
}
