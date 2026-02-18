package util;

import model.User;

public class SessionManager {
    // Stores the currently logged-in user
    private static User currentUser;

    public static void startSession(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void cleanSession() {
        currentUser = null;
    }
}