package ch.cyberduck.core.ftp.list;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.ftp.FTPExceptionMappingService;
import ch.cyberduck.core.ftp.FTPParserSelector;
import ch.cyberduck.core.ftp.FTPSession;
import ch.cyberduck.core.ftp.parser.CompositeFileEntryParser;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.preferences.PreferencesReader;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPCmd;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

public class FTPListService implements ListService {
    private static final Logger log = LogManager.getLogger(FTPListService.class);

    private final FTPSession session;

    protected final Map<Command, ListService> implementations
        = new HashMap<>();

    public enum Command {
        stat(FTPCmd.STAT),
        mlsd(FTPCmd.MLSD),
        list(FTPCmd.LIST),
        lista(FTPCmd.LIST, "-a");

        private final FTPCmd command;
        private final String arg;

        Command(FTPCmd command) {
            this(command, null);
        }

        Command(FTPCmd command, String arg) {
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

    public FTPListService(final FTPSession session, final String system, final TimeZone zone) {
        this.session = session;
        // Directory listing parser depending on response for SYST command
        final CompositeFileEntryParser parser = new FTPParserSelector().getParser(system, zone);
        this.implementations.put(Command.list, new FTPDefaultListService(session, parser, Command.list));
        final PreferencesReader preferences = new HostPreferences(session.getHost());
        if(preferences.getBoolean("ftp.command.stat")) {
            if(StringUtils.isNotBlank(system)) {
                if(!system.toUpperCase(Locale.ROOT).contains(FTPClientConfig.SYST_NT)) {
                    // Workaround for #5572.
                    this.implementations.put(Command.stat, new FTPStatListService(session, parser));
                }
            }
            else {
                this.implementations.put(Command.stat, new FTPStatListService(session, parser));
            }
        }
        if(preferences.getBoolean("ftp.command.mlsd")) {
            this.implementations.put(Command.mlsd, new FTPMlsdListService(session));
        }
        if(preferences.getBoolean("ftp.command.lista")) {
            this.implementations.put(Command.lista, new FTPDefaultListService(session, parser, Command.lista));
        }
    }

    protected void remove(final Command command) {
        log.warn(String.format("Remove %s from listing strategies", command));
        implementations.remove(command);
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        try {
            if(implementations.containsKey(Command.mlsd)) {
                // Note that there is no distinct FEAT output for MLSD. The presence of the MLST feature
                // indicates that both MLST and MLSD are supported.
                if(session.getClient().hasFeature(FTPCmd.MLST.getCommand())) {
                    try {
                        return this.post(directory, implementations.get(Command.mlsd).list(directory, listener), listener);
                    }
                    catch(InteroperabilityException e) {
                        this.remove(Command.mlsd);
                    }
                }
                else {
                    this.remove(Command.mlsd);
                }
            }
            if(implementations.containsKey(Command.stat)) {
                try {
                    return this.post(directory, implementations.get(Command.stat).list(directory, listener), listener);
                }
                catch(InteroperabilityException | AccessDeniedException | NotfoundException e) {
                    this.remove(Command.stat);
                }
            }
            if(implementations.containsKey(Command.lista)) {
                try {
                    return this.post(directory, implementations.get(Command.lista).list(directory, listener), listener);
                }
                catch(FTPInvalidListException e) {
                    // Empty directory listing. #7737
                }
                catch(InteroperabilityException e) {
                    this.remove(Command.lista);
                }
            }
            try {
                return this.post(directory, implementations.get(Command.list).list(directory, listener), listener);
            }
            catch(FTPInvalidListException f) {
                // Empty directory listing
                return this.post(directory, f.getParsed(), listener);
            }
        }
        catch(IOException e) {
            throw new FTPExceptionMappingService().map("Listing directory {0} failed", e, directory);
        }
    }

    /**
     * Handle all symbolic link targets
     */
    protected AttributedList<Path> post(final Path directory, final AttributedList<Path> list,
                                        final ListProgressListener listener) throws BackgroundException {
        try {
            final List<Path> verified = new ArrayList<Path>();
            final Set<Path> removal = new HashSet<>();
            for(final Path file : list) {
                if(file.isSymbolicLink()) {
                    final Path target = file.getSymlinkTarget();
                    if(session.getClient().changeWorkingDirectory(file.getAbsolute())) {
                        file.setType(EnumSet.of(Path.Type.directory, Path.Type.symboliclink));
                        target.setType(EnumSet.of(Path.Type.directory));
                    }
                    else {
                        // Try if change working directory to symbolic link target succeeds
                        if(session.getClient().changeWorkingDirectory(target.getAbsolute())) {
                            // Workdir change succeeded
                            file.setType(EnumSet.of(Path.Type.directory, Path.Type.symboliclink));
                            target.setType(EnumSet.of(Path.Type.directory));
                        }
                        else {
                            file.setType(EnumSet.of(Path.Type.file, Path.Type.symboliclink));
                            target.setType(EnumSet.of(Path.Type.file));
                        }
                    }
                    verified.add(file);
                    // Make sure we remove and add because hash code will change
                    removal.add(file);
                }
            }
            list.removeAll(removal);
            list.addAll(verified);
            listener.chunk(directory, list);
            return list;
        }
        catch(IOException e) {
            throw new FTPExceptionMappingService().map("Listing directory {0} failed", e, directory);
        }
    }
}
