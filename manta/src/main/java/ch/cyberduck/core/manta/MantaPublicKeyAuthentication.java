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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.sftp.SSHFingerprintGenerator;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;

import com.hierynomus.sshj.userauth.keyprovider.OpenSSHKeyV1KeyFile;
import com.joyent.manta.config.SettableConfigContext;
import net.schmizz.sshj.userauth.keyprovider.FileKeyProvider;
import net.schmizz.sshj.userauth.keyprovider.KeyFormat;
import net.schmizz.sshj.userauth.keyprovider.KeyProviderUtil;
import net.schmizz.sshj.userauth.keyprovider.OpenSSHKeyFile;
import net.schmizz.sshj.userauth.keyprovider.PKCS5KeyFile;
import net.schmizz.sshj.userauth.keyprovider.PKCS8KeyFile;
import net.schmizz.sshj.userauth.keyprovider.PuTTYKeyFile;
import net.schmizz.sshj.userauth.password.PasswordFinder;
import net.schmizz.sshj.userauth.password.Resource;

public class MantaPublicKeyAuthentication implements MantaAuthentication {
    private static final Logger log = Logger.getLogger(MantaPublicKeyAuthentication.class);

    private final MantaSession session;

    public MantaPublicKeyAuthentication(final MantaSession session) {
        this.session = session;
    }

    public String authenticate(final Host bookmark, final HostPasswordStore keychain, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        final Credentials credentials = bookmark.getCredentials();
        final Local identity = credentials.getIdentity();
        final KeyFormat format = this.detectKeyFormat(identity);
        final FileKeyProvider provider = this.buildProvider(identity, format);
        this.readKeyContentsIntoConfig(identity);
        if(log.isInfoEnabled()) {
            log.info(String.format("Reading private key %s with key format %s", identity, format));
        }
        provider.init(
            new InputStreamReader(identity.getInputStream(), StandardCharsets.UTF_8),
            new PasswordFinder() {
                @Override
                public char[] reqPassword(Resource<?> resource) {
                    final String password = keychain.find(bookmark);
                    if(StringUtils.isEmpty(password)) {
                        try {
                            prompt.prompt(
                                bookmark,
                                credentials.getUsername(),
                                LocaleFactory.localizedString("Private key password protected", "Credentials"),
                                String.format("%s (%s)",
                                    LocaleFactory.localizedString("Enter the passphrase for the private key file", "Credentials"),
                                    identity.getAbbreviatedPath()),
                                new LoginOptions(bookmark.getProtocol()));
                        }
                        catch(LoginCanceledException e) {
                            return null; // user cancelled
                        }

                        if(StringUtils.isEmpty(credentials.getPassword())) {
                            return null; // user left field blank
                        }
                        final SettableConfigContext config = (SettableConfigContext) session.getClient().getContext();
                        config.setPassword(credentials.getPassword());
                        return credentials.getPassword().toCharArray();
                    }
                    return password.toCharArray();
                }

                @Override
                public boolean shouldRetry(Resource<?> resource) {
                    return false;
                }
            });
        return this.computeFingerprint(provider);
    }

    private String computeFingerprint(final FileKeyProvider provider) throws BackgroundException {
        try {
            final KeyPair keyPair = new KeyPair(provider.getPublic(), provider.getPrivate());
            return new SSHFingerprintGenerator().fingerprint(keyPair.getPublic());
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    private FileKeyProvider buildProvider(final Local identity, final KeyFormat format) throws InteroperabilityException {
        switch(format) {
            case PKCS5:
                return new PKCS5KeyFile.Factory().create();
            case PKCS8:
                return new PKCS8KeyFile.Factory().create();
            case OpenSSH:
                return new OpenSSHKeyFile.Factory().create();
            case OpenSSHv1:
                return new OpenSSHKeyV1KeyFile.Factory().create();
            case PuTTY:
                return new PuTTYKeyFile.Factory().create();
            default:
                throw new InteroperabilityException(String.format("Unknown key format for file %s", identity.getName()));
        }
    }

    private KeyFormat detectKeyFormat(final Local identity) throws BackgroundException {
        final KeyFormat format;
        try (InputStream is = identity.getInputStream()) {
            format = KeyProviderUtil.detectKeyFileFormat(
                new InputStreamReader(is, StandardCharsets.UTF_8),
                true);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
        return format;
    }

    /**
     * This method is required as a result of https://github.com/joyent/java-manta/issues/294
     *
     * @param identity credentials identity to read
     * @throws BackgroundException when reading the key contents fails
     */
    private void readKeyContentsIntoConfig(final Local identity) throws BackgroundException {
        try (InputStream is = identity.getInputStream();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            IOUtils.copy(is, baos);
            final SettableConfigContext config = (SettableConfigContext) session.getClient().getContext();
            config.setPrivateKeyContent(new String(baos.toByteArray(), StandardCharsets.UTF_8));
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }
}
