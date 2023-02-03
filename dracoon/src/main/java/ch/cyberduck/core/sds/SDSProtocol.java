package ch.cyberduck.core.sds;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.features.CredentialsCleanupService;
import ch.cyberduck.core.features.DelegatingPairingFeature;
import ch.cyberduck.core.features.Pairing;
import ch.cyberduck.core.features.Scheduler;
import ch.cyberduck.core.sds.triplecrypt.TripleCryptCleanupFeature;
import ch.cyberduck.core.synchronization.ComparisonService;
import ch.cyberduck.core.synchronization.DefaultComparisonService;
import ch.cyberduck.core.synchronization.RevisionComparisonService;
import ch.cyberduck.core.synchronization.VersionIdComparisonService;

import org.apache.commons.lang3.StringUtils;

public class SDSProtocol extends AbstractProtocol {
    @Override
    public String getIdentifier() {
        return "dracoon";
    }

    @Override
    public String getName() {
        return "DRACOON";
    }

    @Override
    public String getDescription() {
        return "DRACOON";
    }

    @Override
    public Type getType() {
        return Type.dracoon;
    }

    @Override
    public Scheme getScheme() {
        return Scheme.https;
    }

    @Override
    public String getContext() {
        return "/api";
    }

    @Override
    public String getAuthorization() {
        return Authorization.oauth.name();
    }

    @Override
    public String disk() {
        return String.format("%s.tiff", "ftp");
    }

    @Override
    public String getPrefix() {
        return String.format("%s.%s", SDSProtocol.class.getPackage().getName(), StringUtils.upperCase("sds"));
    }

    @Override
    public boolean isUsernameConfigurable() {
        switch(Authorization.valueOf(this.getAuthorization())) {
            case oauth:
                return false;
        }
        return true;
    }

    @Override
    public boolean isPasswordConfigurable() {
        switch(Authorization.valueOf(this.getAuthorization())) {
            case oauth:
                return false;
        }
        return true;
    }

    @Override
    public Case getCaseSensitivity() {
        return Case.insensitive;
    }

    @Override
    public DirectoryTimestamp getDirectoryTimestamp() {
        return DirectoryTimestamp.explicit;
    }

    public enum Authorization {
        sql,
        radius,
        active_directory,
        oauth,
        password
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getFeature(final Class<T> type) {
        if(type == Scheduler.class) {
            return (T) new SDSMissingFileKeysSchedulerFeature();
        }
        if(type == Pairing.class) {
            return (T) new DelegatingPairingFeature(new CredentialsCleanupService(), new TripleCryptCleanupFeature());
        }
        if(type == ComparisonService.class) {
            return (T) new DefaultComparisonService(new VersionIdComparisonService(), new RevisionComparisonService());
        }
        return super.getFeature(type);
    }
}
