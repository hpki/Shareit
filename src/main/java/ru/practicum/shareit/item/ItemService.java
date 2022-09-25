package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;

public interface ItemService {
    ItemDto addItem(long userId, ItemDto itemDto);

    ItemDto editItem(long userId, long itemId, ItemDto itemDto);

    ItemWithBookingDto getItem(long userId, long itemId);

    List<ItemWithBookingDto> getAll(long userId);

    List<ItemDto> search(String text);

    CommentDto addComment(long userId, long itemId, Comment comment);
}
