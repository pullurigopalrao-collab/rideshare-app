package com.rideshare.userservice.repository;

import com.rideshare.userservice.entity.Role;
import com.rideshare.userservice.entity.User;
import com.rideshare.userservice.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.config.import=",
        "spring.cloud.vault.enabled=false"
})
class UserRepositoryTest {

    static {
        System.setProperty("spring.config.import", "");
        System.setProperty("spring.cloud.bootstrap.enabled", "false");
        System.setProperty("spring.cloud.vault.enabled", "false");
    }

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private Role riderRole;

    @BeforeEach
    void setup() {
        riderRole = new Role();
        riderRole.setName("RIDER");
        riderRole = roleRepository.save(riderRole);
    }

    @Test
    void findByMobileNumberReturnsUserWhenMobileNumberExists() {
        User user = new User();
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setGender("Female");
        user.setMobileNumber("8888888888");
        user.setStatus(UserStatus.REGISTERED);

        User saved = userRepository.save(user);

        Optional<User> found = userRepository.findByMobileNumber("8888888888");
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
        assertThat(found.get().getFirstName()).isEqualTo("Jane");
    }

    @Test
    void findByMobileNumberReturnsEmptyWhenMobileNumberDoesNotExist() {
        Optional<User> found = userRepository.findByMobileNumber("7777777777");
        assertThat(found).isEmpty();
    }
}
