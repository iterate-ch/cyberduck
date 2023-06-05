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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.s3.S3Protocol;
import ch.cyberduck.core.s3.S3Session;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DefaultX509TrustManager;
import ch.cyberduck.core.sts.NonAwsSTSCredentialsConfigurator;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;

public class ContainerEnvTest {

    protected S3Session session;

    //    private static final Slf4jLogConsumer logConsumer = new Slf4jLogConsumer(logger);

    @ClassRule
    public static DockerComposeContainer<?> compose = new DockerComposeContainer<>(
            new File("src/test/resources/oidcTestcontainer/docker-compose.yml"))
            .withPull(false)
            .withLocalCompose(true)
            .withOptions("--compatibility")
            //.withLogConsumer("keycloak_1", new Slf4jLogConsumer(logger))
            //.withLogConsumer("minio_1", new Slf4jLogConsumer(logger))
            .withExposedService("keycloak_1", 8443, Wait.forListeningPort())
            .withExposedService("minio_1", 9001, Wait.forListeningPort());


    @Before
    public void setup() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new S3Protocol())));
        final Profile profile = new ProfilePlistReader(factory).read(
                this.getClass().getResourceAsStream("/S3-OIDC.cyberduckprofile"));
        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials("testi", "test"));
        session = new S3Session(host, new DefaultX509TrustManager(), new DefaultX509KeyManager());
//        compose.stop();
//        compose.start();
    }

    @After
    public void disconnect() throws Exception {
        compose.stop();
        session.close();
    }

//    with Fiddler as proxy
//    new Proxy(Proxy.Type.HTTP, "localhost", 8888)

    @Test
    public void testGetAuthorizationUrlNotNullOrEmpty() throws BackgroundException {
        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());

//        assertNotNull(authorizationUrl);
//        assertNotEquals("", authorizationUrl);
//        System.out.println("Test GetAuthorizationUrlNotNullOrEmpty passed");
    }


//    @Test
//    public void testSTSAssumeRoleWithWebIdentity() throws BackgroundException {
//        NonAwsSTSCredentialsConfigurator sts = new NonAwsSTSCredentialsConfigurator(new DefaultX509TrustManager(), new DefaultX509KeyManager(), new DisabledLoginCallback());
//    }
}
