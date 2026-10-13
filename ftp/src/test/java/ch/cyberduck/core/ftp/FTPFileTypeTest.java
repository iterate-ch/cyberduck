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
import ch.cyberduck.core.preferences.MemoryPreferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.net.ftp.FTP;
import org.junit.Before;
import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FTPFileTypeTest {

    @Before
    public void setup() {
        PreferencesFactory.set(new MemoryPreferences());
    }

    private static Path file(final String name) {
        return new Path("/" + name, EnumSet.of(Path.Type.file));
    }

    @Test
    public void testBinaryIsTheDefault() {
        final Host host = new Host(new FTPProtocol(), "example.net");
        assertEquals(FTP.BINARY_FILE_TYPE, FTPFileType.of(host, file("readme.txt")));
        assertEquals(FTP.BINARY_FILE_TYPE, FTPFileType.of(host, file("photo.jpg")));
    }

    @Test
    public void testAscii() {
        final Host host = new Host(new FTPProtocol(), "example.net").setProperty(FTPFileType.MODE, "ascii");
        assertEquals(FTP.ASCII_FILE_TYPE, FTPFileType.of(host, file("photo.jpg")));
    }

    @Test
    public void testAutoUsesTheExtension() {
        final Host host = new Host(new FTPProtocol(), "example.net").setProperty(FTPFileType.MODE, "auto");
        assertEquals(FTP.ASCII_FILE_TYPE, FTPFileType.of(host, file("readme.txt")));
        assertEquals(FTP.ASCII_FILE_TYPE, FTPFileType.of(host, file("INDEX.HTML")));
        assertEquals(FTP.ASCII_FILE_TYPE, FTPFileType.of(host, file(".htaccess")));
        assertEquals(FTP.BINARY_FILE_TYPE, FTPFileType.of(host, file("photo.jpg")));
        assertEquals(FTP.BINARY_FILE_TYPE, FTPFileType.of(host, file("archive")));
    }

    @Test
    public void testListOfExtensionsCanBeChanged() {
        PreferencesFactory.get().setProperty(FTPFileType.EXTENSIONS, "jpg; dat");
        final Host host = new Host(new FTPProtocol(), "example.net").setProperty(FTPFileType.MODE, "auto");
        assertEquals(FTP.ASCII_FILE_TYPE, FTPFileType.of(host, file("photo.jpg")));
        assertEquals(FTP.ASCII_FILE_TYPE, FTPFileType.of(host, file("x.dat")));
        assertEquals(FTP.BINARY_FILE_TYPE, FTPFileType.of(host, file("readme.txt")));
    }

    @Test
    public void testSettingOfAllBookmarksAndOfOne() {
        PreferencesFactory.get().setProperty(FTPFileType.MODE, "ascii");
        assertEquals(FTP.ASCII_FILE_TYPE, FTPFileType.of(new Host(new FTPProtocol(), "example.net"), file("a.bin")));
        // The bookmark wins
        assertEquals(FTP.BINARY_FILE_TYPE, FTPFileType.of(new Host(new FTPProtocol(), "example.net").setProperty(FTPFileType.MODE, "binary"), file("a.bin")));
    }

    @Test
    public void testNotAKnownModeIsBinary() {
        assertEquals(FTP.BINARY_FILE_TYPE, FTPFileType.of(new Host(new FTPProtocol(), "example.net").setProperty(FTPFileType.MODE, "ebcdic"), file("a.txt")));
    }

    @Test
    public void testFileOfVaultIsBinary() {
        final Host host = new Host(new FTPProtocol(), "example.net").setProperty(FTPFileType.MODE, "ascii");
        assertEquals(FTP.BINARY_FILE_TYPE, FTPFileType.of(host, new Path("/d/AB/readme.txt.c9r", EnumSet.of(Path.Type.file, Path.Type.encrypted))));
        assertEquals(FTP.BINARY_FILE_TYPE, FTPFileType.of(host.setProperty(FTPFileType.MODE, "auto"), new Path("/d/AB/readme.txt", EnumSet.of(Path.Type.file, Path.Type.encrypted))));
    }

    @Test
    public void testAsciiUploadIsNotResumed() throws Exception {
        final TransferStatus status = new TransferStatus().setExists(true).setLength(10L);
        assertFalse(new FTPUploadFeature(new FTPSession(new Host(new FTPProtocol(), "example.net").setProperty(FTPFileType.MODE, "ascii")))
            .append(file("readme.txt"), status).append);
        assertTrue(new FTPUploadFeature(new FTPSession(new Host(new FTPProtocol(), "example.net")))
            .append(file("readme.txt"), status).append);
    }
}
