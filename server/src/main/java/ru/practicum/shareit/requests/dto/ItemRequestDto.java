package ru.practicum.shareit.requests.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.dto.ItemDto;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class ItemRequestDto {
    public ItemRequestDto(long id, String description, LocalDateTime created) {
        this.id = id;
        this.description = description;
        this.created = created;
        this.items = new ArrayList<>();
    }

    private long id;
    private String description;
    private LocalDateTime created;
    private List<ItemDto> items;
}
