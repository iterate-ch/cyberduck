package ch.cyberduck.core.ftp;

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

import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.TranscriptListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Command;

import org.apache.commons.net.ProtocolCommandListener;
import org.apache.log4j.Logger;

import java.io.IOException;

public class FTPCommandFeature implements Command {
    private static final Logger log = Logger.getLogger(FTPCommandFeature.class);

    private FTPSession session;

    public FTPCommandFeature(final FTPSession session) {
        this.session = session;
    }

    @Override
    public void send(final String command, final ProgressListener progress, final TranscriptListener transcript) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Send command %s", command));
        }
        progress.message(command);
        final ProtocolCommandListener listener = new LoggingProtocolCommandListener(transcript);
        try {
            session.getClient().addProtocolCommandListener(listener);
            session.getClient().sendSiteCommand(command);
        }
        catch(IOException e) {
            throw new FTPExceptionMappingService().map(e);
        }
        finally {
            session.getClient().removeProtocolCommandListener(listener);
        }
    }
}
