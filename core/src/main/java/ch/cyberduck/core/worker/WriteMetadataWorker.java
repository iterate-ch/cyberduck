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
import ch.cyberduck.core.MetadataOverwrite;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.Headers;

import java.text.MessageFormat;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WriteMetadataWorker extends Worker<Boolean> {

    /**
     * The updated metadata to apply
     */
    private final MetadataOverwrite metadata;

    /**
     * Descend into directories
     */
    private final RecursiveCallback<String> callback;

    private final ProgressListener listener;

    public WriteMetadataWorker(final MetadataOverwrite metadata,
                               final boolean recursive,
                               final ProgressListener listener) {
        this(metadata, new BooleanRecursiveCallback<String>(recursive), listener);
    }

    public WriteMetadataWorker(final MetadataOverwrite metadata,
                               final RecursiveCallback<String> callback,
                               final ProgressListener listener) {
        this.metadata = metadata;
        this.callback = callback;
        this.listener = listener;
    }

    @Override
    public Boolean run(final Session<?> session) throws BackgroundException {
        final Headers feature = session.getFeature(Headers.class);

        for(Map.Entry<Path, Map<String, String>> file : metadata.original.entrySet()) {
            if(this.isCanceled()) {
                throw new ConnectionCanceledException();
            }
            this.write(session, feature, file);
        }
        return true;
    }

    protected String toString(final Set<Path> files) {
        if(files.isEmpty()) {
            return LocaleFactory.localizedString("None");
        }
        final String name = files.stream().findAny().get().getName();
        if(files.size() > 1) {
            return String.format("%s… (%s) (%d)", name, LocaleFactory.localizedString("Multiple files"), files.size());
        }
        return String.format("%s…", name);
    }

    protected void write(final Session<?> session, final Headers feature, final Map.Entry<Path, Map<String, String>> pathMapEntry) throws BackgroundException {
        if(this.isCanceled()) {
            throw new ConnectionCanceledException();
        }
        Map<String, Set<String>> configMap = Stream.concat(
                pathMapEntry.getValue().entrySet().stream().map(x -> new AbstractMap.SimpleImmutableEntry<>(x.getKey(), "OLD")),
                metadata.metadata.entrySet().stream().map(x -> new AbstractMap.SimpleImmutableEntry<>(x.getKey(), "NEW"))
        ).collect(Collectors.groupingBy(x -> x.getKey(), Collectors.mapping(x -> x.getValue(), Collectors.toSet())));

        this.write(session, feature, pathMapEntry.getKey(), configMap);
    }

    protected void write(
            final Session<?> session,
            final Headers feature,
            final Path file,
            final Map<String, Set<String>> configMap) throws BackgroundException {
        if(this.isCanceled()) {
            throw new ConnectionCanceledException();
        }
        Map<String, String> originalMetadata = new HashMap<>(file.attributes().getMetadata());
        boolean anyChanged = false;
        for(Map.Entry<String, Set<String>> entry : configMap.entrySet()) {
            Set<String> config = entry.getValue();
            String value = metadata.metadata.get(entry.getKey());

            if(!config.contains("NEW")) {
                anyChanged = true;
                originalMetadata.remove(entry.getKey());
            }
            else if(value != null) {
                String oldValue = config.contains("OLD") ? originalMetadata.get(entry.getKey()) : null;
                if(!Objects.equals(value, oldValue)) {
                    anyChanged = true;
                    originalMetadata.put(entry.getKey(), value);
                }
            }
        }

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
        return MessageFormat.format(LocaleFactory.localizedString("Writing metadata of {0}", "Status"),
                this.toString(metadata.original.keySet()));
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
        return Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(metadata);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("WriteMetadataWorker{");
        sb.append("files=").append(metadata.original.keySet());
        sb.append('}');
        return sb.toString();
    }
}
