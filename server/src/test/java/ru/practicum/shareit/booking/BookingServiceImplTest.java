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
import ru.practicum.shareit.exception.WrongStartOrEndTimeException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

@ExtendWith(MockitoExtension.class)
public class BookingServiceImplTest {
    @Mock
    BookingRepository bookingRepository;
    @Mock
    ItemRepository itemRepository;
    @Mock
    UserRepository userRepository;
    @InjectMocks
    BookingServiceImpl bookingService;

    User userOwner = new User(1, "testOwnerName", "testOwnerEmail@yandex.ru");
    User userBooker = new User(2, "testBookerName", "testBookerEmail@yandex.ru");
    Item item = new Item(1, "testName", "testDescription", true, userOwner, null);
    Booking booking = new Booking(1, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), item, userBooker, Status.WAITING);
    Booking bookingWaiting = new Booking(1, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), item, userBooker, Status.WAITING);

    @Test
    void createBooking() {
        Mockito.when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        Mockito.when(userRepository.findById(anyLong())).thenReturn(Optional.of(userBooker));
        Mockito.when(bookingRepository.save(any())).thenReturn(booking);
        Assertions.assertEquals(booking, bookingService.createBooking(userBooker.getId(), BookingMapper.toBookingDto(booking)));
        item.setAvailable(false);
        Assertions.assertThrows(ItemNotAvailableException.class, () -> bookingService.createBooking(userBooker.getId(), BookingMapper.toBookingDto(booking)));
        item.setAvailable(true);
        Booking bookingWrong = new Booking(1, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(2), item, userBooker, Status.WAITING);
        Assertions.assertThrows(WrongStartOrEndTimeException.class, () -> bookingService.createBooking(userBooker.getId(), BookingMapper.toBookingDto(bookingWrong)));
        Mockito.when(userRepository.findById(anyLong())).thenReturn(Optional.of(userOwner));
        Assertions.assertThrows(UserNotFoundException.class, () -> bookingService.createBooking(userOwner.getId(), BookingMapper.toBookingDto(booking)));

    }

    @Test
    void updateBooking() {
        Booking bookingApproved = new Booking(1, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), item, userBooker, Status.APPROVED);
        Booking bookingRejected = new Booking(1, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), item, userBooker, Status.REJECTED);
        Mockito.when(bookingRepository.findById(any())).thenReturn(Optional.of(bookingApproved));
        Assertions.assertThrows(IllegalArgumentException.class, () -> bookingService.updateBooking(userOwner.getId(), bookingApproved.getId(), true));
        Mockito.when(bookingRepository.findById(any())).thenReturn(Optional.of(bookingWaiting));
        Mockito.when(bookingRepository.save(any())).thenReturn(bookingApproved);
        Assertions.assertEquals(bookingApproved, bookingService.updateBooking(userOwner.getId(), bookingApproved.getId(), true));
        bookingWaiting.setStatus(Status.WAITING);
        Mockito.when(bookingRepository.save(any())).thenReturn(bookingRejected);
        Assertions.assertEquals(bookingRejected, bookingService.updateBooking(userOwner.getId(), bookingApproved.getId(), false));
        Assertions.assertThrows(BookingNotFoundException.class, () -> bookingService.updateBooking(userBooker.getId(), bookingApproved.getId(), true));
    }

    @Test
    void getBooking() {
        Mockito.when(bookingRepository.findById(any())).thenReturn(Optional.of(booking));
        Assertions.assertEquals(booking, bookingService.getBooking(userBooker.getId(), booking.getId()));
        Assertions.assertEquals(booking, bookingService.getBooking(userOwner.getId(), booking.getId()));
        Assertions.assertThrows(BookingNotFoundException.class, () -> bookingService.getBooking(3, booking.getId()));
    }

    @Test
    void getAllBookings() {
        Mockito.when(userRepository.findById(anyLong())).thenReturn(Optional.of(userBooker));

        Mockito.when(bookingRepository.findByBookerIdOrderByStartDesc(anyLong(), any())).thenReturn(List.of(booking));
        Assertions.assertEquals(List.of(booking), bookingService.getAllBookings(userOwner.getId(), "ALL", PageRequest.of(1, 1)));

        Mockito.when(bookingRepository.findByBookerIdAndStatusOrderByStartDesc(anyLong(), any())).thenReturn(List.of(booking));
        Assertions.assertEquals(List.of(booking), bookingService.getAllBookings(userOwner.getId(), "WAITING", PageRequest.of(1, 1)));

        Mockito.when(bookingRepository.findByBookerIdAndStatusOrderByStartDesc(anyLong(), any())).thenReturn(List.of(booking));
        Assertions.assertEquals(List.of(booking), bookingService.getAllBookings(userOwner.getId(), "REJECTED", PageRequest.of(1, 1)));

        Mockito.when(bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(anyLong(), any())).thenReturn(List.of(booking));
        Assertions.assertEquals(List.of(booking), bookingService.getAllBookings(userOwner.getId(), "PAST", PageRequest.of(1, 1)));

        Mockito.when(bookingRepository.findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(), any(), any())).thenReturn(List.of(booking));
        Assertions.assertEquals(List.of(booking), bookingService.getAllBookings(userOwner.getId(), "CURRENT", PageRequest.of(1, 1)));

        Mockito.when(bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(anyLong(), any())).thenReturn(List.of(booking));
        Assertions.assertEquals(List.of(booking), bookingService.getAllBookings(userOwner.getId(), "FUTURE", PageRequest.of(1, 1)));

        Assertions.assertThrows(IllegalArgumentException.class, () -> bookingService.getAllBookings(userOwner.getId(), "test", PageRequest.of(1, 1)));
    }

    @Test
    void getAllBookingsForOwner() {
        Mockito.when(userRepository.findById(anyLong())).thenReturn(Optional.of(userOwner));

        Mockito.when(bookingRepository.findByItemOwnerIdOrderByStartDesc(anyLong(), any())).thenReturn(List.of(booking));
        Assertions.assertEquals(List.of(booking), bookingService.getAllBookingsForOwner(userOwner.getId(), "ALL", PageRequest.of(1, 1)));

        Mockito.when(bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(anyLong(), any())).thenReturn(List.of(booking));
        Assertions.assertEquals(List.of(booking), bookingService.getAllBookingsForOwner(userOwner.getId(), "WAITING", PageRequest.of(1, 1)));

        Mockito.when(bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(anyLong(), any())).thenReturn(List.of(booking));
        Assertions.assertEquals(List.of(booking), bookingService.getAllBookingsForOwner(userOwner.getId(), "REJECTED", PageRequest.of(1, 1)));

        Mockito.when(bookingRepository.findByItemOwnerIdAndEndBeforeOrderByStartDesc(anyLong(), any())).thenReturn(List.of(booking));
        Assertions.assertEquals(List.of(booking), bookingService.getAllBookingsForOwner(userOwner.getId(), "PAST", PageRequest.of(1, 1)));

        Mockito.when(bookingRepository.findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(), any(), any())).thenReturn(List.of(booking));
        Assertions.assertEquals(List.of(booking), bookingService.getAllBookingsForOwner(userOwner.getId(), "CURRENT", PageRequest.of(1, 1)));

        Mockito.when(bookingRepository.findByItemOwnerIdAndStartAfterOrderByStartDesc(anyLong(), any())).thenReturn(List.of(booking));
        Assertions.assertEquals(List.of(booking), bookingService.getAllBookingsForOwner(userOwner.getId(), "FUTURE", PageRequest.of(1, 1)));

        Assertions.assertThrows(IllegalArgumentException.class, () -> bookingService.getAllBookingsForOwner(userOwner.getId(), "test", PageRequest.of(1, 1)));
    }
}
