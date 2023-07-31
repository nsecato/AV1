package com.example.av1;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;
import org.json.JSONObject;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Security;

public class CryptoManager {

    private static final String chave = "1234567812345678"; // 16 char

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static JSONObject encrypt(String textoPlano) {
        try {
            SecretKeySpec key = new SecretKeySpec(chave.getBytes("UTF-8"), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding", "BC");
            cipher.init(Cipher.ENCRYPT_MODE, key);

            byte[] cipherText = cipher.doFinal(textoPlano.getBytes("UTF-8"));
            String criptografado = new String(Base64.encode(cipherText));

            // Criar o JSON
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("dados", criptografado);

            return jsonObject;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String decrypt(String textoCriptografado) {
        try {
            SecretKeySpec key = new SecretKeySpec(chave.getBytes("UTF-8"), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding", "BC");
            cipher.init(Cipher.DECRYPT_MODE, key);

            byte[] cipherText = Base64.decode(textoCriptografado);
            String descriptografado = new String(cipher.doFinal(cipherText), "UTF-8");

            return descriptografado;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
