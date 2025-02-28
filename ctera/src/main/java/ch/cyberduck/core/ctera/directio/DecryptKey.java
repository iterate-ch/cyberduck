package ch.cyberduck.core.ctera.directio;

import org.jose4j.keys.AesKey;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;
import java.util.Base64;


public class DecryptKey {
    private String encryptedKey;
    private String decryptedKey;

    public DecryptKey(String encryptedKey) {
        this.encryptedKey = encryptedKey;
    }

    public void decrypt(String wrappingKey) {
        try {
            final byte[] decoded = java.util.Base64.getDecoder().decode(wrappingKey);
            AesKey wrappingKeyObj = new AesKey(Arrays.copyOf(decoded, 32));

            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.UNWRAP_MODE, wrappingKeyObj);
            byte[] wrappedKey = Base64.getDecoder().decode(this.encryptedKey);

            // Unwrap the key (assuming it's an AES key)
            Key unwrappedKey = cipher.unwrap(wrappedKey, "AES", Cipher.SECRET_KEY);

            // Convert the unwrapped key bytes back to a string
            byte[] keyBytes = unwrappedKey.getEncoded();
            this.decryptedKey = new String(keyBytes, StandardCharsets.UTF_8);

        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public String getDecryptedKey() {
        return decryptedKey;
    }
}