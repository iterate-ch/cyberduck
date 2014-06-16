package ch.cyberduck.core.ssl;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.CertificateStore;
import ch.cyberduck.core.CertificateStoreFactory;

import org.apache.commons.lang3.StringUtils;

import java.net.Socket;
import java.security.KeyStore;
import java.security.Principal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @version $Id$
 */
public class KeychainX509KeyManager extends CertificateStoreX509KeyManager implements X509KeyManager {

    private Map<Alias, String> memory
            = new HashMap<Alias, String>();

    public KeychainX509KeyManager() {
        super(CertificateStoreFactory.get());
    }

    public KeychainX509KeyManager(final CertificateStore callback) {
        super(callback);
    }

    public KeychainX509KeyManager(final CertificateStore callback, final KeyStore store) {
        super(callback, store);
    }

    @Override
    public String chooseClientAlias(final String[] keyTypes, final Principal[] issuers, final Socket socket) {
        final Alias key = new Alias(socket.getInetAddress().getHostName(), socket.getPort(), issuers);
        if(memory.containsKey(key)) {
            return memory.get(key);
        }
        final String alias = super.chooseClientAlias(keyTypes, issuers, socket);
        if(StringUtils.isNotBlank(alias)) {
            memory.put(key, alias);
        }
        return alias;
    }

    private static final class Alias {
        private String hostname;
        private int port;
        private Principal[] issuers;

        private Alias(String hostname, int port, Principal[] issuers) {
            this.hostname = hostname;
            this.port = port;
            this.issuers = issuers;
        }

        @Override
        public boolean equals(Object o) {
            if(this == o) {
                return true;
            }
            if(o == null || getClass() != o.getClass()) {
                return false;
            }
            Alias alias = (Alias) o;
            if(port != alias.port) {
                return false;
            }
            if(hostname != null ? !hostname.equals(alias.hostname) : alias.hostname != null) {
                return false;
            }
            if(!Arrays.equals(issuers, alias.issuers)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int result = hostname != null ? hostname.hashCode() : 0;
            result = 31 * result + port;
            result = 31 * result + (issuers != null ? Arrays.hashCode(issuers) : 0);
            return result;
        }
    }
}