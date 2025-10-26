package com.rideshare.userservice.repository;

import com.rideshare.userservice.entity.Role;
import com.rideshare.userservice.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@DataJpaTest
@Testcontainers
@EnableJpaRepositories(basePackages = "com.rideshare.userservice.repository")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {
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
    void testUserSaveAndFindByMobileNumber() {
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setGender("Male");
        user.setMobileNumber("9999999999");
        user.setRole(riderRole);

        User saved = userRepository.save(user);
        assertThat(saved.getId()).isNotNull();

        User found = userRepository.findByMobileNumber("9999999999").orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getFirstName()).isEqualTo("John");
        assertThat(found.getRole().getName()).isEqualTo("RIDER");
    }
}
