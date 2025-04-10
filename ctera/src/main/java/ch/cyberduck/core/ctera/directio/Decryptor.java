package ch.cyberduck.core.ctera.directio;

/*
 * Copyright (c) 2002-2025 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xerial.snappy.SnappyInputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

public class Decryptor {
    private static final Logger log = LogManager.getLogger(Decryptor.class);

    private static final String ENCRYPTION_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String ENCRYPTION_KEY_ALGORITHM = "AES";

    private static final byte[] GZIP_MAGIC = {0x1F, (byte) 0x8B};
    private static final byte[] SNAPPY_MAGIC = {-126, 83, 78, 65, 80, 80, 89, 0};

    public InputStream decryptData(final InputStream blockData, final EncryptInfo encryptInfo) throws IOException {
        try {
            final DecryptKey decryptKey = new DecryptKey(encryptInfo.getWrappedKey());
            decryptKey.decrypt(encryptInfo.getWrappingKey());
            final SecretKeySpec key = new SecretKeySpec(Base64.decodeBase64(decryptKey.getDecryptedKey()), ENCRYPTION_KEY_ALGORITHM);
            blockData.read();
            final byte[] iv = new byte[16];
            IOUtils.readFully(blockData, iv);

            final Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
            return this.getDecompressingStream(new CipherInputStream(blockData, cipher));
        }
        catch(NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException |
              InvalidKeyException e) {
            log.error("Failure decrypting data", e);
            throw new IOException(e);
        }
    }

    private InputStream getDecompressingStream(final InputStream inputStream) throws IOException {
        final InputStream stream = new BufferedInputStream(inputStream);
        {
            stream.mark(SNAPPY_MAGIC.length);
            final byte[] header = new byte[SNAPPY_MAGIC.length];
            IOUtils.read(stream, header);
            stream.reset();
            if(Arrays.equals(SNAPPY_MAGIC, header)) {
                return new SnappyInputStream(stream);
            }
        }
        {
            stream.mark(GZIP_MAGIC.length);
            final byte[] header = new byte[GZIP_MAGIC.length];
            IOUtils.read(stream, header);
            stream.reset();
            if(Arrays.equals(GZIP_MAGIC, header)) {
                return new GZIPInputStream(stream);
            }
        }

        throw new IOException("Unsupported compression type");
    }
}
