package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
public class BookingDto {
    private LocalDate start;
    private LocalDate end;
    private Item item;

    public static class Item {
        private long id;
        private String name;

        public Item(long id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}
