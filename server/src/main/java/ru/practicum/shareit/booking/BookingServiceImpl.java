package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.enums.State;
import ru.practicum.shareit.enums.Status;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.ItemStorage;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserStorage;
import ru.practicum.shareit.user.model.User;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class BookingServiceImpl implements BookingService {
    private final BookingStorage bookingStorage;
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    public BookingServiceImpl(BookingStorage bookingStorage, ItemStorage itemStorage, UserStorage userStorage) {
        this.bookingStorage = bookingStorage;
        this.itemStorage = itemStorage;
        this.userStorage = userStorage;
    }

    @Override
    public Booking addBooking(long userId, BookingDto bookingDto) {
        Item item = itemStorage.findById(bookingDto.getItemId())
                .orElseThrow(() -> new ItemNotFoundException("Вещь с таким id не найдена!"));
        isNotValidate(item);
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с таким id не найден!"));
        isEquals(user, item);
        if (bookingDto.getStart().isBefore(LocalDateTime.now()) || bookingDto.getEnd().isBefore(bookingDto.getStart())) {
            throw new WrongTimeException("Неверное время старта и/или конца бронирования!");
        }
        Booking booking = bookingStorage.save(
                new Booking(0,
                        bookingDto.getStart(),
                        bookingDto.getEnd(),
                        item,
                        user,
                        Status.WAITING)
        );
        log.info("The item with the id {} is created", booking.getId());
        return booking;
    }

    @Override
    public Booking editBooking(long userId, long bookingId, boolean approved) {
        Booking booking = bookingStorage.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Бронирование с таким id не найдено!"));
        isEqualsStatus(booking);
        if (userId == booking.getItem().getOwner().getId()) {
            if (approved) {
                booking.setStatus(Status.APPROVED);
            } else {
                booking.setStatus(Status.REJECTED);
            }
        } else {
            throw new BookingNotFoundException("Неверный id пользователя!");
        }
        log.info("Edited items with id {}", bookingStorage.save(booking).getId());
        return bookingStorage.save(booking);
    }


    @Override
    public Booking getBooking(long userId, long bookingId) {
        Booking booking = bookingStorage.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Бронирование с таким id не найдено!"));
        if (booking.getBooker().getId() == userId || booking.getItem().getOwner().getId() == userId) {
            log.info("Found booking with id {}", booking.getId());
            return booking;
        } else {
            throw new BookingNotFoundException("Неверный id пользователя!");
        }
    }

    @Override
    public List<Booking> getAllBookings(long userId, String state, Pageable pageable) {
        userStorage.findById(userId).orElseThrow(() -> new UserNotFoundException("Пользователь с таким id не найден!"));
        try {
            switch (State.valueOf(state)) {
                case ALL:
                    return bookingStorage.findByBookerIdOrderByStartDesc(userId, pageable);
                case WAITING:
                    return bookingStorage.findByBookerIdAndStatusOrderByStartDesc(userId, Status.WAITING);
                case REJECTED:
                    return bookingStorage.findByBookerIdAndStatusOrderByStartDesc(userId, Status.REJECTED);
                case PAST:
                    return bookingStorage.findByBookerIdAndEndBeforeOrderByStartDesc(userId, LocalDateTime.now());
                case CURRENT:
                    return bookingStorage.findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId,
                            LocalDateTime.now(), LocalDateTime.now());
                case FUTURE:
                    return bookingStorage.findByBookerIdAndStartAfterOrderByStartDesc(userId, LocalDateTime.now());
                default:
                    return new ArrayList<>();
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown state: " + state);
        }
    }

    @Override
    public List<Booking> getAllBookingsByOwner(long userId, String state, Pageable pageable) {
        userStorage.findById(userId).orElseThrow(() -> new UserNotFoundException("Пользователь с таким id не найден!"));
        try {
            switch (State.valueOf(state)) {
                case ALL:
                    return bookingStorage.findByItemOwnerIdOrderByStartDesc(userId, pageable);
                case WAITING:
                    return bookingStorage.findByItemOwnerIdAndStatusOrderByStartDesc(userId, Status.WAITING);
                case REJECTED:
                    return bookingStorage.findByItemOwnerIdAndStatusOrderByStartDesc(userId, Status.REJECTED);
                case PAST:
                    return bookingStorage.findByItemOwnerIdAndEndBeforeOrderByStartDesc(userId, LocalDateTime.now());
                case CURRENT:
                    return bookingStorage.findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId,
                            LocalDateTime.now(), LocalDateTime.now());
                case FUTURE:
                    return bookingStorage.findByItemOwnerIdAndStartAfterOrderByStartDesc(userId, LocalDateTime.now());
                default:
                    return new ArrayList<>();
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown state: " + state);
        }
    }


    private void isNotValidate(Item item) {
        if (!item.isAvailable()) {
            throw new ItemNotAvailableException("Вещь недоступна для бронирования!");
        }
    }

    private void isEquals(User user, Item item) {
        if (user.equals(item.getOwner())) {
            throw new UserNotFoundException("Владелец вещи не может её забронировать!");
        }
    }

    private void isEqualsStatus(Booking booking) {
        if (booking.getStatus().equals(Status.APPROVED)) {
            throw new IllegalArgumentException("Booking is already approved!");
        }
    }
}
