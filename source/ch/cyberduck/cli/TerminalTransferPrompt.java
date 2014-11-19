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
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferPrompt;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * @version $Id$
 */
public class TerminalTransferPrompt implements TransferPrompt {

    private Console console = new Console();

    private Transfer transfer;

    public TerminalTransferPrompt(final Transfer transfer) {
        this.transfer = transfer;
    }

    @Override
    public TransferAction prompt(final TransferItem file) {
        final StringBuilder actions = new StringBuilder().append(StringUtils.LF);
        for(TransferAction a : TransferAction.forTransfer(transfer)) {
            actions.append("\t").append(a.getTitle()).append("\t").append(a.getDescription()).append(String.format(" (%s)", a.name())).append(StringUtils.LF);
        }
        final String input;
        switch(transfer.getType()) {
            case download:
                input = console.readLine("The local file %s already exists. Choose what action to take:\n%s\nAction %s: ",
                        file.local.getName(), actions, Arrays.toString(TransferAction.forTransfer(transfer).toArray()));
                break;
            case upload:
            case move:
            case copy:
                input = console.readLine("The remote file %s already exists. Choose what action to take:\n%s\nAction %s: ",
                        file.remote.getName(), actions, Arrays.toString(TransferAction.forTransfer(transfer).toArray()));
                break;
            case sync:
                input = console.readLine("Choose what action to take:\n%s\nAction %s: ",
                        file.remote.getName(), actions, Arrays.toString(TransferAction.forTransfer(transfer).toArray()));
                break;
            default:
                return TransferAction.cancel;
        }
        final TransferAction action = TransferAction.forName(input);
        if(null == action) {
            return this.prompt(file);
        }
        return action;
    }

    @Override
    public boolean isSelected(final TransferItem file) {
        return true;
    }

    @Override
    public void message(final String message) {
        console.printf(message);
    }
}
