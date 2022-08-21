package ru.practicum.shareit.item;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;

    public ItemServiceImpl(ItemStorage itemStorage) {
        this.itemStorage = itemStorage;
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isEmpty() || text.isBlank()) {
            return new ArrayList<>();
        }
        List<ItemDto> list = new ArrayList<>();
        for (Item item : itemStorage.getItems().values()) {
            if (item.getName().toLowerCase().contains(text.toLowerCase())) {
                if (item.getAvailable()) {
                    list.add(ItemMapper.toItemDto(item));
                }
            } else if (item.getDescription().toLowerCase().contains(text.toLowerCase())) {
                if (item.getAvailable()) {
                    list.add(ItemMapper.toItemDto(item));
                }
            }
        }
        return list;
    }

    @Override
    public ItemDto get(long itemId) throws NoSuchElementException {
        return itemStorage.get(itemId);
    }

    @Override
    public List<ItemDto> getAll(long userId) throws NoSuchElementException {
        return itemStorage.getAll(userId);
    }

    @Override
    public Item addItem(ItemDto item, long userId) throws NoSuchElementException, IllegalArgumentException {
        return itemStorage.addItem(item, userId);
    }

    @Override
    public Item changeItem(long itemId, long userId, Item item) throws AccessDeniedException {
        return itemStorage.changeItem(itemId, userId, item);
    }
}
