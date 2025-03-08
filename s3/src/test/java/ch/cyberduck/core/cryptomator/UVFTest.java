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
import org.cryptomator.cryptolib.api.UVFMasterkey;
import org.jetbrains.annotations.NotNull;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

@Category(TestcontainerTest.class)
public class UVFTest {

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

    @BeforeClass
    public static void start() {
        container.start();
    }

    @AfterClass
    public static void shutdown() {
        container.stop();
    }

    @Test
    public void listMinio() throws BackgroundException, IOException {
        final String bucketName = "cyberduckbucket";

        final Host bookmark = getMinIOBookmark();
        final S3Session storage = getS3SessionForBookmark(bookmark);

        final Path bucket = new Path(bucketName, EnumSet.of(AbstractPath.Type.directory));
        new S3BucketCreateService(storage).create(bucket, "us-east-1");

        final List<String> files = Arrays.asList(
                "/d/RZ/K7ZH7KBXULNEKBMGX3CU42PGUIAIX4/rExOms183v5evFwgIKiW0qvbsor1Hg==.uvf/dir.uvf",
                "/d/RZ/K7ZH7KBXULNEKBMGX3CU42PGUIAIX4/dir.uvf",
                "/d/RZ/K7ZH7KBXULNEKBMGX3CU42PGUIAIX4/GsMMTRvsuuP_6NjgRwopmWcuof-PyRQ=.uvf",
                "/d/WV/ZUTJPJT6FR7ZQRRW4FD2DPPBKJOIIF/0rAlpJBOfOCNkoummiK96xSJvAFLPbk=.uvf",
                "/d/WV/ZUTJPJT6FR7ZQRRW4FD2DPPBKJOIIF/dir.uvf"
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
                "    \"kdfSalt\": \"NIlr89R7FhochyP4yuXZmDqCnQ0dBB3UZ2D+6oiIjr8=\"\n" +
                "}";

        try {
            for(final String fi : files) {
                final Path file = new Path("/" + bucketName + "/" + fi, EnumSet.of(AbstractPath.Type.file));
                byte[] content = new byte[1000];
                final int size = UVFTest.class.getResourceAsStream("/uvf/first_vault" + fi).read(content);
                final TransferStatus transferStatus = new TransferStatus().withLength(size);
                transferStatus.setChecksum(storage.getFeature(Write.class).checksum(file, transferStatus).compute(new ByteArrayInputStream(content), transferStatus));
                storage.getFeature(Bulk.class).pre(Transfer.Type.upload, Collections.singletonMap(new TransferItem(file), transferStatus), new DisabledConnectionCallback());
                final StatusOutputStream<?> out = storage.getFeature(Write.class).write(file, transferStatus, new DisabledConnectionCallback());
                IOUtils.copyLarge(UVFTest.class.getResourceAsStream("/uvf/first_vault" + fi), out);
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
            try(
                    UVFMasterkey masterKey = UVFMasterkey.fromDecryptedPayload(jwe)) {
                attr.setDirectoryId(masterKey.rootDirId());
            }
            storage.withRegistry(vaults);
            // TODO should be fixed with https://github.com/iterate-ch/cryptolib/pull/12, see https://github.com/cryptomator/cryptolib/pull/51/commits/1e9bd327d6a04f9eb617fe83564eae3ec65a48f8
            final BackgroundException backgroundException = assertThrows(BackgroundException.class, () -> storage.getFeature(ListService.class).list(vault.getHome().withAttributes(attr).withType(EnumSet.of(AbstractPath.Type.directory, AbstractPath.Type.vault)), new DisabledListProgressListener()));
            assertEquals("File not found", backgroundException.getMessage());
            assertEquals("/" + bucketName + "/d/RK/HZLENL3PQIW6GZHE3KRRRGLFBHWHRU. Please contact your web hosting service provider for assistance.", backgroundException.getDetail());
        }
        finally {
            storage.withRegistry(new DefaultVaultRegistry(new DisabledPasswordCallback()));
            new DeleteWorker(new DisabledLoginCallback(),
                    storage.getFeature(ListService.class).list(bucket, new DisabledListProgressListener()).toList().stream()
                            .filter(f -> storage.getFeature(Delete.class).isSupported(f)).collect(Collectors.toList()),
                    new DisabledProgressListener()).run(storage);
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
}
