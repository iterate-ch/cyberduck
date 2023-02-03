package ch.cyberduck.core.sftp.auth;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AuthenticationProvider;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.sftp.SFTPExceptionMappingService;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

import com.hierynomus.sshj.userauth.keyprovider.OpenSSHKeyV1KeyFile;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.userauth.keyprovider.FileKeyProvider;
import net.schmizz.sshj.userauth.keyprovider.KeyFormat;
import net.schmizz.sshj.userauth.keyprovider.KeyProviderUtil;
import net.schmizz.sshj.userauth.keyprovider.OpenSSHKeyFile;
import net.schmizz.sshj.userauth.keyprovider.PKCS8KeyFile;
import net.schmizz.sshj.userauth.keyprovider.PuTTYKeyFile;
import net.schmizz.sshj.userauth.method.AuthPublickey;
import net.schmizz.sshj.userauth.password.PasswordFinder;
import net.schmizz.sshj.userauth.password.Resource;

public class SFTPPublicKeyAuthentication implements AuthenticationProvider<Boolean> {
    private static final Logger log = LogManager.getLogger(SFTPPublicKeyAuthentication.class);

    private final SSHClient client;

    public SFTPPublicKeyAuthentication(final SSHClient client) {
        this.client = client;
    }

    @Override
    public Boolean authenticate(final Host bookmark, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        final Credentials credentials = bookmark.getCredentials();
        if(credentials.isPublicKeyAuthentication()) {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Login using public key authentication with credentials %s", credentials));
            }
            final Local identity = credentials.getIdentity();
            final FileKeyProvider provider;
            final AtomicBoolean canceled = new AtomicBoolean();
            try {
                final KeyFormat format = KeyProviderUtil.detectKeyFileFormat(
                        new InputStreamReader(identity.getInputStream(), StandardCharsets.UTF_8), true);
                if(log.isInfoEnabled()) {
                    log.info(String.format("Reading private key %s with key format %s", identity, format));
                }
                switch(format) {
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
                provider.init(new InputStreamReader(identity.getInputStream(), StandardCharsets.UTF_8), new PasswordFinder() {
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
                                canceled.set(true);
                                // Return null if user cancels
                                return StringUtils.EMPTY.toCharArray();
                            }
                        }
                        return credentials.getIdentityPassphrase().toCharArray();
                    }

                    @Override
                    public boolean shouldRetry(Resource<?> resource) {
                        return false;
                    }
                });
                client.auth(credentials.getUsername(), new AuthPublickey(provider));
                return client.isAuthenticated();
            }
            catch(IOException e) {
                if(canceled.get()) {
                    throw new LoginCanceledException();
                }
                throw new SFTPExceptionMappingService().map(e);
            }
        }
        return false;
    }

    @Override
    public String getMethod() {
        return "publickey";
    }
}
