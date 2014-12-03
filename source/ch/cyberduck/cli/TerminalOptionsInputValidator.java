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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostParser;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.ProtocolFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.lang3.StringUtils;

/**
 * @version $Id$
 */
public class TerminalOptionsInputValidator {

    protected static boolean validate(final CommandLine input) {
        final TerminalAction action = TerminalActionFinder.get(input);
        if(null == action) {
            return false;
        }
        // Validate arguments
        switch(action) {
            case list:
            case download:
                if(StringUtils.isBlank(input.getOptionValue(action.name()))) {
                    return false;
                }
                if(!validate(input.getOptionValue(action.name()))) {
                    return false;
                }
                break;
            case upload:
            case copy:
            case synchronize:
                if(StringUtils.isBlank(input.getOptionValue(action.name()))) {
                    return false;
                }
                if(!validate(input.getOptionValue(action.name()))) {
                    return false;
                }
                break;
        }
        return true;
    }

    /**
     * Validate URI
     */
    private static boolean validate(final String uri) {
        if(uri.indexOf("://", 0) != -1) {
            final Protocol protocol = ProtocolFactory.forName(uri.substring(0, uri.indexOf("://", 0)));
            if(null == protocol) {
                return false;
            }
        }
        final Host host = HostParser.parse(uri);
        if(StringUtils.isBlank(host.getHostname())) {
            return false;
        }
        if(StringUtils.isBlank(host.getDefaultPath())) {
            return false;
        }
        return true;
    }
}
