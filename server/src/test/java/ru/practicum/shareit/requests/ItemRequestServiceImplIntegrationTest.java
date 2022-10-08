package ru.practicum.shareit.requests;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.requests.dto.ItemRequestDto;
import ru.practicum.shareit.requests.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Transactional
@SpringBootTest(
        properties = "db.name=shareit-test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext
public class ItemRequestServiceImplIntegrationTest {
    private final EntityManager em;
    private final ItemRequestServiceImpl itemRequestService;

    @Test
    void getItemRequestById() {
        em.createNativeQuery("insert into users (name, email) values (?,?)")
                .setParameter(1, "userOwnerName")
                .setParameter(2, "userOwnerEmail@mail.ru")
                .executeUpdate();
        em.createNativeQuery("insert into users (name, email) values (?,?)")
                .setParameter(1, "userRequestorName")
                .setParameter(2, "userRequestorEmail@mail.ru")
                .executeUpdate();
        em.createNativeQuery("insert into requests (description, requestor_id, created) values (?,?,?)")
                .setParameter(1, "requestForItem1")
                .setParameter(2, 2)
                .setParameter(3, LocalDateTime.of(1990, Month.APRIL, 2, 12, 12, 12))
                .executeUpdate();
        em.createNativeQuery("insert into items (name, description, available, owner_id, request_id) values (?,?,?,?,?)")
                .setParameter(1, "itemName1")
                .setParameter(2, "itemDescription1")
                .setParameter(3, true)
                .setParameter(4, 1)
                .setParameter(5, 1)
                .executeUpdate();

        User user = new User(2, "userRequestorName", "userRequestorEmail@mail.ru");
        ItemRequest itemRequest = new ItemRequest(1, "requestForItem1", user, LocalDateTime.of(1990, Month.APRIL, 2, 12, 12, 12));
        Item item = new Item(1, "itemName1", "itemDescription1", true, user, itemRequest);
        ItemRequestDto itemRequestDto = ItemRequestMapper.toItemRequestDto(itemRequest);
        itemRequestDto.setItems(List.of(ItemMapper.toItemDto(item, new ArrayList<>())));
        assertThat(itemRequestService.getItemRequestById(2, 1), equalTo(itemRequestDto));
    }
}
