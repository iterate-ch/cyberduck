package ch.cyberduck.core.irods;

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

import ch.cyberduck.test.TestcontainerTest;

import org.junit.experimental.categories.Category;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Category(TestcontainerTest.class)
public abstract class IRODSDockerComposeManager {

    private static final ComposeContainer container;

    // Launch the docker compose project once and run all tests against that environment.
    // Ryuk will clean it up at the end of the run.
    static {
        container = new ComposeContainer(
                new File(IRODSDockerComposeManager.class.getResource("/docker/docker-compose.yml").getFile()))
                .withPull(false)
                .withExposedService("irods-catalog-provider-1", 1347,
                        Wait.forLogMessage(".*\"log_message\":\"Initializing delay server.\".*", 1)
                                .withStartupTimeout(Duration.ofMinutes(5)));

        container.start();
    }

    protected static final Map<String, String> PROPERTIES = new HashMap<String, String>() {{
        put("irods.key", "rods");
        put("irods.secret", "rods");
    }};

}
