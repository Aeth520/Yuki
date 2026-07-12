package cn.aetheris.yuki.util.encrypt;

import cn.aetheris.yuki.Yuki;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.Base64;

public final class AESUtil {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int KEY_SIZE = 256;
    private static final int IV_SIZE = 12;
    private static final int TAG_SIZE = 128;
    private static final String KEY_FILE_NAME = "aes.key";

    private static SecretKey secretKey;

    private AESUtil() {
    }

    public static void init() {
        File keyFile = new File(Yuki.getInstance().getDataFolder(), KEY_FILE_NAME);
        if (keyFile.exists()) {
            try {
                byte[] keyBytes = Files.readAllBytes(keyFile.toPath());
                if (keyBytes.length == 16 || keyBytes.length == 24 || keyBytes.length == 32) {
                    secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
                    return;
                }
                Yuki.getInstance().getLogger().warning("AES key file has invalid length, regenerating...");
            } catch (Exception e) {
                Yuki.getInstance().getLogger().warning("Failed to read AES key file, regenerating: " + e.getMessage());
            }
        }
        generateAndStoreKey(keyFile);
    }

    private static void generateAndStoreKey(File keyFile) {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
            keyGen.init(KEY_SIZE, new SecureRandom());
            secretKey = keyGen.generateKey();
            if (!keyFile.getParentFile().exists()) {
                keyFile.getParentFile().mkdirs();
            }
            Files.write(keyFile.toPath(), secretKey.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate AES key", e);
        }
    }

    public static String encrypt(String plainText) {
        if (secretKey == null) {
            throw new IllegalStateException("AESUtil not initialized, call init() first");
        }
        try {
            byte[] iv = new byte[IV_SIZE];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_SIZE, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes());

            byte[] result = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, result, 0, iv.length);
            System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);

            return Base64.getEncoder().encodeToString(result);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt", e);
        }
    }

    public static String decrypt(String cipherText) {
        if (secretKey == null) {
            throw new IllegalStateException("AESUtil not initialized, call init() first");
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(cipherText);
            if (decoded.length < IV_SIZE) {
                throw new IllegalArgumentException("Invalid ciphertext length");
            }
            byte[] iv = new byte[IV_SIZE];
            byte[] encrypted = new byte[decoded.length - IV_SIZE];
            System.arraycopy(decoded, 0, iv, 0, IV_SIZE);
            System.arraycopy(decoded, IV_SIZE, encrypted, 0, encrypted.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_SIZE, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
            byte[] decrypted = cipher.doFinal(encrypted);

            return new String(decrypted);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt", e);
        }
    }
}
