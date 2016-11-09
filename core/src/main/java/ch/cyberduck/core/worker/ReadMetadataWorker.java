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
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Headers;
import ch.cyberduck.core.stream.ExtendedCollectors;
import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.AbstractMap.Entry;
import static java.util.AbstractMap.SimpleImmutableEntry;

public class ReadMetadataWorker extends Worker<MetadataOverwrite> {
    private static final Logger log = Logger.getLogger(ReadMetadataWorker.class);

    /**
     * Selected files.
     */
    private final List<Path> files;

    public ReadMetadataWorker(final List<Path> files) {
        this.files = files;
    }

    /**
     * @return Metadata
     */
    @Override
    public MetadataOverwrite run(final Session<?> session) throws BackgroundException {
        final Headers feature = session.getFeature(Headers.class);

        Map<Path, Map<String, String>> onlineMetadata = new HashMap<>();
        for(Path file : files)
        {
            Map<String, String> metadata = feature.getMetadata(file);
            file.attributes().setMetadata(metadata);
            onlineMetadata.put(file, metadata);
        }

        Supplier<Stream<Entry<Path, Entry<String, String>>>> flatMeta = () -> onlineMetadata.entrySet().stream().flatMap(
                x -> x.getValue().entrySet().stream().map(
                        y -> new SimpleImmutableEntry<>(x.getKey(), y))
        );
        Map<String, Map<Path, String>> metaGraph = flatMeta.get().collect(
                Collectors.groupingBy(
                        x -> x.getValue().getKey(),
                        Collectors.toMap(x -> x.getKey(), x -> x.getValue().getValue()))
        ).entrySet().stream().filter(x -> x.getValue().size() == files.size()
        ).collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));

        Map<Path, Map<String, String>> pathOriginalMeta = flatMeta.get().filter(
                x -> metaGraph.containsKey(x.getValue().getKey())
        ).collect(
                Collectors.groupingBy(
                        x -> x.getKey(),
                        Collectors.toMap(x -> x.getValue().getKey(), x -> x.getValue().getValue())));

        Map<String, String> metadata = metaGraph.entrySet().stream().collect(ExtendedCollectors.toMap(
                x -> x.getKey(),
                x -> {
                    Supplier<Stream<String>> valueSupplier = () -> x.getValue().entrySet().stream().map(y -> y.getValue()).distinct();
                    return valueSupplier.get().count() == 1 ? valueSupplier.get().findAny().get() : null;
                }));

        return new MetadataOverwrite(pathOriginalMeta, metadata);
    }

    @Override
    public String getActivity() {
        return MessageFormat.format(LocaleFactory.localizedString("Reading metadata of {0}", "Status"),
                this.toString(files));
    }

    @Override
    public MetadataOverwrite initialize() {
        return new MetadataOverwrite(Collections.emptyMap(), Collections.emptyMap());
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
