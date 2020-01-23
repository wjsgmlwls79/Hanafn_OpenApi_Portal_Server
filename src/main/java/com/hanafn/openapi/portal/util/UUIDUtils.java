package com.hanafn.openapi.portal.util;

import org.apache.commons.lang3.StringUtils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.math.BigInteger;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.UUID;

public class UUIDUtils {
    static public String generateUUID() {
        return StringUtils.replace(UUID.randomUUID().toString(), "-", "");
    }

    static public String generateSecret() {
//		KeyGenerator generator = KeyGenerator.getInstance("AES");
//		SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
//		generator.init(256, random); /* 256-bit AES */
//		SecretKey secretKey = generator.generateKey();

        PBEKeySpec keySpec = new PBEKeySpec(UUIDUtils.generateUUID().toCharArray());
        SecretKeyFactory keyFactory = null;
        try {
            keyFactory = SecretKeyFactory.getInstance("PBE");
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        Key secretKey;
        try {
            secretKey = keyFactory.generateSecret( keySpec );
        } catch (InvalidKeySpecException e) {
            return null;
        }

        byte[] binary = secretKey.getEncoded();
        String secret = String.format("%032X", new BigInteger(+1, binary));

        return secret;
    }

    public static void main(String[] args) {

        for (int i = 0; i < 1; i++) {
            System.out.println(UUIDUtils.generateUUID());
            System.out.println(UUIDUtils.generateSecret());
        }

    }
}
