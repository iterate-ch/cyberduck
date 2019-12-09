package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;

import org.apache.commons.lang3.StringUtils;

public abstract class S3AbstractListService implements ListService {

    private final PathContainerService containerService
        = new S3PathContainerService();

    protected String createPrefix(final Path directory) {
        // Keys can be listed by prefix. By choosing a common prefix
        // for the names of related keys and marking these keys with
        // a special character that delimits hierarchy, you can use the list
        // operation to select and browse keys hierarchically
        String prefix = StringUtils.EMPTY;
        if(!containerService.isContainer(directory)) {
            // Restricts the response to only contain results that begin with the
            // specified prefix. If you omit this optional argument, the value
            // of Prefix for your query will be the empty string.
            // In other words, the results will be not be restricted by prefix.
            prefix = containerService.getKey(directory);
            if(StringUtils.isBlank(prefix)) {
                return StringUtils.EMPTY;
            }
            if(!prefix.endsWith(String.valueOf(Path.DELIMITER))) {
                prefix += Path.DELIMITER;
            }
        }
        return prefix;
    }
}
