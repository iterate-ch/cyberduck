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

import java.util.Collections;

public final class TerminalOptionsBuilder {

    private TerminalOptionsBuilder() {
        //
    }

    public static Options options() {
        final Options options = new Options();
        options.addOption(OptionBuilder
            .withDescription("Username")
            .withLongOpt(Params.username.name())
            .hasArg(true).withArgName("username or access key")
            .isRequired(false)
            .create('u'));
        options.addOption(OptionBuilder
            .withDescription("Password")
            .withLongOpt(Params.password.name())
            .hasArg(true).withArgName("password or secret key")
            .isRequired(false)
            .create('p'));
        options.addOption(OptionBuilder
            .withDescription("Use connection profile")
            .withLongOpt(Params.profile.name())
            .hasArg(true).withArgName("profile")
            .isRequired(false)
            .create());
        options.addOption(OptionBuilder
            .withDescription("Selects a file from which the identity (private key) for public key authentication is read")
            .withLongOpt(Params.identity.name())
            .hasArg(true).withArgName("private key file")
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
            .withDescription("Set explicit permission from octal mode value for uploaded file")
            .withLongOpt(Params.chmod.name())
            .hasArgs(1).withArgName("<mode>").withValueSeparator(' ')
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
            .withDescription("Delete")
            .withLongOpt(TerminalAction.delete.name())
            .hasArg(true).withArgName("url")
            .isRequired(false)
            .create("D"));
        options.addOption(OptionBuilder
            .withDescription("Make directory")
            .withLongOpt(TerminalAction.mkdir.name())
            .hasArg(true).withArgName("url")
            .isRequired(false)
            .create("c"));
        options.addOption(OptionBuilder
            .withDescription("Long list format with modification date and permission mask")
            .withLongOpt(Params.longlist.name())
            .hasArg(true).withArgName("url")
            .isRequired(false)
            .create('L'));
        options.addOption(OptionBuilder
            .withDescription("Location of bucket or container")
            .withLongOpt(Params.region.name())
            .hasArg(true).withArgName("location")
            .isRequired(false)
            .create());
        options.addOption(OptionBuilder
            .withDescription("Preserve permissions and modification date for transferred files")
            .withLongOpt(Params.preserve.name())
            .hasArg(false)
            .isRequired(false)
            .create('P'));
        options.addOption(OptionBuilder
            .withDescription("Retry failed connection attempts")
            .withLongOpt(Params.retry.name())
            .hasOptionalArg().withArgName("count")
            .isRequired(false)
            .create('r'));
        options.addOption(OptionBuilder
            .withDescription("Use UDT protocol if applicable")
            .withLongOpt(Params.udt.name())
            .isRequired(false)
            .create());
        options.addOption(OptionBuilder
            .withDescription("Number of concurrent connections to use for transfers")
            .withLongOpt(Params.parallel.name())
            .hasOptionalArg().withArgName("connections")
            .isRequired(false)
            .create());
        options.addOption(OptionBuilder
            .withDescription("Throttle bandwidth")
            .withLongOpt(Params.throttle.name())
            .hasArg(true).withArgName("bytes per second")
            .isRequired(false)
            .create());
        options.addOption(OptionBuilder
            .withDescription("Skip verifying checksum")
            .withLongOpt(Params.nochecksum.name())
            .isRequired(false)
            .create());
        options.addOption(OptionBuilder
            .withDescription("Do not save passwords in keychain")
            .withLongOpt(Params.nokeychain.name())
            .isRequired(false)
            .create());
        final StringBuilder b = new StringBuilder().append(StringUtils.LF);
        b.append("Options for downloads and uploads:").append(StringUtils.LF);
        for(TransferAction a : TransferAction.forTransfer(Transfer.Type.download)) {
            b.append("\t").append(a.getTitle()).append("\t").append(a.getDescription()).append(String.format(" (%s)", a.name())).append(StringUtils.LF);
        }
        for(TransferAction a : Collections.singletonList(TransferAction.cancel)) {
            b.append("\t").append(a.getTitle()).append("\t").append(a.getDescription()).append(String.format(" (%s)", a.name())).append(StringUtils.LF);
        }
        b.append("Options for synchronize:").append(StringUtils.LF);
        for(TransferAction a : TransferAction.forTransfer(Transfer.Type.sync)) {
            b.append("\t").append(a.getTitle()).append("\t").append(a.getDescription()).append(String.format(" (%s)", a.name())).append(StringUtils.LF);
        }
        for(TransferAction a : Collections.singletonList(TransferAction.cancel)) {
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
            .withDescription("Assume yes for all prompts")
            .withLongOpt(Params.assumeyes.name())
            .hasArg(false)
            .isRequired(false)
            .create('y'));
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
        region,
        longlist,
        preserve,
        retry,
        udt,
        parallel,
        throttle,
        nochecksum,
        nokeychain,
        existing,
        verbose,
        quiet,
        assumeyes,
        username,
        password,
        identity,
        application,
        chmod,
        profile
    }
}
