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
import ch.cyberduck.core.DirectoryDelimiterPathContainerService;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.synchronization.ChainedComparisonService;
import ch.cyberduck.core.synchronization.Comparison;
import ch.cyberduck.core.synchronization.ComparisonService;
import ch.cyberduck.core.synchronization.DefaultComparisonService;
import ch.cyberduck.core.synchronization.ETagComparisonService;
import ch.cyberduck.core.synchronization.SizeComparisonService;
import ch.cyberduck.core.synchronization.TimestampComparisonService;
import ch.cyberduck.core.text.DefaultLexicographicOrderComparator;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class GoogleStorageProtocol extends AbstractProtocol {

    @Override
    public String getName() {
        return "Google Storage";
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
    public Set<Location.Name> getRegions(final List<String> regions) {
        return regions.stream().map(GoogleStorageLocationFeature.GoogleStorageRegion::new).collect(Collectors.toSet());
    }

    @Override
    public Scheme getScheme() {
        return Scheme.https;
    }

    @Override
    public boolean isPortConfigurable() {
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

    @Override
    public DirectoryTimestamp getDirectoryTimestamp() {
        return DirectoryTimestamp.explicit;
    }

    @Override
    public Comparator<String> getListComparator() {
        return new DefaultLexicographicOrderComparator();
    }

    @Override
    public VersioningMode getVersioningMode() {
        return VersioningMode.storage;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getFeature(final Class<T> type) {
        if(type == PathContainerService.class) {
            return (T) new DirectoryDelimiterPathContainerService();
        }
        if(type == ComparisonService.class) {
            return (T) new DefaultComparisonService(new ChainedComparisonService(EnumSet.of(Comparison.unknown, Comparison.notequal),
                    new ETagComparisonService(),
                    new ChainedComparisonService(
                            EnumSet.of(Comparison.unknown, Comparison.equal), new TimestampComparisonService(), new SizeComparisonService())), ComparisonService.disabled);
        }
        return super.getFeature(type);
    }
}
