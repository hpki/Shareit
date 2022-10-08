package ru.practicum.shareit.booking.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class BookingDto {
    private long id;
    private long itemId;
    private long bookerId;
    private LocalDateTime start;
    private LocalDateTime end;

    public BookingDto(long id, long itemId, long bookerId, LocalDateTime start, LocalDateTime end) {
        this.id = id;
        this.itemId = itemId;
        this.bookerId = bookerId;
        this.start = start;
        this.end = end;
    }
}
