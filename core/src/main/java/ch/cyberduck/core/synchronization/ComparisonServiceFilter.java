package ch.cyberduck.core.synchronization;

/*
 * Copyright (c) 2012 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Attributes;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.io.ChecksumComputeFactory;
import ch.cyberduck.core.shared.DefaultAttributesFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;

import java.text.MessageFormat;
import java.util.TimeZone;

public class ComparisonServiceFilter implements ComparePathFilter {

    private Find finder;

    private Attributes attribute;

    private final ComparisonService checksum;

    private final ComparisonService size;

    private final ComparisonService timestamp;

    private final ProgressListener progress;

    public ComparisonServiceFilter(final Session<?> session, final TimeZone tz, final ProgressListener listener) {
        this.finder = new DefaultFindFeature(session);
        this.attribute = new DefaultAttributesFeature(session);
        this.timestamp = new TimestampComparisonService(tz);
        this.size = new SizeComparisonService();
        this.checksum = new ChecksumComparisonService();
        this.progress = listener;
    }

    public ComparisonServiceFilter withFinder(final Find finder) {
        this.finder = finder;
        return this;
    }

    public ComparisonServiceFilter withAttributes(final Attributes attribute) {
        this.attribute = attribute;
        return this;
    }

    public ComparisonServiceFilter withCache(final PathCache cache) {
        finder.withCache(cache);
        attribute.withCache(cache);
        return this;
    }

    @Override
    public Comparison compare(final Path file, final Local local) throws BackgroundException {
        if(local.exists()) {
            if(finder.find(file)) {
                if(file.isDirectory()) {
                    // Do not compare directories
                    return Comparison.equal;
                }
                final PathAttributes attributes = attribute.find(file);
                {
                    // MD5/ETag Checksum is supported
                    if(attributes.getChecksum() != null) {
                        progress.message(MessageFormat.format(
                                LocaleFactory.localizedString("Compute MD5 hash of {0}", "Status"), file.getName()));
                        local.attributes().setChecksum(ChecksumComputeFactory.get(attributes.getChecksum().algorithm)
                                .compute(local.getInputStream()));
                        final Comparison comparison = checksum.compare(attributes, local.attributes());
                        if(!Comparison.notequal.equals(comparison)) {
                            // Decision is available
                            return comparison;
                        }
                    }
                }
                // We must always compare the size because the download filter will have already created a temporary 0 byte file
                {
                    final Comparison comparison = size.compare(attributes, local.attributes());
                    if(!Comparison.notequal.equals(comparison)) {
                        // Decision is available. Equal local or remote.
                        return comparison;
                    }
                    // Continue to decide with timestamp when both files exist and are not zero bytes
                }
                // Default comparison is using timestamp of file.
                {
                    final Comparison comparison = timestamp.compare(attributes, local.attributes());
                    if(!Comparison.notequal.equals(comparison)) {
                        // Decision is available
                        return comparison;
                    }
                }
            }
            else {
                // Only the local file exists
                return Comparison.local;
            }
        }
        else {
            if(finder.find(file)) {
                // Only the remote file exists
                return Comparison.remote;
            }
        }
        return Comparison.equal;
    }
}
