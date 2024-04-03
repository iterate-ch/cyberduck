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
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.text.MessageFormat;
import java.util.EnumSet;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Category(IntegrationTest.class)
public class CteraCopyFeatureTest extends AbstractCteraTest {
    @Test
    public void testPreflightDirectoryMissingCustomProps() throws BackgroundException {
        final Path source = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        source.setAttributes(source.attributes().withAcl(Acl.EMPTY));
        final Path target = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        target.getParent().setAttributes(target.getParent().attributes().withAcl(Acl.EMPTY));
        new CteraCopyFeature(session).preflight(source, target);
    }

    @Test
    public void testPreflightFileMissingCustomProps() throws BackgroundException {
        final Path source = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        source.setAttributes(source.attributes().withAcl(Acl.EMPTY));
        final Path target = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        target.getParent().setAttributes(target.getParent().attributes().withAcl(Acl.EMPTY));
        new CteraCopyFeature(session).preflight(source, target);
    }

    @Test
    public void testPreflightDirectoryAccessDeniedTargetNoCreatedirectoriesPermissionCustomProps() throws BackgroundException {
        final Path source = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        source.setAttributes(source.attributes().withAcl(new Acl(new Acl.CanonicalUser(), CteraAttributesFinderFeature.READPERMISSION)));
        final Path target = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        // target's parent without createdirectories permission
        target.getParent().setAttributes(target.getParent().attributes().withAcl(new Acl(new Acl.CanonicalUser())));
        final AccessDeniedException accessDeniedException = assertThrows(AccessDeniedException.class, () -> new CteraCopyFeature(session).preflight(source, target));
        assertTrue(accessDeniedException.getDetail().contains(MessageFormat.format(LocaleFactory.localizedString("Cannot create folder {0}", "Error"), target.getName())));
    }

    @Test
    public void testPreflightDirectoryAccessDeniedTargetExistsNotWritablePermissionCustomProps() throws BackgroundException {
        final Path source = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        source.setAttributes(source.attributes().withAcl(new Acl(new Acl.CanonicalUser(), CteraAttributesFinderFeature.READPERMISSION)));
        final Path target = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        target.setAttributes(target.attributes().withAcl(new Acl(new Acl.CanonicalUser())));
        target.getParent().setAttributes(target.getParent().attributes().withAcl(new Acl(new Acl.CanonicalUser(), CteraAttributesFinderFeature.CREATEDIRECTORIESPERMISSION)));
        final CteraAttributesFinderFeature mock = mock(CteraAttributesFinderFeature.class);
        // target exists and not writable
        when(mock.find(eq(target))).thenReturn(new PathAttributes().withAcl(new Acl(new Acl.CanonicalUser())));
        final AccessDeniedException accessDeniedException = assertThrows(AccessDeniedException.class, () -> new CteraCopyFeature(session, mock).preflight(source, target));
        assertTrue(accessDeniedException.getDetail().contains(MessageFormat.format(LocaleFactory.localizedString("Upload {0} failed", "Error"), target.getName())));
    }

    @Test
    public void testPreflightFileAccessDeniedTargetExistsNotWritableCustomProps() throws BackgroundException {
        final Path source = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        source.setAttributes(source.attributes().withAcl(new Acl(new Acl.CanonicalUser(), CteraAttributesFinderFeature.READPERMISSION)));
        final Path target = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        target.setAttributes(target.attributes().withAcl(new Acl(new Acl.CanonicalUser())));
        // no createfilespermission required for now
        target.getParent().setAttributes(target.getParent().attributes().withAcl(new Acl(new Acl.CanonicalUser())));
        final CteraAttributesFinderFeature mock = mock(CteraAttributesFinderFeature.class);
        // target exists and not writable
        when(mock.find(eq(target))).thenReturn(new PathAttributes().withAcl(new Acl(new Acl.CanonicalUser())));
        final AccessDeniedException accessDeniedException = assertThrows(AccessDeniedException.class, () -> new CteraCopyFeature(session, mock).preflight(source, target));
        assertTrue(accessDeniedException.getDetail().contains(MessageFormat.format(LocaleFactory.localizedString("Upload {0} failed", "Error"), target.getName())));
    }

    @Test
    public void testPreflightDirectoryAccessGrantedCustomProps() throws BackgroundException {
        final Path source = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        source.setAttributes(source.attributes().withAcl(new Acl(new Acl.CanonicalUser(), CteraAttributesFinderFeature.DELETEPERMISSION)));
        final Path target = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        target.getParent().setAttributes(target.getParent().attributes().withAcl(new Acl(new Acl.CanonicalUser(), CteraAttributesFinderFeature.CREATEDIRECTORIESPERMISSION)));
        new CteraCopyFeature(session).preflight(source, target);
        // assert no fail
    }

    @Test
    public void testPreflightFileAccessGrantedCustomProps() throws BackgroundException {
        final Path source = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        source.setAttributes(source.attributes().withAcl(new Acl(new Acl.CanonicalUser(), CteraAttributesFinderFeature.DELETEPERMISSION)));
        final Path target = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        // no createfilespermission required for now
        target.getParent().setAttributes(target.getParent().attributes().withAcl(new Acl(new Acl.CanonicalUser())));
        new CteraCopyFeature(session).preflight(source, target);
        // assert no fail
    }
}