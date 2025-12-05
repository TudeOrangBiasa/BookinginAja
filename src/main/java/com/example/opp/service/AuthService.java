package com.example.opp.service;

import com.example.opp.model.User;
import com.example.opp.repository.UserRepository;
import com.example.opp.util.PasswordUtil;

import java.sql.SQLException;
import java.util.Optional;

public class AuthService {

    private final UserRepository userRepository;
    private User currentUser;

    public AuthService() {
        this.userRepository = new UserRepository();
    }

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean login(String username, String password) {
        try {
            Optional<User> userOpt = userRepository.findByUsername(username);

            if (userOpt.isEmpty()) {
                return false;
            }

            User user = userOpt.get();

            if (!user.isActive()) {
                return false;
            }

            if (PasswordUtil.verify(password, user.getPassword())) {
                currentUser = user;
                return true;
            }

            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void logout() {
        currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean register(String username, String password, String email, String fullName) {
        try {
            if (userRepository.existsByUsername(username)) {
                return false;
            }

            String hashedPassword = PasswordUtil.hash(password);
            User user = new User();
            user.setUsername(username);
            user.setPassword(hashedPassword);
            user.setEmail(email);
            user.setFullName(fullName);
 
            
            long id = userRepository.save(user);
            return id > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
