package com.hotel.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/** Hash password dengan SHA-256 agar password tidak tersimpan sebagai teks biasa di file. */
public final class PasswordUtil {

    private PasswordUtil() {
    }

    public static String hash(String plain) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hasil = md.digest(plain.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hasil);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 tidak tersedia", e);
        }
    }
}
