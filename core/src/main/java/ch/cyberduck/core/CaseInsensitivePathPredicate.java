package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import org.apache.commons.lang3.StringUtils;

public class CaseInsensitivePathPredicate extends SimplePathPredicate {

    public CaseInsensitivePathPredicate(final Path file) {
        super(file.isSymbolicLink() ? Path.Type.symboliclink : file.isFile() ? Path.Type.file : Path.Type.directory,
                StringUtils.lowerCase(file.getAbsolute()));
    }

    public CaseInsensitivePathPredicate(final Path.Type type, final String path) {
        super(type, StringUtils.lowerCase(path));
    }

    @Override
    public boolean test(final Path test) {
        return this.equals(new CaseInsensitivePathPredicate(test));
    }
}
