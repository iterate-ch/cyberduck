package ch.cyberduck.core.s3;

/*
 *  Copyright (c) 2008 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import org.jets3t.service.S3ServiceException;
import org.jets3t.service.CloudFrontServiceException;

import java.io.IOException;

/**
 * @version $Id:$
 */
public class S3Exception extends IOException {

    private Throwable cause;

    public S3Exception(String message) {
        super(message);
    }

    public S3Exception(S3ServiceException cause) {
        super(cause.getMessage());
        this.cause = cause;
    }

    public S3Exception(String message, CloudFrontServiceException cause) {
        super(message);
        this.cause = cause;
    }

    public Throwable getCause() {
        return this.cause;
    }
}
