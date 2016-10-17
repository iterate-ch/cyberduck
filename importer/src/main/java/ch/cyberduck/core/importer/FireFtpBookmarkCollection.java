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

import ch.cyberduck.core.Filter;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.ftp.FTPConnectMode;
import ch.cyberduck.core.ftp.FTPProtocol;
import ch.cyberduck.core.ftp.FTPTLSProtocol;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.sftp.SFTPProtocol;

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

public class FireFtpBookmarkCollection extends ThirdpartyBookmarkCollection {
    private static final Logger log = Logger.getLogger(FireFtpBookmarkCollection.class);

    private static final long serialVersionUID = -1802799231453221690L;

    @Override
    public String getBundleIdentifier() {
        return "org.mozdev.fireftp";
    }

    @Override
    public String getName() {
        return "FireFTP";
    }

    @Override
    public Local getFile() {
        return LocalFactory.get(PreferencesFactory.get().getProperty("bookmark.import.fireftp.location"));
    }

    /**
     * FireFTP settings are in Firefox/Profiles/.*\.default/fireFTPsites.dat
     */
    @Override
    protected void parse(final Local folder) throws AccessDeniedException {
        for(Local settings : folder.list().filter(new Filter<Local>() {
            @Override
            public boolean accept(Local file) {
                return file.isDirectory();
            }
        })) {
            for(Local child : settings.list().filter(new Filter<Local>() {
                @Override
                public boolean accept(Local file) {
                    if(file.isFile()) {
                        return "fireFTPsites.dat".equals(file.getName());
                    }
                    return false;
                }
            })) {
                this.read(child);
            }
        }
    }

    /**
     * Read invalid JSON format.
     */
    protected void read(final Local file) throws AccessDeniedException {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(file.getInputStream(), Charset.forName("UTF-8")));
            try {
                String l;
                while((l = in.readLine()) != null) {
                    Matcher array = Pattern.compile("\\[(.*?)\\]").matcher(l);
                    while(array.find()) {
                        Matcher entries = Pattern.compile("\\{(.*?)\\}").matcher(array.group(1));
                        while(entries.find()) {
                            final String entry = entries.group(1);
                            this.read(entry);
                        }
                    }
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

    private void read(final String entry) {
        final Host current = new Host(new FTPProtocol(), PreferencesFactory.get().getProperty("connection.hostname.default"));
        current.getCredentials().setUsername(
                PreferencesFactory.get().getProperty("connection.login.anon.name"));
        for(String attribute : entry.split(", ")) {
            Scanner scanner = new Scanner(attribute);
            scanner.useDelimiter(":");
            if(!scanner.hasNext()) {
                log.warn("Missing key in line:" + attribute);
                continue;
            }
            String name = scanner.next().toLowerCase(Locale.ROOT);
            if(!scanner.hasNext()) {
                log.warn("Missing value in line:" + attribute);
                continue;
            }
            String value = scanner.next().replaceAll("\"", StringUtils.EMPTY);
            if("host".equals(name)) {
                current.setHostname(value);
            }
            else if("port".equals(name)) {
                try {
                    current.setPort(Integer.parseInt(value));
                }
                catch(NumberFormatException e) {
                    log.warn("Invalid Port:" + e.getMessage());
                }
            }
            else if("remotedir".equals(name)) {
                current.setDefaultPath(value);
            }
            else if("webhost".equals(name)) {
                current.setWebURL(value);
            }
            else if("encoding".equals(name)) {
                current.setEncoding(value);
            }
            else if("notes".equals(name)) {
                current.setComment(value);
            }
            else if("account".equals(name)) {
                current.setNickname(value);
            }
            else if("privatekey".equals(name)) {
                current.getCredentials().setIdentity(LocalFactory.get(value));
            }
            else if("pasvmode".equals(name)) {
                if(Boolean.TRUE.toString().equals(value)) {
                    current.setFTPConnectMode(FTPConnectMode.passive);
                }
                if(Boolean.FALSE.toString().equals(value)) {
                    current.setFTPConnectMode(FTPConnectMode.active);
                }
            }
            else if("login".equals(name)) {
                current.getCredentials().setUsername(value);
            }
            else if("password".equals(name)) {
                current.getCredentials().setPassword(value);
            }
            else if("anonymous".equals(name)) {
                if(Boolean.TRUE.toString().equals(value)) {
                    current.getCredentials().setUsername(
                            PreferencesFactory.get().getProperty("connection.login.anon.name"));
                }
            }
            else if("security".equals(name)) {
                if("authtls".equals(value)) {
                    current.setProtocol(new FTPTLSProtocol());
                    // Reset port to default
                    current.setPort(-1);
                }
                if("sftp".equals(value)) {
                    current.setProtocol(new SFTPProtocol());
                    // Reset port to default
                    current.setPort(-1);
                }
            }
        }
        this.add(current);
    }
}