package ru.practicum.shareit.item;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.ItemRequestNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.requests.ItemRequestRepository;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemRequestRepository itemRequestRepository;

    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;


    public ItemServiceImpl(ItemRepository itemRepository, UserRepository userRepository, ItemRequestRepository itemRequestRepository, BookingRepository bookingRepository, CommentRepository commentRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.itemRequestRepository = itemRequestRepository;
        this.bookingRepository = bookingRepository;
        this.commentRepository = commentRepository;
    }

    public ItemWithBookingDto getItem(long userId, long itemId) {
        List<Booking> itemBookings = bookingRepository.findByItemIdOrderByStartDesc(itemId);
        BookingDto nextBooking = null;
        BookingDto lastBooking = null;
        if (!itemBookings.isEmpty()) {
            if (itemRepository.findById(itemId).get().getOwner().getId() == userId) {
                nextBooking = BookingMapper.toBookingDto(itemBookings.get(0));
                lastBooking = BookingMapper.toBookingDto(itemBookings.get(itemBookings.size() - 1));
            }
        }
        return ItemMapper.toItemWithBookingDto(itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Вещь с таким id не найдена!")), nextBooking, lastBooking, getCommentDtoList(itemId));
    }

    public ItemDto createItem(long userId, ItemDto itemDto) {
        Item item;
        if (itemDto.getRequestId() == null) {
            item = itemRepository.save(ItemMapper.toItem(userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("Пользователь с таким id не найден!")), itemDto));
        } else {
            item = itemRepository.save(ItemMapper.toItemWithRequest(userRepository.findById(userId)
                            .orElseThrow(() -> new UserNotFoundException("Пользователь с таким id не найден!")), itemDto,
                    itemRequestRepository.findById(itemDto.getRequestId()).orElseThrow(() -> new ItemRequestNotFoundException("Запрос вещи с таким id не найден!"))));
        }
        return ItemMapper.toItemDto(item, new ArrayList<>());
    }

    public ItemDto updateItem(long userId, long itemId, ItemDto itemDto) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Вещь с таким id не найдена!"));
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
        return ItemMapper.toItemDto(itemRepository.save(item), getCommentDtoList(itemId));
    }

    public List<ItemWithBookingDto> getAllItems(long userId) {
        return itemRepository.findAll().stream()
                .filter(x -> x.getOwner().getId() == userId)
                .map(x -> getItem(userId, x.getId()))
                .sorted(Comparator.comparingLong(ItemWithBookingDto::getId))
                .collect(Collectors.toList());
    }

    public List<ItemDto> search(String text) {
        if (!text.isBlank()) {
            return itemRepository.search(text).stream()
                    .map(x -> ItemMapper.toItemDto(x, getCommentDtoList(x.getId())))
                    .collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    public CommentDto createComment(long userId, long itemId, Comment comment) {
        List<Booking> userBookings = bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(userId, LocalDateTime.now());
        if (!userBookings.isEmpty()) {
            return CommentMapper.toCommentDto(commentRepository.save(new Comment(comment.getId(),
                    comment.getText(),
                    itemRepository.findById(itemId)
                            .orElseThrow(() -> new ItemNotFoundException("Вещь с таким id не найдена!")),
                    userRepository.findById(userId)
                            .orElseThrow(() -> new UserNotFoundException("Пользователь с таким id не найден!")),
                    LocalDateTime.now())));
        } else {
            throw new IllegalArgumentException(
                    String.format("Пользователь с id=%d не пользовался вещью с id=%d", userId, itemId));
        }
    }

    private List<CommentDto> getCommentDtoList(long itemId) {
        return commentRepository.findByItemIdOrderByIdAsc(itemId).stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }
}
