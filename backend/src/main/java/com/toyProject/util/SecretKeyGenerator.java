package com.toyProject.util;

import java.security.SecureRandom;
import java.util.Base64;

public class SecretKeyGenerator {
    public static void main(String[] args) {
        byte[] key1 = new byte[32]; // Access Token Secret Key
        byte[] key2 = new byte[32]; // Refresh Token Secret Key

        new SecureRandom().nextBytes(key1);
        new SecureRandom().nextBytes(key2);

        String accessSecret = Base64.getEncoder().encodeToString(key1);
        String refreshSecret = Base64.getEncoder().encodeToString(key2);

        System.out.println("Access Token Secret Key: " + accessSecret);
        System.out.println("Refresh Token Secret Key: " + refreshSecret);
    }
}

