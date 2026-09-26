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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Signature;
import java.util.HashMap;
import java.util.Map;

/**
 * Signature engine for signing with a key that is not accessible as key material such as a key object on a PKCS#11
 * token. Contrary to {@link net.schmizz.sshj.common.SecurityUtils#getSignature(String)} the engine is not created
 * from the security provider registered with sshj (BouncyCastle) which fails for keys that cannot be extracted from
 * the device but from the provider of the token.
 */
public final class PKCS11Signature {
    private static final Logger log = LogManager.getLogger(PKCS11Signature.class);

    /**
     * SSH public key algorithm name to JCA signature algorithm
     */
    private static final Map<String, String> ALGORITHMS = new HashMap<>();

    static {
        ALGORITHMS.put("ssh-rsa", "SHA1withRSA");
        ALGORITHMS.put("rsa-sha2-256", "SHA256withRSA");
        ALGORITHMS.put("rsa-sha2-512", "SHA512withRSA");
        ALGORITHMS.put("ssh-dss", "SHA1withDSA");
        ALGORITHMS.put("ecdsa-sha2-nistp256", "SHA256withECDSA");
        ALGORITHMS.put("ecdsa-sha2-nistp384", "SHA384withECDSA");
        ALGORITHMS.put("ecdsa-sha2-nistp521", "SHA512withECDSA");
        ALGORITHMS.put("ssh-ed25519", "Ed25519");
    }

    private PKCS11Signature() {
        // Utility
    }

    /**
     * @param algorithm SSH public key algorithm name such as <code>rsa-sha2-256</code>
     * @return JCA signature algorithm name
     */
    public static String algorithm(final String algorithm) throws NoSuchAlgorithmException {
        final String jca = ALGORITHMS.get(algorithm);
        if(null == jca) {
            throw new NoSuchAlgorithmException(String.format("Unsupported public key algorithm %s", algorithm));
        }
        return jca;
    }

    /**
     * @param algorithm SSH public key algorithm name such as <code>rsa-sha2-256</code>
     * @param provider  Security provider of the token or null to determine provider from key when initialized
     * @return Signature engine not yet initialized
     */
    public static Signature create(final String algorithm, final Provider provider) throws NoSuchAlgorithmException {
        return engine(algorithm(algorithm), provider);
    }

    private static Signature engine(final String jca, final Provider provider) throws NoSuchAlgorithmException {
        if(provider != null) {
            try {
                return Signature.getInstance(jca, provider);
            }
            catch(NoSuchAlgorithmException e) {
                log.warn("Provider {} does not support {}", provider.getName(), jca);
            }
        }
        // Delayed provider selection determines provider accepting the key when initialized for signing
        return Signature.getInstance(jca);
    }
}
