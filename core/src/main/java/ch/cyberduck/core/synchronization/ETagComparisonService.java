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

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ETagComparisonService implements ComparisonService {
    private static final Logger log = LogManager.getLogger(ETagComparisonService.class.getName());

    @Override
    public Comparison compare(final Path.Type type, final PathAttributes local, final PathAttributes remote) {
        if(null != local.getETag() && null != remote.getETag()) {
            if(StringUtils.equals(local.getETag(), remote.getETag())) {
                log.debug("Equal ETag {}", remote.getETag());
                return Comparison.equal;
            }
            log.debug("Local ETag {} not equal remote {}", local.getETag(), remote.getETag());
            return Comparison.notequal;
        }
        return Comparison.unknown;
    }

    @Override
    public int hashCode(final Path.Type type, final PathAttributes attr) {
        if(null == attr.getETag()) {
            return 0;
        }
        return attr.getETag().hashCode();
    }
}
