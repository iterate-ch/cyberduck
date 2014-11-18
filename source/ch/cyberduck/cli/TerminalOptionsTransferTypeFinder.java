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

import org.apache.commons.cli.CommandLine;

import java.util.List;

/**
 * @version $Id$
 */
public class TerminalOptionsTransferTypeFinder {

    protected static Transfer.Type get(final CommandLine input) {
        final List arguments = input.getArgList();
        final Transfer.Type type;
        if(input.hasOption(TerminalAction.download.name())) {
            type = Transfer.Type.download;
        }
        else if(input.hasOption(TerminalAction.upload.name())) {
            type = Transfer.Type.upload;
        }
        else {
            if(arguments.size() == 2) {
                type = Transfer.Type.upload;
            }
            else {
                type = Transfer.Type.download;
            }
        }
        return type;
    }
}
