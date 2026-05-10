package ch.cyberduck.core.ssl;

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

import ch.cyberduck.core.CertificateIdentityCallback;
import ch.cyberduck.core.CertificateStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.exception.ConnectionCanceledException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.Socket;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

public abstract class CertificateStoreX509KeyManager extends AbstractX509KeyManager {

    protected final CertificateIdentityCallback prompt;
    protected final Host bookmark;
    protected final CertificateStore callback;
    protected final LazyInitializer<KeyStore> keystore;

    protected CertificateStoreX509KeyManager(final CertificateIdentityCallback prompt, final Host bookmark,
                                             final CertificateStore callback, final LazyInitializer<KeyStore> keystore) {
        this.prompt = prompt;
        this.bookmark = bookmark;
        this.callback = callback;
        this.keystore = keystore;
    }

    public CertificateStoreX509KeyManager init() {
        return this;
    }

    @Override
    public List<String> list() {
        final Logger log = LogManager.getLogger(getClass());
        final List<String> list = new ArrayList<>();
        if(keystore == null) {
            return list;
        }
        try {
            final Enumeration<String> aliases = keystore.get().aliases();
            while(aliases.hasMoreElements()) {
                final String alias = aliases.nextElement();
                log.debug("Alias in keystore {}", alias);
                if(keystore.get().isKeyEntry(alias)) {
                    log.info("Found private key for {}", alias);
                    list.add(alias);
                }
                else {
                    log.warn("Missing private key for alias {}", alias);
                }
            }
        }
        catch(ConcurrentException | KeyStoreException e) {
            log.error("Keystore not loaded {}", e.getMessage());
        }
        list.sort(String::compareTo);
        return list;
    }

    public X509Certificate getCertificate(final String alias, final String[] keyTypes, final Principal[] issuers) {
        final Logger log = LogManager.getLogger(getClass());
        if(keystore == null) {
            return null;
        }
        try {
            final Certificate cert = keystore.get().getCertificate(alias);
            if(this.matches(cert, keyTypes, issuers)) {
                return (X509Certificate) cert;
            }
            final Certificate[] chain = keystore.get().getCertificateChain(alias);
            if(chain != null) {
                for(Certificate c : chain) {
                    if(c instanceof X509Certificate) {
                        if(this.matches(c, keyTypes, issuers)) {
                            return (X509Certificate) cert;
                        }
                    }
                }
            }
        }
        catch(ConcurrentException | KeyStoreException e) {
            log.error("Keystore not loaded {}", e.getMessage());
        }
        log.info("No matching certificate found for alias {} and issuers {}", alias, Arrays.toString(issuers));
        return null;
    }

    @Override
    public X509Certificate[] getCertificateChain(final String alias) {
        final Logger log = LogManager.getLogger(getClass());
        if(keystore == null) {
            return null;
        }
        try {
            final List<X509Certificate> result = new ArrayList<>();
            final Certificate[] chain = keystore.get().getCertificateChain(alias);
            if(null == chain) {
                log.warn("No certificate chain for alias {}", alias);
                return null;
            }
            for(Certificate cert : chain) {
                if(cert instanceof X509Certificate) {
                    result.add((X509Certificate) cert);
                }
            }
            if(result.isEmpty()) {
                log.warn("No certificate chain for alias {}", alias);
                final Certificate cert = keystore.get().getCertificate(alias);
                if(null == cert) {
                    return null;
                }
                if(cert instanceof X509Certificate) {
                    result.add((X509Certificate) cert);
                }
            }
            return result.toArray(new X509Certificate[0]);
        }
        catch(ConcurrentException | KeyStoreException e) {
            log.error("Keystore not loaded {}", e.getMessage());
        }
        return null;
    }

    @Override
    public PrivateKey getPrivateKey(final String alias) {
        final Logger log = LogManager.getLogger(getClass());
        if(keystore == null) {
            return null;
        }
        try {
            if(keystore.get().isKeyEntry(alias)) {
                final Key key = keystore.get().getKey(alias, null);
                if(key instanceof PrivateKey) {
                    return (PrivateKey) key;
                }
                else {
                    log.warn("Key {} for alias {} is not a private key", key, alias);
                }
            }
            else {
                log.warn("Alias {} is not a key entry", alias);
            }
        }
        catch(ConcurrentException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            log.error("Keystore not loaded {}", e.getMessage());
        }
        log.warn("No private key for alias {}", alias);
        return null;
    }

    @Override
    public String[] getClientAliases(final String keyType, final Principal[] issuers) {
        return this.getClientAliases(new String[]{keyType}, issuers);
    }

    public String[] getClientAliases(final String[] keyTypes, final Principal[] issuers) {
        final Logger log = LogManager.getLogger(getClass());
        final List<String> list = new ArrayList<>();
        for(String alias : this.list()) {
            final X509Certificate cert = this.getCertificate(alias, keyTypes, issuers);
            if(null == cert) {
                log.warn("Failed to retrieve certificate for alias {}", alias);
                continue;
            }
            log.info("Add X509 certificate entry {} to list", cert);
            list.add(alias);
        }
        if(list.isEmpty()) {
            return null;
        }
        return list.toArray(new String[0]);
    }

    @Override
    public String chooseClientAlias(final String[] keyTypes, final Principal[] issuers, final Socket socket) {
        final Logger log = LogManager.getLogger(getClass());
        final String saved = bookmark.getCredentials().getCertificate();
        if(StringUtils.isNotBlank(saved)) {
            log.info("Return saved certificate alias {} for host {}", saved, bookmark);
            return saved;
        }
        final X509Certificate selected;
        try {
            selected = callback.choose(prompt, keyTypes, issuers, bookmark);
        }
        catch(ConnectionCanceledException e) {
            log.info("No certificate selected for socket {}", socket);
            return null;
        }
        if(null == selected) {
            log.info("No certificate selected for socket {}", socket);
            return null;
        }
        final String[] aliases = this.getClientAliases(keyTypes, issuers);
        if(null != aliases) {
            for(String alias : aliases) {
                final X509Certificate[] chain = this.getCertificateChain(alias);
                if(chain != null && chain.length > 0 && chain[0].equals(selected)) {
                    log.info("Selected certificate alias {} for certificate {}", alias, selected);
                    bookmark.getCredentials().setCertificate(alias);
                    return alias;
                }
            }
        }
        log.warn("No matching alias found for selected certificate {}", selected);
        return null;
    }
}
