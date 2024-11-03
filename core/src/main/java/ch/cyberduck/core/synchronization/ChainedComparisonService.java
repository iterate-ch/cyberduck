package ch.cyberduck.core.synchronization;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.PathAttributes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Iterator;

public class ChainedComparisonService implements ComparisonService {
    private static final Logger log = LogManager.getLogger(ChainedComparisonService.class);

    private final ComparisonService[] delegates;
    private final EnumSet<Comparison> skipped;

    public ChainedComparisonService(final ComparisonService... delegates) {
        this(EnumSet.of(Comparison.unknown), delegates);
    }

    public ChainedComparisonService(final EnumSet<Comparison> skipped, final ComparisonService... delegates) {
        this.delegates = delegates;
        this.skipped = skipped;
    }

    @Override
    public Comparison compare(final Path.Type type, final PathAttributes local, final PathAttributes remote) {
        for(Iterator<ComparisonService> iter = Arrays.asList(delegates).iterator(); iter.hasNext(); ) {
            final ComparisonService delegate = iter.next();
            log.debug("Compare local attributes {} with remote {} using {}", local, remote, delegate);
            final Comparison result = delegate.compare(type, local, remote);
            if(skipped.contains(result)) {
                if(!iter.hasNext()) {
                    log.debug("Return {} from {}", result, delegate);
                    // Return regardless if last comparison
                    return result;
                }
                continue;
            }
            log.debug("Return {} from {}", result, delegate);
            return result;
        }
        return Comparison.unknown;
    }

    @Override
    public int hashCode(final Path.Type type, final PathAttributes attr) {
        int result = 0;
        for(ComparisonService delegate : delegates) {
            result = 31 * result + delegate.hashCode(type, attr);
        }
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ChainedComparisonService{");
        sb.append("delegates=").append(Arrays.toString(delegates));
        sb.append('}');
        return sb.toString();
    }
}
