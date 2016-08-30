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
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.aquaticprime.License;
import ch.cyberduck.core.aquaticprime.LicenseFactory;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;

public final class TerminalHelpPrinter {

    private TerminalHelpPrinter() {
        //
    }

    public static void print(final Options options) {
        print(options, new TerminalHelpFormatter());
    }

    public static void print(final Options options, final HelpFormatter formatter) {
        formatter.setSyntaxPrefix("Usage:");
        final StringBuilder protocols = new StringBuilder(StringUtils.LF);
        protocols.append("Supported protocols");
        protocols.append(StringUtils.LF);
        for(Protocol p : ProtocolFactory.getEnabledProtocols()) {
            protocols.append(p.getProvider()).append("\t").append(p.getDescription());
            protocols.append(StringUtils.LF);
            switch(p.getType()) {
                case s3:
                case googlestorage:
                case swift:
                case azure:
                    protocols.append("\t").append(String.format("%s://<container>/<key>", p.getProvider()));
                    break;
                default:
                    protocols.append("\t").append(String.format("%s://<hostname>/<folder>/<file>", p.getProvider()));
                    break;
            }
            protocols.append(StringUtils.LF);
        }
        final StringBuilder header = new StringBuilder(StringUtils.LF);
        header.append("\t");
        header.append("URLs must be fully qualified. Paths can either denote "
                + "a remote file (ftps://user@example.net/resource) or folder (ftps://user@example.net/directory/) "
                + "with a trailing slash. You can reference files relative to your home directory with /~ (ftps://user@example.net/~/).");
        header.append(protocols.toString());
        final Preferences preferences = PreferencesFactory.get();
        final Local profiles = LocalFactory.get(preferences.getProperty("application.support.path"),
                PreferencesFactory.get().getProperty("profiles.folder.name"));
        header.append(StringUtils.LF);
        header.append(String.format("You can install additional connection profiles in %s",
                profiles.getAbbreviatedPath()));
        header.append(StringUtils.LF);
        final StringBuilder footer = new StringBuilder(StringUtils.LF);
        footer.append(String.format("Cyberduck is libre software licenced under the GPL. For general help about using Cyberduck, please refer to %s and the wiki at %s. For bug reports or feature requests open a ticket at %s.",
                preferences.getProperty("website.cli"), preferences.getProperty("website.help"), MessageFormat.format(preferences.getProperty("website.bug"), preferences.getProperty("application.version"))));
        final License l = LicenseFactory.find();
        footer.append(StringUtils.LF);
        if(l.verify()) {
            footer.append(l.toString());
        }
        else {
            footer.append("Not registered. Purchase a donation key to support the development of this software.");
        }
        formatter.printHelp("duck [options...]", header.toString(), options, footer.toString());
    }

}
