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
import ch.cyberduck.core.Controller;

import java.net.Socket;
import java.security.KeyStore;
import java.security.Principal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class KeychainX509KeyManager extends CertificateStoreX509KeyManager implements X509KeyManager {

    private final Map<Key, String> memory
            = new HashMap<Key, String>();

    public KeychainX509KeyManager() {
        super(CertificateStoreFactory.get());
    }

    public KeychainX509KeyManager(final Controller controller) {
        super(CertificateStoreFactory.get(controller));
    }

    public KeychainX509KeyManager(final CertificateStore callback) {
        super(callback);
    }

    public KeychainX509KeyManager(final CertificateStore callback, final KeyStore store) {
        super(callback, store);
    }

    @Override
    public String chooseClientAlias(final String[] keyTypes, final Principal[] issuers, final Socket socket) {
        final Key key = new Key(socket.getInetAddress().getHostName(), socket.getPort(), issuers);
        final String alias = this.find(key);
        if(alias != null) {
            return alias;
        }
        final String s = super.chooseClientAlias(keyTypes, issuers, socket);
        if(null == s) {
            return null;
        }
        return this.save(key, s);
    }

    protected String find(final Key key) {
        if(memory.containsKey(key)) {
            return memory.get(key);
        }
        return null;
    }

    protected String save(final Key key, final String alias) {
        memory.put(key, alias);
        return alias;
    }

    protected static final class Key {
        private final String hostname;
        private final int port;
        private final Principal[] issuers;

        private Key(String hostname, int port, Principal[] issuers) {
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
            Key key = (Key) o;
            if(port != key.port) {
                return false;
            }
            if(hostname != null ? !hostname.equals(key.hostname) : key.hostname != null) {
                return false;
            }
            if(!Arrays.equals(issuers, key.issuers)) {
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

        @Override
        public String toString() {
            return String.format("connection.ssl.keystore.%s:%s.%s.alias",
                    hostname, port, Arrays.toString(issuers));
        }
    }
}