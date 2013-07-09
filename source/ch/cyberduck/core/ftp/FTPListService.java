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
import ch.cyberduck.core.DefaultHostKeyController;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.FTPExceptionMappingService;
import ch.cyberduck.core.ftp.parser.CompositeFileEntryParser;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPCmd;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * @version $Id$
 */
public class FTPListService implements ListService {
    private static final Logger log = Logger.getLogger(FTPListService.class);

    private FTPSession session;

    /**
     * Directory listing parser depending on response for SYST command
     */
    private CompositeFileEntryParser parser;

    /**
     * The sever supports STAT file listings
     */
    private boolean statListSupportedEnabled = Preferences.instance().getBoolean("ftp.command.stat");

    /**
     * The server supports MLSD
     */
    private boolean mlsdListSupportedEnabled = Preferences.instance().getBoolean("ftp.command.mlsd");

    /**
     * The server supports LIST -a
     */
    private boolean extendedListEnabled = Preferences.instance().getBoolean("ftp.command.lista");

    public FTPListService(final FTPSession session, final String system, final TimeZone zone) {
        this.session = session;
        this.parser = new FTPParserSelector().getParser(system, zone);
        if(StringUtils.isNotBlank(system)) {
            if(system.toUpperCase(java.util.Locale.ENGLISH).contains(FTPClientConfig.SYST_NT)) {
                // Workaround for #5572.
                statListSupportedEnabled = false;
            }
        }
    }

    @Override
    public AttributedList<Path> list(final Path file) throws BackgroundException {
        try {
            final AttributedList<Path> children = new AttributedList<Path>();
            // Cached file parser determined from SYST response with the timezone set from the bookmark
            boolean success = false;
            try {
                if(statListSupportedEnabled) {
                    int response = session.getClient().stat(file.getAbsolute());
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
                        success = new FTPListResponseReader().read(children, session, file, parser, result);
                    }
                    else {
                        statListSupportedEnabled = false;
                    }
                }
            }
            catch(IOException e) {
                log.warn("Command STAT failed with I/O error:" + e.getMessage());
                session.interrupt();
                session.open(new DefaultHostKeyController());
                session.login(new DisabledPasswordStore(), new DisabledLoginController());
            }
            if(!success || children.isEmpty()) {
                success = session.data(file, new DataConnectionAction<Boolean>() {
                    @Override
                    public Boolean execute() throws IOException {
                        if(!session.getClient().changeWorkingDirectory(file.getAbsolute())) {
                            throw new FTPException(session.getClient().getReplyCode(),
                                    session.getClient().getReplyString());
                        }
                        if(!session.getClient().setFileType(FTPClient.ASCII_FILE_TYPE)) {
                            // Set transfer type for traditional data socket file listings. The data transfer is over the
                            // data connection in type ASCII or type EBCDIC.
                            throw new FTPException(session.getClient().getReplyCode(),
                                    session.getClient().getReplyString());
                        }
                        boolean success = false;
                        // STAT listing failed or empty
                        if(mlsdListSupportedEnabled
                                // Note that there is no distinct FEAT output for MLSD.
                                // The presence of the MLST feature indicates that both MLST and MLSD are supported.
                                && session.getClient().hasFeature(FTPCmd.MLST.getCommand())) {
                            success = new FTPMlsdListResponseReader().read(children, session, file,
                                    null, session.getClient().list(FTPCmd.MLSD));
                            if(!success) {
                                mlsdListSupportedEnabled = false;
                            }
                        }
                        if(!success) {
                            // MLSD listing failed or not enabled
                            if(extendedListEnabled) {
                                try {
                                    success = new FTPListResponseReader().read(children, session, file,
                                            parser, session.getClient().list(FTPCmd.LIST, "-a"));
                                }
                                catch(FTPException e) {
                                    extendedListEnabled = false;
                                }
                            }
                            if(!success) {
                                // LIST -a listing failed or not enabled
                                success = new FTPListResponseReader().read(children, session, file,
                                        parser, session.getClient().list(FTPCmd.LIST));
                            }
                        }
                        return success;
                    }
                });
            }
            for(Path child : children) {
                if(child.attributes().isSymbolicLink()) {
                    if(session.getClient().changeWorkingDirectory(child.getAbsolute())) {
                        child.attributes().setType(Path.SYMBOLIC_LINK_TYPE | Path.DIRECTORY_TYPE);
                    }
                    else {
                        // Try if CWD to symbolic link target succeeds
                        if(session.getClient().changeWorkingDirectory(child.getSymlinkTarget().getAbsolute())) {
                            // Workdir change succeeded
                            child.attributes().setType(Path.SYMBOLIC_LINK_TYPE | Path.DIRECTORY_TYPE);
                        }
                        else {
                            child.attributes().setType(Path.SYMBOLIC_LINK_TYPE | Path.FILE_TYPE);
                        }
                    }
                }
            }
            if(!success) {
                // LIST listing failed
                log.error("No compatible file listing method found");
            }
            return children;
        }
        catch(IOException e) {
            throw new FTPExceptionMappingService().map("Listing directory failed", e, file);
        }
    }
}
