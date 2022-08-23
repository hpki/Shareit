package ru.practicum.shareit.item;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserStorage;

import java.nio.file.AccessDeniedException;
import java.util.*;

@Repository
public class ItemStorageImpl implements ItemStorage {
    private final UserStorage userStorage;

    public ItemStorageImpl(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    private long id = 0;
    private final Map<Long, Item> items = new HashMap<>();

    @Override
    public Map<Long, Item> getItems() {
        return items;
    }

    @Override
    public ItemDto get(long itemId) throws NoSuchElementException {
        return ItemMapper.toItemDto(getItemById(itemId));
    }

    @Override
    public List<ItemDto> getAll(long userId) {
        List<ItemDto> list = new ArrayList<>();
        boolean wasFound = false;
        for (Item item : items.values()) {
            if (item.getOwner().getId() == userId) {
                wasFound = true;
                list.add(ItemMapper.toItemDto(item));
                break;
            }
        }
        if (!wasFound) {
            throw new NoSuchElementException("Пользователь c ID = " + userId + " не найден");
        }
        return list;
    }

    @Override
    public Item getItemById(long itemId) {
        return items.get(itemId);
    }

    @Override
    public Item addItem(ItemDto item, long userId) throws NoSuchElementException, IllegalArgumentException {
        if (!isValid(item)) {
            throw new IllegalArgumentException("Пустые поля не допускаются");
        }
        User owner = userStorage.getUserById(userId);
        id++;
        Item result = new Item(id, item.getName(), item.getDescription(), item.getAvailable(), owner, null);
        items.put(id, result);
        return result;
    }

    @Override
    public Item changeItem(long itemId, long userId, Item item) throws AccessDeniedException {
        if (getItemById(itemId).getOwner().getId() != userId) {
            throw new AccessDeniedException("Редактирование предмета запрещено! Редактировать может только его" +
                    " владелец");
        }
        Item oldItem = items.get(itemId);
        String name;
        if (item.getName() == null) {
            name = oldItem.getName();
        } else {
            name = item.getName();
        }
        String description;
        if (item.getDescription() == null) {
            description = oldItem.getDescription();
        } else {
            description = item.getDescription();
        }
        Boolean isAvailable = item.getAvailable() == null ? oldItem.getAvailable() : item.getAvailable();
        Item newItem = new Item(oldItem.getId(), name, description, isAvailable, oldItem.getOwner(),
                oldItem.getRequest());
        items.replace(itemId, newItem);

        return newItem;
    }

    private boolean isValid(ItemDto item) {
        if (item.getAvailable() == null) {
            return false;
        }
        if (item.getDescription() == null || item.getDescription().isBlank() || item.getDescription().isEmpty()) {
            return false;
        }
        if (item.getName() == null || item.getName().isBlank() || item.getName().isEmpty()) {
            return false;
        }
        return true;
    }
}
