package ch.cyberduck.core.cryptomator;

/*
 * Copyright (c) 2002-2025 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.*;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Bulk;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.proxy.ProxyFactory;
import ch.cyberduck.core.s3.S3BucketCreateService;
import ch.cyberduck.core.s3.S3Protocol;
import ch.cyberduck.core.s3.S3Session;
import ch.cyberduck.core.shared.DefaultPathHomeFeature;
import ch.cyberduck.core.sts.AbstractAssumeRoleWithWebIdentityTest;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.DefaultVaultRegistry;
import ch.cyberduck.core.vault.VaultRegistry;
import ch.cyberduck.core.worker.DeleteWorker;
import ch.cyberduck.test.TestcontainerTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.cryptomator.cryptolib.api.UVFMasterkey;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

/**
 * Test {@link UVFVault} implementation against test data from
 * <a href="https://github.com/cryptomator/cryptolib/tree/develop/src/test/java/org/cryptomator/cryptolib/v3/UVFIntegrationTest.java">org.cryptomator.cryptolib.v3.UVFIntegrationTest</a>
 */
@Category(TestcontainerTest.class)
public class UVFIntegrationTest {

    private static final ComposeContainer container = new ComposeContainer(
            new File(AbstractAssumeRoleWithWebIdentityTest.class.getResource("/uvf/docker-compose.yml").getFile()))
            .withPull(false)
//            .withLocalCompose(true)
            .withEnv(
                    Stream.of(
                            new AbstractMap.SimpleImmutableEntry<>("MINIO_PORT", "9000"),
                            new AbstractMap.SimpleImmutableEntry<>("MINIO_CONSOLE_PORT", "9001")
                    ).collect(Collectors.toMap(AbstractMap.SimpleImmutableEntry::getKey, AbstractMap.SimpleImmutableEntry::getValue)))
            .withExposedService("minio-1", 9000, Wait.forListeningPort());


    @Test
    public void listMinio() throws BackgroundException, IOException {
        final String bucketName = "cyberduckbucket";

        final Host bookmark = getMinIOBookmark();
        final S3Session storage = getS3SessionForBookmark(bookmark);

        final Path bucket = new Path(bucketName, EnumSet.of(AbstractPath.Type.directory));
        new S3BucketCreateService(storage).create(bucket, "us-east-1");

        final List<String> files = Arrays.asList(
                "/d/RZ/K7ZH7KBXULNEKBMGX3CU42PGUIAIX4/rExOms183v5evFwgIKiW0qvbsor1Hg==.uvf/dir.uvf", // -> /subir
                "/d/RZ/K7ZH7KBXULNEKBMGX3CU42PGUIAIX4/dir.uvf", // -> /
                "/d/RZ/K7ZH7KBXULNEKBMGX3CU42PGUIAIX4/GsMMTRvsuuP_6NjgRwopmWcuof-PyRQ=.uvf", // -> /foo.txt
                "/d/6L/HPWBEU3OJP2EZUCP4CV3HHL47BXVEX/5qTOPMA1BouBRhz_G7qfmKety92geI4=.uvf", // -> /subdir/bar.txt
                "/d/6L/HPWBEU3OJP2EZUCP4CV3HHL47BXVEX/dir.uvf" // /subdir
        );
        final String jwe = "{\n" +
                "    \"fileFormat\": \"AES-256-GCM-32k\",\n" +
                "    \"nameFormat\": \"AES-SIV-512-B64URL\",\n" +
                "    \"seeds\": {\n" +
                "        \"HDm38g\": \"ypeBEsobvcr6wjGzmiPcTaeG7/gUfE5yuYB3ha/uSLs=\",\n" +
                "        \"gBryKw\": \"PiPoFgA5WUoziU9lZOGxNIu9egCI1CxKy3PurtWcAJ0=\",\n" +
                "        \"QBsJFg\": \"Ln0sA6lQeuJl7PW1NWiFpTOTogKdJBOUmXJloaJa78Y=\"\n" +
                "    },\n" +
                "    \"initialSeed\": \"HDm38i\",\n" +
                "    \"latestSeed\": \"QBsJFo\",\n" +
                "    \"kdf\": \"HKDF-SHA512\",\n" +
                "    \"kdfSalt\": \"NIlr89R7FhochyP4yuXZmDqCnQ0dBB3UZ2D+6oiIjr8=\",\n" +
                "    \"org.example.customfield\": 42\n" +
                "}";

        try {
            for(final String fi : files) {
                final Path file = new Path("/" + bucketName + "/" + fi, EnumSet.of(AbstractPath.Type.file));
                byte[] content = new byte[1000];
                final int size;
                try(final InputStream in = UVFIntegrationTest.class.getResourceAsStream("/uvf/first_vault" + fi)) {
                    size = in.read(content);
                }
                final TransferStatus transferStatus = new TransferStatus().setLength(size);
                transferStatus.setChecksum(storage.getFeature(Write.class).checksum(file, transferStatus).compute(new ByteArrayInputStream(content), transferStatus));
                storage.getFeature(Bulk.class).pre(Transfer.Type.upload, Collections.singletonMap(new TransferItem(file), transferStatus), new DisabledConnectionCallback());
                final StatusOutputStream<?> out = storage.getFeature(Write.class).write(file, transferStatus, new DisabledConnectionCallback());
                IOUtils.copyLarge(UVFIntegrationTest.class.getResourceAsStream("/uvf/first_vault" + fi), out);
                out.close();
            }

            final VaultRegistry vaults = new DefaultVaultRegistry(new DisabledPasswordCallback());
            bookmark.setDefaultPath("/" + bucketName);
            final UVFVault vault = new UVFVault(new DefaultPathHomeFeature(bookmark).find());
            vaults.add(vault.load(storage, new DisabledPasswordCallback() {
                @Override
                public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) {
                    return new Credentials().withPassword(jwe);
                }
            }));
            final PathAttributes attr = storage.getFeature(AttributesFinder.class).find(vault.getHome());
            storage.withRegistry(vaults);
            try(final UVFMasterkey masterKey = UVFMasterkey.fromDecryptedPayload(jwe)) {
                assertArrayEquals(masterKey.rootDirId(), vault.getRootDirId());
            }

            final Path home = vault.getHome().withAttributes(attr).withType(EnumSet.of(AbstractPath.Type.directory, AbstractPath.Type.vault));
            {
                final AttributedList<Path> list = storage.getFeature(ListService.class).list(home, new DisabledListProgressListener());
                assertEquals(
                        new HashSet<>(Arrays.asList(
                                new Path("/cyberduckbucket/subdir", EnumSet.of(AbstractPath.Type.directory, AbstractPath.Type.placeholder, AbstractPath.Type.decrypted)),
                                new Path("/cyberduckbucket/foo.txt", EnumSet.of(AbstractPath.Type.file, AbstractPath.Type.decrypted)))
                        ),
                        new HashSet<>(list.toList()));
                assertEquals("Hello Foo", readFile(storage, new Path("/cyberduckbucket/foo.txt", EnumSet.of(AbstractPath.Type.file, AbstractPath.Type.decrypted))));
            }
            {
                final byte[] expected = writeRandomFile(storage, new Path("/cyberduckbucket/alice.txt", EnumSet.of(AbstractPath.Type.file, AbstractPath.Type.decrypted)), 57);
                final AttributedList<Path> list = storage.getFeature(ListService.class).list(home, new DisabledListProgressListener());
                assertEquals(
                        new HashSet<>(Arrays.asList(
                                new Path("/cyberduckbucket/subdir", EnumSet.of(AbstractPath.Type.directory, AbstractPath.Type.placeholder, AbstractPath.Type.decrypted)),
                                new Path("/cyberduckbucket/alice.txt", EnumSet.of(AbstractPath.Type.file, AbstractPath.Type.decrypted)),
                                new Path("/cyberduckbucket/foo.txt", EnumSet.of(AbstractPath.Type.file, AbstractPath.Type.decrypted)))),
                        new HashSet<>(list.toList()));

                assertEquals(new String(expected), readFile(storage, new Path("/cyberduckbucket/alice.txt", EnumSet.of(AbstractPath.Type.file, AbstractPath.Type.decrypted))));
            }
            {
                final PathAttributes subdir = storage.getFeature(AttributesFinder.class).find(new Path("/cyberduckbucket/subdir", EnumSet.of(AbstractPath.Type.directory, AbstractPath.Type.placeholder, AbstractPath.Type.decrypted)));
                final AttributedList<Path> list = storage.getFeature(ListService.class).list(new Path("/cyberduckbucket/subdir", EnumSet.of(AbstractPath.Type.directory, AbstractPath.Type.placeholder, AbstractPath.Type.decrypted)).withAttributes(subdir), new DisabledListProgressListener());
                assertEquals(
                        new HashSet<>(Collections.singletonList(
                                new Path("/cyberduckbucket/subdir/bar.txt", EnumSet.of(AbstractPath.Type.file, AbstractPath.Type.decrypted)))
                        ),
                        new HashSet<>(list.toList()));
                assertEquals("Hello Bar", readFile(storage, new Path("/cyberduckbucket/subdir/bar.txt", EnumSet.of(AbstractPath.Type.file, AbstractPath.Type.decrypted))));
            }
            {
                final byte[] expected = writeRandomFile(storage, new Path("/cyberduckbucket/subdir/alice.txt", EnumSet.of(AbstractPath.Type.file, AbstractPath.Type.decrypted)), 55);
                final AttributedList<Path> list = storage.getFeature(ListService.class).list(new Path("/cyberduckbucket/subdir", EnumSet.of(AbstractPath.Type.directory, AbstractPath.Type.placeholder, AbstractPath.Type.decrypted)), new DisabledListProgressListener());
                assertEquals(
                        new HashSet<>(Arrays.asList(
                                new Path("/cyberduckbucket/subdir/alice.txt", EnumSet.of(AbstractPath.Type.file, AbstractPath.Type.decrypted)),
                                new Path("/cyberduckbucket/subdir/bar.txt", EnumSet.of(AbstractPath.Type.file, AbstractPath.Type.decrypted)))),
                        new HashSet<>(list.toList()));

                assertEquals(new String(expected), readFile(storage, new Path("/cyberduckbucket/subdir/alice.txt", EnumSet.of(AbstractPath.Type.file, AbstractPath.Type.decrypted))));
            }
            {
                storage.getFeature(Delete.class).delete(Collections.singletonList(new Path("/cyberduckbucket/subdir/bar.txt", EnumSet.of(AbstractPath.Type.file, AbstractPath.Type.decrypted))), new DisabledPasswordCallback(), new Delete.DisabledCallback());
                final AttributedList<Path> list = storage.getFeature(ListService.class).list(new Path("/cyberduckbucket/subdir", EnumSet.of(AbstractPath.Type.directory, AbstractPath.Type.placeholder, AbstractPath.Type.decrypted)), new DisabledListProgressListener());
                assertEquals(1, list.size());
                assertTrue(Arrays.toString(list.toArray()), list.contains(new Path("/cyberduckbucket/subdir/alice.txt", EnumSet.of(AbstractPath.Type.file, AbstractPath.Type.decrypted))));
                assertEquals(
                        new HashSet<>(Collections.singletonList(
                                new Path("/cyberduckbucket/subdir/alice.txt", EnumSet.of(AbstractPath.Type.file, AbstractPath.Type.decrypted))
                        )),
                        new HashSet<>(list.toList()));
            }
            {
                storage.getFeature(Move.class).move(
                        new Path("/cyberduckbucket/foo.txt", EnumSet.of(AbstractPath.Type.file, AbstractPath.Type.decrypted)),
                        new Path("/cyberduckbucket/subdir/Dave.txt", EnumSet.of(AbstractPath.Type.file, AbstractPath.Type.decrypted)),
                        new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback()
                );

                final AttributedList<Path> listSubDir = storage.getFeature(ListService.class).list(new Path("/cyberduckbucket/subdir", EnumSet.of(AbstractPath.Type.directory, AbstractPath.Type.placeholder, AbstractPath.Type.decrypted)), new DisabledListProgressListener());
                assertEquals(
                        new HashSet<>(Arrays.asList(
                                new Path("/cyberduckbucket/subdir/alice.txt", EnumSet.of(AbstractPath.Type.file, AbstractPath.Type.decrypted)),
                                new Path("/cyberduckbucket/subdir/Dave.txt", EnumSet.of(AbstractPath.Type.file, AbstractPath.Type.decrypted)))
                        ),
                        new HashSet<>(listSubDir.toList()));
                final AttributedList<Path> listHome = storage.getFeature(ListService.class).list(new Path("/cyberduckbucket/", EnumSet.of(AbstractPath.Type.directory, AbstractPath.Type.placeholder, AbstractPath.Type.decrypted)), new DisabledListProgressListener());
                assertEquals(
                        new HashSet<>(Arrays.asList(
                                new Path("/cyberduckbucket/alice.txt", EnumSet.of(AbstractPath.Type.file, AbstractPath.Type.decrypted)),
                                new Path("/cyberduckbucket/subdir", EnumSet.of(AbstractPath.Type.directory, AbstractPath.Type.placeholder, AbstractPath.Type.decrypted)))
                        ),
                        new HashSet<>(listHome.toList()));
            }
        }
        finally {
            storage.withRegistry(new DefaultVaultRegistry(new DisabledPasswordCallback()));
            new DeleteWorker(new DisabledLoginCallback(),
                    storage.getFeature(ListService.class).list(bucket, new DisabledListProgressListener()).toList().stream()
                            .filter(f -> storage.getFeature(Delete.class).isSupported(f)).collect(Collectors.toList()),
                    new DisabledProgressListener()).run(storage);
            storage.getFeature(Delete.class).delete(Collections.singletonList(bucket), new DisabledPasswordCallback(), new Delete.DisabledCallback());
        }
    }

    private static @NotNull Host getMinIOBookmark() {
        final Host bookmark = new Host(new S3Protocol() {
            @Override
            public Scheme getScheme() {
                return Scheme.http;
            }
        }, "localhost", 9000).withCredentials(new Credentials("minioadmin", "minioadmin"));
        bookmark.setProperty("s3.bucket.virtualhost.disable", "true");
        bookmark.setDefaultPath("/");
        return bookmark;
    }

    private static @NotNull S3Session getS3SessionForBookmark(final Host bookmark) throws BackgroundException {
        final S3Session storage = new S3Session(bookmark);
        storage.open(ProxyFactory.get(), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        storage.login(new DisabledLoginCallback() {
                          @Override
                          public Credentials prompt(final Host bookmark, final String username, final String title, final String reason,
                                                    final LoginOptions options) {
                              return storage.getHost().getCredentials();
                          }
                      },
                new DisabledCancelCallback());
        return storage;
    }

    private static byte @NotNull [] writeRandomFile(final Session<?> session, final Path file, int size) throws BackgroundException, IOException {
        final byte[] content = RandomUtils.nextBytes(size);
        final TransferStatus transferStatus = new TransferStatus().setLength(content.length);
        transferStatus.setChecksum(session.getFeature(Write.class).checksum(file, transferStatus).compute(new ByteArrayInputStream(content), transferStatus));
        session.getFeature(Bulk.class).pre(Transfer.Type.upload, Collections.singletonMap(new TransferItem(file), transferStatus), new DisabledConnectionCallback());
        final StatusOutputStream<?> out = session.getFeature(Write.class).write(file, transferStatus, new DisabledConnectionCallback());
        IOUtils.copyLarge(new ByteArrayInputStream(content), out);
        out.close();
        return content;
    }

    private static String readFile(final Session<?> session, final Path foo) throws IOException, BackgroundException {
        final byte[] buf = new byte[300];
        final TransferStatus status = new TransferStatus();
        try(final InputStream inputStream = session.getFeature(Read.class).read(foo, status, new DisabledConnectionCallback())) {
            int l = inputStream.read(buf);
            return new String(Arrays.copyOfRange(buf, 0, l));
        }
    }
}
