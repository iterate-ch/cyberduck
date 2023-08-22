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

import ch.cyberduck.test.TestcontainerTest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.experimental.categories.Category;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;

@Category(TestcontainerTest.class)
public abstract class AbstractAssumeRoleWithWebIdentityTest {
    protected static final Logger log = LogManager.getLogger(AbstractAssumeRoleWithWebIdentityTest.class);

    protected static final int OAUTH_TTL_MILLIS = 5000;

    public static DockerComposeContainer prepareDockerComposeContainer() {
        log.info("Preparing docker compose container...");
        return new DockerComposeContainer<>(
                new File(AbstractAssumeRoleWithWebIdentityTest.class.getResource("/testcontainer/docker-compose.yml").getFile()))
                .withPull(false)
                .withLocalCompose(true)
                .withOptions("--compatibility")
                .withExposedService("keycloak_1", 8080, Wait.forListeningPort())
                .withExposedService("minio_1", 9000, Wait.forListeningPort());
    }


}