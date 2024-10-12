package edu.cmipt.gcs.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class MD5Converter {
    private static String MD5_SALT;

    @Value("${md5.salt}")
    public void setMD5Salt(String salt) {
        MD5_SALT = salt;
        if (MD5_SALT == null) {
            MD5_SALT = "";
        }
    }

    public static String convertToMD5(String input) {
        try {
            byte[] hashBytes =
                    MessageDigest.getInstance("MD5")
                            .digest(
                                    new StringBuilder(input)
                                            .append(MD5_SALT)
                                            .toString()
                                            .getBytes());
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
            throw new RuntimeException(e);
        }
    }
}
