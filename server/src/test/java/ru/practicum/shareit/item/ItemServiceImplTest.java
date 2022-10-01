package ru.practicum.shareit.item;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.enums.Status;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.requests.ItemRequestRepository;
import ru.practicum.shareit.requests.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemServiceImplTest {
    @Mock
    ItemRepository itemRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    ItemRequestRepository itemRequestRepository;
    @Mock
    BookingRepository bookingRepository;
    @Mock
    CommentRepository commentRepository;
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
        Mockito.when(bookingRepository.findByItemIdOrderByStartDesc(anyLong()))
                .thenReturn(List.of(booking));
        Mockito.when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        ItemWithBookingDto itemWithBookingDto = ItemMapper.toItemWithBookingDto(item, BookingMapper.toBookingDto(booking),
                BookingMapper.toBookingDto(booking), new ArrayList<>());
        Assertions.assertEquals(itemWithBookingDto, itemService.getItem(user.getId(), item.getId()));
    }

    @Test
    void createItem() {
        Mockito.when(itemRepository.save(any())).thenReturn(item);
        Mockito.when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        Assertions.assertEquals(ItemMapper.toItemDto(item, new ArrayList<>()), itemService.createItem(user.getId(), ItemMapper.toItemDto(item, new ArrayList<>())));
        Mockito.when(itemRepository.save(any())).thenReturn(itemWithRequest);
        Mockito.when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.of(itemRequest));
        Assertions.assertEquals(ItemMapper.toItemDto(itemWithRequest, new ArrayList<>()), itemService.createItem(user.getId(), ItemMapper.toItemDto(itemWithRequest, new ArrayList<>())));
    }

    @Test
    void updateItem() {
        Mockito.when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        Mockito.when(itemRepository.save(any())).thenReturn(itemUpdate);
        Assertions.assertEquals(ItemMapper.toItemDto(itemUpdate, new ArrayList<>()), itemService.updateItem(user.getId(), item.getId(), ItemMapper.toItemDto(itemUpdate, new ArrayList<>())));
    }

    @Test
    void getAllItems() {
        Mockito.when(itemRepository.findAll()).thenReturn(List.of(item));
        Mockito.when(bookingRepository.findByItemIdOrderByStartDesc(anyLong()))
                .thenReturn(List.of(booking));
        Mockito.when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        ItemWithBookingDto itemWithBookingDto = ItemMapper.toItemWithBookingDto(item, BookingMapper.toBookingDto(booking),
                BookingMapper.toBookingDto(booking), new ArrayList<>());
        Assertions.assertEquals(List.of(itemWithBookingDto), itemService.getAllItems(user.getId()));
    }

    @Test
    void search() {
        Mockito.when(itemRepository.search(anyString())).thenReturn(List.of(item));
        Assertions.assertEquals(List.of(ItemMapper.toItemDto(item, new ArrayList<>())), itemService.search("text"));
    }

    @Test
    void createComment() {
        Mockito.when(bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(anyLong(), any())).thenReturn(List.of(booking));
        Mockito.when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        Mockito.when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        Mockito.when(commentRepository.save(any())).thenReturn(comment);
        Assertions.assertEquals(CommentMapper.toCommentDto(comment), itemService.createComment(user.getId(), item.getId(), comment));
        Mockito.when(bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(anyLong(), any())).thenReturn(List.of());
        Assertions.assertThrows(IllegalArgumentException.class, () -> itemService.createComment(user.getId(), item.getId(), comment));

    }
}
