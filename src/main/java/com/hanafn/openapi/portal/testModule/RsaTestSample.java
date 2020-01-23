package com.hanafn.openapi.portal.testModule;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.security.spec.*;
import java.util.Base64;

/**
 *  1. 공개키와 개인키를 얻기위하여, testsample.test를 실행하여, 공개키를 얻는다.
 *  2. 얻은 공개키로 포탈에 키 다운로드를 요청한다.
 *  3. 가지고 있는 개인키와, 받은 파일내용으로 복호화를 한다.
 */

public class RsaTestSample {

    public void test() {
        System.out.println("Server Start-----------------------------------");
        // 서버측 키 파일 생성 하기
        PublicKey publicKey1 = null;
        PrivateKey privateKey1 = null;

        SecureRandom secureRandom = new SecureRandom();
        KeyPairGenerator keyPairGenerator;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(4096, secureRandom);

            KeyPair keyPair = keyPairGenerator.genKeyPair();
            publicKey1 = keyPair.getPublic();
            privateKey1 = keyPair.getPrivate();

            KeyFactory keyFactory1 = KeyFactory.getInstance("RSA");
            RSAPublicKeySpec rsaPublicKeySpec = keyFactory1.getKeySpec(publicKey1, RSAPublicKeySpec.class);
            RSAPrivateKeySpec rsaPrivateKeySpec = keyFactory1.getKeySpec(privateKey1, RSAPrivateKeySpec.class);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

        // Key to String
        byte[] bPublicKey1 = publicKey1.getEncoded();
        String sPublicKey1 = Base64.getEncoder().encodeToString(bPublicKey1);

        byte[] bPrivateKey1 = privateKey1.getEncoded();
        String sPrivateKey1 = Base64.getEncoder().encodeToString(bPrivateKey1);

        System.out.println("★문자열화된 공개키:"+sPublicKey1);
        System.out.println("★공개키 길이:"+sPublicKey1.length());
        System.out.println("★문자열화된 개인키:"+sPrivateKey1);

        /** 개인키 입력 창 **/
        sPrivateKey1 = "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";

        // String to Key
        byte[] bPublicKey2 = Base64.getDecoder().decode(sPublicKey1.getBytes());
        PublicKey publicKey2 = null;

        byte[] bPrivateKey2 = Base64.getDecoder().decode(sPrivateKey1.getBytes());
        PrivateKey privateKey2 = null;

        System.out.println("주어진 바이트배열로 암호화시작");

        try {
            KeyFactory keyFactory2 = KeyFactory.getInstance("RSA");

            X509EncodedKeySpec publicKeySpec
                    = new X509EncodedKeySpec(bPublicKey2);
            publicKey2 = keyFactory2.generatePublic(publicKeySpec);

            PKCS8EncodedKeySpec privateKeySpec
                    = new PKCS8EncodedKeySpec(bPrivateKey2);
            privateKey2 = keyFactory2.generatePrivate(privateKeySpec);

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }

        String sPlain1 = String.format("%500s","hi");   // 4096의 경우 데이터 최대 501byte만 암호화 허용
        String sPlain2 = null;

        try {
            Cipher cipher = Cipher.getInstance("RSA");

            // 공개키 이용 암호화
            cipher.init(Cipher.ENCRYPT_MODE, publicKey2);
            byte[] bCipher1 = cipher.doFinal(sPlain1.getBytes());
            String sCipherBase64 = Base64.getEncoder().encodeToString(bCipher1);
            System.out.println("암호화된 평문:" + sCipherBase64);

            /** 받은 파일 내용 세팅 **/
            sCipherBase64 = "aLh8pnYGB/9McJGJ/kAyJrqPbWMfoPYWN0e4ZL8pnec2J6UFAdwwiOuBsT2tye+ozBfkzDWPq5myj/OGH5iS2gPzkuDx68xUvFZwmHCGaapWjXIH7dVuRZlvPb8qMBEuSsy9aJQDsAcbAyARFouMWc6I3YVyPvhxSb4YEwaTfbINUesiOzN0crpXFAWvMnMXRlaLVL+wiC5VVUt3LgV/WgNElsP/vTR+DSnly2FJFfXRu637yBIhNIX7BwLMWEe3jxwwTn/RE5yWmjS2s8tEKtdx6Tu493UpDjFNzMptPzGh5D0uZe4eud0MomFhkqp5LEAruuVTg2DOuqeJA5VxqE6gFIV+r8yMPTiYGl7LmQAIAOZgX6nSkO6YMUuewxRPio7ekVfSyxAb4ppOKDoLXPgKFkkDwo3clz9qNSBR9UticskeMNlA0caev3jPmidxu+EgdKNFN7DgrcXcBZqeSODAMg5rXkP18rJz+xAF3xOUYlYNZxYXrqR+wiNq+NGGDUH/QmhCtCn4ZU+tHBug8U30i0GZ9zinptYIq0vnI4lM72Rl817e9T+ZaGue8V92BFI++ioeFGP/x49WUzouVn5fDLO0TypOHiq7ltwwYh5QruSPyEf2mBMgGmdr/go3flUnqMeBW6vEzx2hmbOVkn6ptFPw6EU1oYMp96/WrU8=";

            // 개인키 이용 복호화
            byte[] bCipher2 = Base64.getDecoder().decode(sCipherBase64.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, privateKey2);
            byte[] bPlain2 = cipher.doFinal(bCipher2);
            sPlain2 = new String(bPlain2);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }

        System.out.println("sPlain1 : " + sPlain1); // 평문(원본)
        System.out.println("sPlain2 : " + sPlain2); // 평문(암호화후 복호화된 평문)
    }
}
