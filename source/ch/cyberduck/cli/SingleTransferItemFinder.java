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

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * @version $Id$
 */
public class SingleTransferItemFinder implements TransferItemFinder {

    @Override
    public Set<TransferItem> find(final CommandLine input, final TerminalAction action, final Path remote) {
        final Local local;
        if(input.getOptionValues(action.name()).length == 2) {
            if(LocalFactory.get(input.getOptionValues(action.name())[1]).isDirectory()) {
                local = LocalFactory.get(input.getOptionValues(action.name())[1], remote.getName());
            }
            else {
                local = LocalFactory.get(input.getOptionValues(action.name())[1]);
            }
        }
        else {
            switch(action) {
                case upload:
                case synchronize:
                    return Collections.emptySet();
            }
            local = LocalFactory.get(remote.getName());
        }
        if(remote.isDirectory()) {
            return Collections.singleton(new TransferItem(new Path(remote, local.getName(), EnumSet.of(Path.Type.file)), local));
        }
        return Collections.singleton(new TransferItem(remote, local));
    }
}
