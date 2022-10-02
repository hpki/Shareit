package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.enums.Status;
import ru.practicum.shareit.exception.BookingNotFoundException;
import ru.practicum.shareit.exception.ItemNotAvailableException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.WrongTimeException;
import ru.practicum.shareit.item.ItemStorage;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserStorage;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

@ExtendWith(MockitoExtension.class)
public class BookingServiceImplTest {
    @Mock
    private BookingStorage bookingStorage;
    @Mock
    private ItemStorage itemStorage;
    @Mock
    private UserStorage userStorage;
    @InjectMocks
    private BookingServiceImpl bookingService;

    private User userOwner = new User(1, "testOwnerName", "testOwnerEmail@yandex.ru");
    private User userBooker = new User(2, "testBookerName", "testBookerEmail@yandex.ru");
    private Item item = new Item(1, "testName", "testDescription", true, userOwner, null);
    private Booking booking = new Booking(1, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), item, userBooker, Status.WAITING);
    private Booking bookingWaiting = new Booking(1, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), item, userBooker, Status.WAITING);

    @Test
    void addBooking_when_save_and_addBooking() {
        Mockito.when(itemStorage.findById(anyLong())).thenReturn(Optional.of(item));
        Mockito.when(bookingStorage.save(any())).thenReturn(booking);
        Mockito.when(userStorage.findById(anyLong())).thenReturn(Optional.of(userBooker));
        Assertions.assertEquals(booking, bookingService.addBooking(userBooker.getId(), BookingMapper.toBookingDto(booking)));
    }

    @Test
    void addBooking_when_item_not_avaliable() {
        Mockito.when(itemStorage.findById(anyLong())).thenReturn(Optional.of(item));
        item.setAvailable(false);
        Assertions.assertThrows(ItemNotAvailableException.class, () -> bookingService.addBooking(userBooker.getId(),
                BookingMapper.toBookingDto(booking)));
    }

    @Test
    void addBooking_when_wrong_time() {
        Mockito.when(itemStorage.findById(anyLong())).thenReturn(Optional.of(item));
        Mockito.when(userStorage.findById(anyLong())).thenReturn(Optional.of(userBooker));
        Booking bookingWrong = new Booking(1, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(2), item,
                userBooker, Status.WAITING);
        Assertions.assertThrows(WrongTimeException.class, () -> bookingService.addBooking(userBooker.getId(),
                BookingMapper.toBookingDto(bookingWrong)));
    }

    @Test
    void addBooking_when_user_not_found() {
        Mockito.when(itemStorage.findById(anyLong())).thenReturn(Optional.of(item));
        Mockito.when(userStorage.findById(anyLong())).thenReturn(Optional.of(userBooker));
        Mockito.when(userStorage.findById(anyLong())).thenReturn(Optional.of(userOwner));
        Assertions.assertThrows(UserNotFoundException.class, () -> bookingService.addBooking(userOwner.getId(),
                BookingMapper.toBookingDto(booking)));
    }

    @Test
    void editBooking_when_illegal_argument() {
        Booking bookingApproved = new Booking(1, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                item, userBooker, Status.APPROVED);
        Mockito.when(bookingStorage.findById(any())).thenReturn(Optional.of(bookingApproved));
        Assertions.assertThrows(IllegalArgumentException.class, () -> bookingService.editBooking(userOwner.getId(),
                bookingApproved.getId(), true));
    }

    @Test
    void editBooking_when_status_approved() {
        Booking bookingApproved = new Booking(1, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                item, userBooker, Status.APPROVED);
        Mockito.when(bookingStorage.findById(any())).thenReturn(Optional.of(bookingWaiting));
        Mockito.when(bookingStorage.save(any())).thenReturn(bookingApproved);
        Assertions.assertEquals(bookingApproved, bookingService.editBooking(userOwner.getId(), bookingApproved.getId(),
                true));
    }

    @Test
    void editBooking_when_reject_and_approved() {
        Booking bookingApproved = new Booking(1, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), item, userBooker, Status.APPROVED);
        Booking bookingRejected = new Booking(1, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), item, userBooker, Status.REJECTED);
        Mockito.when(bookingStorage.findById(any())).thenReturn(Optional.of(bookingWaiting));
        Mockito.when(bookingStorage.save(any())).thenReturn(bookingApproved);
        bookingWaiting.setStatus(Status.WAITING);
        Mockito.when(bookingStorage.save(any())).thenReturn(bookingRejected);
        Assertions.assertEquals(bookingRejected, bookingService.editBooking(userOwner.getId(), bookingApproved.getId(), false));
    }

    @Test
    void editBooking_when_booking_not_found() {
        Booking bookingApproved = new Booking(1, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), item, userBooker, Status.APPROVED);
        Assertions.assertThrows(BookingNotFoundException.class, () -> bookingService.editBooking(userBooker.getId(), bookingApproved.getId(), true));
    }

    @Test
    void getBooking_when_userBooker() {
        Mockito.when(bookingStorage.findById(any())).thenReturn(Optional.of(booking));
        Assertions.assertEquals(booking, bookingService.getBooking(userBooker.getId(), booking.getId()));
    }

    @Test
    void getBooking_when_userOwner() {
        Mockito.when(bookingStorage.findById(any())).thenReturn(Optional.of(booking));
        Assertions.assertEquals(booking, bookingService.getBooking(userOwner.getId(), booking.getId()));
    }

    @Test
    void getBooking_when_booking_not_found() {
        Mockito.when(bookingStorage.findById(any())).thenReturn(Optional.of(booking));
        Assertions.assertThrows(BookingNotFoundException.class, () -> bookingService.getBooking(3, booking.getId()));
    }

    @Test
    void getAllBookings_when_ALL() {
        Mockito.when(userStorage.findById(anyLong())).thenReturn(Optional.of(userBooker));
        Mockito.when(bookingStorage.findByBookerIdOrderByStartDesc(anyLong(), any())).thenReturn(List.of(booking));
        Assertions.assertEquals(List.of(booking), bookingService.getAllBookings(userOwner.getId(), "ALL",
                PageRequest.of(1, 1)));
    }

    @Test
    void getAllBookings_when_WAITING() {
        Mockito.when(userStorage.findById(anyLong())).thenReturn(Optional.of(userBooker));
        Mockito.when(bookingStorage.findByBookerIdAndStatusOrderByStartDesc(anyLong(), any()))
                .thenReturn(List.of(booking));
        Assertions.assertEquals(List.of(booking), bookingService.getAllBookings(userOwner.getId(), "WAITING",
                PageRequest.of(1, 1)));
    }

    @Test
    void getAllBookings_when_REJECTED() {
        Mockito.when(userStorage.findById(anyLong())).thenReturn(Optional.of(userBooker));
        Mockito.when(bookingStorage.findByBookerIdAndStatusOrderByStartDesc(anyLong(), any()))
                .thenReturn(List.of(booking));
        Assertions.assertEquals(List.of(booking), bookingService.getAllBookings(userOwner.getId(), "REJECTED",
                PageRequest.of(1, 1)));
    }

    @Test
    void getAllBookings_when_PAST() {
        Mockito.when(userStorage.findById(anyLong())).thenReturn(Optional.of(userBooker));
        Mockito.when(bookingStorage.findByBookerIdAndEndBeforeOrderByStartDesc(anyLong(), any()))
                .thenReturn(List.of(booking));
        Assertions.assertEquals(List.of(booking), bookingService.getAllBookings(userOwner.getId(), "PAST",
                PageRequest.of(1, 1)));
    }

    @Test
    void getAllBookings_when_CURRENT() {
        Mockito.when(userStorage.findById(anyLong())).thenReturn(Optional.of(userBooker));
        Mockito.when(bookingStorage.findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(List.of(booking));
        Assertions.assertEquals(List.of(booking), bookingService.getAllBookings(userOwner.getId(), "CURRENT",
                PageRequest.of(1, 1)));
    }

    @Test
    void getAllBookings_when_FUTURE() {
        Mockito.when(userStorage.findById(anyLong())).thenReturn(Optional.of(userBooker));
        Mockito.when(bookingStorage.findByBookerIdAndStartAfterOrderByStartDesc(anyLong(), any())).thenReturn(List.of(booking));
        Assertions.assertEquals(List.of(booking), bookingService.getAllBookings(userOwner.getId(), "FUTURE",
                PageRequest.of(1, 1)));
    }

    @Test
    void getAllBookings_when_IllegalArgument() {
        Mockito.when(userStorage.findById(anyLong())).thenReturn(Optional.of(userBooker));
        Assertions.assertThrows(IllegalArgumentException.class, () -> bookingService.getAllBookings(userOwner.getId(),
                "test", PageRequest.of(1, 1)));
    }

    @Test
    void getAllBookingsByOwner_when_ALL() {
        Mockito.when(userStorage.findById(anyLong())).thenReturn(Optional.of(userOwner));
        Mockito.when(bookingStorage.findByItemOwnerIdOrderByStartDesc(anyLong(), any())).thenReturn(List.of(booking));
        Assertions.assertEquals(List.of(booking), bookingService.getAllBookingsByOwner(userOwner.getId(), "ALL",
                PageRequest.of(1, 1)));
    }

    @Test
    void getAllBookingsByOwner_when_WAITING() {
        Mockito.when(userStorage.findById(anyLong())).thenReturn(Optional.of(userOwner));
        Mockito.when(bookingStorage.findByItemOwnerIdAndStatusOrderByStartDesc(anyLong(), any())).thenReturn(List.of(booking));
        Assertions.assertEquals(List.of(booking), bookingService.getAllBookingsByOwner(userOwner.getId(), "WAITING",
                PageRequest.of(1, 1)));
    }

    @Test
    void getAllBookingsByOwner_when_REJECTED() {
        Mockito.when(userStorage.findById(anyLong())).thenReturn(Optional.of(userOwner));
        Mockito.when(bookingStorage.findByItemOwnerIdAndStatusOrderByStartDesc(anyLong(), any())).thenReturn(List.of(booking));
        Assertions.assertEquals(List.of(booking), bookingService.getAllBookingsByOwner(userOwner.getId(), "REJECTED",
                PageRequest.of(1, 1)));
    }

    @Test
    void getAllBookingsByOwner_when_PAST() {
        Mockito.when(userStorage.findById(anyLong())).thenReturn(Optional.of(userOwner));
        Mockito.when(bookingStorage.findByItemOwnerIdAndEndBeforeOrderByStartDesc(anyLong(), any())).thenReturn(List.of(booking));
        Assertions.assertEquals(List.of(booking), bookingService.getAllBookingsByOwner(userOwner.getId(), "PAST",
                PageRequest.of(1, 1)));
    }

    @Test
    void getAllBookingsByOwner_when_CURRENT() {
        Mockito.when(userStorage.findById(anyLong())).thenReturn(Optional.of(userOwner));
        Mockito.when(bookingStorage.findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(List.of(booking));
        Assertions.assertEquals(List.of(booking), bookingService.getAllBookingsByOwner(userOwner.getId(), "CURRENT",
                PageRequest.of(1, 1)));
    }

    @Test
    void getAllBookingsByOwner_when_FUTURE() {
        Mockito.when(userStorage.findById(anyLong())).thenReturn(Optional.of(userOwner));
        Mockito.when(bookingStorage.findByItemOwnerIdAndStartAfterOrderByStartDesc(anyLong(), any())).thenReturn(List.of(booking));
        Assertions.assertEquals(List.of(booking), bookingService.getAllBookingsByOwner(userOwner.getId(), "FUTURE",
                PageRequest.of(1, 1)));
    }

    @Test
    void getAllBookingsByOwner_when_IllegalArgument() {
        Mockito.when(userStorage.findById(anyLong())).thenReturn(Optional.of(userOwner));
        Assertions.assertThrows(IllegalArgumentException.class, () -> bookingService.getAllBookingsByOwner(userOwner.getId(), "test", PageRequest.of(1, 1)));
    }

}
