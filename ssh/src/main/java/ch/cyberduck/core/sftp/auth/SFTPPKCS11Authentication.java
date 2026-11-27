package ch.cyberduck.core.sftp.auth;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.sftp.SFTPExceptionMappingService;
import ch.cyberduck.core.sftp.openssh.OpenSSHCredentialsConfigurator;
import ch.cyberduck.core.sftp.openssh.OpenSSHIdentitiesOnlyConfigurator;
import ch.cyberduck.core.ssl.PKCS11KeyStore;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import com.jcraft.jsch.agentproxy.Identity;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.userauth.UserAuthException;

/**
 * Public key authentication with keys on a PKCS#11 token such as a smartcard or a hardware token, equivalent to
 * <code>ssh -I /path/to/pkcs11.so</code> respectively the <code>PKCS11Provider</code> option in
 * <code>ssh_config</code>.
 * <p>
 * Every key on the token with a certificate is offered to the server in turn, the same way keys from an authentication
 * agent are. Only keys with a certificate on the token are found because the JCA PKCS#11 keystore does not expose
 * private key objects without a matching certificate.
 * <p>
 * With <code>IdentitiesOnly</code> in <code>ssh_config</code> only the key on the token matching the public key file
 * set with <code>IdentityFile</code> is offered. The token is not used when no public key file is configured.
 * <p>
 * Likewise with a public key file selected in the bookmark only the matching key on the token is offered. The token is
 * not used with a private key file selected in the bookmark to not prompt for the PIN of the token.
 */
public class SFTPPKCS11Authentication implements AuthenticationProvider<Boolean> {
    private static final Logger log = LogManager.getLogger(SFTPPKCS11Authentication.class);

    private final SSHClient client;
    private final String library;

    /**
     * @param library Native PKCS#11 library name or path
     */
    public SFTPPKCS11Authentication(final SSHClient client, final String library) {
        this.client = client;
        this.library = library;
    }

    @Override
    public Boolean authenticate(final Host bookmark, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        log.debug("Login using PKCS11 library {} for {}", library, bookmark);
        // Public key file to limit authentication to or null to attempt with all keys on the token
        final Local file;
        if(new OpenSSHIdentitiesOnlyConfigurator().isIdentitiesOnly(bookmark.getHostname())) {
            final Credentials configuration = new OpenSSHCredentialsConfigurator().configure(bookmark);
            if(!configuration.isPublicKeyAuthentication()) {
                log.warn("Missing IdentityFile configuration for {}", bookmark);
                return false;
            }
            file = configuration.getIdentity();
        }
        else if(bookmark.getCredentials().isPublicKeyAuthentication()) {
            file = bookmark.getCredentials().getIdentity();
        }
        else {
            file = null;
        }
        // Public key to limit authentication to or null to attempt with all keys on the token
        final byte[] configured;
        if(null == file) {
            configured = null;
        }
        else {
            try {
                if(SFTPAgentAuthentication.isPrivateKey(file)) {
                    // Private key file cannot be on token. Skip to not prompt for PIN of token
                    log.warn("Skip authentication with PKCS11 library {} with private key {}", library, file);
                    return false;
                }
                final Identity identity = SFTPAgentAuthentication.identityFromPublicKey(file);
                if(null == identity) {
                    log.warn("Missing public key for {}", file);
                    return false;
                }
                log.info("Only read key matching public key {} from PKCS11 token", file);
                configured = identity.getBlob();
            }
            catch(IOException e) {
                throw new DefaultIOExceptionMappingService().map(e);
            }
        }
        final KeyStore store;
        try {
            // Prompts for PIN if required by token
            store = this.load(bookmark, prompt);
        }
        catch(ConcurrentException e) {
            // Continue with other authentication methods when failing to load token or prompt for PIN is canceled
            log.warn("Skip authentication with PKCS11 library {} failing with {}", library, e.getMessage());
            return false;
        }
        final List<PKCS11KeyProvider> identities = this.filter(this.list(store), configured);
        if(identities.isEmpty()) {
            log.warn("No identity found in PKCS11 token for library {}", library);
            return false;
        }
        for(PKCS11KeyProvider identity : identities) {
            cancel.verify();
            try {
                client.auth(bookmark.getCredentials().getUsername(), new AuthPKCS11Publickey(identity));
                // Successfully authenticated
                break;
            }
            catch(UserAuthException e) {
                log.warn("Login refused for identity {} with failure {}", identity, e.getMessage());
                // Continue with next identity on token
            }
            catch(TransportException e) {
                throw new SFTPExceptionMappingService().map(e);
            }
            catch(IOException e) {
                log.warn("Failure {} reading public key for identity {}", e.getMessage(), identity);
            }
        }
        return client.isAuthenticated();
    }

    /**
     * Load keystore from token
     *
     * @throws ConcurrentException Failure loading library or prompt for PIN canceled
     */
    protected KeyStore load(final Host bookmark, final LoginCallback prompt) throws ConcurrentException {
        return PKCS11KeyStore.build(library, bookmark, prompt).get();
    }

    /**
     * @param configured Public key blob to limit authentication to or null to attempt with all keys on the token
     * @return Keys on the token matching the configured public key
     */
    protected List<PKCS11KeyProvider> filter(final List<PKCS11KeyProvider> identities, final byte[] configured) {
        if(null == configured) {
            return identities;
        }
        for(PKCS11KeyProvider identity : identities) {
            try {
                if(Arrays.equals(identity.getBlob(), configured)) {
                    log.debug("Matching identity {} found by public key blob", identity);
                    return Collections.singletonList(identity);
                }
            }
            catch(IOException e) {
                log.warn("Failure {} reading public key for identity {}", e.getMessage(), identity);
            }
        }
        log.warn("No identity in PKCS11 token matches configured public key");
        return Collections.emptyList();
    }

    /**
     * @return Keys on the token with a certificate to read the public key from
     */
    protected List<PKCS11KeyProvider> list(final KeyStore store) {
        final List<PKCS11KeyProvider> identities = new ArrayList<>();
        try {
            final Enumeration<String> aliases = store.aliases();
            while(aliases.hasMoreElements()) {
                final String alias = aliases.nextElement();
                if(!store.isKeyEntry(alias)) {
                    log.debug("Skip alias {} with no private key", alias);
                    continue;
                }
                if(null == store.getCertificate(alias)) {
                    log.warn("Skip alias {} with no certificate to read public key from", alias);
                    continue;
                }
                log.debug("Found identity for alias {}", alias);
                identities.add(new PKCS11KeyProvider(store, alias));
            }
        }
        catch(KeyStoreException e) {
            log.warn("Failure {} reading aliases from PKCS11 token", e.getMessage());
            return Collections.emptyList();
        }
        return identities;
    }

    @Override
    public String getMethod() {
        return "publickey";
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SFTPPKCS11Authentication{");
        sb.append("library='").append(library).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
