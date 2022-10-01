package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
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
public class ItemServiseImplIntegrationTest {
    private final EntityManager em;
    private final ItemServiceImpl itemService;

    @Test
    void getAllItems() {
        em.createNativeQuery("insert into users (name, email) values (?, ?)")
                .setParameter(1, "userName")
                .setParameter(2, "userEmail@mail.ru")
                .executeUpdate();
        em.createNativeQuery("insert into items (name, description, available, owner_id) values (?, ?, ?,?)")
                .setParameter(1, "itemName1")
                .setParameter(2, "itemDescription1")
                .setParameter(3, true)
                .setParameter(4, 1)
                .executeUpdate();
        em.createNativeQuery("insert into items (name, description, available, owner_id) values (?, ?, ?,?)")
                .setParameter(1, "itemName2")
                .setParameter(2, "itemDescription2")
                .setParameter(3, true)
                .setParameter(4, 1)
                .executeUpdate();
        User user = new User(1, "userName", "userEmail@mail.ru");
        ItemWithBookingDto itemWithBookingDto1 = ItemMapper.toItemWithBookingDto(
                new Item(1, "itemName1", "itemDescription1", true, user, null),
                null,
                null,
                new ArrayList<>());
        ItemWithBookingDto itemWithBookingDto2 = ItemMapper.toItemWithBookingDto(
                new Item(2, "itemName2", "itemDescription2", true, user, null),
                null,
                null,
                new ArrayList<>());
        assertThat(itemService.getAllItems(1), equalTo(List.of(itemWithBookingDto1, itemWithBookingDto2)));
    }
}
