package ch.cyberduck.core.socket;

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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.Socket;

public class DefaultSocketConfigurator implements SocketConfigurator {
    private static final Logger log = Logger.getLogger(DefaultSocketConfigurator.class);

    private final Preferences preferences
            = PreferencesFactory.get();

    @Override
    public void configure(final Socket socket) throws IOException {
        if(preferences.getInteger("connection.buffer.receive") > 0) {
            socket.setReceiveBufferSize(preferences.getInteger("connection.buffer.receive"));
        }
        if(preferences.getInteger("connection.buffer.send") > 0) {
            socket.setSendBufferSize(preferences.getInteger("connection.buffer.send"));
        }
        final int timeout = preferences.getInteger("connection.timeout.seconds") * 1000;
        if(log.isInfoEnabled()) {
            log.info(String.format("Set timeout to %dms for socket %s", timeout, socket));
        }
        socket.setSoTimeout(timeout);
        if(preferences.getBoolean("connection.socket.linger")) {
            // The setting only affects socket close. Make sure closing SSL socket does not hang because close_notify cannot be sent.
            socket.setSoLinger(true, timeout);
        }
        if(preferences.getBoolean("connection.socket.keepalive")) {
            socket.setKeepAlive(true);
        }
    }
}
