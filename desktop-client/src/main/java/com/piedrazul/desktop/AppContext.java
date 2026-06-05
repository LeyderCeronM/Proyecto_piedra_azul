package com.piedrazul.desktop;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Contexto de la aplicación para almacenar token y datos del usuario en memoria.
 */
public class AppContext {

    private static volatile String token = null;
    private static volatile JsonNode user = null;

    private AppContext() { }

    public static synchronized void setToken(String t) {
        token = t;
    }

    public static synchronized String getToken() {
        return token;
    }

    public static synchronized void clearToken() {
        token = null;
        user = null;
    }

    public static synchronized void setUser(JsonNode u) {
        user = u;
    }

    public static synchronized JsonNode getUser() {
        return user;
    }
}
