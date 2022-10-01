package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
public class ItemControllerTest {
    @Autowired
    ObjectMapper mapper;

    @MockBean
    ItemService itemService;

    @Autowired
    private MockMvc mvc;

    User userOwner = new User(1, "testOwnerName", "testOwnerEmail@yandex.ru");
    User userCommentator = new User(2, "testCommentatorName", "testCommentatorEmail@yandex.ru");
    Item item = new Item(1, "testName", "testDescription", true, userOwner, null);
    Comment comment = new Comment(1, "testText", item, userCommentator, LocalDateTime.of(2020, Month.APRIL, 2, 12, 12, 12));
    CommentDto commentDto = CommentMapper.toCommentDto(comment);
    ItemDto itemDto = ItemMapper.toItemDto(item, List.of(commentDto));
    ItemWithBookingDto itemWithBookingDto = ItemMapper.toItemWithBookingDto(item, null, null, List.of(commentDto));

    @Test
    void saveNewItem() throws Exception {
        when(itemService.createItem(anyLong(), any())).thenReturn(itemDto);
        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$.comments[0].id", is(commentDto.getId()), Long.class))
                .andExpect(jsonPath("$.comments[0].text", is(commentDto.getText())))
                .andExpect(jsonPath("$.comments[0].authorName", is(commentDto.getAuthorName())))
                .andExpect(jsonPath("$.comments[0].created", is(commentDto.getCreated().withNano(0).toString())))
                .andExpect(jsonPath("$.requestId", nullValue()));
    }

    @Test
    void updateExistingItem() throws Exception {
        when(itemService.updateItem(anyLong(), anyLong(), any())).thenReturn(itemDto);
        mvc.perform(patch("/items/{itemId}", String.valueOf(item.getId()))
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$.comments[0].id", is(commentDto.getId()), Long.class))
                .andExpect(jsonPath("$.comments[0].text", is(commentDto.getText())))
                .andExpect(jsonPath("$.comments[0].authorName", is(commentDto.getAuthorName())))
                .andExpect(jsonPath("$.comments[0].created", is(commentDto.getCreated().withNano(0).toString())))
                .andExpect(jsonPath("$.requestId", nullValue()));
    }

    @Test
    void getItemById() throws Exception {
        when(itemService.getItem(anyLong(), anyLong())).thenReturn(itemWithBookingDto);
        mvc.perform(get("/items/{itemId}", String.valueOf(item.getId()))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemWithBookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemWithBookingDto.getName())))
                .andExpect(jsonPath("$.description", is(itemWithBookingDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemWithBookingDto.getAvailable())))
                .andExpect(jsonPath("$.comments[0].id", is(commentDto.getId()), Long.class))
                .andExpect(jsonPath("$.comments[0].text", is(commentDto.getText())))
                .andExpect(jsonPath("$.comments[0].authorName", is(commentDto.getAuthorName())))
                .andExpect(jsonPath("$.comments[0].created", is(commentDto.getCreated().withNano(0).toString())))
                .andExpect(jsonPath("$.nextBooking", nullValue()))
                .andExpect(jsonPath("$.lastBooking", nullValue()));
    }

    @Test
    void getAllItems() throws Exception {
        when(itemService.getAllItems(anyLong())).thenReturn(List.of(itemWithBookingDto));
        mvc.perform(get("/items")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(itemWithBookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].name", is(itemWithBookingDto.getName())))
                .andExpect(jsonPath("$.[0].description", is(itemWithBookingDto.getDescription())))
                .andExpect(jsonPath("$.[0].available", is(itemWithBookingDto.getAvailable())))
                .andExpect(jsonPath("$.[0].comments[0].id", is(commentDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].comments[0].text", is(commentDto.getText())))
                .andExpect(jsonPath("$.[0].comments[0].authorName", is(commentDto.getAuthorName())))
                .andExpect(jsonPath("$.[0].comments[0].created", is(commentDto.getCreated().withNano(0).toString())))
                .andExpect(jsonPath("$.[0].nextBooking", nullValue()))
                .andExpect(jsonPath("$.[0].lastBooking", nullValue()));
    }

    @Test
    void searchForItems() throws Exception {
        when(itemService.search(anyString())).thenReturn(List.of(itemDto));
        mvc.perform(get("/items/search")
                        .param("text", "testText")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].name", is(itemDto.getName())))
                .andExpect(jsonPath("$.[0].description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.[0].available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$.[0].comments[0].id", is(commentDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].comments[0].text", is(commentDto.getText())))
                .andExpect(jsonPath("$.[0].comments[0].authorName", is(commentDto.getAuthorName())))
                .andExpect(jsonPath("$.[0].comments[0].created", is(commentDto.getCreated().withNano(0).toString())))
                .andExpect(jsonPath("$.[0].requestId", nullValue()));
    }

    @Test
    void saveNewComment() throws Exception {
        when(itemService.createComment(anyLong(), anyLong(), any())).thenReturn(commentDto);
        mvc.perform(post("/items/{itemId}/comment", String.valueOf(item.getId()))
                        .header("X-Sharer-User-Id", 1)
                        .content(mapper.writeValueAsString(comment))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(comment.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(comment.getText())))
                .andExpect(jsonPath("$.authorName", is(comment.getAuthor().getName())))
                .andExpect(jsonPath("$.created", is(commentDto.getCreated().withNano(0).toString())));
    }
}
