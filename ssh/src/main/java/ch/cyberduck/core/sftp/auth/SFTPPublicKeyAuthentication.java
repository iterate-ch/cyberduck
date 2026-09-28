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
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.preferences.HostPreferencesFactory;
import ch.cyberduck.core.sftp.SFTPExceptionMappingService;
import ch.cyberduck.core.sftp.openssh.OpenSSHSecurityKeyProviderConfigurator;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.concurrent.atomic.AtomicBoolean;

import com.hierynomus.sshj.userauth.keyprovider.OpenSSHKeyFileUtil;
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
            log.debug("Login using public key authentication with credentials {}", credentials);
            final Local privKey = credentials.getIdentity();
            final Local pubKey;
            final FileKeyProvider provider;
            final AtomicBoolean canceled = new AtomicBoolean();
            try {
                final KeyFormat format = KeyProviderUtil.detectKeyFileFormat(
                        new InputStreamReader(privKey.getInputStream(), StandardCharsets.UTF_8), true);
                log.info("Reading private key {} with key format {}", privKey, format);
                switch(format) {
                    case PKCS8:
                        provider = new PKCS8KeyFile.Factory().create();
                        pubKey = null;
                        break;
                    case OpenSSH: {
                        provider = new OpenSSHKeyFile.Factory().create();
                        final File f = OpenSSHKeyFileUtil.getPublicKeyFile(new File(privKey.getAbsolute()));
                        if(f != null) {
                            pubKey = LocalFactory.get(f.getAbsolutePath());
                        }
                        else {
                            pubKey = null;
                        }
                        break;
                    }
                    case OpenSSHv1: {
                        provider = new OpenSSHKeyV1KeyFile.Factory().create();
                        final File f = OpenSSHKeyFileUtil.getPublicKeyFile(new File(privKey.getAbsolute()));
                        if(f != null) {
                            pubKey = LocalFactory.get(f.getAbsolutePath());
                        }
                        else {
                            pubKey = null;
                        }
                        break;
                    }
                    case PuTTY:
                        provider = new PuTTYKeyFile.Factory().create();
                        pubKey = null;
                        break;
                    default:
                        log.warn("Unknown key format for file {}", privKey.getName());
                        return false;
                }
                provider.init(new InputStreamReader(privKey.getInputStream(), StandardCharsets.UTF_8),
                        pubKey != null ? new InputStreamReader(pubKey.getInputStream(), StandardCharsets.UTF_8) : null,
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
                                                privKey.getAbbreviatedPath()),
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
                switch(provider.getType()) {
                    case SK_ECDSA:
                    case SK_ED25519: {
                        // The private key is on the authenticator and not in the key file. Signing is delegated to the
                        // middleware of the security key, the equivalent of SecurityKeyProvider in ssh_config
                        final SecurityKeyMiddleware middleware;
                        try {
                            middleware = this.middleware(bookmark);
                        }
                        catch(IOException e) {
                            log.warn("Failure {} loading middleware for security key {}", e, privKey);
                            throw new LoginFailureException(e.getMessage(), e);
                        }
                        client.auth(credentials.getUsername(),
                                new AuthSecurityKeyPublickey(provider, middleware, bookmark, prompt));
                        return client.isAuthenticated();
                    }
                }
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

    /**
     * @return Middleware library of the security key to sign with
     * @throws IOException Middleware library not found or not implementing the expected API version
     */
    protected SecurityKeyMiddleware middleware(final Host bookmark) throws IOException {
        final String library = this.provider(bookmark);
        log.debug("Load middleware {} for security key", library);
        return new NativeSecurityKeyMiddleware(library);
    }

    /**
     * Middleware library of the security key, read from <code>SecurityKeyProvider</code> in <code>ssh_config</code>,
     * the <code>SSH_SK_PROVIDER</code> environment variable as read by OpenSSH or the host preference.
     *
     * @return Path to shared library implementing the OpenSSH security key API
     */
    protected String provider(final Host bookmark) {
        final String configuration = new OpenSSHSecurityKeyProviderConfigurator().getProvider(bookmark.getHostname());
        if(StringUtils.isNotBlank(configuration)) {
            return configuration;
        }
        final String environment = System.getenv("SSH_SK_PROVIDER");
        if(StringUtils.isNotBlank(environment)) {
            log.debug("Determined security key provider {} from environment", environment);
            return environment;
        }
        return HostPreferencesFactory.get(bookmark).getProperty("ssh.authentication.securitykey.provider");
    }

}
