package com.example.foodapp.database;


import com.example.foodapp.models.User;
import java.util.ArrayList;
import java.util.List;

public class UserDatabaseHelper {
    private static List<User> users = new ArrayList<>();
    private static int nextId = 1;

    static {
        User sampleUser = new User();
        sampleUser.setId(1);
        sampleUser.setEmail("test@example.com");
        sampleUser.setUsername("testuser");
        sampleUser.setPassword("123456");
        sampleUser.setFullName("Test User");
        users.add(sampleUser);
    }

    public static boolean registerUser(User user) {
        for (User u : users) {
            if (u.getEmail().equals(user.getEmail())) {
                return false;
            }
        }

        user.setId(nextId++);
        users.add(user);
        return true;
    }

    public static User loginUser(String email, String password) {
        for (User user : users) {
            if (user.getEmail().equals(email) && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }

    public static boolean updateUser(User updatedUser) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId() == updatedUser.getId()) {
                users.set(i, updatedUser);
                return true;
            }
        }
        return false;
    }

    public static User getUserById(int id) {
        for (User user : users) {
            if (user.getId() == id) {
                return user;
            }
        }
        return null;
    }

    public static User getUserByEmail(String email) {
        for (User user : users) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                return user;
            }
        }
        return null;
    }

    public static boolean updatePassword(String email, String newPassword) {
        User user = getUserByEmail(email);
        if (user != null) {
            user.setPassword(newPassword);
            return true;
        }
        return false;
    }
}