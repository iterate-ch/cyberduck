package ch.cyberduck.core.importer;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
 *
 * http://cyberduck.ch/
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
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.*;
import ch.cyberduck.core.ftp.FTPConnectMode;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @version $Id$
 */
public class FireFtpBookmarkCollection extends ThirdpartyBookmarkCollection {
    private static Logger log = Logger.getLogger(FireFtpBookmarkCollection.class);

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
        return LocalFactory.createLocal(Preferences.instance().getProperty("bookmark.import.fireftp.location"));
    }

    /**
     * FireFTP settings are in Firefox/Profiles/.*\.default/fireFTPsites.dat
     *
     * @param folder
     */
    @Override
    protected void parse(Local folder) {
        for(Local settings : folder.children(new PathFilter<Local>() {
            public boolean accept(Local file) {
                return file.attributes().isDirectory();
            }
        })) {
            for(Local child : settings.children(new PathFilter<Local>() {
                public boolean accept(Local file) {
                    if(file.attributes().isFile()) {
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
     *
     * @param file
     */
    private void read(Local file) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(file.getInputStream()));
            try {
                String l;
                while((l = in.readLine()) != null) {
                    Matcher array = Pattern.compile("\\[(.*?)\\]").matcher(l);
                    while(array.find()) {
                        Matcher entries = Pattern.compile("\\{(.*?)\\}").matcher(array.group(1));
                        while(entries.find()) {
                            String entry = entries.group(1);
                            Host current = new Host(Preferences.instance().getProperty("connection.hostname.default"));
                            current.getCredentials().setUsername(
                                    Preferences.instance().getProperty("connection.login.anon.name"));
                            current.setProtocol(Protocol.FTP);
                            for(String attribute : entry.split(", ")) {
                                Scanner scanner = new Scanner(attribute);
                                scanner.useDelimiter(":");
                                if(!scanner.hasNext()) {
                                    log.warn("Missing key in line:" + attribute);
                                    continue;
                                }
                                String name = scanner.next().toLowerCase();
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
                                    current.getCredentials().setIdentity(LocalFactory.createLocal(value));
                                }
                                else if("pasvmode".equals(name)) {
                                    if(Boolean.TRUE.toString().equals(value)) {
                                        current.setFTPConnectMode(FTPConnectMode.PASV);
                                    }
                                    if(Boolean.FALSE.toString().equals(value)) {
                                        current.setFTPConnectMode(FTPConnectMode.PORT);
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
                                                Preferences.instance().getProperty("connection.login.anon.name"));
                                    }
                                }
                                else if("security".equals(name)) {
                                    if("authtls".equals(value)) {
                                        current.setProtocol(Protocol.FTP_TLS);
                                    }
                                    if("sftp".equals(value)) {
                                        current.setProtocol(Protocol.SFTP);
                                    }
                                }
                            }
                            this.add(current);
                        }
                    }
                }
            }
            finally {
                IOUtils.closeQuietly(in);
            }
        }
        catch(IOException e) {
            log.error(e.getMessage());
        }
    }
}