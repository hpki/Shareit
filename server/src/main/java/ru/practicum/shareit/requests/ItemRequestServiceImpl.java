package ru.practicum.shareit.requests;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ItemRequestNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemStorage;
import ru.practicum.shareit.requests.dto.ItemRequestDto;
import ru.practicum.shareit.requests.model.ItemRequest;
import ru.practicum.shareit.user.UserStorage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemRequestServiceImpl implements ItemRequestService {
    private final UserStorage userStorage;
    private final ItemStorage itemStorage;
    private final ItemRequestStorage itemRequestStorage;

    public ItemRequestServiceImpl(UserStorage userStorage, ItemStorage itemStorage,
                                  ItemRequestStorage itemRequestStorage) {
        this.userStorage = userStorage;
        this.itemStorage = itemStorage;
        this.itemRequestStorage = itemRequestStorage;
    }

    public ItemRequestDto addRequest(long userId, ItemRequestDto itemRequestDto) {
        ItemRequest itemRequest = itemRequestStorage.save(ItemRequestMapper.toItemRequest(userStorage
                .findById(userId).orElseThrow(() -> new UserNotFoundException("Пользователь с таким id не найден!")),
                itemRequestDto));
        return ItemRequestMapper.toItemRequestDto(itemRequest);
    }

    public ItemRequestDto getItemRequestById(long userId, long requestId) {
        if (userStorage.existsById(userId)) {
            ItemRequestDto itemRequestDto = ItemRequestMapper.toItemRequestDto(itemRequestStorage.findById(requestId)
                    .orElseThrow(() -> new ItemRequestNotFoundException("Запрос вещи с таким id не найден!")));
            itemRequestDto.setItems(itemStorage.findByRequestIdOrderByIdAsc(itemRequestDto.getId()).stream()
                    .map(x -> ItemMapper.toItemDto(x, new ArrayList<>()))
                    .collect(Collectors.toList()));
            return itemRequestDto;
        } else {
            throw new UserNotFoundException("Пользователь с таким id не найден!");
        }
    }

    public List<ItemRequestDto> getUserItemRequests(long userId) {
        if (userStorage.existsById(userId)) {
            return itemRequestStorage.findAll().stream()
                    .filter(x -> x.getRequestor().getId() == userId)
                    .map(ItemRequestMapper::toItemRequestDto)
                    .peek(x -> x.setItems(
                            itemStorage.findByRequestIdOrderByIdAsc(x.getId()).stream()
                                    .map(y -> ItemMapper.toItemDto(y, new ArrayList<>()))
                                    .collect(Collectors.toList())))
                    .sorted(Comparator.comparing(ItemRequestDto::getCreated))
                    .collect(Collectors.toList());
        } else {
            throw new UserNotFoundException("Пользователь с таким id не найден!");
        }
    }

    public List<ItemRequestDto> getItemRequests(long userId, Pageable pageable) {
        return itemRequestStorage.findAllByRequestorIdIsNot(userId, pageable).getContent().stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .peek(x -> x.setItems(
                        itemStorage.findByRequestIdOrderByIdAsc(x.getId()).stream()
                                .map(y -> ItemMapper.toItemDto(y, new ArrayList<>()))
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }
}