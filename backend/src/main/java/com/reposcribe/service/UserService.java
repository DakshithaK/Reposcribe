package com.reposcribe.service;

import com.reposcribe.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserService {

    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final PasswordEncoder passwordEncoder;

    public UserService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Register a new user
     * @param username Username
     * @param password Plain text password (will be hashed)
     * @return User if successful, null if username already exists
     */
    public User register(String username, String password) {
        if (users.containsKey(username)) {
            return null; // Username already exists
        }

        // Hash the password before storing
        String hashedPassword = passwordEncoder.encode(password);
        User user = new User(username, hashedPassword);
        users.put(username, user);
        return user;
    }

    /**
     * Authenticate a user
     * @param username Username
     * @param password Plain text password
     * @return User if credentials are valid, null otherwise
     */
    public User authenticate(String username, String password) {
        User user = users.get(username);
        if (user == null) {
            return null; // User doesn't exist
        }

        // Check if password matches the hash
        if (passwordEncoder.matches(password, user.getPasswordHash())) {
            return user;
        }

        return null; // Invalid password
    }

    /**
     * Get user by username
     */
    public User findByUsername(String username) {
        return users.get(username);
    }
}

