package ch.cyberduck.core.worker;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.PasswordStoreFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.s3.S3AccessControlListFeature;
import ch.cyberduck.core.s3.S3FindFeature;
import ch.cyberduck.core.s3.S3MetadataFeature;
import ch.cyberduck.core.s3.S3Protocol;
import ch.cyberduck.core.s3.S3Session;
import ch.cyberduck.core.s3.S3TouchFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class MoveWorkerTest {

    @Test
    public void testMoveFile() throws Exception {
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(), new Credentials(
            System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
        ));
        final S3Session session = new S3Session(host);
        session.open(new DisabledHostKeyCallback(), new DisabledLoginCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path home = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path source = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Path target = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new S3TouchFeature(session).touch(source, new TransferStatus().withMime("application/cyberduck"));
        new S3AccessControlListFeature(session).setPermission(source, new Acl(
            new Acl.UserAndRole(
                new Acl.CanonicalUser("80b9982b7b08045ee86680cc47f43c84bf439494a89ece22b5330f8a49477cf6"), new Acl.Role(Acl.Role.FULL)
            ),
            new Acl.UserAndRole(
                new Acl.GroupUser("http://acs.amazonaws.com/groups/global/AllUsers"), new Acl.Role(Acl.Role.READ)
            )
        ));
        assertTrue(new S3FindFeature(session).find(source));
        final MoveWorker worker = new MoveWorker(Collections.singletonMap(source, target), PathCache.empty(), PasswordStoreFactory.get(), new DisabledLoginCallback(), new DisabledHostKeyCallback(), new DisabledProgressListener(), new DisabledTranscriptListener());
        worker.run(session);
        assertFalse(new S3FindFeature(session).find(source));
        assertTrue(new S3FindFeature(session).find(target));
        assertEquals("application/cyberduck",
            new S3MetadataFeature(session, new S3AccessControlListFeature(session)).getMetadata(target).get("Content-Type"));
        assertTrue(new S3AccessControlListFeature(session).getPermission(target).asList().contains(
            new Acl.UserAndRole(
                new Acl.CanonicalUser("80b9982b7b08045ee86680cc47f43c84bf439494a89ece22b5330f8a49477cf6"), new Acl.Role(Acl.Role.FULL)
            )
        ));
        assertTrue(new S3AccessControlListFeature(session).getPermission(target).asList().contains(
            new Acl.UserAndRole(
                new Acl.GroupUser("http://acs.amazonaws.com/groups/global/AllUsers"), new Acl.Role(Acl.Role.READ)
            )
        ));
        new DeleteWorker(new DisabledLoginCallback(), Collections.singletonList(target), PathCache.empty(), new DisabledProgressListener()).run(session);
        session.close();
    }
}
