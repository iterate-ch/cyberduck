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
import ch.cyberduck.core.NullFilter;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.ftp.FTPConnectMode;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FireFtpBookmarkCollection extends ThirdpartyBookmarkCollection {
    private static final Logger log = LogManager.getLogger(FireFtpBookmarkCollection.class);

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
    protected void parse(final ProtocolFactory protocols, final Local folder) throws AccessDeniedException {
        for(Local settings : folder.list().filter(new NullFilter<Local>() {
            @Override
            public boolean accept(Local file) {
                return file.isDirectory();
            }
        })) {
            for(Local child : settings.list().filter(new NullFilter<Local>() {
                @Override
                public boolean accept(Local file) {
                    if(file.isFile()) {
                        return "fireFTPsites.dat".equals(file.getName());
                    }
                    return false;
                }
            })) {
                this.read(protocols, child);
            }
        }
    }

    /**
     * Read invalid JSON format.
     */
    protected void read(final ProtocolFactory protocols, final Local file) throws AccessDeniedException {
        try (final BufferedReader in = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String l;
            while((l = in.readLine()) != null) {
                Matcher array = Pattern.compile("\\[(.*?)\\]").matcher(l);
                while(array.find()) {
                    Matcher entries = Pattern.compile("\\{(.*?)\\}").matcher(array.group(1));
                    while(entries.find()) {
                        final String entry = entries.group(1);
                        this.read(protocols, entry);
                    }
                }
            }
        }
        catch(IOException e) {
            throw new AccessDeniedException(e.getMessage(), e);
        }
    }

    private void read(final ProtocolFactory protocols, final String entry) {
        final Host current = new Host(protocols.forScheme(Scheme.ftp));
        current.getCredentials().setUsername(
            PreferencesFactory.get().getProperty("connection.login.anon.name"));
        for(String attribute : entry.split(", ")) {
            Scanner scanner = new Scanner(attribute);
            scanner.useDelimiter(":");
            if(!scanner.hasNext()) {
                log.warn("Missing key in line:{}", attribute);
                continue;
            }
            String name = scanner.next().toLowerCase(Locale.ROOT);
            if(!scanner.hasNext()) {
                log.warn("Missing value in line:{}", attribute);
                continue;
            }
            String value = scanner.next().replaceAll("\"", StringUtils.EMPTY);
            switch(name) {
                case "host":
                    current.setHostname(value);
                    break;
                case "port":
                    try {
                        current.setPort(Integer.parseInt(value));
                    }
                    catch(NumberFormatException e) {
                        log.warn("Invalid Port:{}", e.getMessage());
                    }
                    break;
                case "remotedir":
                    current.setDefaultPath(value);
                    break;
                case "webhost":
                    current.setWebURL(value);
                    break;
                case "encoding":
                    current.setEncoding(value);
                    break;
                case "notes":
                    current.setComment(value);
                    break;
                case "account":
                    current.setNickname(value);
                    break;
                case "privatekey":
                    current.getCredentials().setIdentity(LocalFactory.get(value));
                    break;
                case "pasvmode":
                    if(Boolean.TRUE.toString().equals(value)) {
                        current.setFTPConnectMode(FTPConnectMode.passive);
                    }
                    if(Boolean.FALSE.toString().equals(value)) {
                        current.setFTPConnectMode(FTPConnectMode.active);
                    }
                    break;
                case "login":
                    current.getCredentials().setUsername(value);
                    break;
                case "password":
                    current.getCredentials().setPassword(value);
                    break;
                case "anonymous":
                    if(Boolean.TRUE.toString().equals(value)) {
                        current.getCredentials().setUsername(
                            PreferencesFactory.get().getProperty("connection.login.anon.name"));
                    }
                    break;
                case "security":
                    if("authtls".equals(value)) {
                        current.setProtocol(protocols.forScheme(Scheme.ftps));
                        // Reset port to default
                        current.setPort(-1);
                    }
                    if("sftp".equals(value)) {
                        current.setProtocol(protocols.forScheme(Scheme.sftp));
                        // Reset port to default
                        current.setPort(-1);
                    }
                    break;
            }
        }
        this.add(current);
    }
}
