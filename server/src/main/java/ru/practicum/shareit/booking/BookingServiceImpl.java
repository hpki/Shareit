package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.enums.State;
import ru.practicum.shareit.enums.Status;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public BookingServiceImpl(BookingRepository bookingRepository, ItemRepository itemRepository, UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Booking createBooking(long userId, BookingDto bookingDto) {
        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new ItemNotFoundException("Вещь с таким id не найдена!"));
        if (!item.isAvailable()) {
            throw new ItemNotAvailableException("Вещь недоступна для бронирования!");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с таким id не найден!"));
        if (user.equals(item.getOwner())) {
            throw new UserNotFoundException("Владелец вещи не может её забронировать!");
        }
        if (bookingDto.getStart().isBefore(LocalDateTime.now()) || bookingDto.getEnd().isBefore(bookingDto.getStart())) {
            throw new WrongStartOrEndTimeException("Неверное время старта и/или конца бронирования!");
        }
        Booking booking = bookingRepository.save(
                new Booking(0,
                        bookingDto.getStart(),
                        bookingDto.getEnd(),
                        item,
                        user,
                        Status.WAITING)
        );
        return booking;
    }

    @Override
    public Booking updateBooking(long userId, long bookingId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Бронирование с таким id не найдено!"));
        if (booking.getStatus().equals(Status.APPROVED)) {
            throw new IllegalArgumentException("Booking is already approved!");
        }
        if (userId == booking.getItem().getOwner().getId()) {
            if (approved) {
                booking.setStatus(Status.APPROVED);
            } else {
                booking.setStatus(Status.REJECTED);
            }
        } else {
            throw new BookingNotFoundException("Неверный id пользователя!");
        }
        return bookingRepository.save(booking);
    }


    @Override
    public Booking getBooking(long userId, long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Бронирование с таким id не найдено!"));
        if (booking.getBooker().getId() == userId || booking.getItem().getOwner().getId() == userId) {
            return booking;
        } else {
            throw new BookingNotFoundException("Неверный id пользователя!");
        }
    }

    @Override
    public List<Booking> getAllBookings(long userId, String state, Pageable pageable) {
        userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("Пользователь с таким id не найден!"));
        try {
            switch (State.valueOf(state)) {
                case ALL:
                    return bookingRepository.findByBookerIdOrderByStartDesc(userId, pageable);
                case WAITING:
                    return bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, Status.WAITING);
                case REJECTED:
                    return bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, Status.REJECTED);
                case PAST:
                    return bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(userId, LocalDateTime.now());
                case CURRENT:
                    return bookingRepository.findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, LocalDateTime.now(), LocalDateTime.now());
                case FUTURE:
                    return bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(userId, LocalDateTime.now());
                default:
                    return new ArrayList<>();
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown state: " + state);
        }
    }

    @Override
    public List<Booking> getAllBookingsForOwner(long userId, String state, Pageable pageable) {
        userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("Пользователь с таким id не найден!"));
        try {
            switch (State.valueOf(state)) {
                case ALL:
                    return bookingRepository.findByItemOwnerIdOrderByStartDesc(userId, pageable);
                case WAITING:
                    return bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId, Status.WAITING);
                case REJECTED:
                    return bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId, Status.REJECTED);
                case PAST:
                    return bookingRepository.findByItemOwnerIdAndEndBeforeOrderByStartDesc(userId, LocalDateTime.now());
                case CURRENT:
                    return bookingRepository.findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, LocalDateTime.now(), LocalDateTime.now());
                case FUTURE:
                    return bookingRepository.findByItemOwnerIdAndStartAfterOrderByStartDesc(userId, LocalDateTime.now());
                default:
                    return new ArrayList<>();
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown state: " + state);
        }
    }
}
