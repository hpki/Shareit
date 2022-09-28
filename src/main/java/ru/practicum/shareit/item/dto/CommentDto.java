package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@AllArgsConstructor
@Data
public class CommentDto {
    private long id;
    @NotNull
    @NotBlank
    private String text;
    private String authorName;
    private LocalDateTime created;
}
