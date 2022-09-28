package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ItemDtoTest {
    @Autowired
    private JacksonTester<ItemDto> json;

    @Test
    void testItemDto() throws Exception {
        CommentDto commentDto = new CommentDto(1, "commentText", "commentAuthorName", LocalDateTime.now());
        ItemDto itemDto = new ItemDto(1, "itemDtoName", "itemDtoDescription", true, List.of(commentDto), 1L);
        JsonContent<ItemDto> result = json.write(itemDto);
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("itemDtoName");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("itemDtoDescription");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(true);
        assertThat(result).extractingJsonPathArrayValue("$.comments", List.of(commentDto));
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isEqualTo(1);
    }
}
