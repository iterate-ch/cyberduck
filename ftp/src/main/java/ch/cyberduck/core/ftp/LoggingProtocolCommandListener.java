package ch.cyberduck.core.ftp;

/*
 * Copyright (c) 2012 David Kocher. All rights reserved.
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
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.TranscriptListener;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.FTPCmd;

public class LoggingProtocolCommandListener implements ProtocolCommandListener, TranscriptListener {

    private final TranscriptListener transcript;

    protected LoggingProtocolCommandListener(final TranscriptListener transcript) {
        this.transcript = transcript;
    }

    @Override
    public void protocolCommandSent(final ProtocolCommandEvent event) {
        String message = StringUtils.chomp(event.getMessage());
        if(message.startsWith(FTPCmd.PASS.name())) {
            message = String.format("%s ********", FTPCmd.PASS.name());
        }
        this.log(Type.request, message);
    }

    @Override
    public void protocolReplyReceived(final ProtocolCommandEvent event) {
        this.log(Type.response, StringUtils.chomp(event.getMessage()));
    }

    @Override
    public void log(final Type request, final String event) {
        transcript.log(request, event);
    }
}
