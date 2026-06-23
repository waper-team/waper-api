package com.waper.waperapi.validation;

import java.util.regex.Pattern;

public final class UserInputValidator {

    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern USERNAME_PATTERN =
        Pattern.compile("^[A-Za-z0-9_.]{3,30}$");

    private UserInputValidator() {
    }

    public static String validateRegistration(
        String name,
        String username,
        String email,
        String password
    ) {
        if (isBlank(name) || isBlank(username) || isBlank(email) || isBlank(password)) {
            return "Nombre, username, email y password son obligatorios";
        }
        if (name.trim().length() > 80) return "El nombre no puede superar 80 caracteres";
        if (!USERNAME_PATTERN.matcher(username.trim()).matches()) {
            return "El username debe tener entre 3 y 30 caracteres y usar solo letras, numeros, punto o guion bajo";
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) return "El email no es valido";
        if (password.length() < 8) return "La password debe tener al menos 8 caracteres";
        return null;
    }

    public static String validateUpdate(
        String name,
        String username,
        String email,
        String password,
        String bio
    ) {
        if (name != null && name.trim().length() > 80) {
            return "El nombre no puede superar 80 caracteres";
        }
        if (username != null && !USERNAME_PATTERN.matcher(username.trim()).matches()) {
            return "El username debe tener entre 3 y 30 caracteres y usar solo letras, numeros, punto o guion bajo";
        }
        if (email != null && !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            return "El email no es valido";
        }
        if (password != null && !password.isBlank() && password.length() < 8) {
            return "La password debe tener al menos 8 caracteres";
        }
        if (bio != null && bio.length() > 300) {
            return "La biografia no puede superar 300 caracteres";
        }
        return null;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
