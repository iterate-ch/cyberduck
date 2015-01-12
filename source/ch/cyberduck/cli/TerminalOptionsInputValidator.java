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
import org.apache.commons.cli.Option;
import org.apache.commons.lang3.StringUtils;

/**
 * @version $Id$
 */
public class TerminalOptionsInputValidator {

    private Console console = new Console();

    public boolean validate(final CommandLine input) {
        for(Option o : input.getOptions()) {
            if(Option.UNINITIALIZED == o.getArgs()) {
                continue;
            }
            if(o.getArgs() != o.getValuesList().size()) {
                console.printf("%s %s%n", "Missing argument for option", o.getLongOpt());
                return false;
            }
        }
        final TerminalAction action = TerminalActionFinder.get(input);
        if(null == action) {
            console.printf("%s%n", "Missing argument");
            return false;
        }
        // Validate arguments
        switch(action) {
            case list:
            case download:
                if(!validate(input.getOptionValue(action.name()))) {
                    return false;
                }
                break;
            case upload:
            case copy:
            case synchronize:
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
    protected boolean validate(final String uri) {
        if(uri.indexOf("://", 0) != -1) {
            final Protocol protocol = ProtocolFactory.forName(uri.substring(0, uri.indexOf("://", 0)));
            if(null == protocol) {
                console.printf("Missing protocol in URI %s%n", uri);
                return false;
            }
        }
        final Host host = HostParser.parse(uri);
        switch(host.getProtocol().getType()) {
            case s3:
            case googlestorage:
            case swift:
                break;
            default:
                if(StringUtils.isBlank(host.getHostname())) {
                    console.printf("Missing hostname in URI %s%n", uri);
                    return false;
                }
        }
        if(StringUtils.isBlank(host.getDefaultPath())) {
            console.printf("Missing path in URI %s%n", uri);
            return false;
        }
        return true;
    }
}
