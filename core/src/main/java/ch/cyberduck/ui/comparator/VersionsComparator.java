package ch.cyberduck.ui.comparator;

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

import org.apache.commons.lang3.StringUtils;

public final class VersionsComparator extends TimestampComparator {

    public VersionsComparator(final boolean ascending) {
        super(ascending);
    }

    @Override
    protected int compareFirst(final Path p1, final Path p2) {
        // Version with no duplicate flag first
        final int duplicateComparison = Boolean.compare(!p1.attributes().isDuplicate(), !p2.attributes().isDuplicate());
        if(0 == duplicateComparison) {
            final int timestampComparison = super.compareFirst(p1, p2);
            if(0 == timestampComparison) {
                return StringUtils.compare(p1.attributes().getVersionId(), p2.attributes().getVersionId());
            }
            return timestampComparison;
        }
        return duplicateComparison;
    }
}
