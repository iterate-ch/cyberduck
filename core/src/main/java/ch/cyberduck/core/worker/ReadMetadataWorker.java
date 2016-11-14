package ch.cyberduck.core.worker;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
 *
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
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Headers;

import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ReadMetadataWorker extends Worker<Map<String, String>> {
    private static final Logger log = Logger.getLogger(ReadMetadataWorker.class);

    /**
     * Selected files.
     */
    private final List<Path> files;

    public ReadMetadataWorker(final List<Path> files) {
        this.files = files;
    }

    @Override
    public Map<String, String> run(final Session<?> session) throws BackgroundException {
        final Headers feature = session.getFeature(Headers.class);

        // create a list for temporarily storing entries
        List<String> removedEntries = new ArrayList<>();
        // create two maps, one for Map->Meta, and one for Key->Path:Value
        Map<Path, Map<String, String>> onlineMetadata = new HashMap<>();
        Map<String, Map<Path, String>> metaGraph = new HashMap<>();

        // iterate through all files
        for (Path file : files) {
            // read online metadata
            Map<String, String> metadata = feature.getMetadata(file);
            // put it into onlineMetadata (do nothing with it)
            onlineMetadata.put(file, new HashMap<>(metadata));
            // take every entry of current metadata and store it in metaGraph
            for (Map.Entry<String, String> entry : metadata.entrySet()) {
                if (metaGraph.containsKey(entry.getKey())) {
                    // if existing, get map, put value
                    metaGraph.get(entry.getKey()).put(file, entry.getValue());
                } else {
                    // if not existent create hashmap and put it back
                    Map<Path, String> map = new HashMap<>();
                    metaGraph.put(entry.getKey(), map);
                    map.put(file, entry.getValue());
                }
            }
        }

        for (Map.Entry<String, Map<Path, String>> entry : metaGraph.entrySet()) {
            // if current key does not have equal to files amount items remove it
            if (entry.getValue().size() != files.size()) {
                removedEntries.add(entry.getKey());
            }
        }
        // deferred removing of files due to InvalidOperationException if changed while iteration
        for (String key : removedEntries) {
            metaGraph.remove(key);
        }
        // iterate all Path->Meta values
        for (Map.Entry<Path, Map<String, String>> entry : onlineMetadata.entrySet()) {
            // before continue, clear removedEntries
            removedEntries.clear();
            for (Map.Entry<String, String> metaPair : entry.getValue().entrySet()) {
                // if filtered metaGraph does not contain this key, remove it
                if (!metaGraph.containsKey(metaPair.getKey())) {
                    removedEntries.add(metaPair.getKey());
                }
            }
            // deferred removing of entries due to InvalidOperationException on change while iteration
            for (String key : removedEntries) {
                entry.getValue().remove(key);
            }
            // put filtered metadata to file attributes
            entry.getKey().attributes().setMetadata(entry.getValue());
        }

        // store result metadata in hashmap
        Map<String, String> metadata = new HashMap<>();
        for (Map.Entry<String, Map<Path, String>> entry : metaGraph.entrySet()) {
            // single use of streams, reason: distinct is easier in Streams than it would be writing it manually
            Supplier<Stream<String>> valueSupplier = () -> entry.getValue().entrySet().stream().map(y -> y.getValue()).distinct();
            // check count against 1, if it is use that value, otherwise use null
            String value = valueSupplier.get().count() == 1 ? valueSupplier.get().findAny().get() : null;
            // store it
            metadata.put(entry.getKey(), value);
        }

        return metadata;
    }

    @Override
    public String getActivity() {
        return MessageFormat.format(LocaleFactory.localizedString("Reading metadata of {0}", "Status"),
                this.toString(files));
    }

    @Override
    public Map<String, String> initialize() {
        return Collections.emptyMap();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ReadMetadataWorker that = (ReadMetadataWorker) o;
        if (files != null ? !files.equals(that.files) : that.files != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return files != null ? files.hashCode() : 0;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ReadMetadataWorker{");
        sb.append("files=").append(files);
        sb.append('}');
        return sb.toString();
    }
}
