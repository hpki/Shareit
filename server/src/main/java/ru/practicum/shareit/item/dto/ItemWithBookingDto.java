package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.booking.dto.BookingDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@AllArgsConstructor
@Data
public class ItemWithBookingDto {
    private long id;
    private String name;
    private String description;
    private Boolean available;
    private BookingDto nextBooking;
    private BookingDto lastBooking;
    private List<CommentDto> comments;
}