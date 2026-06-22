package com.syncstream.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

//  PasswordUtil — Hashing passwords with SHA-256
public class PasswordUtil {


    public static String hash(String plainText) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(plainText.getBytes("UTF-8"));

            // Convert byte array to hex string
            StringBuilder hex = new StringBuilder();
            for (byte b : hashBytes) {
                // format each byte as 2 hex digits
                hex.append(String.format("%02x", b));
            }
            return hex.toString();

        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    public static boolean verify(String plainText, String storedHash) {
        return hash(plainText).equals(storedHash);
    }
}

