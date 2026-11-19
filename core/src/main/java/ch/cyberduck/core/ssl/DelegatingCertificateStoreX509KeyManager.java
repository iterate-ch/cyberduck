package ch.cyberduck.core.ssl;

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

import ch.cyberduck.core.CertificateIdentityCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.exception.ConnectionCanceledException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Combines multiple {@link CertificateStoreX509KeyManager} delegates into a single key manager.
 * Certificates and keys from all delegates are presented as one unified list; routing to the
 * correct delegate is transparent to callers.
 * <p>
 * When the server requests a client certificate and none is selected in the bookmark, the certificates from all
 * delegates accepted by the server are offered to choose from in a single prompt.
 */
public class DelegatingCertificateStoreX509KeyManager implements X509KeyManager {
    private static final Logger log = LogManager.getLogger(DelegatingCertificateStoreX509KeyManager.class);

    private final CertificateIdentityCallback prompt;
    private final Host bookmark;
    private final CertificateStoreX509KeyManager[] delegates;
    private final Map<String, CertificateStoreX509KeyManager> aliases = new LinkedHashMap<>();

    /**
     * @param prompt Prompt to choose from the certificates of all delegates
     */
    public DelegatingCertificateStoreX509KeyManager(final CertificateIdentityCallback prompt, final Host bookmark,
                                                    final CertificateStoreX509KeyManager... delegates) {
        this.prompt = prompt;
        this.bookmark = bookmark;
        this.delegates = delegates;
    }

    /**
     * Resolve delegate implementation handling alias
     */
    private CertificateStoreX509KeyManager resolve(final String alias) {
        if(aliases.containsKey(alias)) {
            return aliases.get(alias);
        }
        // Fallback for aliases resolved before list() was called (e.g. saved alias from preferences)
        for(CertificateStoreX509KeyManager delegate : delegates) {
            final X509Certificate[] chain = delegate.getCertificateChain(alias);
            if(chain != null && chain.length > 0) {
                return delegate;
            }
        }
        log.warn("No delegate found for alias {}", alias);
        return null;
    }


    @Override
    public X509KeyManager init() {
        for(CertificateStoreX509KeyManager delegate : delegates) {
            delegate.init();
        }
        return this;
    }

    @Override
    public List<String> list() {
        aliases.clear();
        final List<String> list = new ArrayList<>();
        for(CertificateStoreX509KeyManager delegate : delegates) {
            for(String alias : delegate.list()) {
                if(!aliases.containsKey(alias)) {
                    aliases.put(alias, delegate);
                    list.add(alias);
                }
                else {
                    log.warn("Duplicate alias {} from delegate {}, keeping first", alias, delegate);
                }
            }
        }
        list.sort(String::compareTo);
        return list;
    }

    @Override
    public X509Certificate getCertificate(final String alias, final String[] keyTypes, final Principal[] issuers) {
        final CertificateStoreX509KeyManager delegate = resolve(alias);
        if(delegate == null) {
            log.info("No matching certificate found for alias {} and issuers {}", alias, Arrays.toString(issuers));
            return null;
        }
        return delegate.getCertificate(alias, keyTypes, issuers);
    }

    @Override
    public String[] getClientAliases(final String keyType, final Principal[] issuers) {
        final Set<String> aliases = new LinkedHashSet<>();
        for(CertificateStoreX509KeyManager delegate : delegates) {
            final String[] c = delegate.getClientAliases(keyType, issuers);
            if(null == c) {
                // No matches
                continue;
            }
            aliases.addAll(Arrays.asList(c));
        }
        if(aliases.isEmpty()) {
            return null;
        }
        return aliases.toArray(new String[0]);
    }

    @Override
    public String chooseClientAlias(final String[] keyTypes, final Principal[] issuers, final Socket socket) {
        final String saved = bookmark.getCredentials().getCertificate();
        if(null != saved) {
            log.info("Return saved certificate alias {} for host {}", saved, bookmark);
            if(StringUtils.isBlank(saved)) {
                // Empty selection by user
                return null;
            }
            return saved;
        }
        // Certificates from all stores issued by an authority accepted by the server to choose from by alias
        final Map<String, X509Certificate> candidates = new LinkedHashMap<>();
        final Map<String, CertificateStoreX509KeyManager> stores = new LinkedHashMap<>();
        // Certificate found with a key type not requested
        boolean skipped = false;
        for(CertificateStoreX509KeyManager delegate : delegates) {
            for(String alias : delegate.list()) {
                final X509Certificate[] chain = delegate.getCertificateChain(alias);
                if(null == chain || chain.length == 0) {
                    continue;
                }
                final X509Certificate certificate = chain[0];
                final String type = certificate.getPublicKey().getAlgorithm();
                if(null == delegate.getCertificate(alias, new String[]{type}, issuers)) {
                    // Not issued by authority accepted by the server
                    continue;
                }
                if(!Arrays.asList(keyTypes).contains(type)) {
                    log.debug("Skip certificate with alias {} with key type {} not matching any of {}", alias, type, Arrays.toString(keyTypes));
                    skipped = true;
                    continue;
                }
                if(candidates.containsKey(alias) || candidates.containsValue(certificate)) {
                    // Same certificate available from multiple stores
                    log.debug("Skip duplicate certificate with alias {} from {}, keeping first", alias, delegate);
                    continue;
                }
                candidates.put(alias, certificate);
                stores.put(alias, delegate);
            }
        }
        if(candidates.isEmpty()) {
            if(skipped) {
                // May be requested again for another key type. Do not save empty selection
                log.info("No certificate found for key types {} and issuers {}", Arrays.toString(keyTypes), Arrays.toString(issuers));
                return null;
            }
            log.info("No certificate found for issuers {}", Arrays.toString(issuers));
            // Save empty selection with no certificate in any store
            bookmark.getCredentials().setCertificate(StringUtils.EMPTY);
            return null;
        }
        final X509Certificate selected;
        try {
            // Single prompt with certificates from all stores
            selected = prompt.prompt(bookmark.getHostname(), new ArrayList<>(candidates.values()));
        }
        catch(ConnectionCanceledException e) {
            log.info("No certificate selected for socket {}", socket);
            // Save empty selection with no certificate selected
            bookmark.getCredentials().setCertificate(StringUtils.EMPTY);
            return null;
        }
        for(Map.Entry<String, X509Certificate> candidate : candidates.entrySet()) {
            if(candidate.getValue().equals(selected)) {
                final String alias = candidate.getKey();
                log.info("Selected certificate alias {} for certificate {}", alias, selected);
                aliases.put(alias, stores.get(alias));
                bookmark.getCredentials().setCertificate(alias);
                return alias;
            }
        }
        log.warn("No matching alias found for selected certificate {}", selected);
        return null;
    }

    @Override
    public String[] getServerAliases(final String keyType, final Principal[] issuers) {
        final Set<String> aliases = new LinkedHashSet<>();
        for(CertificateStoreX509KeyManager delegate : delegates) {
            final String[] c = delegate.getServerAliases(keyType, issuers);
            if(null == c) {
                continue;
            }
            aliases.addAll(Arrays.asList(c));
        }
        if(aliases.isEmpty()) {
            return null;
        }
        return aliases.toArray(new String[0]);
    }

    @Override
    public String chooseServerAlias(final String keyType, final Principal[] issuers, final Socket socket) {
        for(CertificateStoreX509KeyManager delegate : delegates) {
            final String alias = delegate.chooseServerAlias(keyType, issuers, socket);
            if(null == alias) {
                continue;
            }
            return alias;
        }
        return null;
    }

    @Override
    public X509Certificate[] getCertificateChain(final String alias) {
        final CertificateStoreX509KeyManager delegate = this.resolve(alias);
        if(delegate == null) {
            log.warn("No certificate chain for alias {}", alias);
            return null;
        }
        return delegate.getCertificateChain(alias);
    }

    @Override
    public PrivateKey getPrivateKey(final String alias) {
        final CertificateStoreX509KeyManager delegate = this.resolve(alias);
        if(delegate == null) {
            log.warn("No private key for alias {}", alias);
            return null;
        }
        return delegate.getPrivateKey(alias);
    }
}
