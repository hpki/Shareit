package ru.practicum.shareit.item;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import javax.validation.Valid;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public List<ItemDto> getAll(@RequestHeader(value = "X-Sharer-User-Id") long userId) throws NoSuchElementException {
        return itemService.getAll(userId);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItem(@PathVariable long itemId, @RequestHeader(value = "X-Sharer-User-Id") long userId)
            throws NoSuchElementException {
        return itemService.get(itemId, userId);
    }

    @PostMapping
    public Item addItem(@Valid @RequestBody ItemDto item, @RequestHeader(value = "X-Sharer-User-Id") long userId)
            throws NoSuchElementException, IllegalArgumentException {
        return itemService.addItem(item, userId);
    }

    @PatchMapping("/{itemId}")
    public Item editItem(@PathVariable long itemId, @RequestHeader(value = "X-Sharer-User-Id") long userId,
                         @RequestBody ItemDto item) throws AccessDeniedException {
        return itemService.editItem(itemId, userId, item);
    }

    @GetMapping("/search")
    public List<ItemDto> searchByWord(@RequestParam String text) {
        return itemService.search(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@PathVariable("itemId") Long itemId,
                                 @RequestBody CommentDto comment,
                                 @RequestHeader("X-Sharer-User-Id") Long userPrincipal)
            throws NoSuchElementException, IllegalArgumentException {
        return itemService.addComment(itemId, comment, userPrincipal);
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handleNoSuchElement(final NoSuchElementException e) {
        return new ResponseEntity<>(
                Map.of("message", e.getMessage()),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handleIllegalArgument(final IllegalArgumentException e) {
        return new ResponseEntity<>(
                Map.of("message", e.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handleAccessDenied(final AccessDeniedException e) {
        return new ResponseEntity<>(
                Map.of("message", e.getMessage()),
                HttpStatus.NOT_FOUND
        );
    }
}