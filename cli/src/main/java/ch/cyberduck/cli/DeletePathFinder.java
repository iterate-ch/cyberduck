package ch.cyberduck.cli;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.transfer.TransferItem;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.Set;

public class DeletePathFinder implements TransferItemFinder {

    @Override
    public Set<TransferItem> find(final CommandLine input, final TerminalAction action, final Path remote) throws AccessDeniedException {
        if(StringUtils.containsAny(remote.getName(), '*')) {
            // Treat asterisk as wildcard
            return Collections.singleton(new TransferItem(remote.getParent()));
        }
        return Collections.singleton(new TransferItem(remote));
    }
}
