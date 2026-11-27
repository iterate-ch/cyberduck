package ch.cyberduck.core.shared;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Path;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Skip metadata entries exceeding size limits of a protocol. Sizes are measured as the number of bytes of key and value
 * in UTF-8 after encoding of values. Entries are added in order of their size, starting with the smallest, until the limit of the total size or
 * number of entries is reached.
 */
public final class MetadataSizeFilter {
    private static final Logger log = LogManager.getLogger(MetadataSizeFilter.class);

    /**
     * No limit
     */
    public static final int UNLIMITED = -1;

    private int maximumKeySize = UNLIMITED;
    private int maximumValueSize = UNLIMITED;
    private int maximumEntrySize = UNLIMITED;
    private int maximumCount = UNLIMITED;
    private int maximumTotalSize = UNLIMITED;
    /**
     * Keys not subject to limits such as standard HTTP headers
     */
    private Predicate<String> excluded = key -> false;
    /**
     * Keys always kept and counted for limits of number of entries and total size
     */
    private Predicate<String> required = key -> false;
    /**
     * Encoding applied to values when sent to the server
     */
    private Function<String, String> encoder = Function.identity();
    /**
     * Key as counted by the protocol, such as without a prefix added for transport in headers
     */
    private Function<String, String> name = Function.identity();
    /**
     * Number of bytes added to the size of each entry, such as a prefix of header names
     */
    private int overhead = 0;

    /**
     * @param size Maximum number of bytes of key
     */
    public MetadataSizeFilter withMaximumKeySize(final int size) {
        this.maximumKeySize = size;
        return this;
    }

    /**
     * @param size Maximum number of bytes of value
     */
    public MetadataSizeFilter withMaximumValueSize(final int size) {
        this.maximumValueSize = size;
        return this;
    }

    /**
     * @param size Maximum number of bytes of key and value of a single entry
     */
    public MetadataSizeFilter withMaximumEntrySize(final int size) {
        this.maximumEntrySize = size;
        return this;
    }

    /**
     * @param count Maximum number of entries
     */
    public MetadataSizeFilter withMaximumCount(final int count) {
        this.maximumCount = count;
        return this;
    }

    /**
     * @param size Maximum number of bytes of keys and values of all entries
     */
    public MetadataSizeFilter withMaximumTotalSize(final int size) {
        this.maximumTotalSize = size;
        return this;
    }

    /**
     * @param excluded Keys not subject to limits such as standard HTTP headers
     */
    public MetadataSizeFilter withExcluded(final Predicate<String> excluded) {
        this.excluded = excluded;
        return this;
    }

    /**
     * @param required Keys always kept and counted for limits of number of entries and total size
     */
    public MetadataSizeFilter withRequired(final Predicate<String> required) {
        this.required = required;
        return this;
    }

    /**
     * @param encoder Encoding applied to values when sent to the server, such as percent encoding in headers
     */
    public MetadataSizeFilter withEncoder(final Function<String, String> encoder) {
        this.encoder = encoder;
        return this;
    }

    /**
     * @param name Key as counted by the protocol, such as without a prefix added for transport in headers. The
     *             original key is kept in the result
     */
    public MetadataSizeFilter withName(final Function<String, String> name) {
        this.name = name;
        return this;
    }

    /**
     * @param overhead Number of bytes added to the size of each entry, such as a prefix of header names
     */
    public MetadataSizeFilter withOverhead(final int overhead) {
        this.overhead = overhead;
        return this;
    }

    /**
     * @param file     File to write metadata for
     * @param metadata Metadata to write
     * @return Metadata with entries exceeding limits removed
     */
    public Map<String, String> filter(final Path file, final Map<String, String> metadata) {
        final Map<String, String> result = new HashMap<>();
        final List<Map.Entry<String, String>> candidates = new ArrayList<>();
        int count = 0;
        int total = 0;
        for(Map.Entry<String, String> entry : metadata.entrySet()) {
            if(excluded.test(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
                continue;
            }
            if(required.test(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
                count++;
                total += this.size(entry);
                continue;
            }
            final int key = size(name.apply(entry.getKey()));
            if(maximumKeySize != UNLIMITED && key > maximumKeySize) {
                log.warn("Skip metadata {} for {} with key size {} exceeding limit {}", entry.getKey(), file, key, maximumKeySize);
                continue;
            }
            final int value = size(encoder.apply(entry.getValue()));
            if(maximumValueSize != UNLIMITED && value > maximumValueSize) {
                log.warn("Skip metadata {} for {} with value size {} exceeding limit {}", entry.getKey(), file, value, maximumValueSize);
                continue;
            }
            if(maximumEntrySize != UNLIMITED && overhead + key + value > maximumEntrySize) {
                log.warn("Skip metadata {} for {} with size {} exceeding limit {}", entry.getKey(), file, overhead + key + value, maximumEntrySize);
                continue;
            }
            candidates.add(entry);
        }
        // Prefer smaller entries to keep as many entries as possible
        candidates.sort(Comparator.<Map.Entry<String, String>>comparingInt(this::size).thenComparing(Map.Entry::getKey));
        for(Map.Entry<String, String> entry : candidates) {
            if(maximumCount != UNLIMITED && count + 1 > maximumCount) {
                log.warn("Skip metadata {} for {} exceeding limit of {} entries", entry.getKey(), file, maximumCount);
                continue;
            }
            final int size = this.size(entry);
            if(maximumTotalSize != UNLIMITED && total + size > maximumTotalSize) {
                log.warn("Skip metadata {} for {} with size {} exceeding limit {} of total size", entry.getKey(), file, size, maximumTotalSize);
                continue;
            }
            result.put(entry.getKey(), entry.getValue());
            count++;
            total += size;
        }
        return result;
    }

    private int size(final Map.Entry<String, String> entry) {
        return overhead + size(name.apply(entry.getKey())) + size(encoder.apply(entry.getValue()));
    }

    private static int size(final String value) {
        return StringUtils.defaultString(value).getBytes(StandardCharsets.UTF_8).length;
    }
}
