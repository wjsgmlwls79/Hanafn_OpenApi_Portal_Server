package com.hanafn.openapi.portal.util;

import com.hanafn.openapi.portal.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.*;
import java.util.Base64;

@Slf4j
@Component
public class RSAUtil {

    private static final String ALGORITHM_RSA = "RSA";

    /**
     * @param1 target : 대상 String
     * @param2 publicKey : String화된 publicKey
     * @brief target 문자열을 주어진 공개키로 암호화하여 돌려준다.
     */
    public static String encrypt(String target, String strPublicKey) {
        String strResult = "";
        byte[] result = null;
        try {
            PublicKey publicKey = strToPubKey(strPublicKey);
            Cipher cipher = Cipher.getInstance(ALGORITHM_RSA);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            result = cipher.doFinal(target.getBytes());
            strResult = Base64.getEncoder().encodeToString(result);
        } catch (InvalidKeyException e) {
            log.error("공개키 입력 에러");
            throw new BusinessException("올바른 공개키를 입력해주세요.");
        } catch (Exception e) {
            log.error("RSA Util Encryption Error");
            throw new RuntimeException("RSA Util Encryption Error", e.getCause());
        }
        return strResult;
    }

    public static String decrypt(String encodedTarget, String strPrivateKey) {
        byte[] b64_decode = Base64.getDecoder().decode(encodedTarget.getBytes());
        String strResult = "";
        try {
            PrivateKey privateKey = strToPrivateKey(strPrivateKey);
            Cipher cipher = Cipher.getInstance(ALGORITHM_RSA);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] result = cipher.doFinal(b64_decode);
            strResult = new String(result, "euc-kr");
        } catch (InvalidKeyException e) {
            log.error("RSA Decryption Invalid Private Key 입력 에러");
            throw new RuntimeException("RSA Util Decryption Error", e.getCause());
        } catch (Exception e) {
            log.error("RSA Util Decryption Error");
            throw new RuntimeException("RSA Util Decryption Error", e.getCause());
        }
        return strResult;
    }

    public static PublicKey strToPubKey(String strPublicKey) {
        byte[] decodedBytes = Base64.getDecoder().decode(strPublicKey.getBytes());
        PublicKey publicKey = null;
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM_RSA);
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(decodedBytes);
            publicKey = keyFactory.generatePublic(publicKeySpec);
        } catch (InvalidKeySpecException e) {
            log.error("RSA strToPubKey ERROR (INVALID KEY)");
            throw new RuntimeException("RSA strToPubKey ERROR (INVALID KEY)", e.getCause());
        } catch (Exception e) {
            log.error("RSA strToPubKey Error");
            throw new RuntimeException("RSA strToPubKey Error", e.getCause());
        }

        return publicKey;
    }

    public static PrivateKey strToPrivateKey(String strPrivateKey) {
        byte[] bytePrivateKey = Base64.getDecoder().decode(strPrivateKey.getBytes());
        PrivateKey privateKey = null;
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM_RSA);
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(bytePrivateKey);
            privateKey = keyFactory.generatePrivate(privateKeySpec);
        } catch (InvalidKeySpecException e) {
            log.error("RSA strToPrivateKey ERROR (INVALID KEY)");
            throw new RuntimeException("RSA strToPubKey ERROR (INVALID KEY)", e.getCause());
        } catch (Exception e) {
            log.error("RSA strToPrivateKey Error");
            throw new RuntimeException("RSA strToPrivateKey Error", e.getCause());
        }
        return privateKey;
    }

    public static byte[] hexToByteArray(String hex) {
        if (hex == null || hex.length() == 0) {
            return null;
        }
        byte[] ba = new byte[hex.length() / 2];
        for (int i = 0; i < ba.length; i++) {
            ba[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return ba;
    }
}