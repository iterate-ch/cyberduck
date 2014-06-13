package ch.cyberduck.core.ssl;

/*
*  Copyright (c) 2005 David Kocher. All rights reserved.
*  http://cyberduck.ch/
*
*  This program is free software; you can redistribute it and/or modify
*  it under the terms of the GNU General Public License as published by
*  the Free Software Foundation; either version 2 of the License, or
*  (at your option) any later version.
*
*  This program is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU General Public License for more details.
*
*  Bug fixes, suggestions and comments should be sent to:
*  dkocher@cyberduck.ch
*/

import ch.cyberduck.core.CertificateStoreFactory;

import org.apache.commons.lang3.StringUtils;

import java.net.Socket;
import java.security.Principal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @version $Id$
 */
public class KeychainX509KeyManager extends CertificateStoreX509KeyManager implements X509KeyManager {

    private Map<String, String> memory = new HashMap<String, String>();

    public KeychainX509KeyManager() {
        super(CertificateStoreFactory.get());
    }

    @Override
    public String chooseClientAlias(final String[] keyTypes, final Principal[] issuers, final Socket socket) {
        final String key = String.format("%s.certificate.%s.alias",
                socket.getInetAddress().getHostName(), Arrays.toString(issuers));
        final String saved = memory.get(key);
        if(StringUtils.isNotBlank(saved)) {
            return saved;
        }
        final String alias = super.chooseClientAlias(keyTypes, issuers, socket);
        if(StringUtils.isNotBlank(alias)) {
            memory.put(key, alias);
        }
        return alias;
    }
}