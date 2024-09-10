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

import ch.cyberduck.core.BundledProtocolPredicate;
import ch.cyberduck.core.DefaultProtocolPredicate;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.ProfileProtocolPredicate;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.StringAppender;
import ch.cyberduck.core.aquaticprime.License;
import ch.cyberduck.core.aquaticprime.LicenseFactory;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.preferences.SupportDirectoryFinderFactory;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.EnumSet;

public final class TerminalHelpPrinter {

    private TerminalHelpPrinter() {
        //
    }

    public static void print(final Options options) {
        print(options, new TerminalHelpFormatter());
    }

    public static void print(final Options options, final HelpFormatter formatter) {
        formatter.setSyntaxPrefix("Usage:");
        final Preferences preferences = PreferencesFactory.get();
        final StringBuilder builder = new StringBuilder()
                .append("Default protocols")
                .append(StringUtils.LF);

        final ProtocolFactory protocols = ProtocolFactory.get();
        for(Protocol p : protocols.find(new DefaultProtocolPredicate(EnumSet.of(Protocol.Type.ftp, Protocol.Type.sftp, Protocol.Type.dav, Protocol.Type.smb)))) {
            append(p, builder);
        }
        builder.append(StringUtils.LF);
        for(Protocol p : protocols.find(new DefaultProtocolPredicate(EnumSet.of(Protocol.Type.s3, Protocol.Type.swift, Protocol.Type.azure, Protocol.Type.b2, Protocol.Type.googlestorage)))) {
            append(p, builder);
        }
        builder.append(StringUtils.LF);
        for(Protocol p : protocols.find(new DefaultProtocolPredicate(EnumSet.of(Protocol.Type.dropbox, Protocol.Type.onedrive, Protocol.Type.googledrive, Protocol.Type.nextcloud, Protocol.Type.owncloud, Protocol.Type.dracoon, Protocol.Type.brick)))) {
            append(p, builder);
        }
        builder.append("Local Disk");
        builder.append(StringUtils.LF);
        for(Protocol p : protocols.find(new DefaultProtocolPredicate(EnumSet.of(Protocol.Type.file)))) {
            append(p, builder);
        }
        builder.append(StringUtils.LF);
        builder.append(String.format("Third party connection profiles. Install additional connection profiles in %s",
                LocalFactory.get(SupportDirectoryFinderFactory.get().find(),
                        PreferencesFactory.get().getProperty("profiles.folder.name")).getAbbreviatedPath()));
        builder.append(StringUtils.LF);
        for(Protocol p : protocols.find(new ProfileProtocolPredicate())) {
            append(p, builder);
        }
        final StringBuilder header = new StringBuilder(StringUtils.LF)
                .append("\t")
                .append("URLs must be fully qualified. Paths can either denote "
                        + "a remote file (ftps://user@example.net/resource) or folder (ftps://user@example.net/directory/) "
                        + "with a trailing slash. You can reference files relative to your home directory with /~ (ftps://user@example.net/~/).")
                .append(StringUtils.LF)
                .append(builder)
                .append(StringUtils.LF)
                .append(StringUtils.LF);
        final StringBuilder footer = new StringBuilder(StringUtils.LF);
        footer.append(String.format("Cyberduck is libre software licenced under the GPL. For general help about using Cyberduck, please refer to %s and the wiki at %s. For bug reports or feature requests open a ticket at %s.",
                preferences.getProperty("website.cli"), preferences.getProperty("website.help"), MessageFormat.format(preferences.getProperty("website.bug"), preferences.getProperty("application.version"))));
        final License l = LicenseFactory.find();
        footer.append(StringUtils.LF);
        if(l.verify()) {
            footer.append(l.getEntitlement());
        }
        else {
            final StringAppender message = new StringAppender();
            message.append(LocaleFactory.localizedString("This is free software, but it still costs money to write, support, and distribute it. If you enjoy using it, please consider a donation to the authors of this software. It will help to make Cyberduck even better!", "Donate"));
            message.append(LocaleFactory.localizedString("As a contributor to Cyberduck, you receive a registration key that disables this prompt.", "Donate"));
            footer.append(message);
        }
        formatter.printHelp("duck [options...]", header.toString(), options, footer.toString());
    }

    private static void append(final Protocol protocol, final StringBuilder builder) {
        final String url;

        String format = "%s:";
        if(protocol.isHostnameConfigurable()) {
            if(StringUtils.isBlank(protocol.getDefaultHostname())) {
                format += "//<hostname>";
            }
            else {
                format += "(//<hostname>)";
            }
        }

        switch(protocol.getType()) {
            case b2:
            case s3:
            case googlestorage:
            case swift:
            case azure:
                format += "/<container>/<key>";
                break;
            default:
                format += "/<folder>/<file>";
                break;
        }
        url = String.format(format, getScheme(protocol));

        builder
                .append(String.format("%s %s", StringUtils.leftPad(protocol.getDescription(), 50), url))
                .append(StringUtils.LF);
    }

    protected static String getScheme(final Protocol protocol) {
        if(new BundledProtocolPredicate().test(protocol)) {
            for(String scheme :
                    protocol.getSchemes()) {
                // Return first custom scheme registered
                return scheme;
            }
            // Return default name
            return protocol.getIdentifier();
        }
        // Find parent protocol definition for profile
        final Protocol standard = ProtocolFactory.get().forName(protocol.getIdentifier());
        if(Arrays.equals(protocol.getSchemes(), standard.getSchemes())) {
            // No custom scheme set in profile
            return protocol.getProvider();
        }
        for(String scheme : protocol.getSchemes()) {
            // First custom scheme in profile
            return scheme;
        }
        // Default vendor string of third party profile
        return protocol.getProvider();
    }
}
