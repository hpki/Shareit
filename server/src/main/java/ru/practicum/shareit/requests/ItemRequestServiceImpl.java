package ru.practicum.shareit.requests;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ItemRequestNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.requests.dto.ItemRequestDto;
import ru.practicum.shareit.requests.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemRequestServiceImpl implements ItemRequestService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemRequestRepository itemRequestRepository;

    public ItemRequestServiceImpl(UserRepository userRepository, ItemRepository itemRepository, ItemRequestRepository itemRequestRepository) {
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.itemRequestRepository = itemRequestRepository;
    }

    public ItemRequestDto createItemRequest(long userId, ItemRequestDto itemRequestDto) {
        ItemRequest itemRequest = itemRequestRepository.save(ItemRequestMapper.toItemRequest(userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("Пользователь с таким id не найден!")), itemRequestDto));
        return ItemRequestMapper.toItemRequestDto(itemRequest);
    }

    public ItemRequestDto getItemRequestById(long userId, long requestId) {
        if (userRepository.existsById(userId)) {
            ItemRequestDto itemRequestDto = ItemRequestMapper.toItemRequestDto(itemRequestRepository.findById(requestId)
                    .orElseThrow(() -> new ItemRequestNotFoundException("Запрос вещи с таким id не найден!")));
            itemRequestDto.setItems(itemRepository.findByRequestIdOrderByIdAsc(itemRequestDto.getId()).stream()
                    .map(x -> ItemMapper.toItemDto(x, new ArrayList<>()))
                    .collect(Collectors.toList()));
            return itemRequestDto;
        } else {
            throw new UserNotFoundException("Пользователь с таким id не найден!");
        }
    }

    public List<ItemRequestDto> getUserItemRequests(long userId) {
        if (userRepository.existsById(userId)) {
            return itemRequestRepository.findAll().stream()
                    .filter(x -> x.getRequestor().getId() == userId)
                    .map(ItemRequestMapper::toItemRequestDto)
                    .peek(x -> x.setItems(
                            itemRepository.findByRequestIdOrderByIdAsc(x.getId()).stream()
                                    .map(y -> ItemMapper.toItemDto(y, new ArrayList<>()))
                                    .collect(Collectors.toList())))
                    .sorted(Comparator.comparing(ItemRequestDto::getCreated))
                    .collect(Collectors.toList());
        } else {
            throw new UserNotFoundException("Пользователь с таким id не найден!");
        }
    }

    public List<ItemRequestDto> getItemRequests(long userId, Pageable pageable) {
        return itemRequestRepository.findAllByRequestorIdIsNot(userId, pageable).getContent().stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .peek(x -> x.setItems(
                        itemRepository.findByRequestIdOrderByIdAsc(x.getId()).stream()
                                .map(y -> ItemMapper.toItemDto(y, new ArrayList<>()))
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }
}