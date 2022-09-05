package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.NoSuchElementException;

public interface ItemService {
    List<ItemDto> search(String text);

    ItemDto get(long itemId, long userId) throws NoSuchElementException;

    List<ItemDto> getAll(long userId) throws NoSuchElementException;

    Item addItem(ItemDto item, long userId) throws NoSuchElementException, IllegalArgumentException;

    Item editItem(long itemId, long userId, ItemDto item) throws AccessDeniedException;

    Item getItemById(long itemId) throws NoSuchElementException;

    CommentDto addComment(Long itemId, CommentDto comment, Long userId) throws IllegalArgumentException;
}
