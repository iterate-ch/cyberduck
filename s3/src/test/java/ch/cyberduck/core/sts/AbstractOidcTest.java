package ch.cyberduck.core.sts;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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


import ch.cyberduck.core.Profile;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.s3.S3Protocol;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;
import ch.cyberduck.test.TestcontainerTest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;

@Category(TestcontainerTest.class)
public abstract class AbstractOidcTest {

    protected static final Logger log = LogManager.getLogger(AbstractOidcTest.class);
    protected static Profile profile = null;
    private static Network network;
    private static final DockerComposeContainer<?> compose;

    static {
        compose = new DockerComposeContainer<>(
                new File(AbstractOidcTest.class.getResource("/testcontainer/docker-compose.yml").getFile()))
                .withPull(false)
                .withLocalCompose(true)
                .withOptions("--compatibility")
                .withExposedService("keycloak_1", 8080, Wait.forListeningPort())
                .withExposedService("minio_1", 9000, Wait.forListeningPort());
    }

    @BeforeClass
    public static void beforeAll() {
        compose.start();
    }

    @Before
    public void setup() throws BackgroundException {
        profile = readProfile();
    }

    private Profile readProfile() throws AccessDeniedException {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new S3Protocol())));
        return new ProfilePlistReader(factory).read(
                this.getClass().getResourceAsStream("/S3-OIDC-Testing.cyberduckprofile"));
    }

    @AfterClass
    public static void disconnect() {
        if(compose == null && network != null) {
            network.close();
        }
    }
}