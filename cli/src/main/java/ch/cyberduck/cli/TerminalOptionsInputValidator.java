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
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAction;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TerminalOptionsInputValidator {

    private final Console console = new Console();

    private final ProtocolFactory factory;

    public TerminalOptionsInputValidator() {
        this(ProtocolFactory.global);
    }

    public TerminalOptionsInputValidator(final ProtocolFactory factory) {
        this.factory = factory;
    }

    public boolean validate(final CommandLine input) {
        for(Option o : input.getOptions()) {
            if(Option.UNINITIALIZED == o.getArgs()) {
                continue;
            }
            if(o.hasOptionalArg()) {
                continue;
            }
            if(o.getArgs() != o.getValuesList().size()) {
                console.printf("Missing argument for option %s%n", o.getLongOpt());
                return false;
            }
        }
        final TerminalAction action = TerminalActionFinder.get(input);
        if(null == action) {
            console.printf("%s%n", "Missing argument");
            return false;
        }
        if(input.hasOption(TerminalOptionsBuilder.Params.existing.name())) {
            final String arg = input.getOptionValue(TerminalOptionsBuilder.Params.existing.name());
            if(null == TransferAction.forName(arg)) {
                final Set<TransferAction> actions = new HashSet<TransferAction>(TransferAction.forTransfer(Transfer.Type.download));
                actions.add(TransferAction.cancel);
                console.printf("Invalid argument '%s' for option %s. Must be one of %s%n",
                        arg, TerminalOptionsBuilder.Params.existing.name(), Arrays.toString(actions.toArray()));
                return false;
            }
            switch(action) {
                case download:
                    if(!validate(arg, Transfer.Type.download)) {
                        return false;
                    }
                    break;
                case upload:
                    if(!validate(arg, Transfer.Type.upload)) {
                        return false;
                    }
                    break;
                case synchronize:
                    if(!validate(arg, Transfer.Type.sync)) {
                        return false;
                    }
                    break;
                case copy:
                    if(!validate(arg, Transfer.Type.copy)) {
                        return false;
                    }
                    break;
            }
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

    private boolean validate(final String arg, final Transfer.Type type) {
        final List<TransferAction> actions = TransferAction.forTransfer(type);
        if(!actions.contains(TransferAction.forName(arg))) {
            console.printf("Invalid argument '%s' for option %s. Must be one of %s%n",
                    arg, TerminalOptionsBuilder.Params.existing.name(), Arrays.toString(actions.toArray()));
            return false;
        }
        return true;
    }

    /**
     * Validate URI
     */
    protected boolean validate(final String uri) {
        if(uri.indexOf("://", 0) != -1) {
            final Protocol protocol = factory.find(uri.substring(0, uri.indexOf("://", 0)));
            if(null == protocol) {
                console.printf("Missing protocol in URI %s%n", uri);
                return false;
            }
        }
        final Host host = new HostParser(factory).get(uri);
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
