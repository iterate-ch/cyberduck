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

import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

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
                .withLongOpt("username")
                .hasArg(true)
                .isRequired(false)
                .create('u'));
        options.addOption(OptionBuilder.withArgName("password")
                .withDescription("Password")
                .withLongOpt("password")
                .hasArg(true)
                .isRequired(false)
                .create('p'));
        options.addOption(OptionBuilder
                .withDescription("Download file or folder")
                .withLongOpt(TerminalAction.download.name())
                .hasArg(false)
                .isRequired(false)
                .create('d'));
        options.addOption(OptionBuilder
                .withDescription("Upload file or folder")
                .withLongOpt(TerminalAction.upload.name())
                .hasArg(false)
                .isRequired(false)
                .create());
        options.addOption(OptionBuilder
                .withDescription("Copy between servers")
                .withLongOpt(TerminalAction.copy.name())
                .hasArg(false)
                .isRequired(false)
                .create());
        options.addOption(OptionBuilder
                .withDescription("Synchronize folders")
                .withLongOpt(TerminalAction.synchronize.name())
                .hasArg(false)
                .isRequired(false)
                .create());
        options.addOption(OptionBuilder
                .withDescription("Edit file in external editor")
                .withLongOpt(TerminalAction.edit.name())
                .hasArg(true)
                .isRequired(false)
                .create());
        options.addOption(OptionBuilder
                .withDescription("Print transcript")
                .withLongOpt("verbose")
                .hasArg(false)
                .isRequired(false)
                .create('v'));
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
}
