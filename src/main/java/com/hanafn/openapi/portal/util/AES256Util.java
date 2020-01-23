package com.hanafn.openapi.portal.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
@Slf4j
public class AES256Util {

    private static String iv;
    private static Key keySpec;
    private static String APPSCR_AES_IV;

    @Value("${client.secret.iv}")
    public void setIv (String value) {
        this.APPSCR_AES_IV = value;
    }

    public static String encrypt(String str) throws NoSuchAlgorithmException,
        GeneralSecurityException, UnsupportedEncodingException {

        iv = APPSCR_AES_IV.substring(0, 16);
        byte[] keyBytes = new byte[16];
        byte[] b = APPSCR_AES_IV.getBytes("UTF-8");
        int len = b.length;
        if (len > keyBytes.length) {
            len = keyBytes.length;
        }
        System.arraycopy(b, 0, keyBytes, 0, len);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

        keySpec = keySpec;

        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        c.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(iv.getBytes()));
        byte[] encrypted = c.doFinal(str.getBytes("UTF-8"));
        String enStr = new String(Base64.getEncoder().encode(encrypted));

        return enStr;
    }

    public static String encrypt(String key, String str) throws NoSuchAlgorithmException,
            GeneralSecurityException, UnsupportedEncodingException {

        iv = key.substring(0, 16);
        byte[] keyBytes = new byte[16];
        byte[] b = key.getBytes("UTF-8");
        int len = b.length;
        if (len > keyBytes.length) {
            len = keyBytes.length;
        }
        System.arraycopy(b, 0, keyBytes, 0, len);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        c.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(iv.getBytes()));
        byte[] encrypted = c.doFinal(str.getBytes("UTF-8"));
        String enStr = new String(Base64.getEncoder().encode(encrypted));

        return enStr;
    }

    public static String decrypt(String str) throws NoSuchAlgorithmException,
            GeneralSecurityException, UnsupportedEncodingException {

        iv = APPSCR_AES_IV.substring(0, 16);
        byte[] keyBytes = new byte[16];
        byte[] b = APPSCR_AES_IV.getBytes("UTF-8");
        int len = b.length;
        if (len > keyBytes.length) {
            len = keyBytes.length;
        }
        System.arraycopy(b, 0, keyBytes, 0, len);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

        keySpec = keySpec;

        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        c.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv.getBytes()));
        byte[] byteStr = Base64.getDecoder().decode(str.getBytes());
        System.out.println("★strLength:["+str.length() +"]\n★str:"+str);
        System.out.println("★iv:"+iv);
        return new String(c.doFinal(byteStr), "UTF-8");
    }

    public static String decrypt(String key, String str) throws NoSuchAlgorithmException,
        GeneralSecurityException, UnsupportedEncodingException {

        iv = key.substring(0, 16);
        byte[] keyBytes = new byte[16];
        byte[] b = key.getBytes("UTF-8");
        int len = b.length;
        if (len > keyBytes.length) {
            len = keyBytes.length;
        }
        System.arraycopy(b, 0, keyBytes, 0, len);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

        keySpec = keySpec;

        log.debug("APPSCR_AES_IV: " + APPSCR_AES_IV);

        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        c.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv.getBytes()));

        log.debug("★key: " + key);
        log.debug("★iv: " + iv);
        log.debug("APPSCR_AES_IV: " + APPSCR_AES_IV);
        byte[] byteStr = Base64.getDecoder().decode(str.getBytes());
        return new String(c.doFinal(byteStr), "UTF-8");
    }
}
