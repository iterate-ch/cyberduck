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
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.test.TestcontainerTest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.experimental.categories.Category;
import org.testcontainers.containers.DockerComposeContainer;

import static ch.cyberduck.core.sts.STSTestSetup.*;

@Category(TestcontainerTest.class)
public abstract class AbstractAssumeRoleWithWebIdentityTest {
    protected static final int MILLIS = 1000;

    // lag to wait after token expiry
    protected static final int LAG = 10 * MILLIS;

    protected static final Logger log = LogManager.getLogger(AbstractAssumeRoleWithWebIdentityTest.class);
    protected static int OAUTH_TTL_SECS = 30;

    protected static Profile profile = null;


    @ClassRule
    public static DockerComposeContainer<?> compose = prepareDockerComposeContainer(getKeyCloakFile());


    @Before
    public void setup() throws BackgroundException {
        profile = readProfile();
    }


}