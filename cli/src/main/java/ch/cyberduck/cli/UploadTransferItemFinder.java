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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.transfer.TransferItem;

import org.apache.commons.cli.CommandLine;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public class UploadTransferItemFinder implements TransferItemFinder {

    @Override
    public Set<TransferItem> find(final CommandLine input, final TerminalAction action, final Path remote) {
        final Local local = LocalFactory.get(input.getOptionValues(action.name())[1]);
        final Set<TransferItem> items = new HashSet<>();
        items.add(resolve(remote, local));
        for(String arg : input.getArgs()) {
            items.add(resolve(remote, LocalFactory.get(arg)));
        }
        return items;
    }

    protected static TransferItem resolve(final Path remote, final Local local) {
        if(local.isDirectory()) {
            // Local path resolves to folder
            return new TransferItem(remote, local);
        }
        // Local path resolves to file
        if(remote.isDirectory()) {
            // Append local name to remote target
            return new TransferItem(new Path(remote, local.getName(), EnumSet.of(Path.Type.file)), local);
        }
        // Keep from input for file transfer
        return new TransferItem(remote, local);
    }
}

