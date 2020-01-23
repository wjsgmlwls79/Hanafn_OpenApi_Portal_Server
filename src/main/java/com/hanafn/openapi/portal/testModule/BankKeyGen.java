package com.hanafn.openapi.portal.testModule;

import sun.misc.BASE64Decoder;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.RandomAccessFile;

public class BankKeyGen{

    public static String algorithmhmac = "PBKDF2WithHmacSHA1";
    public static int iterationCount = 1024;
    public static String saltOfKey = "saltkey123";
    public static String saltOfIV = "saltiv1234";
    public static String algID = "AES/CBC/PKCS5Padding";
    //    public static String password = "ineb!1234";
    public static String FILE_DEC_PARAM = "Kebhana.1!";

    public static byte[] loadKey(String password, String saveFile) {

        Cipher decCipher = null;
        byte[] byteKey = null;

        try {

            // derived KEY Length : 32
            byte[] encKey = getPBEkey(algorithmhmac, password.toCharArray(), saltOfKey.getBytes(), iterationCount, 256);
//            System.out.println("pbkdf2 key length : " + encKey.length);
//            System.out.println("pbkdf2 key : " + bytesToHex(encKey));
            // derived IV Length : 16
            byte[] encIv = getPBEkey(algorithmhmac, password.toCharArray(), saltOfIV.getBytes(), iterationCount, 128);
//            System.out.println("pbkdf2 IV length : " + encIv.length);
//            System.out.println("pbkdf2 IV : " + bytesToHex(encIv));

            decCipher = makeCipher(Cipher.DECRYPT_MODE, "AES", "AES/CBC/PKCS5Padding", encKey, encIv);

            File keyFileName = new File(saveFile);
            RandomAccessFile keyips = new RandomAccessFile(keyFileName, "r");
            String encStrkey = keyips.readLine();
            keyips.close();

            BASE64Decoder decoder = new BASE64Decoder();
            byteKey = decCipher.doFinal(decoder.decodeBuffer(encStrkey));


        } catch (Exception e) {
            e.printStackTrace();
        }

        return byteKey;
    }

    public static byte[] loadKey_noFile(String encStrKey) {

        Cipher decCipher = null;
        byte[] byteKey = null;

        try {

            // derived KEY Length : 32
            byte[] encKey = getPBEkey(algorithmhmac, FILE_DEC_PARAM.toCharArray(), saltOfKey.getBytes(), iterationCount, 256);
//            System.out.println("pbkdf2 key length : " + encKey.length);
//            System.out.println("pbkdf2 key : " + bytesToHex(encKey));
            // derived IV Length : 16
            byte[] encIv = getPBEkey(algorithmhmac, FILE_DEC_PARAM.toCharArray(), saltOfIV.getBytes(), iterationCount, 128);
//            System.out.println("pbkdf2 IV length : " + encIv.length);
//            System.out.println("pbkdf2 IV : " + bytesToHex(encIv));

            decCipher = makeCipher(Cipher.DECRYPT_MODE, "AES", "AES/CBC/PKCS5Padding", encKey, encIv);

            BASE64Decoder decoder = new BASE64Decoder();
            byteKey = decCipher.doFinal(decoder.decodeBuffer(encStrKey));


        } catch (Exception e) {
            e.printStackTrace();
        }

        return byteKey;
    }

    private static byte[] getPBEkey(final String algorithmhmac,  final char[] password, final byte[] salt, final int iterations, final int keyLength ) {
        try {
            javax.crypto.SecretKeyFactory skf = javax.crypto.SecretKeyFactory.getInstance( algorithmhmac );
            javax.crypto.spec.PBEKeySpec spec = new javax.crypto.spec.PBEKeySpec( password, salt, iterations, keyLength );
            javax.crypto.SecretKey key = skf.generateSecret( spec );
            byte[] res = key.getEncoded( );
            return res;
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }
    }

    private static Cipher makeCipher(int mode, String alg, String alg_detail, byte[] makeKey, byte[] makeIV)
    {
        Cipher retCipher = null;

        try {
            SecretKeySpec keySpec = new SecretKeySpec(makeKey, "AES");
            retCipher = Cipher.getInstance(alg_detail);
            retCipher.init(mode, keySpec, new IvParameterSpec(makeIV));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return retCipher;
    }

    private static String bytesToHex(byte[] bytes)
    {
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        int v;

        for (int j=0; j < bytes.length; j++)
        {
            v = bytes[j] & 0xFF;
            hexChars[j*2] = hexArray[v >>> 4];
            hexChars[j*2+1] = hexArray[v & 0x0F];
        }

        return new String(hexChars);
    }
}