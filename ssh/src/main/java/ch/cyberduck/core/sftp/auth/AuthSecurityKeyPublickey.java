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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.exception.LoginCanceledException;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;

import com.hierynomus.sshj.userauth.fido.SecurityKeyPrivateKey;
import com.hierynomus.sshj.userauth.fido.SecurityKeyPublicKey;
import net.schmizz.sshj.common.Buffer;
import net.schmizz.sshj.common.KeyType;
import net.schmizz.sshj.common.SSHPacket;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.userauth.UserAuthException;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import net.schmizz.sshj.userauth.method.AuthPublickey;

/**
 * Public key authentication with a FIDO/U2F security key signing through the middleware of the authenticator.
 * <p>
 * Signing is not delegated to {@link com.hierynomus.sshj.signature.AbstractSecurityKeySignature} because the
 * middleware expects the message to sign and computes the client data hash itself, whereas the
 * {@link com.hierynomus.sshj.userauth.fido.SecurityKeySigner} interface only passes the digest. Signing the digest
 * makes the authenticator sign a hash of the hash, which no server accepts.
 *
 * @see <a href="https://github.com/openssh/openssh-portable/blob/master/sk-api.h">sk-api.h</a>
 */
public class AuthSecurityKeyPublickey extends AuthPublickey {

    private final KeyProvider keys;
    private final SecurityKeyMiddleware middleware;
    private final Host bookmark;
    private final LoginCallback prompt;

    public AuthSecurityKeyPublickey(final KeyProvider keys, final SecurityKeyMiddleware middleware,
                                    final Host bookmark, final LoginCallback prompt) {
        super(keys);
        this.keys = keys;
        this.middleware = middleware;
        this.bookmark = bookmark;
        this.prompt = prompt;
    }

    @Override
    protected SSHPacket putSig(final SSHPacket reqBuf) throws UserAuthException {
        try {
            final PublicKey publicKey = keys.getPublic();
            if(!(publicKey instanceof SecurityKeyPublicKey)) {
                throw new UserAuthException(String.format("Expected security key but got %s", publicKey));
            }
            final PrivateKey privateKey = keys.getPrivate();
            if(!(privateKey instanceof SecurityKeyPrivateKey)) {
                throw new UserAuthException(String.format("Expected security key but got %s", privateKey));
            }
            final SecurityKeyPrivateKey key = (SecurityKeyPrivateKey) privateKey;
            final KeyType type = KeyType.fromKey(publicKey);
            // Message the authenticator hashes to obtain the client data hash
            final byte[] data = new Buffer.PlainBuffer()
                    .putString(params.getTransport().getSessionID())
                    .putBuffer(reqBuf) // & rest of the data for sig
                    .getCompactData();
            final SecurityKeyMiddleware.Response response = this.sign(type, data,
                    ((SecurityKeyPublicKey) publicKey).getApplication(), key.getKeyHandle(), key.getFlags());
            // Signature already encodes the key type, flags and counter and is written as one string
            reqBuf.putString(encode(type, response));
            return reqBuf;
        }
        catch(TransportException e) {
            throw new UserAuthException(String.format("Failure signing with %s", keys), e);
        }
        catch(IOException e) {
            throw new UserAuthException(String.format("Failure signing with security key %s", keys), e);
        }
    }

    /**
     * Ask the authenticator for an assertion, prompting for the PIN when the device requires user verification
     */
    private SecurityKeyMiddleware.Response sign(final KeyType type, final byte[] data, final String application,
                                                final byte[] keyHandle, final byte flags) throws IOException {
        final int algorithm = KeyType.SK_ED25519 == type
                ? SecurityKeyMiddleware.SSH_SK_ED25519 : SecurityKeyMiddleware.SSH_SK_ECDSA;
        try {
            return middleware.sign(algorithm, data, application, keyHandle, flags, null);
        }
        catch(SecurityKeyPinRequiredException e) {
            final Credentials input;
            try {
                input = prompt.prompt(bookmark, bookmark.getCredentials().getUsername(),
                        LocaleFactory.localizedString("Provide additional login credentials", "Credentials"),
                        LocaleFactory.localizedString("Enter PIN for security key", "Credentials"),
                        new LoginOptions().user(false).password(true).keychain(false)
                                .icon(bookmark.getProtocol().disk()));
            }
            catch(LoginCanceledException c) {
                throw new IOException("PIN prompt for security key canceled", c);
            }
            if(StringUtils.isBlank(input.getPassword())) {
                throw new IOException("Missing PIN for security key");
            }
            return middleware.sign(algorithm, data, application, keyHandle, flags, input.getPassword());
        }
    }

    /**
     * Assemble the signature value as <code>string keytype || string signature || byte flags || uint32 counter</code>
     * where the signature holds the two integers r and s for ECDSA and the signature as is for Ed25519.
     */
    protected static byte[] encode(final KeyType type, final SecurityKeyMiddleware.Response response)
            throws IOException {
        final byte[] signature;
        if(KeyType.SK_ED25519 == type) {
            // Entire signature is returned in r
            signature = response.getR();
        }
        else {
            if(null == response.getS()) {
                throw new IOException("Missing s in signature from security key");
            }
            signature = new Buffer.PlainBuffer()
                    // Integers as returned from the authenticator are unsigned
                    .putMPInt(new BigInteger(1, response.getR()))
                    .putMPInt(new BigInteger(1, response.getS()))
                    .getCompactData();
        }
        return new Buffer.PlainBuffer()
                .putString(type.toString())
                .putBytes(signature)
                .putByte(response.getFlags())
                .putUInt32(response.getCounter())
                .getCompactData();
    }
}
