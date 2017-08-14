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
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyException;
import java.security.KeyPair;

import com.hierynomus.sshj.userauth.keyprovider.OpenSSHKeyV1KeyFile;
import com.joyent.manta.http.signature.KeyFingerprinter;
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

    private final HostPasswordStore keychain;

    public MantaPublicKeyAuthentication(final MantaSession session, final HostPasswordStore keychain) {
        this.session = session;
        this.keychain = keychain;
    }

    public boolean authenticate(final Host bookmark, final LoginCallback prompt, final CancelCallback cancel)
            throws BackgroundException {

        final Credentials credentials = bookmark.getCredentials();

        log.info(String.format("Login using public key authentication with credentials %s", credentials));

        if(!credentials.isPublicKeyAuthentication()) {
            throw new MantaExceptionMappingService(session).map(new KeyException("Private Key Authentication is required"));
        }

        final Local identity = credentials.getIdentity();
        final KeyFormat format = detectKeyFormat(identity);
        final FileKeyProvider provider = buildProvider(identity, format);
        readKeyContentsIntoConfig(identity);

        log.info(String.format("Reading private key %s with key format %s", identity, format));

        initializePasswordProvider(bookmark, prompt, credentials, identity, provider);

        final String fingerprint = computeFingerprint(provider);

        session.setFingerprint(fingerprint);
        return true;
    }

    private void initializePasswordProvider(final Host bookmark,
                                            final LoginCallback prompt,
                                            final Credentials credentials,
                                            final Local identity,
                                            final FileKeyProvider provider) throws AccessDeniedException {
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
                                        credentials,
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

                            session.getConfig().setPassword(credentials.getPassword());
                            return credentials.getPassword().toCharArray();
                        }
                        return password.toCharArray();
                    }

                    @Override
                    public boolean shouldRetry(Resource<?> resource) {
                        return false;
                    }
                });
    }

    private String computeFingerprint(final FileKeyProvider provider) throws BackgroundException {
        final String fingerprint;
        try {
            final KeyPair keyPair = new KeyPair(provider.getPublic(), provider.getPrivate());
            fingerprint = KeyFingerprinter.md5Fingerprint(keyPair);
        }
        catch(IOException e) {
            throw new MantaExceptionMappingService(session).map(e);
        }
        return fingerprint;
    }

    private FileKeyProvider buildProvider(final Local identity, final KeyFormat format) throws InteroperabilityException {
        final FileKeyProvider provider;
        switch(format) {
            case PKCS5:
                provider = new PKCS5KeyFile.Factory().create();
                break;
            case PKCS8:
                provider = new PKCS8KeyFile.Factory().create();
                break;
            case OpenSSH:
                provider = new OpenSSHKeyFile.Factory().create();
                break;
            case OpenSSHv1:
                provider = new OpenSSHKeyV1KeyFile.Factory().create();
                break;
            case PuTTY:
                provider = new PuTTYKeyFile.Factory().create();
                break;
            default:
                throw new InteroperabilityException(String.format("Unknown key format for file %s", identity.getName()));
        }
        return provider;
    }

    private KeyFormat detectKeyFormat(final Local identity) throws BackgroundException {
        final KeyFormat format;
        try (InputStream is = identity.getInputStream()) {
            format = KeyProviderUtil.detectKeyFileFormat(
                    new InputStreamReader(is, StandardCharsets.UTF_8),
                    true);
        }
        catch(IOException e) {
            throw new MantaExceptionMappingService(session).mapLoginException(e);
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
            session.getConfig().setPrivateKeyContent(new String(baos.toByteArray(), StandardCharsets.UTF_8));
        }
        catch(IOException e) {
            throw new MantaExceptionMappingService(session).mapLoginException(e);
        }
    }
}
