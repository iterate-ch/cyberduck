package ch.cyberduck.core.ctera;

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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.Acl;
import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.dav.DAVAttributesFinderFeature;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.sardine.DavResource;
import com.github.sardine.impl.SardineException;

import static ch.cyberduck.core.ctera.CteraAttributesFinderFeature.*;
import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class CteraAttributesFinderFeatureTest extends AbstractCteraTest {

    @Test(expected = NotfoundException.class)
    public void testFindNotFound() throws Exception {
        final Path test = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final DAVAttributesFinderFeature f = new CteraAttributesFinderFeature(session);
        f.find(test);
        fail();
    }

    @Test
    public void testFindFile() throws Exception {
        final Path root = new DefaultHomeFinderService(session).find();
        final DAVAttributesFinderFeature f = new CteraAttributesFinderFeature(session);
        final long rootTimestamp = f.find(root).getModificationDate();
        final String rootEtag = f.find(root).getETag();
        // No milliseconds precision
        Thread.sleep(1000L);
        final Path folder = new CteraDirectoryFeature(session).mkdir(new Path(root,
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertNotEquals(rootTimestamp, f.find(root).getModificationDate());
        assertNotEquals(rootEtag, f.find(root).getETag());
        final long folderTimestamp = f.find(folder).getModificationDate();
        final String folderEtag = f.find(folder).getETag();
        final Path test = new CteraTouchFeature(session).touch(new Path(folder,
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertEquals(folderTimestamp, f.find(folder).getModificationDate());
        assertEquals(folderEtag, f.find(folder).getETag());
        final PathAttributes attributes = f.find(test);
        assertTrue(attributes.getAcl().asList().stream().anyMatch(userAndRole -> userAndRole.getRole().equals(READPERMISSION)));
        assertTrue(attributes.getAcl().asList().stream().anyMatch(userAndRole -> userAndRole.getRole().equals(WRITEPERMISSION)));
        assertTrue(attributes.getAcl().asList().stream().anyMatch(userAndRole -> userAndRole.getRole().equals(DELETEPERMISSION)));
        assertEquals(0L, attributes.getSize());
        assertNotEquals(-1L, attributes.getModificationDate());
        assertNotNull(attributes.getETag());
        assertEquals(test.attributes().getFileId(), attributes.getFileId());
        assertNotNull(new CteraListService(session).list(folder, new DisabledListProgressListener()).find(new SimplePathPredicate(test)));
        assertEquals(attributes, new CteraListService(session).list(folder, new DisabledListProgressListener()).find(new SimplePathPredicate(test)).attributes());
        // Test wrong type
        assertThrows(NotfoundException.class, () -> f.find(new Path(test.getAbsolute(), EnumSet.of(Path.Type.directory))));
        new CteraDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testFindDirectory() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path test = new CteraDirectoryFeature(session).mkdir(new Path(home,
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final DAVAttributesFinderFeature f = new CteraAttributesFinderFeature(session);
        final PathAttributes attributes = f.find(test);
        assertTrue(attributes.getAcl().asList().stream().anyMatch(userAndRole -> userAndRole.getRole().equals(READPERMISSION)));
        assertTrue(attributes.getAcl().asList().stream().anyMatch(userAndRole -> userAndRole.getRole().equals(WRITEPERMISSION)));
        assertTrue(attributes.getAcl().asList().stream().anyMatch(userAndRole -> userAndRole.getRole().equals(CREATEDIRECTORIESPERMISSION)));
        assertNotEquals(-1L, attributes.getModificationDate());
        assertNotNull(attributes.getETag());
        assertEquals(test.attributes().getFileId(), attributes.getFileId());
        assertNotNull(new CteraListService(session).list(home, new DisabledListProgressListener()).find(new SimplePathPredicate(test)));
        assertEquals(attributes, new CteraListService(session).list(home, new DisabledListProgressListener()).find(new SimplePathPredicate(test)).attributes());
        // Test wrong type
        assertThrows(NotfoundException.class, () -> f.find(new Path(test.getAbsolute(), EnumSet.of(Path.Type.file))));
    }

    @Test
    public void testNoAccessAcl() throws Exception {
        final Path home = new Path("/ServicesPortal/webdav/Shared With Me/ACL test (Alex Berman)", EnumSet.of(AbstractPath.Type.directory));

        // list parent folder to inspect attributes
        final List<DavResource> noAccess = new CteraListService(session).list(home).stream().filter(r -> r.getName().equals("NoAccess")).collect(Collectors.toList());
        assertEquals(noAccess.size(), 1);
        assertEquals(
                Stream.of(
                        new AbstractMap.SimpleEntry<>("writepermission", "false"),
                        new AbstractMap.SimpleEntry<>("readpermission", "false"),
                        new AbstractMap.SimpleEntry<>("deletepermission", "false"),
                        new AbstractMap.SimpleEntry<>("createdirectoriespermission", "false"),
                        new AbstractMap.SimpleEntry<>("guid", "c51c40dc-1de0-441c-a6bf-3d07c0420329:1")
                ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)),
                noAccess.get(0).getCustomProps());
        assertEquals(new Acl(new Acl.CanonicalUser()), new CteraAttributesFinderFeature(session).toAttributes(noAccess.get(0)).getAcl());
        // find fails with 403 in backend
        final AccessDeniedException findException = assertThrows(AccessDeniedException.class, () -> new CteraAttributesFinderFeature(session).find(new Path(home, "NoAccess", EnumSet.of(AbstractPath.Type.directory))));
        assertTrue(findException.getCause() instanceof SardineException);
        assertEquals(403, ((SardineException) findException.getCause()).getStatusCode());
        // listing the folder fails with 403 in the backend
        final SardineException listException = assertThrows(SardineException.class, () -> new CteraListService(session).list(new Path(home, "NoAccess", EnumSet.of(AbstractPath.Type.directory))));
        assertEquals(403, listException.getStatusCode());
    }

    @Test
    public void testNoDeleteAcl() throws Exception {
        final Path home = new Path("/ServicesPortal/webdav/Shared With Me/ACL test (Alex Berman)", EnumSet.of(AbstractPath.Type.directory));
        final Path folder = new Path(home, "NoDelete", EnumSet.of(AbstractPath.Type.directory));
        final Acl folderAcl = new CteraAttributesFinderFeature(session).find(folder).getAcl();
        assertEquals(new Acl(new Acl.UserAndRole(new Acl.CanonicalUser(), READPERMISSION)), folderAcl);

        final Path file = new Path(folder, "RW no delete.txt", EnumSet.of(AbstractPath.Type.file));
        final Acl fileAcl = new CteraAttributesFinderFeature(session).find(file).getAcl();
        assertEquals(new Acl(new Acl.UserAndRole(new Acl.CanonicalUser(), READPERMISSION)), fileAcl);
    }

    @Test
    public void testReadOnlyAcl() throws Exception {
        final Path home = new Path("/ServicesPortal/webdav/Shared With Me/ACL test (Alex Berman)", EnumSet.of(AbstractPath.Type.directory));
        final Path folder = new Path(home, "ReadOnly", EnumSet.of(AbstractPath.Type.directory));
        final Acl folderAcl = new CteraAttributesFinderFeature(session).find(folder).getAcl();
        assertEquals(new Acl(new Acl.UserAndRole(new Acl.CanonicalUser(), READPERMISSION)), folderAcl);

        final Path file = new Path(folder, "ReadOnly.txt", EnumSet.of(AbstractPath.Type.file));
        final Acl fileAcl = new CteraAttributesFinderFeature(session).find(file).getAcl();
        assertEquals(new Acl(new Acl.UserAndRole(new Acl.CanonicalUser(), READPERMISSION)), fileAcl);
    }

    @Test
    public void testReadWriteAcl() throws Exception {
        final Path home = new Path("/ServicesPortal/webdav/Shared With Me/ACL test (Alex Berman)", EnumSet.of(AbstractPath.Type.directory));
        final Path folder = new Path(home, "ReadWrite", EnumSet.of(AbstractPath.Type.directory));
        final Acl folderAcl = new CteraAttributesFinderFeature(session).find(folder).getAcl();
        assertEquals(new Acl(
                new Acl.UserAndRole(new Acl.CanonicalUser(), READPERMISSION),
                new Acl.UserAndRole(new Acl.CanonicalUser(), WRITEPERMISSION),
                new Acl.UserAndRole(new Acl.CanonicalUser(), DELETEPERMISSION),
                new Acl.UserAndRole(new Acl.CanonicalUser(), CREATEDIRECTORIESPERMISSION)
        ), folderAcl);

        final Path file = new Path(folder, "Free Access.txt", EnumSet.of(AbstractPath.Type.file));
        final Acl fileAcl = new CteraAttributesFinderFeature(session).find(file).getAcl();
        assertEquals(new Acl(
                new Acl.UserAndRole(new Acl.CanonicalUser(), READPERMISSION),
                new Acl.UserAndRole(new Acl.CanonicalUser(), WRITEPERMISSION),
                new Acl.UserAndRole(new Acl.CanonicalUser(), DELETEPERMISSION)
        ), fileAcl);

        final Path subfolderNoAccess = new Path(folder, "NoAccess", EnumSet.of(AbstractPath.Type.directory));

        final BackgroundException noAccessException = assertThrows(BackgroundException.class, () -> new CteraAttributesFinderFeature(session).find(subfolderNoAccess));
        assertTrue(noAccessException.getCause() instanceof SardineException);

        final Path subfolderReadOnly = new Path(folder, "subfolder-readonly", EnumSet.of(AbstractPath.Type.directory));
        final Acl subfolderReadOnlyAcl = new CteraAttributesFinderFeature(session).find(subfolderReadOnly).getAcl();
        assertEquals(new Acl(new Acl.UserAndRole(new Acl.CanonicalUser(), READPERMISSION)), subfolderReadOnlyAcl);

        final Path readOnlyFile = new Path(subfolderReadOnly, "readonly.txt", EnumSet.of(AbstractPath.Type.file));
        final Acl readOnlyFileAcl = new CteraAttributesFinderFeature(session).find(readOnlyFile).getAcl();
        assertEquals(new Acl(new Acl.UserAndRole(new Acl.CanonicalUser(), READPERMISSION)), readOnlyFileAcl);
    }

    @Test
    public void testWORMAcl() throws Exception {
        final Path home = new Path("/ServicesPortal/webdav/Shared With Me", EnumSet.of(AbstractPath.Type.directory));
        final Path folder = new Path(home, "WORM test (Alex Berman)", EnumSet.of(AbstractPath.Type.directory));
        final Acl folderAcl = new CteraAttributesFinderFeature(session).find(folder).getAcl();
        assertEquals(new Acl(
                new Acl.UserAndRole(new Acl.CanonicalUser(), READPERMISSION),
                new Acl.UserAndRole(new Acl.CanonicalUser(), WRITEPERMISSION),
                new Acl.UserAndRole(new Acl.CanonicalUser(), DELETEPERMISSION),
                new Acl.UserAndRole(new Acl.CanonicalUser(), CREATEDIRECTORIESPERMISSION)
        ), folderAcl);

        final Path subfolder = new Path(folder, "Retention Folder (no write, no delete)", EnumSet.of(AbstractPath.Type.directory));
        final Acl subfolderAcl = new CteraAttributesFinderFeature(session).find(subfolder).getAcl();
        assertEquals(new Acl(
                new Acl.UserAndRole(new Acl.CanonicalUser(), READPERMISSION),
                new Acl.UserAndRole(new Acl.CanonicalUser(), CREATEDIRECTORIESPERMISSION)
        ), subfolderAcl);

        final Path file = new Path(subfolder, "long term retention.txt", EnumSet.of(AbstractPath.Type.file));
        final Acl fileAcl = new CteraAttributesFinderFeature(session).find(file).getAcl();
        assertEquals(new Acl(
                new Acl.UserAndRole(new Acl.CanonicalUser(), READPERMISSION)
        ), fileAcl);
    }

    @Test
    public void testWORMNoRetentionAcl() throws Exception {
        final Path home = new Path("/ServicesPortal/webdav/Shared With Me", EnumSet.of(AbstractPath.Type.directory));
        final Path folder = new Path(home, "WORM-NoRetention(Delete allowed) (Alex Berman)", EnumSet.of(AbstractPath.Type.directory));
        final Acl folderAcl = new CteraAttributesFinderFeature(session).find(folder).getAcl();
        assertEquals(new Acl(
                new Acl.UserAndRole(new Acl.CanonicalUser(), READPERMISSION),
                new Acl.UserAndRole(new Acl.CanonicalUser(), WRITEPERMISSION),
                new Acl.UserAndRole(new Acl.CanonicalUser(), DELETEPERMISSION),
                new Acl.UserAndRole(new Acl.CanonicalUser(), CREATEDIRECTORIESPERMISSION)
        ), folderAcl);

        final Path file = new Path(folder, "WORM-DeleteAllowed.txt", EnumSet.of(AbstractPath.Type.file));
        final Acl fileAcle = new CteraAttributesFinderFeature(session).find(file).getAcl();
        assertEquals(new Acl(
                new Acl.UserAndRole(new Acl.CanonicalUser(), READPERMISSION),
                new Acl.UserAndRole(new Acl.CanonicalUser(), DELETEPERMISSION)
        ), fileAcle);
    }
}
