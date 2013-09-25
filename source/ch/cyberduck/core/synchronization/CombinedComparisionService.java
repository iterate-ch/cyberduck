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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Find;

import java.util.TimeZone;

/**
 * @version $Id$
 */
public class CombinedComparisionService implements ComparisonService {

    private Session<?> session;

    private ComparisonService checksum;

    private ComparisonService size;

    private ComparisonService timestamp;

    public CombinedComparisionService(final Session session, final TimeZone tz) {
        this.session = session;
        this.timestamp = new TimestampComparisonService(tz);
        this.size = new SizeComparisonService();
        this.checksum = new ChecksumComparisonService();
    }

    /**
     * @see Comparison#equal
     * @see Comparison#remote
     * @see Comparison#local
     */
    @Override
    public Comparison compare(final Path p) throws BackgroundException {
        final Local local = p.getLocal();
        if(local.exists()) {
            if(session.getFeature(Find.class).find(p)) {
                if(Preferences.instance().getBoolean("queue.sync.compare.hash")) {
                    // MD5/ETag Checksum is supported
                    final Comparison comparison = checksum.compare(p);
                    if(!Comparison.notequal.equals(comparison)) {
                        // Decision is available
                        return comparison;
                    }
                }
                if(Preferences.instance().getBoolean("queue.sync.compare.size")) {
                    final Comparison comparison = size.compare(p);
                    if(!Comparison.notequal.equals(comparison)) {
                        // Decision is available
                        return comparison;
                    }
                }
                // Default comparison is using timestamp of file.
                final Comparison comparison = timestamp.compare(p);
                if(!Comparison.notequal.equals(comparison)) {
                    // Decision is available
                    return comparison;
                }
            }
            else {
                // Only the local file exists
                return Comparison.local;
            }
        }
        else {
            if(session.getFeature(Find.class).find(p)) {
                // Only the remote file exists
                return Comparison.remote;
            }
        }
        return Comparison.equal;
    }
}
