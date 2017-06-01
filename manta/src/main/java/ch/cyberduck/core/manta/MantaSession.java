package ch.cyberduck.core.manta;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.MultipartWrite;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.ssl.SSLSession;
import ch.cyberduck.core.ssl.ThreadLocalHostnameDelegatingTrustManager;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.lang3.Validate;

import java.io.IOException;

import com.joyent.manta.client.MantaClient;
import com.joyent.manta.config.BaseChainedConfigContext;
import com.joyent.manta.config.ChainedConfigContext;
import com.joyent.manta.config.DefaultsConfigContext;
import com.joyent.manta.config.SettableConfigContext;
import com.joyent.manta.config.StandardConfigContext;

public class MantaSession extends SSLSession<MantaClient> {

    static final String HEADER_KEY_STORAGE_CLASS = "Durability-Level";

    final MantaPathMapper pathMapper;

    final MantaExceptionMappingService exceptionMapper;

    private SettableConfigContext<BaseChainedConfigContext> config;

    private String keyFingerprint;

    public MantaSession(final Host h) {
        this(h, new DisabledX509TrustManager(), new DefaultX509KeyManager());
    }

    public MantaSession(final Host h, final X509TrustManager trust, final X509KeyManager key) {
        super(h, new ThreadLocalHostnameDelegatingTrustManager(trust, h.getHostname()), key);
        exceptionMapper = new MantaExceptionMappingService(this);

        final Credentials bookmark = host.getCredentials();
        pathMapper = new MantaPathMapper(this);

        config = new ChainedConfigContext(
                new DefaultsConfigContext(),
                new StandardConfigContext()
                        .setHttpsProtocols(DefaultsConfigContext.DEFAULT_HTTPS_PROTOCOLS)
                        .setDisableNativeSignatures(true)
                        .setNoAuth(false)
                        .setMantaURL("https://" + h.getHostname())
                        .setMantaUser(bookmark.getUsername()));
    }

    @Override
    protected MantaClient connect(final HostKeyCallback key) throws BackgroundException {
        return null;
    }

    @Override
    public void login(final HostPasswordStore keychain,
                      final LoginCallback prompt,
                      final CancelCallback cancel,
                      final Cache<Path> cache) throws BackgroundException {
        keyFingerprint = null;

        if(host.getCredentials() != null
                && host.getCredentials().getUsername() != null
                && !host.getCredentials().getUsername().matches("[A-z0-9._]+(/[A-z0-9._]+)?")) {
            throw new LoginFailureException("Invalid username given: " + host.getCredentials().getUsername());
        }

        Validate.notNull(host);
        final boolean success = new MantaPublicKeyAuthentication(this, keychain).authenticate(host, prompt, cancel);

        if(!success) {
            throw new LoginFailureException("Failed to calculate key fingerprint");
        }

        Validate.notNull(keyFingerprint, "Key fingerprint missing.");

        config.setMantaKeyId(keyFingerprint)
                .setMantaKeyPath(host.getCredentials().getIdentity().getAbsolute());

        client = new MantaClient(config);

        try {
            // instantiation of a MantaClient does not validate credentials,
            client.isDirectoryEmpty(pathMapper.getNormalizedHomePath().getAbsolute());
        }
        catch(IOException e) {
            throw exceptionMapper.mapLoginException(e);
        }
    }

    @Override
    protected void logout() throws BackgroundException {
        client.close();
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        return new MantaListService(this).list(directory, listener);
    }

    void setFingerprint(final String f) {
        keyFingerprint = f;
    }

    String getFingerprint() {
        return keyFingerprint;
    }

    boolean userIsOwner() throws IllegalStateException {
        if(pathMapper.getAccountRoot() == null) {
            throw new IllegalStateException("Account owner not set");
        }

        return pathMapper.getAccountRoot().getName().equals(host.getCredentials().getUsername());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T _getFeature(final Class<T> type) {
        if(type == Directory.class) {
            return (T) new MantaDirectoryFeature(this);
        }
        if(type == Read.class) {
            return (T) new MantaReadFeature(this);
        }
        if(type == Write.class) {
            return (T) new MantaWriteFeature(this);
        }
        if(type == Delete.class) {
            return (T) new MantaDeleteFeature(this);
        }
        if(type == Touch.class) {
            return (T) new MantaTouchFeature(this);
        }
        if(type == Move.class) {
            return (T) new MantaMoveFeature(this);
        }
        if(type == AttributesFinder.class) {
            return (T) new MantaAttributesFinderFeature(this);
        }
        if(type == UrlProvider.class) {
            return (T) new MantaUrlProviderFeature();
        }

        return super._getFeature(type);
    }
}
