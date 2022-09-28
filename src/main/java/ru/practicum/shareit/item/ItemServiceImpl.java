package ru.practicum.shareit.item;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingStorage;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.exceptions.ItemNotFoundException;
import ru.practicum.shareit.exceptions.ItemRequestNotFoundException;
import ru.practicum.shareit.exceptions.UserNotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.requests.ItemRequestStorage;
import ru.practicum.shareit.user.UserStorage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {
    private static final Logger log = LoggerFactory.getLogger(ItemServiceImpl.class);

    private final ItemStorage itemStorage;
    private final UserStorage userStorage;
    private final ItemRequestStorage itemRequestStorage;
    private final BookingStorage bookingStorage;
    private final CommentStorage commentStorage;


    public ItemServiceImpl(ItemStorage itemStorage, UserStorage userStorage, ItemRequestStorage itemRequestStorage,
                           BookingStorage bookingStorage, CommentStorage commentStorage) {
        this.itemStorage = itemStorage;
        this.userStorage = userStorage;
        this.itemRequestStorage = itemRequestStorage;
        this.bookingStorage = bookingStorage;
        this.commentStorage = commentStorage;
    }

    public ItemWithBookingDto getItem(long userId, long itemId) {
        List<Booking> itemBookings = bookingStorage.findByItemIdOrderByStartDesc(itemId);
        BookingDto nextBooking = null;
        BookingDto lastBooking = null;
        if (!itemBookings.isEmpty()) {
            if (itemStorage.findById(itemId).get().getOwner().getId() == userId) {
                nextBooking = BookingMapper.toBookingDto(itemBookings.get(0));
                lastBooking = BookingMapper.toBookingDto(itemBookings.get(itemBookings.size() - 1));
            }
        }
        return ItemMapper.toItemWithBookingDto(itemStorage.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Вещь не найдена")), nextBooking, lastBooking,
                getCommentDtoList(itemId));
    }

    public ItemDto addItem(long userId, ItemDto itemDto) {
        Item item;
        if (itemDto.getRequestId() == null) {
            item = itemStorage.save(ItemMapper.toItem(userStorage.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("Пользователь не найден")), itemDto));
        } else {
            item = itemStorage.save(ItemMapper.toItemWithRequest(userStorage.findById(userId)
                            .orElseThrow(() -> new UserNotFoundException("Пользователь не найден")), itemDto,
                    itemRequestStorage.findById(itemDto.getRequestId()).orElseThrow(() ->
                            new ItemRequestNotFoundException("Запрос вещи не найден"))));
        }
        log.debug("Наименование вещи: {}", (ItemMapper.toItemDto(item, new ArrayList<>())).getName());
        return ItemMapper.toItemDto(item, new ArrayList<>());
    }

    public ItemDto editItem(long userId, long itemId, ItemDto itemDto) {
        Item item = itemStorage.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Вещь не найдена"));
        if (item.getOwner().getId() != userId) {
            throw new UserNotFoundException(
                    String.format("Пользователь с id=%d не владеет вещью с id=%d", userId, itemId));
        }
        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
        log.debug("Название вещи: {}", ItemMapper.toItemDto(itemStorage.save(item), getCommentDtoList(itemId)).getName());
        return ItemMapper.toItemDto(itemStorage.save(item), getCommentDtoList(itemId));
    }

    public List<ItemWithBookingDto> getAll(long userId) {
        return itemStorage.findAll().stream()
                .filter(x -> x.getOwner().getId() == userId)
                .map(x -> getItem(userId, x.getId()))
                .sorted(Comparator.comparingLong(ItemWithBookingDto::getId))
                .collect(Collectors.toList());
    }

    public List<ItemDto> search(String text) {
        if (!text.isBlank()) {
            return itemStorage.search(text).stream()
                    .map(x -> ItemMapper.toItemDto(x, getCommentDtoList(x.getId())))
                    .collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    public CommentDto addComment(long userId, long itemId, Comment comment) {
        List<Booking> userBookings = bookingStorage.findByBookerIdAndEndBeforeOrderByStartDesc(userId, LocalDateTime.now());
        if (!userBookings.isEmpty()) {
            return CommentMapper.toCommentDto(commentStorage.save(new Comment(comment.getId(),
                    comment.getText(),
                    itemStorage.findById(itemId)
                            .orElseThrow(() -> new ItemNotFoundException("Вещь не найдена")),
                    userStorage.findById(userId)
                            .orElseThrow(() -> new UserNotFoundException("Пользователь не найден")),
                    LocalDateTime.now())));
        } else {
            throw new IllegalArgumentException(
                    String.format("Пользователь с id=%d не пользовался вещью с id=%d", userId, itemId));
        }
    }

    private List<CommentDto> getCommentDtoList(long itemId) {
        return commentStorage.findByItemId(itemId).stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }
}
