package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {
    private long id;
    @NotNull
    @NotEmpty
    private String name;
    @NotNull
    @NotEmpty
    private String description;
    @NotNull
    private Boolean available;
    private Booking lastBooking;
    private Booking nextBooking;
    private List<Comment> comments;
    private User owner;

    public ItemDto(Long id, String name, String description, boolean available, User owner,
                   List<Comment> comments) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.available = available;
        this.owner = owner;
        this.comments = comments;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Booking {
        private Long id;
        private Long bookerId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class User {
        private Long id;
        private String name;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Comment {
        private Long id;
        private String text;
        private String authorName;
        private LocalDateTime created;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        private Long id;
    }
}
