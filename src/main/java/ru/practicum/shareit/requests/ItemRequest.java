package ru.practicum.shareit.requests;

import lombok.Data;
import ru.practicum.shareit.user.User;

import java.time.LocalDate;

@Data
public class ItemRequest {
    private long id;
    private String description;
    private User requestor;
    private LocalDate created;
}
