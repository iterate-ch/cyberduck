package ch.cyberduck.core.sftp.auth;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.local.DefaultLocalTouchFeature;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.UUID;

import net.schmizz.sshj.common.Buffer;

/**
 * Writes key files for a FIDO/U2F security key the way <code>ssh-keygen -K</code> does when downloading a resident
 * credential. The file holds the public key, the application and the credential handle but no private key.
 */
public final class SecurityKeyFileWriter {

    public static final String SK_ECDSA = "sk-ecdsa-sha2-nistp256@openssh.com";
    public static final String SK_ED25519 = "sk-ssh-ed25519@openssh.com";

    private SecurityKeyFileWriter() {
        // Utility
    }

    public static String type(final KeyPair pair) {
        return "EC".equals(pair.getPublic().getAlgorithm()) ? SK_ECDSA : SK_ED25519;
    }

    /**
     * @return Public key blob as written to the <code>.pub</code> file and sent to the server
     */
    public static byte[] blob(final PublicKey publicKey, final String application) {
        final Buffer.PlainBuffer blob = new Buffer.PlainBuffer();
        if("EC".equals(publicKey.getAlgorithm())) {
            blob.putString(SK_ECDSA);
            final Buffer.PlainBuffer encoded = new Buffer.PlainBuffer().putPublicKey(publicKey);
            try {
                encoded.readString(); // ecdsa-sha2-nistp256
                blob.putString(encoded.readString()); // nistp256
                blob.putBytes(encoded.readBytes()); // Q
            }
            catch(Buffer.BufferException e) {
                throw new IllegalArgumentException(e);
            }
        }
        else {
            blob.putString(SK_ED25519);
            blob.putBytes(raw(publicKey));
        }
        return blob.putString(application).getCompactData();
    }

    /**
     * @param companion Write the public key file <code>ssh-keygen</code> leaves next to the private key
     * @return Private key file in <code>openssh-key-v1</code> format
     */
    public static Local write(final KeyPair pair, final String application, final byte[] handle,
                              final boolean companion) throws Exception {
        final byte[] blob = blob(pair.getPublic(), application);
        final Local key = LocalFactory.get(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        new DefaultLocalTouchFeature().touch(key);
        IOUtils.copy(new StringReader(privateKey(pair.getPublic(), blob, application, handle)),
                key.getOutputStream(false), StandardCharsets.UTF_8);
        if(companion) {
            final Local pub = LocalFactory.get(String.format("%s.pub", key.getAbsolute()));
            new DefaultLocalTouchFeature().touch(pub);
            IOUtils.copy(new StringReader(String.format("%s %s test@localhost%n", type(pair),
                            new String(Base64.encodeBase64(blob), StandardCharsets.US_ASCII))),
                    pub.getOutputStream(false), StandardCharsets.UTF_8);
        }
        return key;
    }

    private static String privateKey(final PublicKey publicKey, final byte[] blob, final String application,
                                     final byte[] handle) throws Exception {
        final Buffer.PlainBuffer section = new Buffer.PlainBuffer()
                .putUInt32(0x01020304L) // checkint1
                .putUInt32(0x01020304L); // checkint2
        if("EC".equals(publicKey.getAlgorithm())) {
            final Buffer.PlainBuffer encoded = new Buffer.PlainBuffer().putPublicKey(publicKey);
            encoded.readString(); // ecdsa-sha2-nistp256
            section.putString(SK_ECDSA)
                    .putString(encoded.readString()) // nistp256
                    .putBytes(encoded.readBytes()); // Q
        }
        else {
            section.putString(SK_ED25519).putBytes(raw(publicKey)); // enc_A
        }
        section.putString(application)
                .putByte((byte) 0x01) // flags, user presence required
                .putBytes(handle)
                .putBytes(new byte[0]) // reserved
                .putString("test@localhost"); // comment
        final byte[] unpadded = section.getCompactData();
        // Private key section is padded to a multiple of the cipher block size with 1, 2, 3, ...
        final Buffer.PlainBuffer padded = new Buffer.PlainBuffer().putRawBytes(unpadded);
        for(int i = 1; i <= (8 - unpadded.length % 8) % 8; i++) {
            padded.putByte((byte) i);
        }
        final byte[] file = new Buffer.PlainBuffer()
                .putRawBytes("openssh-key-v1\0".getBytes(StandardCharsets.US_ASCII))
                .putString("none") // ciphername
                .putString("none") // kdfname
                .putBytes(new byte[0]) // kdfoptions
                .putUInt32(1L) // number of keys
                .putBytes(blob)
                .putBytes(padded.getCompactData())
                .getCompactData();
        final StringBuilder pem = new StringBuilder("-----BEGIN OPENSSH PRIVATE KEY-----\n");
        final String base64 = new String(Base64.encodeBase64(file), StandardCharsets.US_ASCII);
        for(int i = 0; i < base64.length(); i += 70) {
            pem.append(base64, i, Math.min(base64.length(), i + 70)).append('\n');
        }
        return pem.append("-----END OPENSSH PRIVATE KEY-----\n").toString();
    }

    /**
     * @return Trailing 32 bytes of the X.509 encoding holding the raw Ed25519 public key
     */
    private static byte[] raw(final PublicKey publicKey) {
        final byte[] encoded = publicKey.getEncoded();
        return Arrays.copyOfRange(encoded, encoded.length - 32, encoded.length);
    }
}
