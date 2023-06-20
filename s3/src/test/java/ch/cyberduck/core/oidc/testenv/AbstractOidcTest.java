package ch.cyberduck.core.oidc.testenv;/*
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

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.slf4j.Logger;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;

import com.amazonaws.waiters.WaiterHandler;

public abstract class AbstractOidcTest {

    protected Profile profile = null;

    @ClassRule
    public static DockerComposeContainer compose = new DockerComposeContainer(
            new File("src/test/resources/oidcTestcontainer/docker-compose.yml"))
            .withPull(false)
            .withLocalCompose(true)
            .withOptions("--compatibility")
            .withExposedService("keycloak_1", 8080, Wait.forListeningPort())
            .withExposedService("minio_1", 9000, Wait.forHttp("http://localhost:8080/realms/cyberduckrealm/.well-known/openid-configuration"));

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
        compose.stop();
    }
}