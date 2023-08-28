package ch.cyberduck.core.smb;

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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginConnectionService;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.test.TestcontainerTest;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.experimental.categories.Category;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;

import static org.junit.Assert.fail;

@Category(TestcontainerTest.class)
public abstract class AbstractSMBTest {

    @ClassRule
    public static TestContainer container = TestContainer.getInstance();

    @BeforeClass
    public static void start() {
        container.start();
    }

    @AfterClass
    public static void stop() {
        container.stop();
    }

    SMBSession session;

    @Before
    public void setup() throws BackgroundException {
        session = new SMBSession(new Host(new SMBProtocol(), container.getHost(), container.getMappedPort(445), "/user")
                .withCredentials(new Credentials("smbj", "pass")));
        final LoginConnectionService login = new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) {
                fail(reason);
                return null;
            }
        }, new DisabledHostKeyCallback(),
                new DisabledPasswordStore(), new DisabledProgressListener());
        login.check(session, new DisabledCancelCallback());
    }

    @After
    public void disconnect() throws Exception {
        session.close();
    }

    public static class TestContainer extends GenericContainer<TestContainer> {
        private static TestContainer instance;
        private static final int SMB_PORT = 445;
        private static final String SMB_CONF_PATH = "smb.conf";
        private static final String SMB_CONF_CLASS_PATH = "smb/smb.conf";
        private static final String DOCKERFILE_PATH = "Dockerfile";
        private static final String DOCKERFILE_CLASS_PATH = "smb/Dockerfile";
        private static final String SUPERVISORD_CONF_PATH = "supervisord.conf";
        private static final String SUPERVISORD_CONF_CLASS_PATH = "smb/supervisord.conf";
        private static final String ENTRYPOINT_SH_PATH = "entrypoint.sh";
        private static final String ENTRYPOINT_SH_CLASS_PATH = "smb/entrypoint.sh";

        private TestContainer() {
            super(new ImageFromDockerfile()
                    .withFileFromClasspath(SMB_CONF_PATH, SMB_CONF_CLASS_PATH)
                    .withFileFromClasspath(DOCKERFILE_PATH, DOCKERFILE_CLASS_PATH)
                    .withFileFromClasspath(SUPERVISORD_CONF_PATH, SUPERVISORD_CONF_CLASS_PATH)
                    .withFileFromClasspath(ENTRYPOINT_SH_PATH, ENTRYPOINT_SH_CLASS_PATH)
            );

            withEnv("LOCATION", "/smb");
            addExposedPort(SMB_PORT);
            waitingFor(Wait.forListeningPort());
        }

        static TestContainer getInstance() {
            if(instance == null) {
                instance = new TestContainer();
            }
            return instance;
        }
    }
}
