package ch.cyberduck.core.googlestorage;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.AbstractProtocol;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.s3.S3LocationFeature;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class GoogleStorageProtocol extends AbstractProtocol {
    @Override
    public String getName() {
        return "Google Cloud Storage";
    }

    @Override
    public String getDescription() {
        return LocaleFactory.localizedString("Google Cloud Storage", "S3");
    }

    @Override
    public String getIdentifier() {
        return "gs";
    }

    @Override
    public Type getType() {
        return Type.googlestorage;
    }

    @Override
    public String getPrefix() {
        return String.format("%s.%s", GoogleStorageProtocol.class.getPackage().getName(), "GoogleStorage");
    }

    @Override
    public String disk() {
        return String.format("%s.tiff", "googlestorage");
    }

    @Override
    public boolean isHostnameConfigurable() {
        return false;
    }

    @Override
    public boolean isPasswordConfigurable() {
        // Only provide Project ID or Number
        return false;
    }

    @Override
    public String getDefaultHostname() {
        return "storage.googleapis.com";
    }

    @Override
    public Set<Location.Name> getRegions() {
        return new HashSet<Location.Name>(Arrays.asList(
                new S3LocationFeature.S3Region("US"), new S3LocationFeature.S3Region("EU")
        ));
    }

    @Override
    public Scheme getScheme() {
        return Scheme.https;
    }

    @Override
    public String[] getSchemes() {
        return new String[]{this.getScheme().name(), "gs"};
    }

    @Override
    public boolean isPortConfigurable() {
        return false;
    }

    @Override
    public boolean isAnonymousConfigurable() {
        return false;
    }

    @Override
    public String getUsernamePlaceholder() {
        return LocaleFactory.localizedString("Project ID or Number", "Credentials");
    }

    @Override
    public String getPasswordPlaceholder() {
        return LocaleFactory.localizedString("Authorization code", "Credentials");
    }

    @Override
    public String favicon() {
        // Return static icon as endpoint has no favicon configured
        return this.icon();
    }

}
