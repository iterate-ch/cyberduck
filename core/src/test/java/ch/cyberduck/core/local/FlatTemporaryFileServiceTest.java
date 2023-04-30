package ch.cyberduck.core.local;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.File;
import java.util.EnumSet;

import static org.junit.Assert.*;

public class FlatTemporaryFileServiceTest {

    @Test
    public void testExists() {
        {
            final Local f = new FlatTemporaryFileService().create(new AlphanumericRandomStringService().random());
            assertFalse(f.exists());
            assertTrue(f.getParent().exists());
            assertTrue(f.getParent().getParent().exists());
        }
        {
            final Path file = new Path("/p/f", EnumSet.of(Path.Type.file));
            final Local f = new FlatTemporaryFileService().create(new AlphanumericRandomStringService().random(), file);
            assertFalse(f.exists());
            assertTrue(f.getParent().exists());
            assertTrue(f.getParent().getParent().exists());
        }
    }

    @Test
    public void testCreateFile() {
        final String temp = StringUtils.removeEnd(System.getProperty("java.io.tmpdir"), File.separator);
        final String s = System.getProperty("file.separator");
        assertEquals(String.format("%s%su%s887503681-f", temp, s, s),
                new FlatTemporaryFileService().create("u", new Path("/p/f", EnumSet.of(Path.Type.file))).getAbsolute());
        final Path file = new Path("/p/f", EnumSet.of(Path.Type.file));
        file.attributes().setRegion("region");
        assertEquals(String.format("%s%su%s887503681-f", temp, s, s),
                new FlatTemporaryFileService().create("u", file).getAbsolute());
    }

    @Test
    public void testCreateContainer() {
        final String temp = StringUtils.removeEnd(System.getProperty("java.io.tmpdir"), File.separator);
        final String s = System.getProperty("file.separator");
        final Path file = new Path("/container", EnumSet.of(Path.Type.directory));
        file.attributes().setRegion("region");
        assertEquals(String.format("%s%su%s887503681-container", temp, s, s),
                new FlatTemporaryFileService().create("u", file).getAbsolute());
    }

    @Test
    public void testPathTooLong() {
        final String temp = StringUtils.removeEnd(System.getProperty("java.io.tmpdir"), File.separator);
        final String testPathDirectory = "/Lorem/ipsum/dolor/sit/amet/consetetur/sadipscing/elitr/sed/diam/nonumy/eirmod/tempor/invidunt/ut/labore/et/dolore/magna/aliquyam/erat/sed/diam/voluptua/At/vero/eos/et/accusam/et/justo/duo/dolores/et/ea/rebum/Stet/clita/kasd/gubergren/no/sea";
        final String testPathFile = "takimata.sanc";
        final String testPath = String.format("%s/%s", testPathDirectory, testPathFile);

        final Path file = new Path(testPath, EnumSet.of(Path.Type.file));
        file.attributes().setVersionId("2");
        final Local local = new FlatTemporaryFileService().create("UID", file);
        assertTrue(local.getParent().exists());
        final String localFile = local.getAbsolute();
        assertNotEquals(String.format("%s/%s%s/2/%s", temp, "UID", testPathDirectory, testPathFile).replace('/', File.separatorChar), localFile);
    }

    @Test
    public void testPathNotTooLong() {
        final String temp = StringUtils.removeEnd(System.getProperty("java.io.tmpdir"), File.separator);
        final String testPathDirectory = "/Lorem/ipsum/dolor/sit/amet/consetetur/sadipscing/elitr/sed/diam/nonumy/eirmod/tempor";
        final String testPathFile = "takimata.sanc";
        final String testPath = String.format("%s/%s", testPathDirectory, testPathFile);
        final String testPathMD5 = DigestUtils.md5Hex(testPathDirectory);

        Path file = new Path(testPath, EnumSet.of(Path.Type.file));
        file.attributes().setVersionId("2");
        final Local local = new FlatTemporaryFileService().create("UID", file);
        assertTrue(local.getParent().exists());
        final String localFile = local.getAbsolute();
        assertEquals(String.format("%s/%s/887551731-%s", temp, "UID", testPathFile).replace('/', File.separatorChar), localFile);
        assertNotEquals(String.format("%s/%s%s/2/887551731-%s", temp, "UID", testPathMD5, testPathFile).replace('/', File.separatorChar), localFile);
    }

    @Test
    public void testTemporaryPath() {
        final Path file = new Path("/f1/f2/t.txt", EnumSet.of(Path.Type.file));
        file.attributes().setDuplicate(true);
        file.attributes().setVersionId("1");
        final Local local = new FlatTemporaryFileService().create(file);
        assertTrue(local.getParent().exists());
        assertEquals("t.txt", file.getName());
        assertNotEquals("t.txt", local.getName());
        assertTrue(local.getName().endsWith("-t.txt"));
        assertEquals(LocalFactory.get(PreferencesFactory.get().getProperty("tmp.dir")), LocalFactory.get(local.getParent().getAbsolute()));
    }

    @Test
    public void testTemporaryPathCustomPrefix() {
        final Path file = new Path("/f1/f2/t.txt", EnumSet.of(Path.Type.file));
        file.attributes().setDuplicate(true);
        file.attributes().setVersionId("1");
        final Local local = new FlatTemporaryFileService().create("u", file);
        assertTrue(local.getParent().exists());
        assertEquals("t.txt", file.getName());
        assertTrue(local.getName().endsWith("-t.txt"));
        assertEquals(LocalFactory.get(PreferencesFactory.get().getProperty("tmp.dir"), "u"), LocalFactory.get(local.getParent().getAbsolute()));
    }
}