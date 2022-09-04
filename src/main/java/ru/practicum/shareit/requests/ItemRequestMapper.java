package ru.practicum.shareit.requests;


import ru.practicum.shareit.requests.dto.ItemRequestDto;

public class ItemRequestMapper {
    public ItemRequestDto toItemRequestDto(ItemRequest itemRequest) {
        return new ItemRequestDto(itemRequest.getDescription());
    }
}
