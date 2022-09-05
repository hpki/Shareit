package ru.practicum.shareit.item;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.BookingStorage;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserService userService;
    private final BookingStorage bookingStorage;
    private final BookingService bookingService;
    private final CommentStorage commentStorage;

    public ItemServiceImpl(ItemStorage itemRepository, UserService userService, BookingStorage bookingRepository,
                           BookingService bookingService, CommentStorage commentRepository) {
        this.itemStorage = itemRepository;
        this.userService = userService;
        this.bookingStorage = bookingRepository;
        this.bookingService = bookingService;
        this.commentStorage = commentRepository;
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isEmpty() || text.isBlank()) {
            return new ArrayList<>();
        }
        List<ItemDto> result = new ArrayList<>();
        for (Item item : itemStorage.findAll()) {
            if (item.getName().toLowerCase().contains(text.toLowerCase())) {
                if (item.getAvailable()) {
                    result.add(ItemMapper.toItemDto(item));
                }
            } else if (item.getDescription().toLowerCase().contains(text.toLowerCase())) {
                if (item.getAvailable()) {
                    result.add(ItemMapper.toItemDto(item));
                }
            }
        }
        return result;
    }

    @Override
    public ItemDto get(long itemId, long userId) throws NoSuchElementException {
        Item item = getItemById(itemId);
        return setLastAndNextBooking(ItemMapper.toItemDto(item), userId);
    }

    @Override
    public List<ItemDto> getAll(long userId) throws NoSuchElementException {
        User owner = userService.getUserById(userId);
        List<Item> itemDto = itemStorage.findAllByOwnerOrderById(owner);
        List<ItemDto> items = itemDto.stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
        return setLastAndNextBookingForList(items, userId);
    }

    @Override
    public Item addItem(ItemDto itemDto, long userId) throws NoSuchElementException, IllegalArgumentException {
        User owner = userService.getUserById(userId);
        Item item = new Item();
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setOwner(owner);
        item.setAvailable(true);
        return itemStorage.save(item);
    }

    @Override
    public Item editItem(long itemId, long userId, ItemDto itemDto) throws AccessDeniedException, NoSuchElementException {
        User user = userService.getUserById(userId);
        if (getItemById(itemId).getOwner().getId() != user.getId()) {
            throw new AccessDeniedException("Редактировать предмет может только его владелец!");
        }
        Item item = getItemById(itemId);
        item.setName(itemDto.getName() == null ? item.getName() : itemDto.getName());
        item.setDescription(itemDto.getDescription() == null ? item.getDescription() : itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable() == null ? item.getAvailable() : itemDto.getAvailable());

        return itemStorage.save(item);
    }

    @Override
    public Item getItemById(long itemId) throws NoSuchElementException {
        Optional<Item> itemOptional = itemStorage.findById(itemId);
        if (itemOptional.isEmpty()) {
            throw new NoSuchElementException("Элемент не найден!");
        }
        return itemOptional.get();
    }

    @Override
    public CommentDto addComment(Long itemId, CommentDto comment, Long userId)
            throws NoSuchElementException, IllegalArgumentException {
        Comment resultComment = new Comment();
        isValidComment(comment);
        Item item = getItemById(itemId);

        User user = userService.getUserById(userId);
        var bookings = bookingService.checkBookingsForItem(itemId, userId);
        if (bookings.isEmpty()) {
            throw new IllegalArgumentException("У пользователя нет ни одной брони на данную вещь");
        }

        var passedBookings = bookingStorage.findPassedBookings(userId, itemId);
        if (passedBookings.isEmpty()) {
            throw new IllegalArgumentException("Нельзя оставлять комментарии к будущим бронированиям!");
        }

        resultComment.setItem(item);
        resultComment.setAuthor(user);
        resultComment.setText(comment.getText());
        resultComment.setCreated(LocalDateTime.now());
        commentStorage.save(resultComment);
        return CommentMapper.toDto(resultComment);
    }

    private ItemDto setLastAndNextBooking(ItemDto itemDto, Long userId) {
        Booking lastBooking;
        Booking nextBooking;
        
        lastBooking = bookingStorage.findLastBooking(itemDto.getOwner().getId(), itemDto.getId());
        nextBooking = bookingStorage.findNextBooking(itemDto.getOwner().getId(), itemDto.getId());

        itemDto.setLastBooking(lastBooking == null ? null :
                new ItemDto.Booking(lastBooking.getId(), lastBooking.getBooker().getId()));
        itemDto.setNextBooking(nextBooking == null ? null :
                new ItemDto.Booking(nextBooking.getId(), nextBooking.getBooker().getId()));

        return itemDto;
    }

    private List<ItemDto> setLastAndNextBookingForList(List<ItemDto> items, Long userId) {
        return items.stream()
                .map(dto -> setLastAndNextBooking(dto, userId))
                .collect(Collectors.toList());
    }

    private void isValidComment(CommentDto comment) {
        if (StringUtils.isEmpty(comment.getText())) {
            throw new IllegalArgumentException("Комментарий не должен быть пустым");
        }
    }
}