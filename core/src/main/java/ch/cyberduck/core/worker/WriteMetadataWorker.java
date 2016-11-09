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
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.Headers;

import java.text.MessageFormat;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WriteMetadataWorker extends Worker<Boolean> {

    /**
     * Selected files.
     */
    private final List<Path> files;

    /**
     * The updated metadata to apply
     */
    private final Map<String, String> metadata;

    /**
     * Descend into directories
     */
    private final RecursiveCallback<String> callback;

    private final ProgressListener listener;

    public WriteMetadataWorker(List<Path> files, final Map<String, String> metadata,
                               final boolean recursive,
                               final ProgressListener listener) {
        this(files, metadata, new BooleanRecursiveCallback<String>(recursive), listener);
    }

    public WriteMetadataWorker(final List<Path> files, final Map<String, String> metadata,
                               final RecursiveCallback<String> callback,
                               final ProgressListener listener) {
        this.files = files;
        this.metadata = metadata;
        this.callback = callback;
        this.listener = listener;
    }

    @Override
    public Boolean run(final Session<?> session) throws BackgroundException {
        final Headers feature = session.getFeature(Headers.class);
        for(Path file : files) {
            if(this.isCanceled()) {
                throw new ConnectionCanceledException();
            }
            this.write(session, feature, file);
        }
        return true;
    }

    protected void write(final Session<?> session, final Headers feature, final Path file) throws BackgroundException {
        if(this.isCanceled()) {
            throw new ConnectionCanceledException();
        }
        // this does several things,
        // first: Maps all entries from pathMapEntry (from MetadataOverwrite.originalMetadata) to their Metadata Key and the value "OLD"
        // second: Maps all entries from MetadataOverwrite.metadata to their Metadata Key and the value "NEW"
        // last: Group everything by Metadata Key and creating a set of NEW/OLD Values. i.e.:
        // {{Key1, {NEW}}, {Key2, {NEW, OLD}}, {Key3, {OLD}}}
        Map<String, Set<String>> merged = Stream.concat(
                file.attributes().getMetadata().entrySet().stream().map(x -> new AbstractMap.SimpleImmutableEntry<>(x.getKey(), "OLD")),
                metadata.entrySet().stream().map(x -> new AbstractMap.SimpleImmutableEntry<>(x.getKey(), "NEW"))
        ).collect(Collectors.groupingBy(x -> x.getKey(), Collectors.mapping(x -> x.getValue(), Collectors.toSet())));

        this.write(session, feature, file, merged);
    }

    protected void write(final Session<?> session, final Headers feature, final Path file, final Map<String, Set<String>> configMap) throws BackgroundException {
        if(this.isCanceled()) {
            throw new ConnectionCanceledException();
        }
        // read online metadata (storing non-edited metadata entries)
        Map<String, String> originalMetadata = new HashMap<>(file.attributes().getMetadata());
        boolean anyChanged = false;
        // iterate through all configMap entries (created above)
        for(Map.Entry<String, Set<String>> entry : configMap.entrySet()) {
            // get set of OLD/NEW Values for current metadata key
            Set<String> config = entry.getValue();
            // retrieve value of current metadata key in metadataoverwrite.
            String value = metadata.get(entry.getKey());

            // if map does not contain a "NEW" value it is considered REMOVED
            if(!config.contains("NEW")) {
                anyChanged = true;
                originalMetadata.remove(entry.getKey());
            }
            else if(value != null) {
                // otherwise and if value is not null it is considered CHANGED
                String oldValue = config.contains("OLD") ? originalMetadata.get(entry.getKey()) : null;
                // if new value and old value are equal it is considered UNCHANGED
                if(!Objects.equals(value, oldValue)) {
                    anyChanged = true;
                    originalMetadata.put(entry.getKey(), value);
                }
            }
        }

        // if anything has changed save metadata, otherwise continue and do for everything underneath this directory
        if(anyChanged) {
            listener.message(MessageFormat.format(LocaleFactory.localizedString("Writing metadata of {0}", "Status"),
                    file.getName()));
            feature.setMetadata(file, originalMetadata);
        }

        if(file.isDirectory()) {
            if(callback.recurse(file, LocaleFactory.localizedString("Metadata", "Info"))) {
                for(Path child : session.list(file, new ActionListProgressListener(this, listener))) {
                    this.write(session, feature, child, configMap);
                }
            }
        }
    }

    @Override
    public String getActivity() {
        return MessageFormat.format(LocaleFactory.localizedString("Writing metadata of {0}", "Status"), this.toString(files));
    }

    @Override
    public Boolean initialize() {
        return false;
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        final WriteMetadataWorker that = (WriteMetadataWorker) o;
        if(files != null ? !files.equals(that.files) : that.files != null) {
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
        final StringBuilder sb = new StringBuilder("WriteMetadataWorker{");
        sb.append("files=").append(files);
        sb.append('}');
        return sb.toString();
    }
}
