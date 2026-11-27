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

import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.Signature;

import com.hierynomus.asn1.ASN1InputStream;
import com.hierynomus.asn1.encodingrules.der.DERDecoder;
import com.hierynomus.asn1.types.constructed.ASN1Sequence;
import com.hierynomus.asn1.types.primitive.ASN1Integer;
import net.schmizz.sshj.common.Buffer;

/**
 * Authenticator implemented in software, standing in for a security key attached to the machine. Signs the
 * authenticator data <code>SHA256(application) || flags || counter || SHA256(data)</code> with {@code pair} and
 * returns the two integers of the signature the same way the native middleware does.
 * <p>
 * The message to sign is hashed here the same way a native middleware library does, never by the caller.
 */
public class SoftwareSecurityKeyMiddleware implements SecurityKeyMiddleware {

    private final KeyPair pair;
    private final long counter;
    /**
     * PIN to require prior to signing or null to sign without user verification
     */
    private final String required;

    private int attempts;
    private String provided;
    private byte[] challenge;
    private byte[] keyHandle;
    private byte flags;

    public SoftwareSecurityKeyMiddleware(final KeyPair pair, final long counter) {
        this(pair, counter, null);
    }

    public SoftwareSecurityKeyMiddleware(final KeyPair pair, final long counter, final String required) {
        this.pair = pair;
        this.counter = counter;
        this.required = required;
    }

    @Override
    public int version() {
        return SSH_SK_VERSION_MAJOR;
    }

    @Override
    public Response sign(final int algorithm, final byte[] data, final String application,
                         final byte[] keyHandle, final byte flags, final String pin) throws IOException {
        attempts++;
        // Client data hash is computed by the authenticator from the message to sign
        final byte[] challenge = this.sha256(data);
        this.challenge = challenge;
        this.keyHandle = keyHandle;
        this.flags = flags;
        this.provided = pin;
        if(required != null && !StringUtils.equals(required, pin)) {
            throw new SecurityKeyPinRequiredException("PIN required");
        }
        // Flags asserted by the authenticator include user presence
        final byte asserted = (byte) (flags | 0x01);
        final byte[] signed = new Buffer.PlainBuffer()
                .putRawBytes(this.sha256(application.getBytes(StandardCharsets.UTF_8)))
                .putByte(asserted)
                .putUInt32(counter)
                .putRawBytes(challenge)
                .getCompactData();
        try {
            if(SSH_SK_ED25519 == algorithm) {
                final Signature engine = Signature.getInstance("Ed25519");
                engine.initSign(pair.getPrivate());
                engine.update(signed);
                // Entire signature is returned in r
                return new Response(asserted, counter, engine.sign(), null);
            }
            final Signature engine = Signature.getInstance("SHA256withECDSA");
            engine.initSign(pair.getPrivate());
            engine.update(signed);
            return this.split(asserted, engine.sign());
        }
        catch(GeneralSecurityException e) {
            throw new IOException(e);
        }
    }

    /**
     * Split the ASN.1 signature into the two unsigned integers returned by an authenticator
     */
    private Response split(final byte flags, final byte[] der) throws IOException {
        final ByteArrayInputStream in = new ByteArrayInputStream(der);
        try (ASN1InputStream reader = new ASN1InputStream(new DERDecoder(), in)) {
            final ASN1Sequence sequence = reader.readObject();
            final BigInteger r = ((ASN1Integer) sequence.get(0)).getValue();
            final BigInteger s = ((ASN1Integer) sequence.get(1)).getValue();
            return new Response(flags, counter, this.unsigned(r), this.unsigned(s));
        }
    }

    /**
     * @return Big endian representation without sign bit as returned from an authenticator
     */
    private byte[] unsigned(final BigInteger value) {
        final byte[] bytes = value.toByteArray();
        if(bytes.length > 1 && bytes[0] == 0) {
            final byte[] trimmed = new byte[bytes.length - 1];
            System.arraycopy(bytes, 1, trimmed, 0, trimmed.length);
            return trimmed;
        }
        return bytes;
    }

    private byte[] sha256(final byte[] data) throws IOException {
        try {
            return MessageDigest.getInstance("SHA-256").digest(data);
        }
        catch(GeneralSecurityException e) {
            throw new IOException(e);
        }
    }

    /**
     * @return Number of signing requests received
     */
    public int getAttempts() {
        return attempts;
    }

    /**
     * @return PIN received with the last signing request
     */
    public String getProvided() {
        return provided;
    }

    public byte[] getChallenge() {
        return challenge;
    }

    public byte[] getKeyHandle() {
        return keyHandle;
    }

    public byte getFlags() {
        return flags;
    }
}
