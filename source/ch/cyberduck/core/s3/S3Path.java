package ch.cyberduck.core.s3;

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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.cloud.CloudPath;
import ch.cyberduck.core.local.Local;

/**
 * @version $Id$
 */
public class S3Path extends CloudPath {

    public S3Path(Path parent, String name, int type) {
        super(parent, name, type);
    }

    public S3Path(String path, int type) {
        super(path, type);
    }

    public S3Path(Path parent, Local file) {
        super(parent, file);
    }

    public <T> S3Path(T dict) {
        super(dict);
    }
}