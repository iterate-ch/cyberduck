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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.PointerByReference;

/**
 * Native OpenSSH security key middleware such as <code>/usr/lib/ssh-keychain.dylib</code> loaded from a shared
 * library. Signing blocks in the native call until the request is confirmed on the authenticator.
 *
 * @see <a href="https://github.com/openssh/openssh-portable/blob/master/sk-api.h">sk-api.h</a>
 */
public class NativeSecurityKeyMiddleware implements SecurityKeyMiddleware {
    private static final Logger log = LogManager.getLogger(NativeSecurityKeyMiddleware.class);

    private static final int SSH_SK_ERR_GENERAL = -1;
    private static final int SSH_SK_ERR_UNSUPPORTED = -2;
    private static final int SSH_SK_ERR_PIN_REQUIRED = -3;
    private static final int SSH_SK_ERR_DEVICE_NOT_FOUND = -4;

    private final String library;
    private final SecurityKeyLibrary sk;

    /**
     * @param library Path to the shared library implementing the middleware
     * @throws IOException Library not found or not implementing the expected API version
     */
    public NativeSecurityKeyMiddleware(final String library) throws IOException {
        this.library = library;
        try {
            sk = Native.load(library, SecurityKeyLibrary.class);
        }
        catch(UnsatisfiedLinkError | NoClassDefFoundError e) {
            throw new IOException(String.format("Failure loading security key provider %s", library), e);
        }
        final int version = this.version();
        if((version & SSH_SK_VERSION_MAJOR_MASK) != SSH_SK_VERSION_MAJOR) {
            throw new IOException(String.format("Unsupported API version %s in security key provider %s",
                    Integer.toHexString(version), library));
        }
        log.debug("Loaded security key provider {} with API version {}", library, Integer.toHexString(version));
    }

    @Override
    public int version() throws IOException {
        try {
            return sk.sk_api_version();
        }
        catch(UnsatisfiedLinkError e) {
            throw new IOException(String.format("Missing sk_api_version in security key provider %s", library), e);
        }
    }

    @Override
    public Response sign(final int algorithm, final byte[] data, final String application,
                         final byte[] keyHandle, final byte flags, final String pin) throws IOException {
        final PointerByReference reference = new PointerByReference();
        log.debug("Sign message for application {} with flags {} using {}", application, flags, library);
        // Library computes the client data hash from the message
        final int status = sk.sk_sign(algorithm, data, data.length, application,
                keyHandle, keyHandle.length, flags, StringUtils.isBlank(pin) ? null : pin, null, reference);
        if(status != 0) {
            switch(status) {
                case SSH_SK_ERR_PIN_REQUIRED:
                    throw new SecurityKeyPinRequiredException(String.format(
                            "Security key requires PIN for application %s", application));
                case SSH_SK_ERR_DEVICE_NOT_FOUND:
                    throw new IOException("No security key found");
                case SSH_SK_ERR_UNSUPPORTED:
                    throw new IOException(String.format("Unsupported request for security key in %s", library));
                case SSH_SK_ERR_GENERAL:
                default:
                    throw new IOException(String.format("Failure %d signing with security key in %s", status, library));
            }
        }
        final Pointer pointer = reference.getValue();
        if(null == pointer) {
            throw new IOException(String.format("Missing response signing with security key in %s", library));
        }
        final SignResponse response = Structure.newInstance(SignResponse.class, pointer);
        response.read();
        try {
            final byte[] r = this.bytes(response.sig_r, response.sig_r_len);
            final byte[] s = this.bytes(response.sig_s, response.sig_s_len);
            if(null == r) {
                // Request not confirmed on the device is reported with an empty response and no failure
                log.warn("Empty response with flags {} and counter {} from {}", response.flags, response.counter, library);
                throw new IOException("Signing request was not confirmed on the security key");
            }
            log.debug("Received signature with flags {} and counter {}", response.flags, response.counter);
            // Counter is an unsigned integer
            return new Response(response.flags, Integer.toUnsignedLong(response.counter), r, s);
        }
        finally {
            this.free(response, pointer);
        }
    }

    private byte[] bytes(final Pointer pointer, final long length) {
        if(null == pointer || length <= 0L) {
            return null;
        }
        return pointer.getByteArray(0L, (int) length);
    }

    /**
     * Release the response allocated by the middleware. Memory is allocated with the allocator of the C library the
     * same way OpenSSH frees it.
     */
    private void free(final SignResponse response, final Pointer pointer) {
        try {
            if(response.sig_r != null) {
                StandardCLibrary.INSTANCE.free(response.sig_r);
            }
            if(response.sig_s != null) {
                StandardCLibrary.INSTANCE.free(response.sig_s);
            }
            StandardCLibrary.INSTANCE.free(pointer);
        }
        catch(UnsatisfiedLinkError | NoClassDefFoundError e) {
            log.warn("Failure {} releasing response from security key provider {}", e, library);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NativeSecurityKeyMiddleware{");
        sb.append("library='").append(library).append('\'');
        sb.append('}');
        return sb.toString();
    }

    private interface SecurityKeyLibrary extends Library {
        int sk_api_version();

        /**
         * Sizes are <code>size_t</code> mapped to a 64-bit integer as on all supported platforms.
         */
        int sk_sign(int algorithm, byte[] data, long datalen, String application, byte[] keyHandle, long keyHandleLen,
                    byte flags, String pin, Pointer options, PointerByReference response);
    }

    private interface StandardCLibrary extends Library {
        StandardCLibrary INSTANCE = Native.load("c", StandardCLibrary.class);

        void free(Pointer pointer);
    }

    /**
     * <code>struct sk_sign_response</code>
     */
    public static final class SignResponse extends Structure {
        public byte flags;
        public int counter;
        public Pointer sig_r;
        public long sig_r_len;
        public Pointer sig_s;
        public long sig_s_len;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("flags", "counter", "sig_r", "sig_r_len", "sig_s", "sig_s_len");
        }
    }
}
