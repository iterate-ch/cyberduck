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

import ch.cyberduck.core.CertificateIdentityCallback;
import ch.cyberduck.core.CertificateStore;
import ch.cyberduck.core.Host;

import org.apache.commons.lang3.concurrent.LazyInitializer;

import java.net.Socket;
import java.security.KeyStore;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class KeychainX509KeyManager extends CertificateStoreX509KeyManager implements X509KeyManager {

    private final Map<Key, String> memory = new HashMap<>();
    private final Set<String> aliases = new HashSet<>();

    public KeychainX509KeyManager(final CertificateIdentityCallback prompt, final Host bookmark, final CertificateStore callback) {
        super(prompt, bookmark, callback);
    }

    public KeychainX509KeyManager(final CertificateIdentityCallback prompt, final Host bookmark, final CertificateStore callback, final LazyInitializer<KeyStore> keystore) {
        super(prompt, bookmark, callback, keystore);
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

    @Override
    public List<String> list() {
        if(aliases.isEmpty()) {
            aliases.addAll(super.list());
        }
        final ArrayList<String> list = new ArrayList<>(aliases);
        list.sort(String::compareTo);
        return list;
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
            if(!Objects.equals(hostname, key.hostname)) {
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
