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

import javax.net.ssl.X509ExtendedKeyManager;
import java.net.Socket;
import java.security.Principal;

public abstract class AbstractX509KeyManager extends X509ExtendedKeyManager implements X509KeyManager {

    @Override
    public String chooseServerAlias(String s, Principal[] principals, Socket socket) {
        return null;
    }

    @Override
    public String[] getServerAliases(String s, Principal[] principals) {
        return null;
    }
}
