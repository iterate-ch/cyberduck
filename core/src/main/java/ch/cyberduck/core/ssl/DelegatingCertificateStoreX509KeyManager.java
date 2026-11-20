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
import ch.cyberduck.core.CertificateStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.preferences.HostPreferencesFactory;

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
import java.util.stream.Collectors;

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
     * Certificates from the Keychain respectively the system certificate store and from the PKCS#11 token of the
     * library configured
     *
     * @param prompt Prompt to choose from the certificates of all stores
     * @param login  Prompt for the PIN of the token
     */
    public DelegatingCertificateStoreX509KeyManager(final CertificateIdentityCallback prompt, final Host bookmark,
                                                    final CertificateStore store, final LoginCallback login) {
        this(prompt, bookmark, store, login, HostPreferencesFactory.get(bookmark).getProperty("connection.keystore.pkcs11.library"));
    }

    /**
     * @param library Native PKCS11 library name or path. No certificates are listed from a token when blank
     */
    public DelegatingCertificateStoreX509KeyManager(final CertificateIdentityCallback prompt, final Host bookmark,
                                                    final CertificateStore store, final LoginCallback login, final String library) {
        // Prompt to choose from the certificates of all stores displayed by the delegating key manager
        this(prompt, bookmark, StringUtils.isBlank(library) ?
                new CertificateStoreX509KeyManager[]{
                        new KeychainX509KeyManager(CertificateIdentityCallback.noop, bookmark, store)} :
                new CertificateStoreX509KeyManager[]{
                        new KeychainX509KeyManager(CertificateIdentityCallback.noop, bookmark, store),
                        new PKCS11CertificateStoreX509KeyManager(bookmark, store, login, library)});
    }

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

    /**
     * @return Aliases from all delegates with a certificate available from multiple stores listed only once
     */
    @Override
    public List<String> list() {
        aliases.clear();
        final List<String> list = new ArrayList<>();
        for(Map.Entry<String, Candidate> candidate : this.candidates(null).entrySet()) {
            aliases.put(candidate.getKey(), candidate.getValue().store);
            list.add(candidate.getKey());
        }
        list.sort(String::compareTo);
        return list;
    }

    /**
     * Certificates from all delegates. A certificate available from multiple stores is included only once, from the
     * store authenticated to list its certificates if any to not prompt again for authentication by another store when
     * signing, or from the first store otherwise.
     *
     * @param issuers Authorities accepted by the server or null to include certificates of any issuer
     * @return Certificate and store by alias
     */
    private Map<String, Candidate> candidates(final Principal[] issuers) {
        final Map<String, Candidate> candidates = new LinkedHashMap<>();
        for(CertificateStoreX509KeyManager delegate : delegates) {
            for(String alias : delegate.list()) {
                final X509Certificate[] chain = delegate.getCertificateChain(alias);
                if(null == chain || chain.length == 0) {
                    continue;
                }
                final X509Certificate certificate = chain[0];
                if(null != issuers) {
                    // Key types of all certificates in the chain as the issuer of an intermediate authority with a
                    // key type different from the certificate may be the authority accepted by the server
                    final String[] types = Arrays.stream(chain).map(c -> c.getPublicKey().getAlgorithm()).distinct().toArray(String[]::new);
                    if(null == delegate.getCertificate(alias, types, issuers)) {
                        // Not issued by authority accepted by the server
                        continue;
                    }
                }
                // Same certificate available from multiple stores
                final String duplicate = candidates.entrySet().stream()
                        .filter(entry -> entry.getValue().certificate.equals(certificate)).map(Map.Entry::getKey).findFirst().orElse(null);
                if(null != duplicate) {
                    final CertificateStoreX509KeyManager existing = candidates.get(duplicate).store;
                    if(!delegate.isAuthenticatedToList() || existing.isAuthenticatedToList()) {
                        log.debug("Skip duplicate certificate with alias {} from {}, keeping alias {} from {}", alias, delegate, duplicate, existing);
                        continue;
                    }
                    log.debug("Replace duplicate certificate with alias {} from {} with alias {} from {}", duplicate, existing, alias, delegate);
                    candidates.remove(duplicate);
                }
                else if(candidates.containsKey(alias)) {
                    log.warn("Skip certificate with duplicate alias {} from {}, keeping first", alias, delegate);
                    continue;
                }
                candidates.put(alias, new Candidate(certificate, delegate));
            }
        }
        return candidates;
    }

    private static final class Candidate {
        private final X509Certificate certificate;
        private final CertificateStoreX509KeyManager store;

        private Candidate(final X509Certificate certificate, final CertificateStoreX509KeyManager store) {
            this.certificate = certificate;
            this.store = store;
        }
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
            return this.match(saved, keyTypes);
        }
        // Certificates from all stores issued by an authority accepted by the server to choose from by alias. Include
        // certificates of all key types as the key manager is queried separately for each key type in order of
        // preference of the server with only a single prompt shown for the first query
        final Map<String, Candidate> candidates = this.candidates(issuers);
        if(candidates.isEmpty()) {
            log.info("No certificate found for issuers {}", Arrays.toString(issuers));
            // Save empty selection with no certificate in any store
            bookmark.getCredentials().setCertificate(StringUtils.EMPTY);
            return null;
        }
        final X509Certificate selected;
        try {
            // Single prompt with certificates from all stores
            selected = prompt.prompt(bookmark.getHostname(), candidates.values().stream()
                    .map(candidate -> candidate.certificate).collect(Collectors.toList()));
        }
        catch(ConnectionCanceledException e) {
            log.info("No certificate selected for socket {}", socket);
            // Save empty selection with no certificate selected
            bookmark.getCredentials().setCertificate(StringUtils.EMPTY);
            return null;
        }
        for(Map.Entry<String, Candidate> candidate : candidates.entrySet()) {
            if(candidate.getValue().certificate.equals(selected)) {
                final String alias = candidate.getKey();
                log.info("Selected certificate alias {} for certificate {}", alias, selected);
                aliases.put(alias, candidate.getValue().store);
                bookmark.getCredentials().setCertificate(alias);
                return this.match(alias, keyTypes);
            }
        }
        log.warn("No matching alias found for selected certificate {}", selected);
        return null;
    }

    /**
     * @param alias    Selected certificate alias
     * @param keyTypes Key types requested
     * @return Alias when the key type of its certificate is requested. Null otherwise to be returned when queried
     * again with the matching key type
     */
    private String match(final String alias, final String[] keyTypes) {
        final X509Certificate[] chain = this.getCertificateChain(alias);
        if(null == chain || chain.length == 0) {
            // Key type unknown
            return alias;
        }
        final String type = chain[0].getPublicKey().getAlgorithm();
        if(Arrays.asList(keyTypes).contains(type)) {
            return alias;
        }
        log.debug("Skip certificate with alias {} with key type {} not matching any of {}", alias, type, Arrays.toString(keyTypes));
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
