package com.mgaye.banking_application.utility;

import java.io.ByteArrayOutputStream;

// 5. Base32 utility class for TOTP
public class Base32 {
    private static final String BASE32_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

    public static String encode(byte[] data) {
        StringBuilder result = new StringBuilder();
        int buffer = 0;
        int bitsLeft = 0;

        for (byte b : data) {
            buffer = (buffer << 8) | (b & 0xFF);
            bitsLeft += 8;

            while (bitsLeft >= 5) {
                result.append(BASE32_ALPHABET.charAt((buffer >> (bitsLeft - 5)) & 31));
                bitsLeft -= 5;
            }
        }

        if (bitsLeft > 0) {
            result.append(BASE32_ALPHABET.charAt((buffer << (5 - bitsLeft)) & 31));
        }

        return result.toString();
    }

    public static byte[] decode(String encoded) {
        encoded = encoded.toUpperCase();
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        int buffer = 0;
        int bitsLeft = 0;

        for (char c : encoded.toCharArray()) {
            int value = BASE32_ALPHABET.indexOf(c);
            if (value >= 0) {
                buffer = (buffer << 5) | value;
                bitsLeft += 5;

                if (bitsLeft >= 8) {
                    result.write((buffer >> (bitsLeft - 8)) & 255);
                    bitsLeft -= 8;
                }
            }
        }

        return result.toByteArray();
    }
}