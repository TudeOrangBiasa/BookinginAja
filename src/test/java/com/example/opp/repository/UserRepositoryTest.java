package com.example.opp.repository;

import com.example.opp.config.AppConfig;
import com.example.opp.database.DatabaseManager;
import com.example.opp.model.User;
import com.example.opp.util.PasswordUtil;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserRepositoryTest {

    private static DatabaseManager dbManager;
    private static UserRepository userRepository;
    private static Long testUserId;

    @BeforeAll
    static void setup() throws Exception {
        AppConfig.load();
        dbManager = DatabaseManager.getInstance();
        dbManager.connect();
        userRepository = new UserRepository();
    }

    @Test
    @Order(1)
    @DisplayName("Should create new user")
    void testCreateUser() throws Exception {
        User user = new User(
            "testuser",
            PasswordUtil.hash("password123"),
            "test@example.com",
            "Test User"
        );

        testUserId = userRepository.save(user);
        assertTrue(testUserId > 0);
        System.out.println("✓ User created with ID: " + testUserId);
    }

    @Test
    @Order(2)
    @DisplayName("Should find user by ID")
    void testFindById() throws Exception {
        Optional<User> user = userRepository.findById(testUserId);
        assertTrue(user.isPresent());
        assertEquals("testuser", user.get().getUsername());
        System.out.println("✓ User found: " + user.get().getUsername());
    }

    @Test
    @Order(3)
    @DisplayName("Should find user by username")
    void testFindByUsername() throws Exception {
        Optional<User> user = userRepository.findByUsername("testuser");
        assertTrue(user.isPresent());
        assertEquals("test@example.com", user.get().getEmail());
        System.out.println("✓ User found by username");
    }

    @Test
    @Order(4)
    @DisplayName("Should list all users")
    void testFindAll() throws Exception {
        List<User> users = userRepository.findAll();
        assertFalse(users.isEmpty());
        System.out.println("✓ Found " + users.size() + " users");
    }

    @Test
    @Order(5)
    @DisplayName("Should update user")
    void testUpdateUser() throws Exception {
        Optional<User> userOpt = userRepository.findById(testUserId);
        assertTrue(userOpt.isPresent());

        User user = userOpt.get();
        user.setFullName("Updated Name");
        
        int updated = userRepository.update(user);
        assertEquals(1, updated);
        System.out.println("✓ User updated");
    }

    @Test
    @Order(6)
    @DisplayName("Should delete user")
    void testDeleteUser() throws Exception {
        int deleted = userRepository.delete(testUserId);
        assertEquals(1, deleted);
        
        Optional<User> user = userRepository.findById(testUserId);
        assertFalse(user.isPresent());
        System.out.println("✓ User deleted");
    }

    @AfterAll
    static void cleanup() {
        if (dbManager != null) {
            dbManager.disconnect();
        }
    }
}
