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
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import static org.junit.Assert.assertThrows;

@Category(IntegrationTest.class)
public class CteraCopyFeatureTest extends AbstractCteraTest {
    @Test
    public void testPreflightDirectoryMissingCustomProps() throws BackgroundException {
        final Path source = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        source.getParent().setAttributes(source.attributes().withAcl(Acl.EMPTY));
        final Path target = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        target.getParent().setAttributes(target.getParent().attributes().withAcl(Acl.EMPTY));
        new CteraCopyFeature(session).preflight(source, target);
    }

    @Test
    public void testPreflightFileMissingCustomProps() throws BackgroundException {
        final Path source = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        source.getParent().setAttributes(source.attributes().withAcl(Acl.EMPTY));
        final Path target = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        target.getParent().setAttributes(target.getParent().attributes().withAcl(Acl.EMPTY));
        new CteraCopyFeature(session).preflight(source, target);
    }

    @Test
    public void testPreflightDirectoryAccessDeniedCustomProps() throws BackgroundException {
        final Path source = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        source.getParent().setAttributes(source.attributes().withAcl(new Acl(new Acl.CanonicalUser(), CteraAttributesFinderFeature.READPERMISSION)));
        final Path target = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        target.getParent().setAttributes(target.getParent().attributes().withAcl(new Acl(new Acl.CanonicalUser())));
        assertThrows(AccessDeniedException.class, () -> new CteraCopyFeature(session).preflight(source, target));
    }

    @Test
    public void testPreflightFiledAccessDeniedCustomProps() throws BackgroundException {
        final Path source = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        source.getParent().setAttributes(source.attributes().withAcl(new Acl(new Acl.CanonicalUser(), CteraAttributesFinderFeature.READPERMISSION)));
        final Path target = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        target.getParent().setAttributes(target.getParent().attributes().withAcl(new Acl(new Acl.CanonicalUser())));
        assertThrows(AccessDeniedException.class, () -> new CteraCopyFeature(session).preflight(source, target));
    }

    @Test
    public void testPreflightDirectoryAccessGrantedCustomProps() throws BackgroundException {
        final Path source = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        source.getParent().setAttributes(source.attributes().withAcl(new Acl(new Acl.CanonicalUser(), CteraAttributesFinderFeature.DELETEPERMISSION)));
        final Path target = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        target.getParent().setAttributes(target.getParent().attributes().withAcl(new Acl(new Acl.CanonicalUser(), CteraAttributesFinderFeature.CREATEDIRECTORIESPERMISSION)));
        new CteraCopyFeature(session).preflight(source, target);
        // assert no fail
    }

    @Test
    public void testPreflightFileAccessGrantedMinimalCustomProps() throws BackgroundException {
        final Path source = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        source.getParent().setAttributes(source.attributes().withAcl(new Acl(new Acl.CanonicalUser(), CteraAttributesFinderFeature.DELETEPERMISSION)));
        final Path target = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        target.getParent().setAttributes(target.getParent().attributes().withAcl(new Acl(new Acl.CanonicalUser(), CteraAttributesFinderFeature.CREATEFILEPERMISSION)));
        new CteraCopyFeature(session).preflight(source, target);
        // assert no fail
    }
}