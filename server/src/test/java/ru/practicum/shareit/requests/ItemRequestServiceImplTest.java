package ru.practicum.shareit.requests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemStorage;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.requests.dto.ItemRequestDto;
import ru.practicum.shareit.requests.model.ItemRequest;
import ru.practicum.shareit.user.UserStorage;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

@ExtendWith(MockitoExtension.class)
public class ItemRequestServiceImplTest {
    @Mock
    private UserStorage userStorage;

    @Mock
    private ItemStorage itemStorage;

    @Mock
    private ItemRequestStorage itemRequestStorage;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    private User user = new User(1, "testName", "testEmail@yandex.ru");

    private ItemRequest itemRequest = new ItemRequest(1, "testDescription", user, LocalDateTime.now());
    private Item item = new Item(1, "testName", "testDescription", true, user, null);

    @Test
    void addRequest() {
        Mockito.when(itemRequestStorage.save(any())).thenReturn(itemRequest);
        Mockito.when(userStorage.findById(anyLong())).thenReturn(Optional.of(user));
        Assertions.assertEquals(ItemRequestMapper.toItemRequestDto(itemRequest),
                itemRequestService.addRequest(user.getId(), ItemRequestMapper.toItemRequestDto(itemRequest)));
    }

    @Test
    void getItemRequestById() {
        Mockito.when(userStorage.existsById(anyLong())).thenReturn(true);
        Mockito.when(itemRequestStorage.findById(anyLong())).thenReturn(Optional.of(itemRequest));
        Mockito.when(itemStorage.findByRequestIdOrderByIdAsc(anyLong())).thenReturn(List.of(item));
        ItemRequestDto itemRequestDto = ItemRequestMapper.toItemRequestDto(itemRequest);
        itemRequestDto.setItems(List.of(ItemMapper.toItemDto(item, new ArrayList<>())));
        Assertions.assertEquals(itemRequestDto, itemRequestService.getItemRequestById(user.getId(), itemRequest.getId()));
        Mockito.when(userStorage.existsById(anyLong())).thenReturn(false);
        Assertions.assertThrows(UserNotFoundException.class, () -> itemRequestService.getItemRequestById(user.getId(),
                itemRequest.getId()));

    }

    @Test
    void getUserItemRequests() {
        Mockito.when(userStorage.existsById(anyLong())).thenReturn(true);
        Mockito.when(itemRequestStorage.findAll()).thenReturn(List.of(itemRequest));
        Mockito.when(itemStorage.findByRequestIdOrderByIdAsc(anyLong())).thenReturn(List.of(item));
        ItemRequestDto itemRequestDto = ItemRequestMapper.toItemRequestDto(itemRequest);
        itemRequestDto.setItems(List.of(ItemMapper.toItemDto(item, new ArrayList<>())));
        Assertions.assertEquals(List.of(itemRequestDto), itemRequestService.getUserItemRequests(user.getId()));
        Mockito.when(userStorage.existsById(anyLong())).thenReturn(false);
        Assertions.assertThrows(UserNotFoundException.class, () -> itemRequestService.getUserItemRequests(user.getId()));

    }


    @Test
    void getItemRequests() {
        Page<ItemRequest> page = new PageImpl<>(List.of(itemRequest));
        Mockito.when(itemRequestStorage.findAllByRequestorIdIsNot(anyLong(), any())).thenReturn(page);
        Mockito.when(itemStorage.findByRequestIdOrderByIdAsc(itemRequest.getId())).thenReturn(List.of(item));
        ItemRequestDto itemRequestDto = ItemRequestMapper.toItemRequestDto(itemRequest);
        itemRequestDto.setItems(List.of(ItemMapper.toItemDto(item, new ArrayList<>())));
        Assertions.assertEquals(List.of(itemRequestDto), itemRequestService.getItemRequests(user.getId(),
                PageRequest.of(1, 1)));

    }
}
