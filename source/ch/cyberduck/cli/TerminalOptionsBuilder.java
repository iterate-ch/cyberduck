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

import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAction;

import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * @version $Id$
 */
public final class TerminalOptionsBuilder {

    private TerminalOptionsBuilder() {
        //
    }

    public static Options options() {
        final Options options = new Options();
        options.addOption(OptionBuilder.withArgName("username")
                .withDescription("Username")
                .withLongOpt(Params.username.name())
                .hasArg(true)
                .isRequired(false)
                .create('u'));
        options.addOption(OptionBuilder.withArgName("password")
                .withDescription("Password")
                .withLongOpt(Params.password.name())
                .hasArg(true)
                .isRequired(false)
                .create('p'));
        options.addOption(OptionBuilder.withArgName("private key file")
                .withDescription("Selects a file from which the identity (private key) for public key authentication is read")
                .withLongOpt(Params.identity.name())
                .hasArg(true)
                .isRequired(false)
                .create('i'));
        options.addOption(OptionBuilder
                .withDescription("Download file or folder. Denote a folder with a trailing '/'")
                .withLongOpt(TerminalAction.download.name())
                .hasArgs(2).withArgName("url> <[file]").withValueSeparator(' ')
                .isRequired(false)
                .create('d'));
        options.addOption(OptionBuilder
                .withDescription("Upload file or folder recursively")
                .withLongOpt(TerminalAction.upload.name())
                .hasArgs(2).withArgName("url> <file").withValueSeparator(' ')
                .isRequired(false)
                .create());
        options.addOption(OptionBuilder
                .withDescription("Copy between servers")
                .withLongOpt(TerminalAction.copy.name())
                .hasArgs(2).withArgName("url> <url").withValueSeparator(' ')
                .isRequired(false)
                .create());
        options.addOption(OptionBuilder
                .withDescription("Synchronize folders")
                .withLongOpt(TerminalAction.synchronize.name())
                .hasArgs(2).withArgName("url> <directory").withValueSeparator(' ')
                .isRequired(false)
                .create());
        options.addOption(OptionBuilder
                .withDescription("Edit file in external editor")
                .withLongOpt(TerminalAction.edit.name())
                .hasArgs(1).withArgName("url")
                .isRequired(false)
                .create());
        options.addOption(OptionBuilder
                .withDescription("External editor application")
                .withLongOpt(Params.application.name())
                .hasArgs(1).withArgName("path")
                .isRequired(false)
                .create());
        options.addOption(OptionBuilder
                .withDescription("List files in remote folder")
                .withLongOpt(TerminalAction.list.name())
                .hasArg(true).withArgName("url")
                .isRequired(false)
                .create("l"));
        options.addOption(OptionBuilder
                .withDescription("Long list format with modification date and permission mask")
                .withLongOpt(Params.longlist.name())
                .hasArg(false)
                .isRequired(false)
                .create('L'));
        options.addOption(OptionBuilder
                .withDescription("Preserve permissions and modification date for transferred files")
                .withLongOpt(Params.preserve.name())
                .hasArg(false)
                .isRequired(false)
                .create('P'));
        final StringBuilder b = new StringBuilder().append(StringUtils.LF);
        final Set<TransferAction> actions = new HashSet<TransferAction>(TransferAction.forTransfer(Transfer.Type.download));
        actions.add(TransferAction.cancel);
        for(TransferAction a : actions) {
            b.append("\t").append(a.getTitle()).append("\t").append(a.getDescription()).append(String.format(" (%s)", a.name())).append(StringUtils.LF);
        }
        options.addOption(OptionBuilder
                .withDescription(String.format("Transfer action for existing files%s", b.toString()))
                .withLongOpt(Params.existing.name())
                .hasArg(true).withArgName("action")
                .isRequired(false)
                .create('e'));
        options.addOption(OptionBuilder
                .withDescription("Print transcript")
                .withLongOpt(Params.verbose.name())
                .hasArg(false)
                .isRequired(false)
                .create('v'));
        options.addOption(OptionBuilder
                .withDescription("Suppress progress messages")
                .withLongOpt(Params.quiet.name())
                .hasArg(false)
                .isRequired(false)
                .create('q'));
        options.addOption(OptionBuilder
                .withDescription("Show version number and quit")
                .withLongOpt(TerminalAction.version.name())
                .hasArg(false)
                .isRequired(false)
                .create('V'));
        options.addOption(OptionBuilder
                .withDescription("Print this help")
                .withLongOpt(TerminalAction.help.name())
                .hasArg(false)
                .isRequired(false)
                .create("h"));
        return options;
    }

    public enum Params {
        longlist,
        preserve,
        existing,
        verbose,
        quiet,
        username,
        password,
        identity,
        application
    }
}
