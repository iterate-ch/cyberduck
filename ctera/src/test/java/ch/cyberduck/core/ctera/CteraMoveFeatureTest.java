package ch.cyberduck.core.ctera;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.dav.DAVFindFeature;
import ch.cyberduck.core.dav.DAVTimestampFeature;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.ConflictException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Category(IntegrationTest.class)
public class CteraMoveFeatureTest extends AbstractCteraTest {

    @Test
    public void testMove() throws Exception {
        final Path test = new CteraTouchFeature(session).touch(new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertEquals(0L, test.attributes().getSize());
        final TransferStatus status = new TransferStatus();
        new DAVTimestampFeature(session).setTimestamp(test, status.withModified(5000L));
        final PathAttributes attr = new CteraAttributesFinderFeature(session).find(test);
        final Path target = new CteraMoveFeature(session).move(test.withAttributes(status.getResponse()),
                new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new DAVFindFeature(session).find(test));
        assertTrue(new DAVFindFeature(session).find(target));
        assertEquals(status.getResponse(), target.attributes());
        assertEquals(attr, new CteraAttributesFinderFeature(session).find(target));
        new CteraDeleteFeature(session).delete(Collections.<Path>singletonList(target), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveDirectory() throws Exception {
        final Path folder = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new CteraDirectoryFeature(session).mkdir(folder, new TransferStatus());
        final Path test = new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        {
            final byte[] content = RandomUtils.nextBytes(3547);
            final TransferStatus status = new TransferStatus();
            status.setOffset(0L);
            status.setLength(1024L);
            final HttpResponseOutputStream<Void> out = new CteraWriteFeature(session).write(test, status, new DisabledConnectionCallback());
            // Write first 1024
            new StreamCopier(status, status).withOffset(status.getOffset()).withLimit(status.getLength()).transfer(new ByteArrayInputStream(content), out);
            out.close();
        }
        final PathAttributes attr = new CteraAttributesFinderFeature(session).find(test);
        final Path target = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new CteraMoveFeature(session).move(folder, target, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new DAVFindFeature(session).find(folder));
        assertFalse(new DAVFindFeature(session).find(test));
        assertTrue(new DAVFindFeature(session).find(target));
        assertTrue(new DAVFindFeature(session).find(new Path(target, test.getName(), EnumSet.of(Path.Type.file))));
        assertEquals(attr, new CteraAttributesFinderFeature(session).find(new Path(target, test.getName(), EnumSet.of(Path.Type.file))));
        assertEquals(attr.getModificationDate(), new CteraAttributesFinderFeature(session).find(new Path(target, test.getName(), EnumSet.of(Path.Type.file))).getModificationDate());
        assertEquals(attr.getFileId(), new CteraAttributesFinderFeature(session).find(new Path(target, test.getName(), EnumSet.of(Path.Type.file))).getFileId());
        // N.B. ETag should remain constant when moving a resource but Ctera may not be WebDAV compliant.
        new CteraDeleteFeature(session).delete(Collections.<Path>singletonList(target), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveOverride() throws Exception {
        final Path test = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new CteraTouchFeature(session).touch(test, new TransferStatus());
        final Path target = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new CteraTouchFeature(session).touch(target, new TransferStatus());
        assertThrows(ConflictException.class, () -> new CteraMoveFeature(session).move(test, target, new TransferStatus().exists(false), new Delete.DisabledCallback(), new DisabledConnectionCallback()));
        new CteraMoveFeature(session).move(test, target, new TransferStatus().exists(true), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new DAVFindFeature(session).find(test));
        assertTrue(new DAVFindFeature(session).find(target));
        new CteraDeleteFeature(session).delete(Collections.<Path>singletonList(target), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test(expected = NotfoundException.class)
    public void testMoveNotFound() throws Exception {
        final Path test = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new CteraMoveFeature(session).move(test, new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
    }

    @Test
    public void testPreflightDirectoryMissingCustomProps() throws Exception {
        final Path source = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        source.setAttributes(source.attributes().withAcl(Acl.EMPTY));
        final Path target = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        target.setAttributes(target.attributes().withAcl(Acl.EMPTY));
        new CteraMoveFeature(session).preflight(source, target.getParent(), target.getName());
    }

    @Test
    public void testPreflightFileMissingCustomProps() throws Exception {
        final Path source = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        source.setAttributes(source.attributes().withAcl(Acl.EMPTY));
        final Path target = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        target.setAttributes(target.attributes().withAcl(Acl.EMPTY));
        new CteraMoveFeature(session).preflight(source, target.getParent(), target.getName());
    }

    @Test
    public void testPreflightDirectoryAccessDeniedSourceNoDeletePermissionCustomProps() throws Exception {
        final Path source = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        // source without deletepermission
        source.setAttributes(source.attributes().withAcl(new Acl(new Acl.CanonicalUser())));
        final Path target = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        target.getParent().setAttributes(target.getParent().attributes().withAcl(new Acl(new Acl.CanonicalUser())));
        final AccessDeniedException accessDeniedException = assertThrows(AccessDeniedException.class, () -> new CteraMoveFeature(session).preflight(source, target.getParent(), target.getName()));
        assertTrue(accessDeniedException.getDetail().contains(MessageFormat.format(LocaleFactory.localizedString("Cannot delete {0}", "Error"), source.getName())));
    }

    @Test
    public void testPreflightDirectoryAccessDeniedTargetNoCreatedirectoriesPermissionCustomProps() throws Exception {
        final Path source = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        source.setAttributes(source.attributes().withAcl(new Acl(new Acl.CanonicalUser(), CteraAttributesFinderFeature.DELETEPERMISSION)));
        final Path target = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        // target's parent without createdirectories permission
        target.getParent().setAttributes(target.getParent().attributes().withAcl(new Acl(new Acl.CanonicalUser())));
        final AccessDeniedException accessDeniedException = assertThrows(AccessDeniedException.class, () -> new CteraMoveFeature(session).preflight(source, target.getParent(), target.getName()));
        assertTrue(accessDeniedException.getDetail().contains(MessageFormat.format(LocaleFactory.localizedString("Cannot create folder {0}", "Error"), target.getName())));
    }

    @Test
    public void testPreflightDirectoryAccessDeniedTargetExistsNotWritablePermissionCustomProps() throws Exception {
        final Path source = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        source.setAttributes(source.attributes().withAcl(new Acl(new Acl.CanonicalUser(), CteraAttributesFinderFeature.DELETEPERMISSION)));
        final Path target = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        target.setAttributes(target.attributes().withAcl(new Acl(new Acl.CanonicalUser())));
        target.getParent().setAttributes(target.getParent().attributes().withAcl(new Acl(new Acl.CanonicalUser(), CteraAttributesFinderFeature.CREATEDIRECTORIESPERMISSION)));
        final CteraAttributesFinderFeature mock = mock(CteraAttributesFinderFeature.class);
        // target exists and not writable
        when(mock.find(eq(target))).thenReturn(new PathAttributes().withAcl(new Acl(new Acl.CanonicalUser())));
        final AccessDeniedException accessDeniedException = assertThrows(AccessDeniedException.class, () -> new CteraMoveFeature(session, mock).preflight(source, target.getParent(), target.getName()));
        assertTrue(accessDeniedException.getDetail().contains(MessageFormat.format(LocaleFactory.localizedString("Upload {0} failed", "Error"), target.getName())));
    }

    @Test
    public void testPreflightFileAccessDeniedSourceNoDeletePermissionCustomProps() throws Exception {
        final Path source = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        // source without deletepermission
        source.setAttributes(source.attributes().withAcl(new Acl(new Acl.CanonicalUser())));
        final Path target = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        target.getParent().setAttributes(target.getParent().attributes().withAcl(new Acl(new Acl.CanonicalUser())));
        final AccessDeniedException accessDeniedException = assertThrows(AccessDeniedException.class, () -> new CteraMoveFeature(session).preflight(source, target.getParent(), target.getName()));
        assertTrue(accessDeniedException.getDetail().contains(MessageFormat.format(LocaleFactory.localizedString("Cannot delete {0}", "Error"), source.getName())));
    }

    @Test
    public void testPreflightFileAccessDeniedTargetExistsNotWritableCustomProps() throws Exception {
        final Path source = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        source.setAttributes(source.attributes().withAcl(new Acl(new Acl.CanonicalUser(), CteraAttributesFinderFeature.DELETEPERMISSION)));
        final Path target = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        target.setAttributes(target.attributes().withAcl(new Acl(new Acl.CanonicalUser())));
        // no createfilespermission required for now
        target.getParent().setAttributes(target.getParent().attributes().withAcl(new Acl(new Acl.CanonicalUser())));
        final CteraAttributesFinderFeature mock = mock(CteraAttributesFinderFeature.class);
        // target exists and not writable
        when(mock.find(eq(target))).thenReturn(new PathAttributes().withAcl(new Acl(new Acl.CanonicalUser())));
        final AccessDeniedException accessDeniedException = assertThrows(AccessDeniedException.class, () -> new CteraMoveFeature(session, mock).preflight(source, target.getParent(), target.getName()));
        assertTrue(accessDeniedException.getDetail().contains(MessageFormat.format(LocaleFactory.localizedString("Upload {0} failed", "Error"), target.getName())));
    }

    @Test
    public void testPreflightDirectoryAccessGrantedCustomProps() throws Exception {
        final Path source = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        source.setAttributes(source.attributes().withAcl(new Acl(new Acl.CanonicalUser(), CteraAttributesFinderFeature.DELETEPERMISSION)));
        final Path target = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        target.setAttributes(target.attributes().withAcl(new Acl(new Acl.CanonicalUser(), CteraAttributesFinderFeature.WRITEPERMISSION)));
        source.getParent().setAttributes(source.getParent().attributes().withAcl(new Acl(new Acl.CanonicalUser(), CteraAttributesFinderFeature.CREATEDIRECTORIESPERMISSION)));
        new CteraMoveFeature(session).preflight(source, target.getParent(), target.getName());
        // assert no fail
    }

    @Test
    public void testPreflightFileAccessGrantedCustomProps() throws Exception {
        final Path source = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        source.setAttributes(source.attributes().withAcl(new Acl(new Acl.CanonicalUser(), CteraAttributesFinderFeature.DELETEPERMISSION)));
        final Path target = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        target.setAttributes(target.attributes().withAcl(new Acl(new Acl.CanonicalUser(), CteraAttributesFinderFeature.WRITEPERMISSION)));
        new CteraMoveFeature(session).preflight(source, target.getParent(), target.getName());
        // assert no fail
    }
}
