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

import org.apache.commons.cli.CommandLine;

/**
 * @version $Id$
 */
public class TerminalActionFinder {
    public static TerminalAction get(final CommandLine input) {
        if(input.hasOption(TerminalAction.edit.name())) {
            return TerminalAction.edit;
        }
        else if(input.hasOption(TerminalAction.list.name())) {
            return TerminalAction.list;
        }
        else if(input.hasOption(TerminalAction.download.name())) {
            return TerminalAction.download;
        }
        else if(input.hasOption(TerminalAction.upload.name())) {
            return TerminalAction.upload;
        }
        else if(input.hasOption(TerminalAction.synchronize.name())) {
            return TerminalAction.synchronize;
        }
        else if(input.hasOption(TerminalAction.copy.name())) {
            return TerminalAction.copy;
        }
        return null;
    }
}
