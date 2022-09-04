package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;
import java.util.stream.Collectors;

public class BookingMapper {
    public static BookingDto toBookingDto(Booking booking) {
        return new BookingDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getStatus(),
                new BookingDto.User(booking.getBooker().getId(),
                        booking.getBooker().getName()),
                new BookingDto.Item(booking.getItem().getId(),
                        booking.getItem().getName())
        );
    }

    public static List<BookingDto> toBookingDtos(List<Booking> bookings) {
        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }
}