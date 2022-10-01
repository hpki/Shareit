package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;

public interface ItemService {
    ItemDto createItem(long userId, ItemDto itemDto);

    ItemDto updateItem(long userId, long itemId, ItemDto itemDto);

    ItemWithBookingDto getItem(long userId, long itemId);

    List<ItemWithBookingDto> getAllItems(long userId);

    List<ItemDto> search(String text);

    CommentDto createComment(long userId, long itemId, Comment comment);
}
