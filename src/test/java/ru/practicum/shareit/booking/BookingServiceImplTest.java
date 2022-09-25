package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.exceptions.BookingNotFoundException;
import ru.practicum.shareit.exceptions.ItemNotAvailableException;
import ru.practicum.shareit.exceptions.UserNotFoundException;
import ru.practicum.shareit.exceptions.WrongTimeException;
import ru.practicum.shareit.item.ItemStorage;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserStorage;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

@ExtendWith(MockitoExtension.class)
public class BookingServiceImplTest {
    @Mock
    BookingStorage bookingStorage;
    @Mock
    ItemStorage itemStorage;
    @Mock
    UserStorage userStorage;
    @InjectMocks
    BookingServiceImpl bookingService;

    User userOwner = new User(1, "testOwnerName", "testOwnerEmail@yandex.ru");
    User userBooker = new User(2, "testBookerName", "testBookerEmail@yandex.ru");
    Item item = new Item(1, "testName", "testDescription", true, userOwner, null);
    Booking booking = new Booking(1, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), item, userBooker, Status.WAITING);
    Booking bookingWaiting = new Booking(1, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), item, userBooker, Status.WAITING);

    @Test
    void addBooking() {
        Mockito.when(itemStorage.findById(anyLong())).thenReturn(Optional.of(item));
        Mockito.when(userStorage.findById(anyLong())).thenReturn(Optional.of(userBooker));
        Mockito.when(bookingStorage.save(any())).thenReturn(booking);
        Assertions.assertEquals(booking, bookingService.addBooking(userBooker.getId(), BookingMapper.toBookingDto(booking)));
        item.setAvailable(false);
        Assertions.assertThrows(ItemNotAvailableException.class, () -> bookingService.addBooking(userBooker.getId(), BookingMapper.toBookingDto(booking)));
        item.setAvailable(true);
        Booking bookingWrong = new Booking(1, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(2), item, userBooker, Status.WAITING);
        Assertions.assertThrows(WrongTimeException.class, () -> bookingService.addBooking(userBooker.getId(), BookingMapper.toBookingDto(bookingWrong)));
        Mockito.when(userStorage.findById(anyLong())).thenReturn(Optional.of(userOwner));
        Assertions.assertThrows(UserNotFoundException.class, () -> bookingService.addBooking(userOwner.getId(), BookingMapper.toBookingDto(booking)));

    }

    @Test
    void editBooking() {
        Booking bookingApproved = new Booking(1, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), item, userBooker, Status.APPROVED);
        Booking bookingRejected = new Booking(1, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), item, userBooker, Status.REJECTED);
        Mockito.when(bookingStorage.findById(any())).thenReturn(Optional.of(bookingApproved));
        Assertions.assertThrows(IllegalArgumentException.class, () -> bookingService.setApproved(userOwner.getId(), bookingApproved.getId(), true));
        Mockito.when(bookingStorage.findById(any())).thenReturn(Optional.of(bookingWaiting));
        Mockito.when(bookingStorage.save(any())).thenReturn(bookingApproved);
        Assertions.assertEquals(bookingApproved, bookingService.setApproved(userOwner.getId(), bookingApproved.getId(), true));
        bookingWaiting.setStatus(Status.WAITING);
        Mockito.when(bookingStorage.save(any())).thenReturn(bookingRejected);
        Assertions.assertEquals(bookingRejected, bookingService.setApproved(userOwner.getId(), bookingApproved.getId(), false));
        Assertions.assertThrows(BookingNotFoundException.class, () -> bookingService.setApproved(userBooker.getId(), bookingApproved.getId(), true));
    }

    @Test
    void getBooking() {
        Mockito.when(bookingStorage.findById(any())).thenReturn(Optional.of(booking));
        Assertions.assertEquals(booking, bookingService.getBookingById(userBooker.getId(), booking.getId()));
        Assertions.assertEquals(booking, bookingService.getBookingById(userOwner.getId(), booking.getId()));
        Assertions.assertThrows(BookingNotFoundException.class, () -> bookingService.getBookingById(3, booking.getId()));
    }

    @Test
    void getAllBookings() {
        Mockito.when(userStorage.findById(anyLong())).thenReturn(Optional.of(userBooker));

        Mockito.when(bookingStorage.findByBookerIdOrderByStartDesc(anyLong(), any())).thenReturn(List.of(booking));
        Assertions.assertEquals(List.of(booking), bookingService.getAll(userOwner.getId(), "ALL", PageRequest.of(1, 1)));

        Mockito.when(bookingStorage.findByBookerIdAndStatusOrderByStartDesc(anyLong(), any())).thenReturn(List.of(booking));
        Assertions.assertEquals(List.of(booking), bookingService.getAll(userOwner.getId(), "WAITING", PageRequest.of(1, 1)));

        Mockito.when(bookingStorage.findByBookerIdAndStatusOrderByStartDesc(anyLong(), any())).thenReturn(List.of(booking));
        Assertions.assertEquals(List.of(booking), bookingService.getAll(userOwner.getId(), "REJECTED", PageRequest.of(1, 1)));

        Mockito.when(bookingStorage.findByBookerIdAndEndBeforeOrderByStartDesc(anyLong(), any())).thenReturn(List.of(booking));
        Assertions.assertEquals(List.of(booking), bookingService.getAll(userOwner.getId(), "PAST", PageRequest.of(1, 1)));

        Mockito.when(bookingStorage.findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(), any(), any())).thenReturn(List.of(booking));
        Assertions.assertEquals(List.of(booking), bookingService.getAll(userOwner.getId(), "CURRENT", PageRequest.of(1, 1)));

        Mockito.when(bookingStorage.findByBookerIdAndStartAfterOrderByStartDesc(anyLong(), any())).thenReturn(List.of(booking));
        Assertions.assertEquals(List.of(booking), bookingService.getAll(userOwner.getId(), "FUTURE", PageRequest.of(1, 1)));

        Assertions.assertThrows(IllegalArgumentException.class, () -> bookingService.getAll(userOwner.getId(), "test", PageRequest.of(1, 1)));
    }

    @Test
    void getAllBookingsForOwner() {
        Mockito.when(userStorage.findById(anyLong())).thenReturn(Optional.of(userOwner));

        Mockito.when(bookingStorage.findByItemOwnerIdOrderByStartDesc(anyLong(), any())).thenReturn(List.of(booking));
        Assertions.assertEquals(List.of(booking), bookingService.getAllBookingsByOwner(userOwner.getId(), "ALL", PageRequest.of(1, 1)));

        Mockito.when(bookingStorage.findByItemOwnerIdAndStatusOrderByStartDesc(anyLong(), any())).thenReturn(List.of(booking));
        Assertions.assertEquals(List.of(booking), bookingService.getAllBookingsByOwner(userOwner.getId(), "WAITING", PageRequest.of(1, 1)));

        Mockito.when(bookingStorage.findByItemOwnerIdAndStatusOrderByStartDesc(anyLong(), any())).thenReturn(List.of(booking));
        Assertions.assertEquals(List.of(booking), bookingService.getAllBookingsByOwner(userOwner.getId(), "REJECTED", PageRequest.of(1, 1)));

        Mockito.when(bookingStorage.findByItemOwnerIdAndEndBeforeOrderByStartDesc(anyLong(), any())).thenReturn(List.of(booking));
        Assertions.assertEquals(List.of(booking), bookingService.getAllBookingsByOwner(userOwner.getId(), "PAST", PageRequest.of(1, 1)));

        Mockito.when(bookingStorage.findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(), any(), any())).thenReturn(List.of(booking));
        Assertions.assertEquals(List.of(booking), bookingService.getAllBookingsByOwner(userOwner.getId(), "CURRENT", PageRequest.of(1, 1)));

        Mockito.when(bookingStorage.findByItemOwnerIdAndStartAfterOrderByStartDesc(anyLong(), any())).thenReturn(List.of(booking));
        Assertions.assertEquals(List.of(booking), bookingService.getAllBookingsByOwner(userOwner.getId(), "FUTURE", PageRequest.of(1, 1)));

        Assertions.assertThrows(IllegalArgumentException.class, () -> bookingService.getAllBookingsByOwner(userOwner.getId(), "test", PageRequest.of(1, 1)));
    }
}
