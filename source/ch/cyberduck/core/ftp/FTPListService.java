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
import ch.cyberduck.core.LoginConnectionService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.FTPExceptionMappingService;
import ch.cyberduck.core.ftp.parser.CompositeFileEntryParser;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPCmd;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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

    private Map<Command, ListService> implementations
            = new HashMap<Command, ListService>();

    public enum Command {
        stat(FTPCmd.STAT),
        mlsd(FTPCmd.MLSD),
        list(FTPCmd.LIST),
        lista(FTPCmd.LIST, "-a");

        private FTPCmd command;
        private String arg;

        private Command(FTPCmd command) {
            this(command, null);
        }

        private Command(FTPCmd command, String arg) {
            this.command = command;
            this.arg = arg;
        }

        public FTPCmd getCommand() {
            return command;
        }

        public String getArg() {
            return arg;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Command{");
            sb.append("command=").append(command);
            sb.append(", arg='").append(arg).append('\'');
            sb.append('}');
            return sb.toString();
        }
    }

    public FTPListService(final FTPSession session, final String system, final TimeZone zone) throws BackgroundException {
        this.session = session;
        this.parser = new FTPParserSelector().getParser(system, zone);
        this.implementations.put(Command.list, new FTPDefaultListService(session, parser, Command.list));
        if(Preferences.instance().getBoolean("ftp.command.stat")) {
            if(StringUtils.isNotBlank(system)) {
                if(!system.toUpperCase(java.util.Locale.ENGLISH).contains(FTPClientConfig.SYST_NT)) {
                    // Workaround for #5572.
                    this.implementations.put(Command.stat, new FTPStatListService(session, parser));
                }
            }
            else {
                this.implementations.put(Command.stat, new FTPStatListService(session, parser));
            }
        }
        if(Preferences.instance().getBoolean("ftp.command.mlsd")) {
            this.implementations.put(Command.mlsd, new FTPMlsdListService(session));
        }
        if(Preferences.instance().getBoolean("ftp.command.lista")) {
            this.implementations.put(Command.lista, new FTPDefaultListService(session, parser, Command.lista));
        }
    }

    private void remove(final Command command) {
        log.warn(String.format("Remove %s from listing strategies", command));
        implementations.remove(command);
    }

    @Override
    public AttributedList<Path> list(final Path file) throws BackgroundException {
        try {
            if(implementations.containsKey(Command.stat)) {
                try {
                    return this.post(file, implementations.get(Command.stat).list(file));
                }
                catch(FTPInvalidListException e) {
                    this.remove(Command.stat);
                }
                catch(BackgroundException e) {
                    if(e.getCause() instanceof FTPException) {
                        log.warn(String.format("Command STAT failed with FTP error %s", e.getMessage()));
                    }
                    else {
                        log.warn(String.format("Command STAT failed with I/O error %s", e.getMessage()));
                        new LoginConnectionService(new DisabledLoginController(), new DefaultHostKeyController(),
                                new DisabledPasswordStore(), new ProgressListener() {
                            @Override
                            public void message(final String message) {
                                session.message(message);
                            }
                        }).connect(session);
                    }
                    this.remove(Command.stat);
                }
            }
            if(implementations.containsKey(Command.mlsd)) {
                // Note that there is no distinct FEAT output for MLSD.
                // The presence of the MLST feature indicates that both MLST and MLSD are supported.
                if(session.getClient().hasFeature(FTPCmd.MLST.getCommand())) {
                    try {
                        return this.post(file, implementations.get(Command.mlsd).list(file));
                    }
                    catch(FTPInvalidListException e) {
                        this.remove(Command.mlsd);
                    }
                }
                else {
                    this.remove(Command.mlsd);
                }
            }
            if(implementations.containsKey(Command.lista)) {
                try {
                    return this.post(file, implementations.get(Command.lista).list(file));
                }
                catch(FTPInvalidListException e) {
                    this.remove(Command.lista);
                }
            }
            if(implementations.containsKey(Command.list)) {
                return this.post(file, implementations.get(Command.list).list(file));
            }
            throw new BackgroundException("No compatible file listing method found");
        }
        catch(IOException e) {
            throw new FTPExceptionMappingService().map("Listing directory failed", e, file);
        }
    }

    private AttributedList<Path> post(final Path file, final AttributedList<Path> list) throws BackgroundException {
        try {
            for(Path child : list) {
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
            return list;
        }
        catch(IOException e) {
            throw new FTPExceptionMappingService().map("Listing directory failed", e, file);
        }
    }
}