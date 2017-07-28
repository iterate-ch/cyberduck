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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Home;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.EnumSet;

import com.joyent.manta.client.MantaClient;
import com.joyent.manta.client.MantaObject;
import com.joyent.manta.client.crypto.ExternalSecurityProviderLoader;
import com.joyent.manta.config.BaseChainedConfigContext;
import com.joyent.manta.config.ChainedConfigContext;
import com.joyent.manta.config.DefaultsConfigContext;
import com.joyent.manta.config.SettableConfigContext;
import com.joyent.manta.config.StandardConfigContext;
import com.joyent.manta.util.MantaUtils;

public class MantaSession extends Session<MantaClient> {

    static {
        ExternalSecurityProviderLoader.getBouncyCastleProvider();
    }

    public static final Logger log = Logger.getLogger(MantaSession.class);

    private final SettableConfigContext<BaseChainedConfigContext> config;

    private String keyFingerprint;

    private final String accountOwner;

    public static final String HOME_PATH_PRIVATE = "stor";
    public static final String HOME_PATH_PUBLIC = "public";

    private final Path accountRoot;
    private final Path normalizedHomePath;
    private final Path accountPublicRoot;
    private final Path accountPrivateRoot;

    public MantaSession(final Host host) {
        super(host);

        String[] accountPathParts = MantaUtils.parseAccount(host.getCredentials().getUsername());

        accountRoot = new Path(accountPathParts[0], EnumSet.of(AbstractPath.Type.placeholder));
        accountOwner = accountRoot.getName();
        normalizedHomePath = buildNormalizedHomePath(host.getDefaultPath());

        accountPublicRoot = new Path(
                accountRoot,
                HOME_PATH_PUBLIC,
                EnumSet.of(AbstractPath.Type.volume, AbstractPath.Type.directory));
        accountPrivateRoot = new Path(
                accountRoot,
                HOME_PATH_PRIVATE,
                EnumSet.of(AbstractPath.Type.volume, AbstractPath.Type.directory));

        config = new ChainedConfigContext(
                new DefaultsConfigContext(),
                new StandardConfigContext()
                        .setHttpsProtocols(DefaultsConfigContext.DEFAULT_HTTPS_PROTOCOLS)
                        .setDisableNativeSignatures(true)
                        .setNoAuth(false)
                        .setMantaURL("https://" + host.getHostname())
                        .setMantaUser(host.getCredentials().getUsername()));
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

        if(host.getCredentials() == null
                || host.getCredentials().getUsername() == null
                || !host.getCredentials().getUsername().matches("[A-z0-9._]+(/[A-z0-9._]+)?")) {
            throw new LoginFailureException("Invalid username given: " + host.getCredentials().getUsername());
        }

        final boolean success = new MantaPublicKeyAuthentication(this, keychain).authenticate(host, prompt, cancel);

        if (host.getCredentials().getPassword() != null) {
            config.setPassword(host.getCredentials().getPassword());
        }

        if(!success) {
            throw new LoginFailureException("Failed to calculate key fingerprint");
        }

        Validate.notNull(keyFingerprint, "Key fingerprint missing.");

        config.setMantaKeyId(keyFingerprint)
                .setMantaKeyPath(host.getCredentials().getIdentity().getAbsolute());

        client = new MantaClient(config);

        try {
            // instantiation of a MantaClient does not validate credentials,
            // let's list the home path to test the connection
            client.isDirectoryEmpty(normalizedHomePath.getAbsolute());
        }
        catch(IOException e) {
            throw new MantaExceptionMappingService(this).mapLoginException(e);
        }
    }

    @Override
    protected void logout() throws BackgroundException {
        client.closeWithWarning();
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        return new MantaListService(this).list(directory, listener);
    }

    protected void setFingerprint(final String f) {
        keyFingerprint = f;
    }

    protected String getFingerprint() {
        return keyFingerprint;
    }

    protected SettableConfigContext<BaseChainedConfigContext> getConfig() {
        return config;
    }

    protected boolean userIsOwner() throws IllegalStateException {
        return StringUtils.equals(host.getCredentials().getUsername(), accountOwner);
    }

    private Path buildNormalizedHomePath(final String rawHomePath) {
        final String defaultPath = StringUtils.defaultIfBlank(rawHomePath, Path.HOME);
        final String accountRootRegex = "^/?(" + accountRoot.getAbsolute() + "|~~?)/?";
        final String subdirectoryRawPath = defaultPath.replaceFirst(accountRootRegex, "");

        if(StringUtils.isEmpty(subdirectoryRawPath)) {
            return accountRoot;
        }

        final String[] subdirectoryPathSegments = StringUtils.split(subdirectoryRawPath, Path.DELIMITER);
        Path homePath = accountRoot;

        for(final String pathSegment : subdirectoryPathSegments) {
            EnumSet<AbstractPath.Type> types = EnumSet.of(AbstractPath.Type.directory);
            if(homePath.getParent().equals(accountRoot)
                    && StringUtils.equalsAny(pathSegment, HOME_PATH_PRIVATE, HOME_PATH_PUBLIC)) {
                types.add(AbstractPath.Type.volume);
            }

            homePath = new Path(homePath, pathSegment, types);
        }

        return homePath;
    }

    protected Path getNormalizedHomePath() {
        return normalizedHomePath;
    }

    protected Path getAccountRoot() {
        return accountRoot;
    }

    protected String getAccountOwner() {
        return accountOwner;
    }

    protected Path getAccountPublicRoot() {
        return accountPublicRoot;
    }

    protected Path getAccountPrivateRoot() {
        return accountPrivateRoot;
    }

    protected boolean isUserWritable(final MantaObject mantaObject) {
        return StringUtils.startsWithAny(
                mantaObject.getPath(),
                accountPublicRoot.getAbsolute(),
                accountPrivateRoot.getAbsolute());
    }

    protected boolean isUserWritable(final Path path) {
        return path.isChild(accountPublicRoot) || path.isChild(accountPrivateRoot);
    }

    protected boolean isWorldReadable(final MantaObject mantaObject) {
        return StringUtils.startsWithAny(
                mantaObject.getPath(),
                accountPublicRoot.getAbsolute());
    }

    protected boolean isWorldReadable(final Path path) {
        return path.isChild(accountPublicRoot);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T _getFeature(final Class<T> type) {
        if(type == Directory.class) {
            return (T) new MantaDirectoryFeature(this);
        }
        else if(type == Read.class) {
            return (T) new MantaReadFeature(this);
        }
        else if(type == Write.class) {
            return (T) new MantaWriteFeature(this);
        }
        else if(type == Delete.class) {
            return (T) new MantaDeleteFeature(this);
        }
        else if(type == Touch.class) {
            return (T) new MantaTouchFeature(this);
        }
        else if(type == Move.class) {
            return (T) new MantaMoveFeature(this);
        }
        else if(type == AttributesFinder.class) {
            return (T) new MantaAttributesFinderFeature(this);
        }
        else if(type == UrlProvider.class) {
            return (T) new MantaUrlProviderFeature(this);
        }
        else if(type == Home.class) {
            return (T) new MantaHomeFinderFeature(this);
        }

        return super._getFeature(type);
    }
}
