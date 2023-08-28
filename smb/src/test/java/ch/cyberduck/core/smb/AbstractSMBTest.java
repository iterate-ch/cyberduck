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
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;
import ch.cyberduck.test.TestcontainerTest;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.experimental.categories.Category;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.util.Collections;
import java.util.HashSet;
import java.util.UUID;

@Category(TestcontainerTest.class)
public abstract class AbstractSMBTest {

    @ClassRule
    public static TestContainer container = TestContainer.getInstance();

    SMBSession session;

    @Before
    public void setup() throws BackgroundException {
        container.stop();
        container.start();
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new SMBProtocol())));
        final Profile profile = new ProfilePlistReader(factory).read(
                this.getClass().getResourceAsStream("/test.cyberduckprofile"));
        final Host host = new Host(profile, container.getHost(), container.getMappedPort(445))
                .withCredentials(new Credentials("smbj", "pass"));
        host.setDefaultPath("/user");
        session = new SMBSession(host);
        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
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
            instance.start();
            return instance;
        }

        @Override
        public void start() {
            super.start();
        }

        @Override
        public void stop() {
            instance = null;
            super.stop();
        }

        @Override
        public void close() {
            instance.close();
            instance = null;
        }
    }
}
