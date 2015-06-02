package ch.cyberduck.cli;

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

import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAction;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * @version $Id$
 */
public final class TerminalOptionsBuilder {

    private static final Preferences preferences
            = PreferencesFactory.get();

    private TerminalOptionsBuilder() {
        //
    }

    public static Options options() {
        final Options options = new Options();
        options.addOption(Option.builder("u")
                .desc("Username")
                .longOpt(Params.username.name())
                .hasArg(true).argName("username or access key")
                .required(false)
                .build());
        options.addOption(Option.builder("p")
                .desc("Password")
                .longOpt(Params.password.name())
                .hasArg(true).argName("password or secret key")
                .required(false)
                .build());
        options.addOption(Option.builder("i")
                .desc("Selects a file from which the identity (private key) for public key authentication is read")
                .longOpt(Params.identity.name())
                .hasArg(true).argName("private key file")
                .required(false)
                .build());
        options.addOption(Option.builder("d")
                .desc("Download file or folder. Denote a folder with a trailing '/'")
                .longOpt(TerminalAction.download.name())
                .numberOfArgs(2).argName("url> <[file]").optionalArg(true).valueSeparator(' ')
                .required(false)
                .build());
        options.addOption(Option.builder()
                .desc("Upload file or folder recursively")
                .longOpt(TerminalAction.upload.name())
                .numberOfArgs(2).argName("url> <file").optionalArg(true).valueSeparator(' ')
                .required(false)
                .build());
        options.addOption(Option.builder()
                .desc("Copy between servers")
                .longOpt(TerminalAction.copy.name())
                .numberOfArgs(2).argName("url> <url").valueSeparator(' ')
                .required(false)
                .build());
        options.addOption(Option.builder()
                .desc("Synchronize folders")
                .longOpt(TerminalAction.synchronize.name())
                .numberOfArgs(2).argName("url> <directory").valueSeparator(' ')
                .required(false)
                .build());
        options.addOption(Option.builder()
                .desc("Edit file in external editor")
                .longOpt(TerminalAction.edit.name())
                .numberOfArgs(1).argName("url")
                .required(false)
                .build());
        options.addOption(Option.builder()
                .desc("External editor application")
                .longOpt(Params.application.name())
                .numberOfArgs(1).argName("path")
                .required(false)
                .build());
        options.addOption(Option.builder("l")
                .desc("List files in remote folder")
                .longOpt(TerminalAction.list.name())
                .hasArg(true).argName("url")
                .required(false)
                .build());
        if(preferences.getProperty("factory.filesystem.class") != null) {
            options.addOption(Option.builder()
                    .desc("Mount as filesystem")
                    .longOpt(TerminalAction.mount.name())
                    .hasArg(true).argName("url")
                    .required(false)
                    .build());
        }
        options.addOption(Option.builder("L")
                .desc("Long list format with modification date and permission mask")
                .longOpt(Params.longlist.name())
                .hasArg(false)
                .required(false)
                .build());
        options.addOption(Option.builder()
                .desc("Location of bucket or container")
                .longOpt(Params.region.name())
                .hasArg(true).argName("location")
                .required(false)
                .build());
        options.addOption(Option.builder("P")
                .desc("Preserve permissions and modification date for transferred files")
                .longOpt(Params.preserve.name())
                .hasArg(false)
                .required(false)
                .build());
        options.addOption(Option.builder("r")
                .desc("Retry failed connection attempts")
                .longOpt(Params.retry.name())
                .optionalArg(true).argName("count")
                .required(false)
                .build());
        options.addOption(Option.builder()
                .desc("Use UDT protocol if applicable")
                .longOpt(Params.udt.name())
                .required(false)
                .build());
        options.addOption(Option.builder()
                .desc("Number of concurrent connections to use for transfers")
                .longOpt(Params.parallel.name())
                .optionalArg(true).argName("connections")
                .required(false)
                .build());
        options.addOption(Option.builder()
                .desc("Throttle bandwidth")
                .longOpt(Params.throttle.name())
                .hasArg(true).argName("bytes per second")
                .required(false)
                .build());
        final StringBuilder b = new StringBuilder().append(StringUtils.LF);
        final Set<TransferAction> actions = new HashSet<TransferAction>(TransferAction.forTransfer(Transfer.Type.download));
        actions.add(TransferAction.cancel);
        for(TransferAction a : actions) {
            b.append("\t").append(a.getTitle()).append("\t").append(a.getDescription()).append(String.format(" (%s)", a.name())).append(StringUtils.LF);
        }
        options.addOption(Option.builder("e")
                .desc(String.format("Transfer action for existing files%s", b.toString()))
                .longOpt(Params.existing.name())
                .hasArg(true).argName("action")
                .required(false)
                .build());
        options.addOption(Option.builder("v")
                .desc("Print transcript")
                .longOpt(Params.verbose.name())
                .hasArg(false)
                .required(false)
                .build());
        options.addOption(Option.builder("q")
                .desc("Suppress progress messages")
                .longOpt(Params.quiet.name())
                .hasArg(false)
                .required(false)
                .build());
        options.addOption(Option.builder("y")
                .desc("Assume yes for all prompts")
                .longOpt(Params.assumeyes.name())
                .hasArg(false)
                .required(false)
                .build());
        options.addOption(Option.builder("V")
                .desc("Show version number and quit")
                .longOpt(TerminalAction.version.name())
                .hasArg(false)
                .required(false)
                .build());
        options.addOption(Option.builder("h")
                .desc("Print this help")
                .longOpt(TerminalAction.help.name())
                .hasArg(false)
                .required(false)
                .build());
        return options;
    }

    public enum Params {
        region,
        longlist,
        preserve,
        retry,
        udt,
        parallel,
        throttle,
        existing,
        verbose,
        quiet,
        assumeyes,
        username,
        password,
        identity,
        application
    }
}
