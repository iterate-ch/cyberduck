package ch.cyberduck.core.worker;

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

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.pasteboard.PathPasteboardFactory;

import org.apache.log4j.Logger;

import java.text.MessageFormat;

/**
 * @version $Id$
 */
public class DisconnectWorker extends Worker<Void> {
    private static final Logger log = Logger.getLogger(DisconnectWorker.class);

    private Session<?> session;

    public DisconnectWorker(final Session<?> session) {
        this.session = session;
    }

    @Override
    public Void run() {
        try {
            if(session.isConnected()) {
                session.close();
            }
        }
        catch(BackgroundException e) {
            log.warn(String.format("Failure closing connection %s. %s", session, e.getMessage()));
        }
        return null;
    }

    @Override
    public void cleanup(Void result) {
        PathPasteboardFactory.delete(session);
    }

    @Override
    public String getActivity() {
        return MessageFormat.format(LocaleFactory.localizedString("Disconnecting {0}", "Status"),
                session.getHost().getHostname());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DisconnectWorker{");
        sb.append("session=").append(session);
        sb.append('}');
        return sb.toString();
    }
}
