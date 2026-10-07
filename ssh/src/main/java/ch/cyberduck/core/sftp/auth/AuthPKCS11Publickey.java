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

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;

import com.hierynomus.sshj.key.KeyAlgorithm;
import net.schmizz.sshj.common.Buffer;
import net.schmizz.sshj.common.KeyType;
import net.schmizz.sshj.common.SSHPacket;
import net.schmizz.sshj.signature.Signature;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.userauth.UserAuthException;
import net.schmizz.sshj.userauth.method.AuthPublickey;

/**
 * Public key authentication with a key on a PKCS#11 token.
 * <p>
 * Signing is not delegated to {@link net.schmizz.sshj.signature.Signature} created from the key algorithm because the
 * signature engine is created from the security provider registered with sshj (BouncyCastle) which cannot sign with a
 * key object that never leaves the token. The signature is created with the security provider of the token instead and
 * only encoded to the SSH wire format with sshj.
 * <p>
 * The key type is determined from the public key read from the certificate on the token. A private key object on a
 * token is opaque and {@link KeyType#fromKey(java.security.Key)} returns {@link KeyType#UNKNOWN} for it.
 */
public class AuthPKCS11Publickey extends AuthPublickey {

    private final PKCS11KeyProvider provider;

    public AuthPKCS11Publickey(final PKCS11KeyProvider provider) {
        super(provider);
        this.provider = provider;
    }

    @Override
    protected SSHPacket putSig(final SSHPacket reqBuf) throws UserAuthException {
        try {
            final PublicKey publicKey = provider.getPublic();
            final KeyType type = KeyType.fromKey(publicKey);
            final KeyAlgorithm algorithm = this.getPublicKeyAlgorithm(type);
            if(null == algorithm) {
                throw new UserAuthException(String.format("No key algorithm configured for key type %s", type));
            }
            final java.security.Signature engine = PKCS11Signature.create(algorithm.getKeyAlgorithm(), provider.getProvider());
            log.debug("Sign with engine {} from provider {}", engine.getAlgorithm(), engine.getProvider().getName());
            engine.initSign(provider.getPrivate());
            engine.update(new Buffer.PlainBuffer()
                    .putString(params.getTransport().getSessionID())
                    .putBuffer(reqBuf) // & rest of the data for sig
                    .getCompactData());
            // Encode signature returned from token to SSH wire format
            final Signature signature = algorithm.newSignature();
            reqBuf.putSignature(signature.getSignatureName(), signature.encode(engine.sign()));
            return reqBuf;
        }
        catch(TransportException | GeneralSecurityException e) {
            throw new UserAuthException(String.format("Failure signing with %s", provider), e);
        }
        catch(IOException e) {
            throw new UserAuthException(String.format("Problem getting key from %s", provider), e);
        }
    }
}
