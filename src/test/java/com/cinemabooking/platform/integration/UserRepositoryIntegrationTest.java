package com.cinemabooking.platform.integration;

import com.cinemabooking.platform.model.AppUser;
import com.cinemabooking.platform.model.enums.AppRole;
import com.cinemabooking.platform.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryIntegrationTest
        extends PostgreSQLIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindActiveUserIgnoringEmailCase() {
        AppUser user = createUser(
                "MARKO@EXAMPLE.COM"
        );

        AppUser savedUser =
                userRepository.saveAndFlush(user);

        AppUser foundUser = userRepository
                .findByEmailIgnoreCaseAndActiveTrue(
                        "marko@example.com"
                )
                .orElseThrow();

        assertNotNull(savedUser.getId());
        assertNotNull(savedUser.getCreatedAt());
        assertEquals(
                "marko@example.com",
                savedUser.getEmail()
        );
        assertEquals(savedUser.getId(), foundUser.getId());
        assertEquals(AppRole.CUSTOMER, foundUser.getRole());
    }

    @Test
    void shouldRejectDuplicateNormalizedEmail() {
        AppUser firstUser = createUser(
                "marko@example.com"
        );

        AppUser secondUser = createUser(
                "MARKO@EXAMPLE.COM"
        );

        userRepository.saveAndFlush(firstUser);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> userRepository.saveAndFlush(
                        secondUser
                )
        );
    }

    private AppUser createUser(String email) {
        AppUser user = new AppUser();

        user.setFirstName("Marko");
        user.setLastName("Grozdanovic");
        user.setEmail(email);
        user.setPassword("hashed-password");
        user.setRole(AppRole.CUSTOMER);
        user.setActive(true);

        return user;
    }
}