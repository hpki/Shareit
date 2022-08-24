package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public interface ItemStorage {
    Map<Long, Item> getItems();

    ItemDto get(long itemId) throws NoSuchElementException;

    List<ItemDto> getAllItems(long userId);

    Item getItemById(long itemId);

    Item addItem(ItemDto item, long userId) throws NoSuchElementException, IllegalArgumentException;

    Item changeItem(long itemId, long userId, Item item) throws AccessDeniedException;


}
