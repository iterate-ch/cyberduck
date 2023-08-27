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

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.util.UUID;

public class SmbTestContainer extends GenericContainer<SmbTestContainer> {
    private static SmbTestContainer instance;
    private static final int SMB_PORT = 445;
    private static final String SMB_CONF_PATH = "smb.conf";
    private static final String SMB_CONF_CLASS_PATH = "smb/smb.conf";
    private static final String DOCKERFILE_PATH = "Dockerfile";
    private static final String DOCKERFILE_CLASS_PATH = "smb/Dockerfile";
    private static final String SUPERVISORD_CONF_PATH = "supervisord.conf";
    private static final String SUPERVISORD_CONF_CLASS_PATH = "smb/supervisord.conf";
    private static final String ENTRYPOINT_SH_PATH = "entrypoint.sh";
    private static final String ENTRYPOINT_SH_CLASS_PATH = "smb/entrypoint.sh";

    private SmbTestContainer() {
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

    static SmbTestContainer getInstance() {
        if (instance == null) {
            instance = new SmbTestContainer();
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

    public String getBaseUrl() {
        return "http://" + getHost() + ":" + getMappedPort(SMB_PORT) + "/webdav/";
    }

    public String getTestFolderUrl() {
        return getBaseUrl() + "testFolder/";
    }

    public String getRandomTestFileUrl() {
        return String.format("%s%s", getTestFolderUrl(), UUID.randomUUID());
    }

    public String getRandomTestDirectoryUrl() {
        return String.format("%s%s/", getTestFolderUrl(), UUID.randomUUID());
    }

    public String getTestBasicAuthFolderUrl() {
        return getBaseUrl() + "folderWithBasicAuth/";
    }

    public String getRandomTestBasicAuthFileUrl() {
        return String.format("%s%s", getTestBasicAuthFolderUrl(), UUID.randomUUID());
    }

    public String getTestFolderWithLockNotImplementedUrl() {
        return getBaseUrl() + "lockNotImplemented/";
    }
}
