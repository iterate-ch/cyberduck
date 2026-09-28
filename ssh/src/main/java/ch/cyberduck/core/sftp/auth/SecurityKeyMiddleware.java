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

/**
 * Middleware library talking to a FIDO/U2F authenticator, the same interface OpenSSH loads with
 * <code>SecurityKeyProvider</code> respectively <code>ssh-keygen -w</code>. On macOS
 * <code>/usr/lib/ssh-keychain.dylib</code> implements it for keys in the Secure Enclave where user verification is a
 * Touch ID prompt, elsewhere it is implemented by <code>libsk-libfido2</code> for USB tokens.
 *
 * @see <a href="https://github.com/openssh/openssh-portable/blob/master/sk-api.h">sk-api.h</a>
 */
public interface SecurityKeyMiddleware {

    /**
     * API version implemented by this library. Only the major version is significant.
     */
    int SSH_SK_VERSION_MAJOR = 0x000a0000;
    int SSH_SK_VERSION_MAJOR_MASK = 0xffff0000;

    /**
     * Key algorithm of the credential to sign with
     */
    int SSH_SK_ECDSA = 0x00;
    int SSH_SK_ED25519 = 0x01;

    /**
     * @return Version of the API implemented by the library, to be compared against
     * {@link #SSH_SK_VERSION_MAJOR} after masking with {@link #SSH_SK_VERSION_MAJOR_MASK}
     */
    int version() throws IOException;

    /**
     * Ask the authenticator for an assertion. Blocks until the request is confirmed on the device, such as touching a
     * token or authenticating with Touch ID.
     *
     * @param algorithm   {@link #SSH_SK_ECDSA} or {@link #SSH_SK_ED25519}
     * @param data        Message to sign. The client data hash the authenticator signs over is the SHA-256 of this
     *                    message computed by the library, never by the caller
     * @param application Application the credential was enrolled for, such as <code>ssh:</code>
     * @param keyHandle   Credential handle from the key file
     * @param flags       Authenticator flags requested by the key file
     * @param pin         PIN for the authenticator or null if not required
     * @return Signature with the flags and signature counter asserted by the authenticator
     * @throws SecurityKeyPinRequiredException The authenticator requires a PIN to be provided
     * @throws IOException                     The authenticator could not be reached or declined to sign
     */
    Response sign(int algorithm, byte[] data, String application, byte[] keyHandle, byte flags, String pin)
            throws IOException;

    /**
     * Response of the authenticator to a signing request. The signature is split into the two integers r and s for
     * ECDSA, for Ed25519 the entire signature is returned in {@link #getR()}.
     */
    final class Response {
        private final byte flags;
        private final long counter;
        private final byte[] r;
        private final byte[] s;

        public Response(final byte flags, final long counter, final byte[] r, final byte[] s) {
            this.flags = flags;
            this.counter = counter;
            this.r = r;
            this.s = s;
        }

        /**
         * @return Authenticator flags such as user presence and user verification asserted when signing
         */
        public byte getFlags() {
            return flags;
        }

        /**
         * @return Signature counter of the authenticator
         */
        public long getCounter() {
            return counter;
        }

        public byte[] getR() {
            return r;
        }

        public byte[] getS() {
            return s;
        }
    }
}
