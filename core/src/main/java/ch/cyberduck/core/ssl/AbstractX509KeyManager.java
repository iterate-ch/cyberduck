package ch.cyberduck.core.ssl;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.ch
 */

import org.apache.log4j.Logger;

import javax.net.ssl.X509ExtendedKeyManager;
import javax.security.auth.x500.X500Principal;
import java.net.Socket;
import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class AbstractX509KeyManager extends X509ExtendedKeyManager implements X509KeyManager {
    private static final Logger log = Logger.getLogger(AbstractX509KeyManager.class);

    /**
     * @param issuers The list of acceptable CA issuer subject names or null if it does not matter which issuers are used
     * @return True if certificate matches issuer and key type
     */
    protected boolean matches(final Certificate c, final String[] keyTypes, final Principal[] issuers) {
        if(!(c instanceof X509Certificate)) {
            log.warn(String.format("Certificate %s is not of type X509", c));
            return false;
        }
        if(!Arrays.asList(keyTypes).contains(c.getPublicKey().getAlgorithm())) {
            log.warn(String.format("Key type %s does not match any of %s", c.getPublicKey().getAlgorithm(),
                    Arrays.toString(keyTypes)));
            return false;
        }
        if(null == issuers || Arrays.asList(issuers).isEmpty()) {
            // null if it does not matter which issuers are used
            return true;
        }
        final X500Principal issuer = ((X509Certificate) c).getIssuerX500Principal();
        if(!Arrays.asList(issuers).contains(issuer)) {
            log.warn(String.format("Issuer %s does not match", issuer));
            return false;
        }
        return true;
    }

    @Override
    public List<String> list() {
        return Collections.emptyList();
    }

    @Override
    public String chooseServerAlias(String s, Principal[] principals, Socket socket) {
        return null;
    }

    @Override
    public String[] getServerAliases(String s, Principal[] principals) {
        return null;
    }
}
