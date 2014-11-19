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
import ch.cyberduck.core.StringAppender;
import ch.cyberduck.core.local.Application;
import ch.cyberduck.core.local.ApplicationFinder;
import ch.cyberduck.core.local.ApplicationFinderFactory;
import ch.cyberduck.core.transfer.Transfer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @version $Id$
 */
public class TerminalOptionsInputValidator {

    protected static boolean validate(final CommandLine input) {
        final List arguments = input.getArgList();
        if(arguments.size() == 0 || arguments.size() > 2) {
            return false;
        }
        if(!validate(arguments.get(0).toString())) {
            return false;
        }
        if(input.hasOption(TerminalAction.edit.name())) {
            if(StringUtils.isNotBlank(input.getOptionValue(TerminalAction.edit.name()))) {
                final ApplicationFinder finder = ApplicationFinderFactory.get();
                final Application application = finder.getDescription(input.getOptionValue(TerminalAction.edit.name()));
                if(!finder.isInstalled(application)) {
                    final StringAppender appender = new StringAppender();
                    appender.append(String.format("Failed to find application %s", application.getIdentifier()));
                    System.err.println(appender.toString());
                    return false;
                }
            }
            return true;
        }
        final Transfer.Type type = TerminalOptionsTransferTypeFinder.get(input);
        switch(type) {
            case download:
                if(arguments.size() != 1 && arguments.size() != 2) {
                    return false;
                }
                break;
            case upload:
            case copy:
            case sync:
                if(arguments.size() != 2) {
                    return false;
                }
                break;
        }
        switch(type) {
            case copy:
                if(!validate(arguments.get(1).toString())) {
                    return false;
                }
        }
        return true;
    }

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
