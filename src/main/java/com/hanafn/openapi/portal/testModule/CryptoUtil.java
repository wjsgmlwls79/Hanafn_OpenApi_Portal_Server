package com.hanafn.openapi.portal.testModule;

import sun.misc.BASE64Decoder;

import java.io.File;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoUtil {

    private static final String  KEY_SPEC      = "AES";
    private static final String  ALGORITHM     = "AES/CBC/PKCS5Padding";

    /*** 입력받은 key를 32byte로 패딩하여 암호화 ***/
    public static String encryptAES256(String key, String msg, String charset) throws Exception {

        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[20];
        random.nextBytes(bytes);
        byte[] saltBytes = bytes;

        SecretKeySpec secret = new SecretKeySpec(keyPadding(key).getBytes(), KEY_SPEC);

        // 알고리즘/모드/패딩
        // CBC : Cipher Block Chaining Mode
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secret);
        AlgorithmParameters params = cipher.getParameters();

        // Initial Vector(1단계 암호화 블록용)
        byte[] ivBytes = params.getParameterSpec(IvParameterSpec.class).getIV();

        byte[] encryptedTextBytes = cipher.doFinal(msg.getBytes(charset));

        byte[] buffer = new byte[saltBytes.length + ivBytes.length + encryptedTextBytes.length];
        System.arraycopy(saltBytes, 0, buffer, 0, saltBytes.length);
        System.arraycopy(ivBytes, 0, buffer, saltBytes.length, ivBytes.length);
        System.arraycopy(encryptedTextBytes, 0, buffer, saltBytes.length + ivBytes.length, encryptedTextBytes.length);

        return Base64.getEncoder().encodeToString(buffer);
    }

    /*** 입력받은 key를 32byte로 패딩하여 복호화 ***/
    public static String decryptAES256(String key, String msg, String charset) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        ByteBuffer buffer = ByteBuffer.wrap(Base64.getDecoder().decode(msg));

        byte[] saltBytes = new byte[20];
        buffer.get(saltBytes, 0, saltBytes.length);
//        System.out.println("salt >> " + bytesToHexString(saltBytes));

        byte[] ivBytes = new byte[cipher.getBlockSize()];
        buffer.get(ivBytes, 0, ivBytes.length);
//        System.out.println("iv >> " + bytesToHexString(ivBytes));

        byte[] encryptedTextBytes = new byte[buffer.capacity() - saltBytes.length - ivBytes.length];
        buffer.get(encryptedTextBytes);
//        System.out.println("encryptedTextBytes >> " + bytesToHexString(encryptedTextBytes));

        SecretKeySpec secret = new SecretKeySpec(keyPadding(key).getBytes(), KEY_SPEC);
//        System.out.println("keyPadding >> " + keyPadding(key));

        cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(ivBytes));

        byte[] decryptedTextBytes = cipher.doFinal(encryptedTextBytes);
        return new String(decryptedTextBytes, charset);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    /*** 입력받은 Hex String key를 32byte로 패딩하여 암호화 ***/
    public static String encryptAES256_hexKey(String key, String msg, String charset) throws Exception {

        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[20];
        random.nextBytes(bytes);
        byte[] saltBytes = bytes;

        SecretKeySpec secret = new SecretKeySpec(keyPadding(hexToKey(key)).getBytes(), KEY_SPEC);

        // 알고리즘/모드/패딩
        // CBC : Cipher Block Chaining Mode
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secret);
        AlgorithmParameters params = cipher.getParameters();

        // Initial Vector(1단계 암호화 블록용)
        byte[] ivBytes = params.getParameterSpec(IvParameterSpec.class).getIV();

        byte[] encryptedTextBytes = cipher.doFinal(msg.getBytes(charset));

        byte[] buffer = new byte[saltBytes.length + ivBytes.length + encryptedTextBytes.length];
        System.arraycopy(saltBytes, 0, buffer, 0, saltBytes.length);
        System.arraycopy(ivBytes, 0, buffer, saltBytes.length, ivBytes.length);
        System.arraycopy(encryptedTextBytes, 0, buffer, saltBytes.length + ivBytes.length, encryptedTextBytes.length);

        return Base64.getEncoder().encodeToString(buffer);
    }

    /*** 입력받은 Hex String key를 32byte로 패딩하여 복호화 ***/
    public static String decryptAES256_hexKey(String key, String msg, String charset) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        ByteBuffer buffer = ByteBuffer.wrap(Base64.getDecoder().decode(msg));

        byte[] saltBytes = new byte[20];
        buffer.get(saltBytes, 0, saltBytes.length);

        byte[] ivBytes = new byte[cipher.getBlockSize()];
        buffer.get(ivBytes, 0, ivBytes.length);

        byte[] encryptedTextBytes = new byte[buffer.capacity() - saltBytes.length - ivBytes.length];
        buffer.get(encryptedTextBytes);

        SecretKeySpec secret = new SecretKeySpec(keyPadding(hexToKey(key)).getBytes(), KEY_SPEC);

        cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(ivBytes));

        byte[] decryptedTextBytes = cipher.doFinal(encryptedTextBytes);
        return new String(decryptedTextBytes, charset);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    /*** 암호화된 key를 이용하여 암호화 ***/
    public static String encryptAES256_bank(String key, String msg, String charset) throws Exception {

        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[20];
        random.nextBytes(bytes);
        byte[] saltBytes = bytes;

        byte[] decKey = BankKeyGen.loadKey_noFile(key);
        SecretKeySpec secret = new SecretKeySpec(decKey, KEY_SPEC);

        // 알고리즘/모드/패딩
        // CBC : Cipher Block Chaining Mode
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secret);
        AlgorithmParameters params = cipher.getParameters();

        // Initial Vector(1단계 암호화 블록용)
        byte[] ivBytes = params.getParameterSpec(IvParameterSpec.class).getIV();

        byte[] encryptedTextBytes = cipher.doFinal(msg.getBytes(charset));

        byte[] buffer = new byte[saltBytes.length + ivBytes.length + encryptedTextBytes.length];
        System.arraycopy(saltBytes, 0, buffer, 0, saltBytes.length);
        System.arraycopy(ivBytes, 0, buffer, saltBytes.length, ivBytes.length);
        System.arraycopy(encryptedTextBytes, 0, buffer, saltBytes.length + ivBytes.length, encryptedTextBytes.length);

        return Base64.getEncoder().encodeToString(buffer);
    }

    /*** 암호화된 key를 이용하여 복호화 ***/
    public static String decryptAES256_bank(String key, String msg, String charset) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        ByteBuffer buffer = ByteBuffer.wrap(Base64.getDecoder().decode(msg));

        byte[] saltBytes = new byte[20];
        buffer.get(saltBytes, 0, saltBytes.length);

        byte[] ivBytes = new byte[cipher.getBlockSize()];
        buffer.get(ivBytes, 0, ivBytes.length);

        byte[] encryptedTextBytes = new byte[buffer.capacity() - saltBytes.length - ivBytes.length];
        buffer.get(encryptedTextBytes);

        byte[] decKey = BankKeyGen.loadKey_noFile(key);
        SecretKeySpec secret = new SecretKeySpec(decKey, KEY_SPEC);

        cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(ivBytes));

        byte[] decryptedTextBytes = cipher.doFinal(encryptedTextBytes);
        return new String(decryptedTextBytes, charset);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    public static String encryptAES256(String hexKey, String kexIv, String str, String charset) throws Exception {
        byte[] ivBytes  = hexToByteArray(kexIv);
        byte[] keyBytes = hexToByteArray(hexKey);
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
        byte[] encrypted = cipher.doFinal(str.getBytes(charset));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static String decryptAES256(String hexKey, String kexIv, String encStr, String charset) throws Exception {
        byte[] ivBytes  = hexToByteArray(kexIv);
        byte[] keyBytes = hexToByteArray(hexKey);
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
        byte[] encrypted = Base64.getDecoder().decode(encStr);
        return new String(cipher.doFinal(encrypted), charset);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    /*** byte 배열을 hex string으로 변환 ***/
    private static String bytesToHexString(byte[] bytes) {
        final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for ( int j = 0; j < bytes.length; j++ ) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    private static byte[] hexToByteArray(String hex) {
        if (hex == null || hex.length() == 0) {
            return null;
        }

        byte[] ba = new byte[hex.length() / 2];
        for (int i=0; i < ba.length ; i++) {
            ba[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return ba;
    }


    /*** key 길이를 32byte로 일정하게 패딩 ***/
    private static String keyPadding(String src)
    {
        int keyLen = 32;
        if (src.length() <=16) {
            keyLen = 16;
        }
        int src_len = src.length();
        int dest_len = keyLen;
        String result = new String();

        if(src_len == keyLen) {
            result = src;
        }
        else if(src_len>0 && src_len<keyLen) {
            int pad = keyLen - src_len;
            result += src;
            for (int i = 0; i < pad; i++) {
                result += (char)pad;
            }
        }
        return result;
    }

    /*** hex string key를 String으로 변환 ***/
    private static String hexToKey(String hex)
    {
        int len = hex.length();
        StringBuilder key = new StringBuilder();

        if(len>64 || len<2)
        {
//            System.out.println("hexKey too short or long");
            return null;
        }
        else
        {
            if(len%2 != 0)
            {
//                System.out.println("unsuitable format");
                return null;
            }
            else
            {
                for(int i=0; i<len; i+=2)
                {
                    String s = hex.substring(i, i+2);
                    key.append((char)Integer.parseInt(s, 16));
                }
            }
        }
        return key.toString();
    }

}


