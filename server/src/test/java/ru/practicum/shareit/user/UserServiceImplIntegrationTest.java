package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Transactional
@SpringBootTest(
        properties = "db.name=shareit-test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext
public class UserServiceImplIntegrationTest {
    private final EntityManager em;
    private final UserServiceImpl userService;

    @Test
    void getAllUsers() {
        em.createNativeQuery("insert into users (name, email) values (?,?)")
                .setParameter(1, "userOwnerName")
                .setParameter(2, "userOwnerEmail@mail.ru")
                .executeUpdate();
        em.createNativeQuery("insert into users (name, email) values (?,?)")
                .setParameter(1, "userBookerName")
                .setParameter(2, "userBookerEmail@mail.ru")
                .executeUpdate();
        User userOwner = new User(1, "userOwnerName", "userOwnerEmail@mail.ru");
        User userBooker = new User(2, "userBookerName", "userBookerEmail@mail.ru");
        assertThat(userService.getAllUsers(), equalTo(List.of(UserMapper.toUserDto(userOwner), UserMapper.toUserDto(userBooker))));
    }
}
