package ch.cyberduck.core.smb;

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

import com.hierynomus.smbj.SmbConfig;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.connection.PacketEncryptor;
import com.hierynomus.smbj.connection.PacketSignatory;
import com.hierynomus.smbj.event.SMBEventBus;
import com.hierynomus.smbj.paths.PathResolver;
import com.hierynomus.smbj.session.Session;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SMBSession extends Session {

    public SMBSession(Connection connection, SmbConfig config, AuthenticationContext userCredentials, SMBEventBus bus,
            PathResolver pathResolver, PacketSignatory signatory, PacketEncryptor encryptor) {
        super(connection, config, userCredentials, bus, pathResolver, signatory, encryptor);
    }

    private static final Logger log = LogManager.getLogger(SMBSession.class);

    // TODO implement methods or remove class
}
