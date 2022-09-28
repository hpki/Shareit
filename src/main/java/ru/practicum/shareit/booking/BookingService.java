package ru.practicum.shareit.booking;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

public interface BookingService {
    Booking addBooking(long userId, BookingDto bookingDto);

    Booking setApproved(long userId, long bookingId, boolean approved);

    Booking getBookingById(long userId, long bookingId);

    List<Booking> getAll(long userId, String state, Pageable pageable);

    List<Booking> getAllBookingsByOwner(long userId, String state, Pageable pageable);
}
