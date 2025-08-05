package ch.cyberduck.core.irods;

/*
 * Copyright (c) 2002-2025 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.AbstractProtocol;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.Scheme;

import org.apache.commons.lang3.StringUtils;

import com.google.auto.service.AutoService;

import java.util.HashMap;
import java.util.Map;

@AutoService(Protocol.class)
public final class IRODSProtocol extends AbstractProtocol {

    public static final String DESTINATION_RESOURCE = "Destination Resource";
    public static final String DELETE_OBJECTS_PERMANTENTLY = "Delete Objects Permanently";
    public static final String CLIENT_SERVER_NEGOTIATION = "Client Server Negotiation";
    public static final String ENCRYPTION_ALGORITHM = "Encryption Algorithm";
    public static final String ENCRYPTION_KEY_SIZE = "Encryption Key Size";
    public static final String ENCRYPTION_SALT_SIZE = "Encryption Salt Size";
    public static final String ENCRYPTION_HASH_ROUNDS = "Encryption Hash Rounds";
    public static final String PARALLEL_TRANSFER_THRESHOLD = "Parallel Transfer Threshold";
    public static final String PARALLEL_TRANSFER_CONNECTIONS = "Parallel Transfer Connections";
    public static final String PARALLEL_TRANSFER_BUFFER_SIZE = "Parallel Transfer Buffer Size";

    @Override
    public String getIdentifier() {
        return this.getScheme().name();
    }

    @Override
    public String getDescription() {
        return LocaleFactory.localizedString("iRODS (Integrated Rule-Oriented Data System)");
    }

    @Override
    public DirectoryTimestamp getDirectoryTimestamp() {
        return DirectoryTimestamp.explicit;
    }

    @Override
    public Statefulness getStatefulness() {
        return Statefulness.stateful;
    }

    @Override
    public Scheme getScheme() {
        return Scheme.irods;
    }

    @Override
    public String disk() {
        return String.format("%s.tiff", "ftp");
    }

    @Override
    public String getPrefix() {
        return String.format("%s.%s", IRODSProtocol.class.getPackage().getName(), StringUtils.upperCase(this.getType().name()));
    }

    @Override
    public VersioningMode getVersioningMode() {
        return VersioningMode.none;
    }

    @Override
    public Map<String, String> getProperties() {
        final Map<String, String> props = new HashMap<>();
        props.put(DELETE_OBJECTS_PERMANTENTLY, "no");
        props.put(CLIENT_SERVER_NEGOTIATION, "CS_NEG_REFUSE");
        props.put(ENCRYPTION_ALGORITHM, "AES-256-CBC");
        props.put(ENCRYPTION_KEY_SIZE, "32");
        props.put(ENCRYPTION_SALT_SIZE, "8");
        props.put(ENCRYPTION_HASH_ROUNDS, "16");
        props.put(PARALLEL_TRANSFER_THRESHOLD, "33554432"); // 32MB
        props.put(PARALLEL_TRANSFER_CONNECTIONS, "3");
        props.put(PARALLEL_TRANSFER_BUFFER_SIZE, "4194304"); // 4MB
        return props;
    }
}
