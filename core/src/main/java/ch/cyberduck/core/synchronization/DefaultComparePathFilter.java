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
import ch.cyberduck.core.LocalAttributes;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.ChecksumComputeFactory;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferStatus;

import java.text.MessageFormat;
import java.util.Map;
import java.util.TimeZone;

public class DefaultComparePathFilter implements ComparePathFilter {

    private Find finder;
    private AttributesFinder attribute;

    private final ComparisonService checksum;
    private final ComparisonService size;
    private final ComparisonService timestamp;

    public DefaultComparePathFilter(final Session<?> session, final TimeZone tz) {
        this.finder = session.getFeature(Find.class, new DefaultFindFeature(session));
        this.attribute = session.getFeature(AttributesFinder.class, new DefaultAttributesFinderFeature(session));
        this.timestamp = new TimestampComparisonService(tz);
        this.size = new SizeComparisonService();
        this.checksum = new ChecksumComparisonService();
    }

    @Override
    public ComparePathFilter withFinder(final Find finder) {
        this.finder = finder;
        return this;
    }

    @Override
    public ComparePathFilter withAttributes(final AttributesFinder attribute) {
        this.attribute = attribute;
        return this;
    }

    @Override
    public ComparePathFilter withCache(final Map<TransferItem, Comparison> cache) {
        return this;
    }

    @Override
    public Comparison compare(final Path file, final Local local, final ProgressListener listener) throws BackgroundException {
        if(local.exists()) {
            if(finder.find(file)) {
                if(file.isDirectory()) {
                    // Do not compare directories
                    return Comparison.equal;
                }
                final PathAttributes attributes = attribute.find(file);
                final LocalAttributes l = local.attributes();
                // We must always compare the size because the download filter will have already created a temporary 0 byte file
                switch(size.compare(attributes, l)) {
                    case remote:
                        return Comparison.remote;
                    case local:
                        return Comparison.local;
                }
                if(Checksum.NONE != attributes.getChecksum()) {
                    // MD5/ETag Checksum is supported
                    listener.message(MessageFormat.format(LocaleFactory.localizedString("Compute MD5 hash of {0}", "Status"), file.getName()));
                    l.setChecksum(ChecksumComputeFactory.get(attributes.getChecksum().algorithm)
                            .compute(local.getInputStream(), new TransferStatus()));
                    switch(checksum.compare(attributes, l)) {
                        case equal:
                            // Decision is available
                            return Comparison.equal;
                    }
                }
                // Continue to decide with timestamp when both files exist and are not zero bytes
                // Default comparison is using timestamp of file.
                final Comparison compare = timestamp.compare(attributes, l);
                switch(compare) {
                    case unknown:
                        switch(size.compare(attributes, l)) {
                            case local:
                            case notequal:
                                return Comparison.local;
                            case remote:
                                return Comparison.remote;
                            default:
                                return Comparison.equal;
                        }
                    default:
                        return compare;
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
            return Comparison.equal;
        }
    }
}
