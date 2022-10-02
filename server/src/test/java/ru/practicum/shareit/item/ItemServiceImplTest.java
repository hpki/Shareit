package ru.practicum.shareit.item;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingStorage;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.enums.Status;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.requests.ItemRequestStorage;
import ru.practicum.shareit.requests.model.ItemRequest;
import ru.practicum.shareit.user.UserStorage;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemServiceImplTest {
    @Mock
    ItemStorage itemStorage;
    @Mock
    UserStorage userStorage;
    @Mock
    ItemRequestStorage itemRequestStorage;
    @Mock
    BookingStorage bookingStorage;
    @Mock
    CommentStorage commentStorage;
    @InjectMocks
    ItemServiceImpl itemService;

    User user = new User(1, "testName", "testEmail@yandex.ru");
    User userBooker = new User(2, "testBookerName", "testBookerEmail@yandex.ru");
    ItemRequest itemRequest = new ItemRequest(1, "testDescription", user, LocalDateTime.now());
    Item item = new Item(1, "testName", "testDescription", true, user, null);
    Item itemWithRequest = new Item(2, "testName", "testDescription", true, user, itemRequest);
    Item itemUpdate = new Item(1, "testUpdateName", "testUpdateDescription", false, user, null);
    Booking booking = new Booking(1, LocalDateTime.now(), LocalDateTime.now().plusDays(1), item, userBooker, Status.WAITING);
    Comment comment = new Comment(1, "text", item, user, LocalDateTime.now());

    @Test
    void getItem() {
        Mockito.when(bookingStorage.findByItemIdOrderByStartDesc(anyLong()))
                .thenReturn(List.of(booking));
        Mockito.when(itemStorage.findById(anyLong())).thenReturn(Optional.of(item));
        ItemWithBookingDto itemWithBookingDto = ItemMapper.toItemWithBookingDto(item, BookingMapper.toBookingDto(booking),
                BookingMapper.toBookingDto(booking), new ArrayList<>());
        Assertions.assertEquals(itemWithBookingDto, itemService.getItem(user.getId(), item.getId()));
    }

    @Test
    void addItem() {
        Mockito.when(itemStorage.save(any())).thenReturn(item);
        Mockito.when(userStorage.findById(anyLong())).thenReturn(Optional.of(user));
        Assertions.assertEquals(ItemMapper.toItemDto(item, new ArrayList<>()), itemService.addItem(user.getId(),
                ItemMapper.toItemDto(item, new ArrayList<>())));
        Mockito.when(itemStorage.save(any())).thenReturn(itemWithRequest);
        Mockito.when(itemRequestStorage.findById(anyLong())).thenReturn(Optional.of(itemRequest));
        Assertions.assertEquals(ItemMapper.toItemDto(itemWithRequest, new ArrayList<>()), itemService.addItem(user.getId(),
                ItemMapper.toItemDto(itemWithRequest, new ArrayList<>())));
    }

    @Test
    void editItem() {
        Mockito.when(itemStorage.findById(anyLong())).thenReturn(Optional.of(item));
        Mockito.when(itemStorage.save(any())).thenReturn(itemUpdate);
        Assertions.assertEquals(ItemMapper.toItemDto(itemUpdate, new ArrayList<>()), itemService.editItem(user.getId(),
                item.getId(), ItemMapper.toItemDto(itemUpdate, new ArrayList<>())));
    }

    @Test
    void getAllItems() {
        Mockito.when(itemStorage.findAll()).thenReturn(List.of(item));
        Mockito.when(bookingStorage.findByItemIdOrderByStartDesc(anyLong()))
                .thenReturn(List.of(booking));
        Mockito.when(itemStorage.findById(anyLong())).thenReturn(Optional.of(item));
        ItemWithBookingDto itemWithBookingDto = ItemMapper.toItemWithBookingDto(item, BookingMapper.toBookingDto(booking),
                BookingMapper.toBookingDto(booking), new ArrayList<>());
        Assertions.assertEquals(List.of(itemWithBookingDto), itemService.getAllItems(user.getId()));
    }

    @Test
    void search() {
        Mockito.when(itemStorage.search(anyString())).thenReturn(List.of(item));
        Assertions.assertEquals(List.of(ItemMapper.toItemDto(item, new ArrayList<>())), itemService.search("text"));
    }

    @Test
    void addComment() {
        Mockito.when(bookingStorage.findByBookerIdAndEndBeforeOrderByStartDesc(anyLong(), any())).thenReturn(List.of(booking));
        Mockito.when(itemStorage.findById(anyLong())).thenReturn(Optional.of(item));
        Mockito.when(userStorage.findById(anyLong())).thenReturn(Optional.of(user));
        Mockito.when(commentStorage.save(any())).thenReturn(comment);
        Assertions.assertEquals(CommentMapper.toCommentDto(comment), itemService.addComment(user.getId(), item.getId(), comment));
        Mockito.when(bookingStorage.findByBookerIdAndEndBeforeOrderByStartDesc(anyLong(), any())).thenReturn(List.of());
        Assertions.assertThrows(IllegalArgumentException.class, () -> itemService.addComment(user.getId(), item.getId(), comment));

    }
}
