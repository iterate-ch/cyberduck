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

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;

public final class TerminalOptionsBuilder {

    private TerminalOptionsBuilder() {
        //
    }

    public static Options options() {
        final Options options = new Options();

        final OptionGroup actionGroup = new OptionGroup();
        actionGroup.addOption(Option.builder()
            .longOpt(TerminalAction.upload.name())
            .desc("Upload file or folder recursively")
            .hasArgs().numberOfArgs(2).argName("url> <file").build());
        actionGroup.addOption(Option.builder("d")
            .longOpt(TerminalAction.download.name())
            .desc("Download file or folder. Denote a folder with a trailing '/'")
            .hasArgs().numberOfArgs(2).argName("url> <file").build());
        actionGroup.addOption(Option.builder()
            .longOpt(TerminalAction.copy.name())
            .desc("Copy from origin server to target server")
            .hasArgs().numberOfArgs(2).argName("url> <url").build());
        actionGroup.addOption(Option.builder("l")
            .longOpt(TerminalAction.list.name())
            .desc("List files in remote folder")
            .hasArg().argName("url").build());
        actionGroup.addOption(Option.builder("L")
            .longOpt(TerminalAction.longlist.name())
            .desc("Long list format with modification date and permission mask")
            .hasArg().argName("url").build());
        actionGroup.addOption(Option.builder("D")
            .longOpt(TerminalAction.delete.name())
            .desc("Delete")
            .hasArg().argName("url").build());
        actionGroup.addOption(Option.builder("c")
            .longOpt(TerminalAction.mkdir.name())
            .desc("Make directory")
            .hasArg().argName("url").build());
        actionGroup.addOption(Option.builder()
            .longOpt(TerminalAction.synchronize.name())
            .desc("Synchronize folders")
            .hasArgs().numberOfArgs(2).argName("url> <directory").build());
        actionGroup.addOption(Option.builder()
            .longOpt(TerminalAction.edit.name())
            .desc("Edit file in external editor")
            .hasArg().argName("url").build());
        actionGroup.addOption(Option.builder()
            .longOpt(TerminalAction.purge.name())
            .desc("Invalidate file in CDN")
            .hasArg().argName("url").build());
        actionGroup.addOption(Option.builder("V")
            .longOpt(TerminalAction.version.name())
            .desc("Show version number and quit.").build());
        actionGroup.addOption(Option.builder("h")
            .longOpt(TerminalAction.help.name())
            .desc("Print this help").build());

        actionGroup.setRequired(true);
        options.addOptionGroup(actionGroup);

        options.addOption(Option.builder("u")
            .longOpt(Params.username.name())
            .desc("Username")
            .hasArg().argName("username or access key").build());
        options.addOption(Option.builder("p")
            .longOpt(Params.password.name())
            .desc("Password")
            .hasArg().argName("password or secret key").build());
        options.addOption(Option.builder()
                .longOpt(Params.anonymous.name())
                .desc("No login").build());
        options.addOption(Option.builder()
            .longOpt(Params.profile.name())
            .desc("Use connection profile")
            .hasArg().argName("profile").build());
        options.addOption(Option.builder("i")
            .longOpt(Params.identity.name())
            .desc("Selects a file from which the identity (private key) for public key authentication is read.")
            .hasArg().argName("private key file").build());

        options.addOption(Option.builder()
            .longOpt(Params.chmod.name())
            .desc("Set explicit permission from octal mode value for uploaded file")
            .hasArg().argName("mode").build());

        options.addOption(Option.builder()
            .longOpt(Params.application.name())
            .desc("External editor application")
            .hasArg().argName("path").build());

        options.addOption(Option.builder()
            .longOpt(Params.region.name())
            .desc("Location of bucket or container")
            .hasArg().argName("location").build());

        options.addOption(Option.builder("P")
            .longOpt(Params.preserve.name())
            .desc("Preserve permissions and modificatin date for transferred files").build());
        options.addOption(Option.builder("r")
            .longOpt(Params.retry.name())
            .desc("Retry failed connection attempts")
            .optionalArg(true).argName("count").build());
        options.addOption(Option.builder()
            .longOpt(Params.udt.name())
            .desc("Use UDT protocol if applicable").build());
        options.addOption(Option.builder()
            .longOpt(Params.parallel.name())
            .desc("Number of concurrent connections to use for transfers")
            .hasArg().optionalArg(true).argName("connections").build());
        options.addOption(Option.builder()
            .longOpt(Params.throttle.name())
            .desc("Throttle bandwidth")
            .hasArg().argName("bytes per second").build());
        options.addOption(Option.builder()
            .longOpt(Params.nochecksum.name())
            .desc("Skip verifying checksum").build());
        options.addOption(Option.builder()
            .longOpt(Params.nokeychain.name())
            .desc("Do not save passwords in keychain").build());
        options.addOption(Option.builder()
            .longOpt(Params.vault.name())
            .desc("Unlock vault")
            .hasArg().argName("path").build());

        final StringBuilder actions = new StringBuilder("Transfer actions for existing files").append(StringUtils.LF);
        actions.append("Downloads and uploads:").append(StringUtils.LF);
        for(TransferAction a : TransferAction.forTransfer(Transfer.Type.download)) {
            append(actions, a);
        }
        for(TransferAction a : Collections.singletonList(TransferAction.cancel)) {
            append(actions, a);
        }
        actions.append("Synchronize:").append(StringUtils.LF);
        for(TransferAction a : TransferAction.forTransfer(Transfer.Type.sync)) {
            append(actions, a);
        }
        for(TransferAction a : Collections.singletonList(TransferAction.cancel)) {
            append(actions, a);
        }

        options.addOption(Option.builder("e")
            .longOpt(Params.existing.name())
            .desc(actions.toString())
            .hasArg().argName("action").build());
        options.addOption(Option.builder("v")
            .longOpt(Params.verbose.name())
            .desc("Print transcript").build());
        options.addOption(Option.builder()
            .longOpt(Params.debug.name())
            .desc("Print debug output").build());
        options.addOption(Option.builder("q")
            .longOpt(Params.quiet.name())
            .desc("Suppress progress messages").build());
        options.addOption(Option.builder("y")
            .longOpt(Params.assumeyes.name())
            .desc("Assume yes for all prompts").build());

        return options;
    }

    private static void append(final StringBuilder builder, final TransferAction action) {
        builder.append(String.format("  %s  %s (%s)\n",
            StringUtils.leftPad(action.name(), 16), action.getTitle(), action.getDescription()));
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
        vault,
        existing,
        verbose,
        quiet,
        assumeyes,
        username,
        anonymous,
        password,
        identity,
        application,
        chmod,
        profile,
        debug
    }
}
