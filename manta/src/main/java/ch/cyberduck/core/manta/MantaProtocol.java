package ch.cyberduck.core.manta;

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
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.synchronization.ComparisonService;
import ch.cyberduck.core.synchronization.DefaultComparisonService;
import ch.cyberduck.core.synchronization.ETagComparisonService;

import com.google.auto.service.AutoService;

@AutoService(Protocol.class)
public class MantaProtocol extends AbstractProtocol {

    @Override
    public String getIdentifier() {
        return "manta";
    }

    @Override
    public String getDescription() {
        return "Triton Object Storage";
    }

    @Override
    public String getName() {
        return "Triton";
    }

    @Override
    public Type getType() {
        return Type.manta;
    }

    @Override
    public String getPrefix() {
        return String.format("%s.%s", MantaProtocol.class.getPackage().getName(), "Manta");
    }

    @Override
    public Scheme getScheme() {
        return Scheme.https;
    }

    @Override
    public String disk() {
        return String.format("%s.tiff", "ftp");
    }

    @Override
    public DirectoryTimestamp getDirectoryTimestamp() {
        return DirectoryTimestamp.explicit;
    }

    @Override
    public boolean isUsernameConfigurable() {
        return true;
    }

    @Override
    public boolean isHostnameConfigurable() {
        return true;
    }

    @Override
    public boolean isPasswordConfigurable() {
        return false;
    }

    @Override
    public boolean isPrivateKeyConfigurable() {
        return true;
    }

    @Override
    public VersioningMode getVersioningMode() {
        return VersioningMode.custom;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getFeature(final Class<T> type) {
        if(type == ComparisonService.class) {
            return (T) new DefaultComparisonService(new ETagComparisonService(), ComparisonService.disabled);
        }
        return super.getFeature(type);
    }
}
