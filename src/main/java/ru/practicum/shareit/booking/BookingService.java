package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exeptions.BookingUnsupportedTypeException;
import ru.practicum.shareit.exeptions.ItemIsNotAvailableException;

import java.nio.file.AccessDeniedException;
import java.rmi.AccessException;
import java.util.List;
import java.util.NoSuchElementException;

public interface BookingService {
    Booking getBookingById(long bookingId, long userId) throws AccessDeniedException;

    Booking addBooking(BookingRequest bookingRequest, long userId)
            throws NoSuchElementException, ItemIsNotAvailableException, IllegalArgumentException, AccessException;

    Booking setApproved(long bookingId, long userId, boolean isApproved) throws AccessDeniedException;

    List<BookingDto> getAllForUser(long userId, String state)
            throws NoSuchElementException, BookingUnsupportedTypeException;

    List<BookingDto> getBookingsByOwner(Long userId, String state) throws BookingUnsupportedTypeException;

    List<Booking> checkBookingsForItem(long itemId, long userId);
}
