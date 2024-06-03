package ch.cyberduck.core.ctera;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import static ch.cyberduck.core.ctera.CteraAttributesFinderFeature.CREATEDIRECTORIESPERMISSION;
import static ch.cyberduck.core.ctera.CteraAttributesFinderFeature.WRITEPERMISSION;
import static org.junit.Assert.assertThrows;

@Category(IntegrationTest.class)
public class CteraTouchFeatureTest extends AbstractCteraTest {

    @Test
    public void testPreflightFileMissingCustomProps() throws Exception {
        final Path file = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        file.setAttributes(file.attributes().withAcl(Acl.EMPTY));
        new CteraTouchFeature(session).preflight(file, new AlphanumericRandomStringService().random());
    }

    @Test
    public void testPreflightReadPermission() throws Exception {
        final Path file = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        file.setAttributes(file.attributes().withAcl(new Acl(new Acl.CanonicalUser(), CteraAttributesFinderFeature.READPERMISSION)));
        assertThrows(AccessDeniedException.class, () -> new CteraTouchFeature(session).preflight(file, new AlphanumericRandomStringService().random()));
    }

    @Test
    public void testPreflightNoPermissions() throws Exception {
        final Path file = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        file.setAttributes(file.attributes().withAcl(new Acl(new Acl.CanonicalUser())));
        assertThrows(AccessDeniedException.class, () -> new CteraTouchFeature(session).preflight(file, new AlphanumericRandomStringService().random()));
    }

    @Test
    public void testPreflightWritePermission() throws Exception {
        final Path file = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        file.setAttributes(file.attributes().withAcl(new Acl(new Acl.CanonicalUser(), CteraAttributesFinderFeature.WRITEPERMISSION)));
        new CteraTouchFeature(session).preflight(file, new AlphanumericRandomStringService().random());
        // assert no fail
    }

    @Test
    public void testPreflightCreateDirectoriesPermission() throws Exception {
        final Path file = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        file.setAttributes(file.attributes().withAcl(new Acl(new Acl.CanonicalUser(), CteraAttributesFinderFeature.CREATEDIRECTORIESPERMISSION)));
        new CteraTouchFeature(session).preflight(file, new AlphanumericRandomStringService().random());
        // assert no fail
    }

    @Test
    public void testPreflightWriteAndCreateDirectoriesPermission() throws Exception {
        final Path file = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        file.setAttributes(file.attributes().withAcl(
                new Acl(
                        new Acl.UserAndRole(new Acl.CanonicalUser(), WRITEPERMISSION),
                        new Acl.UserAndRole(new Acl.CanonicalUser(), CREATEDIRECTORIESPERMISSION)
                )));
        new CteraTouchFeature(session).preflight(file, new AlphanumericRandomStringService().random());
        // assert no fail
    }

    @Test
    public void testPreflightFileAccessGrantedCustomProps() throws Exception {
        final Path file = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new CteraTouchFeature(session).preflight(file, new AlphanumericRandomStringService().random());
        // assert no fail
    }
}