package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Transactional
@DataJpaTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext
public class ItemRepositoryTest {
    private final TestEntityManager em;
    private final ItemRepository itemRepository;

    @Test
    void search() {
        em.getEntityManager().createNativeQuery("insert into users (name, email) values (?,?)")
                .setParameter(1, "testOwnerName")
                .setParameter(2, "testOwnerEmail@yandex.ru")
                .executeUpdate();
        em.getEntityManager().createNativeQuery("insert into items (name, description, available, owner_id) values (?,?,?,?)")
                .setParameter(1, "testSearchName")
                .setParameter(2, "testDescription")
                .setParameter(3, true)
                .setParameter(4, 1)
                .executeUpdate();
        em.getEntityManager().createNativeQuery("insert into items (name, description, available, owner_id) values (?,?,?,?)")
                .setParameter(1, "testName")
                .setParameter(2, "testSearchDescription")
                .setParameter(3, true)
                .setParameter(4, 1)
                .executeUpdate();
        User userOwner = new User(1, "testOwnerName", "testOwnerEmail@yandex.ru");
        Item item1 = new Item(1, "testSearchName", "testDescription", true, userOwner, null);
        Item item2 = new Item(2, "testName", "testSearchDescription", true, userOwner, null);
        assertThat(List.of(item1, item2), equalTo(itemRepository.search("search")));
    }
}
