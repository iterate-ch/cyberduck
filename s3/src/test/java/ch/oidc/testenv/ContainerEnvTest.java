package ch.oidc.testenv;/*
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

import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;

public class ContainerEnvTest {

    //    private static final Slf4jLogConsumer logConsumer = new Slf4jLogConsumer(logger);

    @ClassRule
    public static DockerComposeContainer<?> compose = new DockerComposeContainer<>(
            new File("src/test/resources/oidcTestcontainer/docker-compose.yml"))
            .withPull(false)
            .withLocalCompose(true)
            .withOptions("--compatibility")
            //.withLogConsumer("keycloak_1", new Slf4jLogConsumer(logger))
            //.withLogConsumer("minio_1", new Slf4jLogConsumer(logger))
            .withExposedService("keycloak_1", 8080, Wait.forListeningPort())
            .withExposedService("minio_1", 9000, Wait.forListeningPort());

}
