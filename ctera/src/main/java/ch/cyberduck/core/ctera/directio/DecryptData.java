package ch.cyberduck.core.ctera.directio;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.tuple.Pair;
import org.xerial.snappy.SnappyInputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.zip.GZIPInputStream;

import static com.amazonaws.util.json.Jackson.fromJsonString;


public class DecryptData {

    public InputStream decryptData(InputStream blockData, EncryptInfo encryptInfo)
            throws IOException, NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException,
            InvalidAlgorithmParameterException {

        SecretKeySpec key = null;
        if(encryptInfo != null && !encryptInfo.mWrappedKey.isEmpty() && !encryptInfo.mWrappingKey.isEmpty()) {
            DecryptKey decryptKey = new DecryptKey(encryptInfo.mWrappedKey);
            decryptKey.decrypt(encryptInfo.mWrappingKey);
            String keyWithoutWrapping = fromJsonString(decryptKey.getDecryptedKey(), String.class);
            key = new SecretKeySpec(Base64.decodeBase64(keyWithoutWrapping), "AES");
        }

        blockData.read();
        InputStream inputStream = blockData;

        try {
            if(encryptInfo != null && key != null && encryptInfo.mIsDataEncrypted) {
                byte[] iv = new byte[16];
                if(InputStreamUtils.readFull(blockData, iv, iv.length) == -1) {
                    throw new IOException("Failed to read IV");
                }
                IvParameterSpec ivspec = new IvParameterSpec(iv);
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                cipher.init(Cipher.DECRYPT_MODE, key, ivspec);
                inputStream = new CipherInputStream(blockData, cipher);
            }

            Pair<InputStream, CompressChecker.CompressionType> result = CompressChecker.detectCompressionType(inputStream);
            if(result.getLeft() == null || result.getRight() == null) {
                throw new RuntimeException("CompressChecker error");
            }
            inputStream = result.getLeft();
            CompressChecker.CompressionType compType = result.getRight();

            if(compType.equals(CompressChecker.CompressionType.GZIP)) {
                inputStream = new GZIPInputStream(inputStream);
            }
            else if(compType.equals(CompressChecker.CompressionType.SNAPPY)) {
                try {
                    inputStream = new SnappyInputStream(inputStream);
                }
                catch(Exception e) {
                    try {
                        if(inputStream != null) {
                            inputStream.close();
                        }
                    }
                    catch(Exception ee) {
                        throw ee;
                    }
                    throw e;
                }
            }
        }
        catch(Exception e) {
            throw e;
        }
        return inputStream;
    }
}
