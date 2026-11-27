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

import ch.cyberduck.core.BookmarkNameProvider;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.LoginCanceledException;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.hierynomus.sshj.userauth.fido.SecurityKeySignatureData;
import com.hierynomus.sshj.userauth.fido.SecurityKeySigner;
import com.hierynomus.sshj.userauth.fido.SecurityKeySigningRequest;
import net.schmizz.sshj.common.KeyType;

/**
 * Sign with a FIDO/U2F security key through the middleware of the authenticator. The middleware expects the message
 * to sign and computes the client data hash itself, hence {@link SecurityKeySigningRequest#getData()} is passed and
 * not the challenge.
 * <p>
 * The middleware is loaded on first use. The signer must be attached to the key file before its private key is read,
 * which is before the key is known to be a security key that requires a middleware.
 *
 * @see <a href="https://github.com/openssh/openssh-portable/blob/master/sk-api.h">sk-api.h</a>
 */
public class SecurityKeyMiddlewareSigner implements SecurityKeySigner {

    /**
     * Load the middleware library of the authenticator
     */
    @FunctionalInterface
    public interface Loader {
        SecurityKeyMiddleware load() throws IOException;
    }

    private final Loader loader;
    private final Host bookmark;
    private final LoginCallback prompt;
    /**
     * Set when the prompt for the PIN of the authenticator is canceled
     */
    private final AtomicBoolean canceled;

    private SecurityKeyMiddleware middleware;

    public SecurityKeyMiddlewareSigner(final Loader loader, final Host bookmark,
                                       final LoginCallback prompt, final AtomicBoolean canceled) {
        this.loader = loader;
        this.bookmark = bookmark;
        this.prompt = prompt;
        this.canceled = canceled;
    }

    /**
     * @return Middleware library, loaded on first call
     * @throws IOException Middleware library not found or not implementing the expected API version
     */
    public synchronized SecurityKeyMiddleware middleware() throws IOException {
        if(null == middleware) {
            middleware = loader.load();
        }
        return middleware;
    }

    @Override
    public SecurityKeySignatureData sign(final SecurityKeySigningRequest request) throws IOException {
        final boolean ed25519 = KeyType.SK_ED25519.toString().equals(request.getKeyTypeName());
        final SecurityKeyMiddleware.Response response = this.sign(
                ed25519 ? SecurityKeyMiddleware.SSH_SK_ED25519 : SecurityKeyMiddleware.SSH_SK_ECDSA,
                request.getData(), request.getApplication(), request.getKeyHandle(), request.getMinFlags());
        try {
            if(ed25519) {
                // Entire signature is returned in r
                return SecurityKeySignatureData.ed25519(response.getFlags(), response.getCounter(), response.getR());
            }
            return SecurityKeySignatureData.ecdsa(response.getFlags(), response.getCounter(), response.getR(), response.getS());
        }
        catch(IllegalArgumentException e) {
            throw new IOException(String.format("Invalid signature from security key. %s", e.getMessage()), e);
        }
    }

    /**
     * Ask the authenticator for an assertion, prompting for the PIN when the device requires user verification
     */
    private SecurityKeyMiddleware.Response sign(final int algorithm, final byte[] data, final String application,
                                                final byte[] keyHandle, final byte flags) throws IOException {
        try {
            return this.sign(algorithm, data, application, keyHandle, flags, null);
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
                // Signal cancel to the authentication provider mapping the failure to LoginCanceledException
                canceled.set(true);
                throw new IOException("PIN prompt for security key canceled", c);
            }
            if(StringUtils.isBlank(input.getPassword())) {
                throw new IOException("Missing PIN for security key");
            }
            return this.sign(algorithm, data, application, keyHandle, flags, input.getPassword());
        }
    }

    /**
     * Sign with the middleware. When the key requires user presence, the call blocks until the token is
     * touched without any indication. In this case, sign in the background while displaying a prompt to touch the
     * security key with the option to cancel.
     */
    private SecurityKeyMiddleware.Response sign(final int algorithm, final byte[] data, final String application,
                                                final byte[] keyHandle, final byte flags, final String pin)
            throws IOException {
        final SecurityKeyMiddleware middleware = this.middleware();
        if(0 == (flags & SecurityKeyMiddleware.SSH_SK_USER_PRESENCE_REQD)) {
            return middleware.sign(algorithm, data, application, keyHandle, flags, pin);
        }
        final CountDownLatch signal = new CountDownLatch(1);
        final AtomicReference<SecurityKeyMiddleware.Response> response = new AtomicReference<>();
        final AtomicReference<Exception> failure = new AtomicReference<>();
        // Native call cannot be interrupted and returns once confirmed or the device gives up
        final Thread thread = new Thread(() -> {
            try {
                response.set(middleware.sign(algorithm, data, application, keyHandle, flags, pin));
            }
            catch(IOException | RuntimeException e) {
                failure.set(e);
            }
            finally {
                signal.countDown();
            }
        }, "security-key-sign");
        thread.setDaemon(true);
        thread.start();
        try {
            prompt.await(signal, bookmark,
                    String.format("%s %s", LocaleFactory.localizedString("Login", "Login"), BookmarkNameProvider.toString(bookmark, true)),
                    LocaleFactory.localizedString("Touch your security key", "Credentials"));
        }
        catch(ConnectionCanceledException e) {
            // Signal cancel to the authentication provider mapping the failure to LoginCanceledException
            canceled.set(true);
            throw new IOException("Confirmation for security key canceled", e);
        }
        final Exception e = failure.get();
        if(e instanceof IOException) {
            throw (IOException) e;
        }
        if(e != null) {
            throw new IOException(String.format("Failure signing with security key %s", bookmark.getCredentials().getIdentity()), e);
        }
        return response.get();
    }
}
