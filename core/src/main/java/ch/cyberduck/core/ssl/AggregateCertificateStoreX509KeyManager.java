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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Combines multiple {@link CertificateStoreX509KeyManager} delegates into a single key manager.
 * Certificates and keys from all delegates are presented as one unified list; routing to the
 * correct delegate is transparent to callers.
 */
public class AggregateCertificateStoreX509KeyManager extends CertificateStoreX509KeyManager {
    private static final Logger log = LogManager.getLogger(AggregateCertificateStoreX509KeyManager.class);

    private final List<CertificateStoreX509KeyManager> delegates;
    private final Map<String, CertificateStoreX509KeyManager> aliasToDelegate = new LinkedHashMap<>();

    public AggregateCertificateStoreX509KeyManager(final CertificateIdentityCallback prompt, final Host bookmark,
                                                   final CertificateStore callback,
                                                   final List<CertificateStoreX509KeyManager> delegates) {
        super(prompt, bookmark, callback);
        this.delegates = delegates;
    }

    private CertificateStoreX509KeyManager resolveDelegate(final String alias) {
        if(aliasToDelegate.containsKey(alias)) {
            return aliasToDelegate.get(alias);
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
    public List<String> list() {
        aliasToDelegate.clear();
        final List<String> list = new ArrayList<>();
        for(CertificateStoreX509KeyManager delegate : delegates) {
            for(String alias : delegate.list()) {
                if(!aliasToDelegate.containsKey(alias)) {
                    aliasToDelegate.put(alias, delegate);
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
        final CertificateStoreX509KeyManager delegate = resolveDelegate(alias);
        if(delegate == null) {
            log.info("No matching certificate found for alias {} and issuers {}", alias, Arrays.toString(issuers));
            return null;
        }
        return delegate.getCertificate(alias, keyTypes, issuers);
    }

    @Override
    public X509Certificate[] getCertificateChain(final String alias) {
        final CertificateStoreX509KeyManager delegate = resolveDelegate(alias);
        if(delegate == null) {
            log.warn("No certificate chain for alias {}", alias);
            return null;
        }
        return delegate.getCertificateChain(alias);
    }

    @Override
    public PrivateKey getPrivateKey(final String alias) {
        final CertificateStoreX509KeyManager delegate = resolveDelegate(alias);
        if(delegate == null) {
            log.warn("No private key for alias {}", alias);
            return null;
        }
        return delegate.getPrivateKey(alias);
    }
}
