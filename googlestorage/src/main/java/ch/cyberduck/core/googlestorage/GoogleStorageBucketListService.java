package ch.cyberduck.core.googlestorage;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

import java.io.IOException;
import java.util.EnumSet;

import com.google.api.services.storage.model.Bucket;
import com.google.api.services.storage.model.Buckets;

public class GoogleStorageBucketListService implements ListService {

    private final Preferences preferences
        = PreferencesFactory.get();

    private final GoogleStorageSession session;
    private GoogleStorageAttributesFinderFeature attributes;

    public GoogleStorageBucketListService(final GoogleStorageSession session) {
        this.session = session;
        this.attributes = new GoogleStorageAttributesFinderFeature(session);
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        try {
            final AttributedList<Path> buckets = new AttributedList<Path>();
            Buckets response;
            String page = null;
            do {
                response = session.getClient().buckets().list(session.getHost().getCredentials().getUsername())
                    .setMaxResults(preferences.getLong("googlestorage.listing.chunksize"))
                    .setPageToken(page)
                    .execute();
                if(null != response.getItems()) {
                    for(Bucket item : response.getItems()) {
                        final Path bucket = new Path(PathNormalizer.normalize(item.getName()), EnumSet.of(Path.Type.volume, Path.Type.directory),
                            attributes.toAttributes(item)
                        );
                        buckets.add(bucket);
                        listener.chunk(directory, buckets);
                    }
                }
                page = response.getNextPageToken();
            }
            while(page != null);
            return buckets;
        }
        catch(IOException e) {
            throw new GoogleStorageExceptionMappingService().map("Listing directory {0} failed", e, directory);
        }
    }

    @Override
    public ListService withCache(final Cache<Path> cache) {
        return this;
    }
}
