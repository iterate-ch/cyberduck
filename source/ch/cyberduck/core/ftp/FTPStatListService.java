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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.ftp.parser.CompositeFileEntryParser;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @version $Id$
 */
public class FTPStatListService implements ListService {
    private static final Logger log = Logger.getLogger(FTPListService.class);

    private FTPSession session;

    private CompositeFileEntryParser parser;

    public FTPStatListService(final FTPSession session, final CompositeFileEntryParser parser) {
        this.session = session;
        this.parser = parser;
    }

    @Override
    public AttributedList<Path> list(final Path file, final ListProgressListener listener) throws BackgroundException {
        try {
            final int response = session.getClient().stat(file.getAbsolute());
            if(FTPReply.isPositiveCompletion(response)) {
                final String[] reply = session.getClient().getReplyStrings();
                final List<String> result = new ArrayList<String>(reply.length);
                for(final String line : reply) {
                    //Some servers include the status code for every line.
                    if(line.startsWith(String.valueOf(response))) {
                        try {
                            result.add(line.substring(line.indexOf(response) + line.length() + 1).trim());
                        }
                        catch(IndexOutOfBoundsException e) {
                            log.error(String.format("Failed parsing line %s", line), e);
                        }
                    }
                    else {
                        result.add(StringUtils.stripStart(line, null));
                    }
                }
                return new FTPListResponseReader().read(session, file, parser, result);
            }
            else {
                throw new FTPException(session.getClient().getReplyCode(), session.getClient().getReplyString());
            }
        }
        catch(IOException e) {
            throw new FTPExceptionMappingService().map("Listing directory failed", e, file);
        }
    }
}
