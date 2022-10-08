package ru.practicum.shareit.requests.dto;

import lombok.Data;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class ItemRequestDto {
    public ItemRequestDto(long id, String description, LocalDateTime created) {
        this.id = id;
        this.description = description;
        this.created = created;
        this.items = new ArrayList<>();
    }

    private long id;
    @NotNull
    @NotBlank
    private String description;
    private LocalDateTime created;
    private List<ItemDto> items;
}
