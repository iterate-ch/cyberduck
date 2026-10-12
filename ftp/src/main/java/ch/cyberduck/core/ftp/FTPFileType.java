package ch.cyberduck.core.ftp;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.preferences.HostPreferencesFactory;
import ch.cyberduck.core.preferences.PreferencesReader;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTP;

import java.util.Arrays;
import java.util.Locale;

/**
 * The type of the data connection for the transfer of a file. Files are transferred as binary, which keeps every byte.
 * The FTP servers of some systems expect text files in the ASCII type, which converts the line breaks between the
 * systems. That changes the size of a file, so it is a choice of the user for a bookmark or for all bookmarks
 * (<code>ftp.transfer.mode</code>) and never the default.
 * <p>
 * It applies to uploads only. A download that is converted has fewer bytes than the size that the server tells, and the
 * transfer would never count as complete, so downloads stay binary.
 */
public final class FTPFileType {

    /**
     * Name of the setting: binary, ascii or auto
     */
    public static final String MODE = "ftp.transfer.mode";

    /**
     * Name of the setting with the file extensions that are transferred as ASCII in the auto mode, separated by commas
     */
    public static final String EXTENSIONS = "ftp.transfer.ascii.extensions";

    public static final String BINARY = "binary";
    public static final String ASCII = "ascii";
    public static final String AUTO = "auto";

    public static final String DEFAULT_EXTENSIONS = "txt,text,htm,html,css,js,json,xml,csv,tsv,php,pl,py,sh,cgi,inc,ini,conf,cfg,log,md,sql,htaccess,svg";

    private FTPFileType() {
        //
    }

    /**
     * @return The file type of the FTP protocol to set before the transfer of the file
     */
    public static int of(final Host host, final Path file) {
        final PreferencesReader preferences = HostPreferencesFactory.get(host);
        final String mode = StringUtils.defaultIfBlank(preferences.getProperty(MODE), BINARY).trim().toLowerCase(Locale.ROOT);
        switch(mode) {
            case ASCII:
                return FTP.ASCII_FILE_TYPE;
            case AUTO:
                final String extension = FilenameUtils.getExtension(file.getName()).toLowerCase(Locale.ROOT);
                // A file without an extension, such as .htaccess, is named by the name
                final String name = StringUtils.isEmpty(extension) ? StringUtils.removeStart(file.getName().toLowerCase(Locale.ROOT), ".") : extension;
                return Arrays.asList(StringUtils.defaultIfBlank(preferences.getProperty(EXTENSIONS), DEFAULT_EXTENSIONS).toLowerCase(Locale.ROOT).split("[,;\\s]+")).contains(name)
                    ? FTP.ASCII_FILE_TYPE : FTP.BINARY_FILE_TYPE;
            default:
                return FTP.BINARY_FILE_TYPE;
        }
    }
}
