package ch.cyberduck.core;

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

public class CopyCredentialsHolder extends Credentials implements CredentialsHolder {

    private final CredentialsHolder delegate;

    /**
     * Copy credentials
     *
     * @param delegate Proxy
     */
    public CopyCredentialsHolder(final Credentials delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    @Override
    public Credentials setUsername(final String user) {
        delegate.setUsername(user);
        return super.setUsername(user);
    }

    @Override
    public Credentials setPassword(final String password) {
        delegate.setPassword(password);
        return super.setPassword(password);
    }

    @Override
    public Credentials setToken(final String token) {
        delegate.setToken(token);
        return super.setToken(token);
    }

    @Override
    public Credentials setTokens(final TemporaryAccessTokens tokens) {
        delegate.setTokens(tokens);
        return super.setTokens(tokens);
    }

    @Override
    public Credentials setOauth(final OAuthTokens oauth) {
        delegate.setOauth(oauth);
        return super.setOauth(oauth);
    }

    @Override
    public Credentials setSaved(final boolean saved) {
        delegate.setSaved(saved);
        return super.setSaved(saved);
    }

    @Override
    public Credentials setIdentity(final Local file) {
        delegate.setIdentity(file);
        return super.setIdentity(file);
    }

    @Override
    public Credentials setIdentityPassphrase(final String identityPassphrase) {
        delegate.setIdentityPassphrase(identityPassphrase);
        return super.setIdentityPassphrase(identityPassphrase);
    }

    @Override
    public Credentials setCertificate(final String certificate) {
        delegate.setCertificate(certificate);
        return super.setCertificate(certificate);
    }

    /**
     * Only reset delegate but keep copied credentials.
     */
    @Override
    public void reset() {
        delegate.reset();
    }
}
