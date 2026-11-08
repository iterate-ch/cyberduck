package ch.cyberduck.core.ftp;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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

import com.github.dockerjava.api.model.Capability;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * FTP server implementations running in a container to execute integration tests against. Containers are started
 * on first use and shared for the lifetime of the JVM.
 */
public enum FTPTestServer {
    VSFTPD("/ftp/test", 21000, 21010, () -> new GenericContainer<>(new ImageFromDockerfile("cyberduck-vsftpd", false)
            .withFileFromClasspath("Dockerfile", "vsftpd/Dockerfile")
            .withFileFromClasspath("vsftpd.conf", "vsftpd/vsftpd.conf")
            .withFileFromClasspath("pam", "vsftpd/pam")
            .withBuildArg("FTP_USER", FTPTestServer.USER)
            .withBuildArg("FTP_PASSWORD", FTPTestServer.PASSWORD)
            .withBuildArg("FTP_HOME", "/ftp/test"))
            // Configuration file must be first as it would otherwise override options on the command line
            .withCommand("vsftpd", "/etc/vsftpd/vsftpd.conf", "-obackground=NO",
                    "-opasv_min_port=21000",
                    "-opasv_max_port=21010",
                    "-opasv_address=127.0.0.1")
            .withExposedPorts(FTPTestServer.PORT)
            .withLogConsumer(f -> System.out.print("FTPD " + f.getUtf8String()))
            .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(2)))),
    PROFTPD("/home/test", 21020, 21030, () -> new GenericContainer<>(new ImageFromDockerfile("cyberduck-proftpd-ftp", false)
            .withFileFromPath("Dockerfile", Paths.get("src/test/resources/proftpd/Dockerfile"))
            .withFileFromPath("proftpd.conf", Paths.get("src/test/resources/proftpd/proftpd.conf")))
            .withExposedPorts(FTPTestServer.PORT)
            .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(5)))),
    PUREFTPD("/home/test", 21040, 21050, () -> new GenericContainer<>(new ImageFromDockerfile("cyberduck-pure-ftpd", false)
            .withFileFromPath("Dockerfile", Paths.get("src/test/resources/pure-ftpd/Dockerfile")))
            // Required to drop privileges
            .withCreateContainerCmdModifier(cmd -> cmd.getHostConfig().withCapAdd(Capability.SYS_NICE, Capability.DAC_READ_SEARCH))
            .withExposedPorts(FTPTestServer.PORT)
            .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(5))));

    public static final String USER = "test";
    public static final String PASSWORD = "test";
    public static final int PORT = 21;

    private final String home;
    private final int pasvMinPort;
    private final int pasvMaxPort;
    private final Supplier<GenericContainer<?>> factory;
    private GenericContainer<?> container;

    FTPTestServer(final String home, final int pasvMinPort, final int pasvMaxPort, final Supplier<GenericContainer<?>> factory) {
        this.home = home;
        this.pasvMinPort = pasvMinPort;
        this.pasvMaxPort = pasvMaxPort;
        this.factory = factory;
    }

    /**
     * @return Home directory of user in container
     */
    public String home() {
        return home;
    }

    public synchronized GenericContainer<?> container() throws Exception {
        if(null == container) {
            final GenericContainer<?> created = factory.get();
            // Passive data connections are made to the port announced by the server and must be mapped one to one
            created.setPortBindings(IntStream.rangeClosed(pasvMinPort, pasvMaxPort)
                    .mapToObj(port -> String.format("%d:%d", port, port)).collect(Collectors.toList()));
            created.start();
            Runtime.getRuntime().addShutdownHook(new Thread(created::stop));
            this.awaitGreeting(created);
            container = created;
        }
        return container;
    }

    public String host() throws Exception {
        return this.container().getHost();
    }

    public int port() throws Exception {
        return this.container().getMappedPort(PORT);
    }

    /**
     * Port forwarded to host accepts connections before server is ready to reply
     */
    private void awaitGreeting(final GenericContainer<?> container) throws Exception {
        final long timeout = System.nanoTime() + Duration.ofSeconds(30).toNanos();
        while(true) {
            try(Socket socket = new Socket(container.getHost(), container.getMappedPort(PORT))) {
                socket.setSoTimeout(2000);
                final String greeting = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)).readLine();
                if(null != greeting && greeting.startsWith("220")) {
                    return;
                }
            }
            catch(IOException e) {
                // Retry
            }
            if(System.nanoTime() > timeout) {
                throw new IOException("Timeout waiting for FTP server");
            }
            Thread.sleep(500L);
        }
    }
}
