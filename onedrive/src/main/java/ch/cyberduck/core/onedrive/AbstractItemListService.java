package ch.cyberduck.core.onedrive;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.onedrive.features.GraphAttributesFinderFeature;
import ch.cyberduck.core.webloc.UrlFileWriter;
import ch.cyberduck.core.webloc.UrlFileWriterFactory;

import org.nuxeo.onedrive.client.types.DriveItem;

import java.util.EnumSet;

public abstract class AbstractItemListService extends AbstractListService<DriveItem.Metadata> {
    private final GraphAttributesFinderFeature attributes;
    private final UrlFileWriter urlFileWriter = UrlFileWriterFactory.get();

    public AbstractItemListService(final GraphAttributesFinderFeature attributes) {
        this.attributes = attributes;
    }

    @Override
    protected Path toPath(final DriveItem.Metadata metadata, final Path directory) {
        final PathAttributes attr = attributes.toAttributes(metadata);

        final String fileName;
        if(metadata.isPackage()) {
            fileName = String.format("%s.%s", PathNormalizer.name(metadata.getName()), urlFileWriter.getExtension());
        }
        else {
            fileName = metadata.getName();
        }

        return new Path(directory, fileName, this.resolveType(metadata), attr);
    }

    @Override
    public ListService withCache(final Cache<Path> cache) {
        attributes.withCache(cache);
        return this;
    }

    private EnumSet<Path.Type> resolveType(final DriveItem.Metadata metadata) {
        if(metadata.isPackage()) {
            return EnumSet.of(Path.Type.file, Path.Type.placeholder);
        }
        else if(metadata.getRemoteItem() != null) {
            final EnumSet<Path.Type> types = this.resolveType(metadata.getRemoteItem());
            types.add(Path.Type.shared);
            return types;
        }
        else {
            return EnumSet.of(metadata.isFolder() ? Path.Type.directory : Path.Type.file);
        }
    }
}
