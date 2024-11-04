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

import ch.cyberduck.core.AuthenticationProvider;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.StringAppender;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.sftp.SSHFingerprintGenerator;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.openssl.EncryptionException;
import org.bouncycastle.openssl.PasswordException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import com.hierynomus.sshj.userauth.keyprovider.OpenSSHKeyV1KeyFile;
import com.joyent.manta.config.SettableConfigContext;
import net.schmizz.sshj.userauth.keyprovider.FileKeyProvider;
import net.schmizz.sshj.userauth.keyprovider.KeyFormat;
import net.schmizz.sshj.userauth.keyprovider.KeyProviderUtil;
import net.schmizz.sshj.userauth.keyprovider.OpenSSHKeyFile;
import net.schmizz.sshj.userauth.keyprovider.PKCS8KeyFile;
import net.schmizz.sshj.userauth.keyprovider.PuTTYKeyFile;
import net.schmizz.sshj.userauth.password.PasswordFinder;
import net.schmizz.sshj.userauth.password.Resource;

public class MantaPublicKeyAuthentication implements AuthenticationProvider<String> {
    private static final Logger log = LogManager.getLogger(MantaPublicKeyAuthentication.class);

    private final MantaSession session;

    public MantaPublicKeyAuthentication(final MantaSession session) {
        this.session = session;
    }

    public String authenticate(final Host bookmark, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        final Credentials credentials = bookmark.getCredentials();
        final Local identity = credentials.getIdentity();
        final KeyFormat format = this.detectKeyFormat(identity);
        final FileKeyProvider provider = this.buildProvider(identity, format);

        final SettableConfigContext config = (SettableConfigContext) session.getClient().getContext();
        config.setMantaKeyPath(identity.getAbsolute());

        log.info("Reading private key {} with key format {}", identity, format);
        provider.init(
                new InputStreamReader(identity.getInputStream(), StandardCharsets.UTF_8),
                new PasswordFinder() {
                    @Override
                    public char[] reqPassword(Resource<?> resource) {
                        if(StringUtils.isEmpty(credentials.getIdentityPassphrase())) {
                            try {
                                // Use password prompt
                                final Credentials input = prompt.prompt(bookmark,
                                        LocaleFactory.localizedString("Private key password protected", "Credentials"),
                                        String.format("%s (%s)",
                                                LocaleFactory.localizedString("Enter the passphrase for the private key file", "Credentials"),
                                                identity.getAbbreviatedPath()),
                                        new LoginOptions()
                                                .icon(bookmark.getProtocol().disk())
                                                .user(false).password(true)
                                );
                                credentials.setSaved(input.isSaved());
                                credentials.setIdentityPassphrase(input.getPassword());
                            }
                            catch(LoginCanceledException e) {
                                // Return null if user cancels
                                return StringUtils.EMPTY.toCharArray();
                            }
                        }
                        config.setPassword(credentials.getIdentityPassphrase());
                        return credentials.getIdentityPassphrase().toCharArray();
                    }

                    @Override
                    public boolean shouldRetry(Resource<?> resource) {
                        return false;
                    }
                }
        );
        return this.computeFingerprint(provider);
    }

    @Override
    public String getMethod() {
        return "publickey";
    }

    private String computeFingerprint(final FileKeyProvider provider) throws BackgroundException {
        try {
            return new SSHFingerprintGenerator().fingerprint(provider.getPublic());
        }
        catch(PasswordException e) {
            throw new LoginCanceledException(e);
        }
        catch(EncryptionException e) {
            final StringAppender appender = new StringAppender();
            appender.append(StringUtils.capitalize(e.getMessage()));
            throw new LoginFailureException(appender.toString(), e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    private FileKeyProvider buildProvider(final Local identity, final KeyFormat format) throws InteroperabilityException {
        switch(format) {
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
}
