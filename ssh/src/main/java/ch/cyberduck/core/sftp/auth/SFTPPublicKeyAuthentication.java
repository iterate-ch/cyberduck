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
import ch.cyberduck.core.preferences.HostPreferencesFactory;
import ch.cyberduck.core.sftp.SFTPExceptionMappingService;
import ch.cyberduck.core.ssl.PKCS11KeyStore;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.concurrent.atomic.AtomicBoolean;

import com.hierynomus.sshj.userauth.fido.SecurityKeyPrivateKey;
import com.hierynomus.sshj.userauth.keyprovider.OpenSSHKeyFileUtil;
import com.hierynomus.sshj.userauth.keyprovider.OpenSSHKeyV1KeyFile;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.KeyType;
import net.schmizz.sshj.userauth.keyprovider.FileKeyProvider;
import net.schmizz.sshj.userauth.keyprovider.KeyFormat;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
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
                client.auth(credentials.getUsername(), new AuthPublickey(new PKCS11KeyProvider(provider, bookmark, prompt)));
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
     * Wraps {@code provider} so that when the loaded private key is a {@link SecurityKeyPrivateKey}
     * with no signer attached (i.e. an {@code sk-*} key loaded from a file), a
     * {@link PKCS11SecurityKeySigner} is injected to drive the PKCS#11 hardware token.
     */
    private static final class PKCS11KeyProvider implements KeyProvider {
        private final FileKeyProvider provider;
        private final Host bookmark;
        private final LoginCallback prompt;

        public PKCS11KeyProvider(final FileKeyProvider provider, final Host bookmark, final LoginCallback prompt) {
            this.provider = provider;
            this.bookmark = bookmark;
            this.prompt = prompt;
        }

        @Override
        public PrivateKey getPrivate() throws IOException {
            final PrivateKey pk = provider.getPrivate();
            if(pk instanceof SecurityKeyPrivateKey) {
                final SecurityKeyPrivateKey sk = (SecurityKeyPrivateKey) pk;
                if(sk.getSigner() == null) {
                    log.debug("Attaching PKCS11 signer to security key {}", sk.getKeyTypeName());
                    return new SecurityKeyPrivateKey(sk.getKeyTypeName(), sk.getPublicKey(), sk.getFlags(), sk.getKeyHandle(),
                            new PKCS11SecurityKeySigner(sk.getPublicKey().getDelegate(),
                                    PKCS11KeyStore.build(
                                            HostPreferencesFactory.get(bookmark).getProperty("connection.ssl.keystore.pkcs11.library"), bookmark, prompt)));
                }
            }
            return pk;
        }

        @Override
        public PublicKey getPublic() throws IOException {
            return provider.getPublic();
        }

        @Override
        public KeyType getType() throws IOException {
            return provider.getType();
        }
    }
}
