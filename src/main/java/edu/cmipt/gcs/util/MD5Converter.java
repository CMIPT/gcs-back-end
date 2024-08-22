package edu.cmipt.gcs.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Converter {
    public static String convertToMD5(String input) {
        try {
            byte[] hashBytes = MessageDigest.getInstance("MD5").digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }
}
