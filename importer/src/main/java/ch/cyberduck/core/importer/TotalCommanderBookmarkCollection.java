package ch.cyberduck.core.importer;

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
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.ftp.FTPProtocol;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TotalCommanderBookmarkCollection extends ThirdpartyBookmarkCollection {
    private static final Logger log = Logger.getLogger(TotalCommanderBookmarkCollection.class);

    private static final long serialVersionUID = -1125641222323961118L;

    @Override
    public String getBundleIdentifier() {
        return "com.ghisler.totalcommander";
    }

    @Override
    public String getName() {
        return "Total Commander";
    }

    @Override
    public Local getFile() {
        return LocalFactory.get(PreferencesFactory.get().getProperty("bookmark.import.totalcommander.location"));
    }

    @Override
    protected void parse(final Local file) throws AccessDeniedException {
        try {
            final BufferedReader in
                    = new BufferedReader(new InputStreamReader(file.getInputStream(), Charset.forName("UTF-8")));
            try {
                Host current = null;
                String line;
                while((line = in.readLine()) != null) {
                    if(line.startsWith("[")) {
                        if(current != null) {
                            this.add(current);
                        }
                        current = new Host(new FTPProtocol(), PreferencesFactory.get().getProperty("connection.hostname.default"));
                        current.getCredentials().setUsername(
                                PreferencesFactory.get().getProperty("connection.login.anon.name"));
                        Pattern pattern = Pattern.compile("\\[(.*)\\]");
                        Matcher matcher = pattern.matcher(line);
                        if(matcher.matches()) {
                            current.setNickname(matcher.group(1));
                        }
                    }
                    else {
                        if(null == current) {
                            log.warn("Failed to detect start of bookmark");
                            continue;
                        }
                        final Scanner scanner = new Scanner(line);
                        scanner.useDelimiter("=");
                        if(!scanner.hasNext()) {
                            log.warn("Missing key in line:" + line);
                            continue;
                        }
                        final String name = scanner.next().toLowerCase(Locale.ROOT);
                        if(!scanner.hasNext()) {
                            log.warn("Missing value in line:" + line);
                            continue;
                        }
                        final String value = scanner.next();
                        if("host".equals(name)) {
                            current.setHostname(value);
                        }
                        else if("directory".equals(name)) {
                            current.setDefaultPath(value);
                        }
                        else if("username".equals(name)) {
                            current.getCredentials().setUsername(value);
                        }
                        else {
                            log.warn(String.format("Ignore property %s", name));
                        }
                    }
                }
                if(current != null) {
                    this.add(current);
                }
            }
            finally {
                IOUtils.closeQuietly(in);
            }
        }
        catch(IOException e) {
            throw new AccessDeniedException(e.getMessage(), e);
        }
    }

    @Override
    public boolean add(final Host bookmark) {
        if(!StringUtils.equals(bookmark.getHostname(), PreferencesFactory.get().getProperty("connection.hostname.default"))) {
            return super.add(bookmark);
        }
        return false;
    }
}
