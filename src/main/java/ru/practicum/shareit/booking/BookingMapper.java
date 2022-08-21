package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;

public class BookingMapper {
    public static BookingDto toBookingDto(Booking booking) {
        ItemDto itemDto = ItemMapper.toItemDto(booking.getItem());
        return new BookingDto(
                booking.getStart(),
                booking.getEnd(),
                new BookingDto.Item(itemDto.getId(), itemDto.getName())
        );
    }
}
