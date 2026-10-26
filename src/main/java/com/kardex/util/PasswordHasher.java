package com.kardex.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class PasswordHasher {

    /**
     * Genera un hash SHA-256 en formato hexadecimal.
     */
    public static String hash(String plainText) {
        if (plainText == null) return "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(plainText.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error al hashear contraseña", e);
        }
    }

    /**
     * Compara una contraseña ingresada con el hash o texto guardado en BD.
     */
    public static boolean verify(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null) return false;
        // 1. Comparar con hash SHA-256
        String hashedInput = hash(rawPassword);
        if (hashedInput.equalsIgnoreCase(storedHash)) {
            return true;
        }
        // 2. Soporte por compatibilidad si la contraseña se guardó en texto plano
        return rawPassword.equals(storedHash);
    }
}
