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
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Home;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.MultipartWrite;
import ch.cyberduck.core.features.Quota;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Search;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.ssl.SSLSession;
import ch.cyberduck.core.ssl.ThreadLocalHostnameDelegatingTrustManager;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.log4j.Logger;

import com.joyent.manta.client.MantaClient;
import com.joyent.manta.client.MantaObject;
import com.joyent.manta.config.BaseChainedConfigContext;
import com.joyent.manta.config.ChainedConfigContext;
import com.joyent.manta.config.DefaultsConfigContext;
import com.joyent.manta.config.StandardConfigContext;

public class MantaSession extends SSLSession<MantaClient> {

    public static final Logger log = Logger.getLogger(MantaSession.class);

    static final String HEADER_KEY_STORAGE_CLASS = "Durability-Level";
    public static final String HOME_PATH_PRIVATE = "/stor";
    public static final String HOME_PATH_PUBLIC = "/public";

    private BaseChainedConfigContext config;

    private String keyFingerprint;
    private String accountOwner;

    private final MantaPathMapper pathMapper;

    public MantaSession(final Host h) {
        this(h, new DisabledX509TrustManager(), new DefaultX509KeyManager());
    }

    public MantaSession(final Host h, final X509TrustManager trust, final X509KeyManager key) {
        super(h, new ThreadLocalHostnameDelegatingTrustManager(trust, h.getHostname()), key);
        pathMapper = new MantaPathMapper(this);
        config = new ChainedConfigContext(
                new DefaultsConfigContext(),
                new StandardConfigContext()
                        .setDisableNativeSignatures(true)
                        .setMantaURL("https://" + h.getHostname())
                        .setNoAuth(false));

    }

    @Override
    protected MantaClient connect(final HostKeyCallback key) throws BackgroundException {
        keyFingerprint = null;
        return null;
    }

    @Override
    public void login(final HostPasswordStore keychain,
                      final LoginCallback prompt,
                      final CancelCallback cancel,
                      final Cache<Path> cache) throws BackgroundException {
        final Credentials bookmark = host.getCredentials();
        final boolean success = new MantaPublicKeyAuthentication(this, keychain).authenticate(host, prompt, cancel);

        if(!success) {
            throw new MantaExceptionMappingService().map(new RuntimeException("Failed to calculate key fingerprint"));
        }

        Validate.notNull(this.keyFingerprint);

        setAccountOwner(bookmark.getUsername());

        config.setMantaUser(bookmark.getUsername())
                .setMantaKeyPath(bookmark.getIdentity().getAbsolute())
                .setMantaKeyId(keyFingerprint);

        try {
            client = new MantaClient(config);
        }
        catch(Exception e) {
            throw new MantaExceptionMappingService().map(e);
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

    private void setAccountOwner(final String username) {
        if(StringUtils.contains("/", username)) {
            // TODO: more string validation
            accountOwner = username.split("/")[0];
        }

        accountOwner = username;
    }

    String getAccountOwner() {
        return accountOwner;
    }

    boolean isUserWritable(final MantaObject mantaObject) {
        return isUserWritable(mantaObject.getPath());
    }

    boolean isUserWritable(final Path homeRelativePath) {
        return isUserWritable(homeRelativePath.getAbsolute());
    }

    private boolean isUserWritable(final String homeRelativePath) {
        return StringUtils.startsWithAny(homeRelativePath, HOME_PATH_PRIVATE, HOME_PATH_PUBLIC);
    }

    boolean isWorldReadable(final MantaObject mantaObject) {
        return isWorldReadable(mantaObject.getPath();
    }

    boolean isWorldReadable(final Path homeRelativePath) {
        return isWorldReadable(homeRelativePath.getAbsolute());
    }

    private boolean isWorldReadable(final String path) {
        return StringUtils.startsWith(path, MantaSession.HOME_PATH_PUBLIC);
    }

    MantaPathMapper getPathMapper() {
        return this.pathMapper;
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
        if(type == MultipartWrite.class) {
            return (T) new MantaBufferWriteFeature(this);
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
            return (T) new MantaUrlProvider();
        }
        if(type == Home.class) {
            return (T) new MantaHomeFinderFeature(this);
        }
        if(type == Quota.class) {
            return (T) new MantaQuotaFeature(this);
        }
        if(type == Search.class) {
            return (T) new MantaSearchFeature(this);
        }
        return super._getFeature(type);
    }

}
