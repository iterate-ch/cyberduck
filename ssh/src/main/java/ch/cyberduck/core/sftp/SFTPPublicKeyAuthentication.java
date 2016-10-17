package ch.cyberduck.core.sftp;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import net.schmizz.sshj.userauth.keyprovider.FileKeyProvider;
import net.schmizz.sshj.userauth.keyprovider.KeyFormat;
import net.schmizz.sshj.userauth.keyprovider.KeyProviderUtil;
import net.schmizz.sshj.userauth.keyprovider.OpenSSHKeyFile;
import net.schmizz.sshj.userauth.keyprovider.PKCS5KeyFile;
import net.schmizz.sshj.userauth.keyprovider.PKCS8KeyFile;
import net.schmizz.sshj.userauth.keyprovider.PuTTYKeyFile;
import net.schmizz.sshj.userauth.password.PasswordFinder;
import net.schmizz.sshj.userauth.password.Resource;

public class SFTPPublicKeyAuthentication implements SFTPAuthentication {
    private static final Logger log = Logger.getLogger(SFTPPublicKeyAuthentication.class);

    private SFTPSession session;

    public SFTPPublicKeyAuthentication(final SFTPSession session) {
        this.session = session;
    }

    @Override
    public boolean authenticate(final Host host, final LoginCallback prompt, final CancelCallback cancel)
            throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Login using public key authentication with credentials %s", host.getCredentials()));
        }
        if(host.getCredentials().isPublicKeyAuthentication()) {
            final Local identity = host.getCredentials().getIdentity();
            final FileKeyProvider provider;
            try {
                final KeyFormat format = KeyProviderUtil.detectKeyFileFormat(
                        new InputStreamReader(identity.getInputStream(), Charset.forName("UTF-8")), true);
                if(log.isInfoEnabled()) {
                    log.info(String.format("Reading private key %s with key format %s", identity, format));
                }
                if(format.equals(KeyFormat.OpenSSH)) {
                    provider = new OpenSSHKeyFile.Factory().create();
                }
                else if(format.equals(KeyFormat.PKCS5)) {
                    provider = new PKCS5KeyFile.Factory().create();
                }
                else if(format.equals(KeyFormat.PKCS8)) {
                    provider = new PKCS8KeyFile.Factory().create();
                }
                else if(format.equals(KeyFormat.PuTTY)) {
                    provider = new PuTTYKeyFile.Factory().create();
                }
                else {
                    throw new InteroperabilityException(String.format("Unknown key format for file %s", identity.getName()));
                }
                provider.init(new InputStreamReader(identity.getInputStream(), Charset.forName("UTF-8")), new PasswordFinder() {
                    @Override
                    public char[] reqPassword(Resource<?> resource) {
                        if(StringUtils.isEmpty(host.getCredentials().getPassword())) {
                            try {
                                prompt.prompt(host, host.getCredentials(),
                                        LocaleFactory.localizedString("Private key password protected", "Credentials"),
                                        String.format("%s (%s)",
                                                LocaleFactory.localizedString("Enter the passphrase for the private key file", "Credentials"),
                                                identity.getAbbreviatedPath()), new LoginOptions(host.getProtocol())
                                );
                            }
                            catch(LoginCanceledException e) {
                                // Return null if user cancels
                                return null;
                            }
                        }
                        return host.getCredentials().getPassword().toCharArray();
                    }

                    @Override
                    public boolean shouldRetry(Resource<?> resource) {
                        return false;
                    }
                });
                session.getClient().authPublickey(host.getCredentials().getUsername(), provider);
                return session.getClient().isAuthenticated();
            }
            catch(IOException e) {
                throw new SFTPExceptionMappingService().map(e);
            }
        }
        return false;
    }
}
