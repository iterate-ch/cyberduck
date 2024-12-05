package ch.cyberduck.core.ftp;

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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.VoidAttributesAdapter;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.ftp.list.FTPDataResponseReader;
import ch.cyberduck.core.ftp.list.FTPMlsdListResponseReader;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.preferences.PreferencesReader;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;

import org.apache.commons.net.ftp.FTPCmd;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

public class FTPAttributesFinderFeature extends VoidAttributesAdapter implements AttributesFinder {
    private static final Logger log = LogManager.getLogger(FTPAttributesFinderFeature.class);

    private final FTPSession session;

    public FTPAttributesFinderFeature(FTPSession session) {
        this.session = session;
    }

    @Override
    public PathAttributes find(final Path file, final ListProgressListener listener) throws BackgroundException {
        if(file.isRoot()) {
            return PathAttributes.EMPTY;
        }
        try {
            try {
                final PreferencesReader preferences = new HostPreferences(session.getHost());
                if(preferences.getBoolean("ftp.command.mlsd")) {
                    if(session.getClient().hasFeature(FTPCmd.MLST.getCommand())) {
                        if(!FTPReply.isPositiveCompletion(session.getClient().sendCommand(FTPCmd.MLST, file.getAbsolute()))) {
                            throw new FTPException(session.getClient().getReplyCode(), session.getClient().getReplyString());
                        }

                        final ArrayList<String> lines = new ArrayList<String>();
                        boolean endOfList = false;
                        boolean newReply = false;
                        while(!endOfList) {
                            if(newReply) {
                                // Synology Diskstation FTP server responds
                                // with reply
                                // 250-
                                // 550
                                // 250
                                // commons-net stops parsing at 550, never seeing the 250 end of list.
                                // This works around this issue.
                                session.getClient().getReply();
                            }

                            newReply = true;
                            final String reply = session.getClient().getReplyString();
                            try (final StringReader replyReader = new StringReader(reply)) {
                                try (final BufferedReader replyLineReader = new BufferedReader(replyReader)) {
                                    String line;
                                    while((line = replyLineReader.readLine()) != null) {
                                        /*
                                         * MLST must follow RFC3659 7.2, in that:
                                         *
                                         * error-response = error-code SP *TCHAR CRLF
                                         * error-code = ("4" / "5") 2DIGIT
                                         *
                                         * mlst-response = control-response / error-response
                                         *
                                         * control-response = "250-" [ response-message ] CRLF
                                         * 1*( SP entry CRLF )
                                         * "250" [ SP response-message ] CRLF
                                         */
                                        if(line.startsWith("250")) {
                                            // EndOfList set when receiving "250"
                                            endOfList |= line.length() < 4 || line.charAt(3) != '-';
                                        }
                                        else if(!endOfList) {
                                            // Not "250-" Preamble
                                            // Not "250" End of List
                                            // Not outside range "250-" - "250"
                                            lines.add(line);
                                        }
                                    }
                                }
                            }
                        }

                        final FTPDataResponseReader reader = new FTPMlsdListResponseReader();
                        final AttributedList<Path> attributes = reader.read(file.getParent(), lines);
                        if(attributes.contains(file)) {
                            return attributes.get(attributes.indexOf(file)).attributes();
                        }
                    }
                    log.warn("No support for MLST in reply to FEAT");
                }
                return new DefaultAttributesFinderFeature(session).find(file, listener);
            }
            catch(IOException e) {
                throw new FTPExceptionMappingService().map("Failure to read attributes of {0}", e, file);
            }
        }
        catch(InteroperabilityException | AccessDeniedException | NotfoundException f) {
            log.warn("Failure reading attributes for {}. {}", file, f.getMessage());
            return new DefaultAttributesFinderFeature(session).find(file, listener);
        }
    }
}
