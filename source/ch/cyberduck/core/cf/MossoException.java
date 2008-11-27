package ch.cyberduck.core.cf;

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

import java.io.IOException;

import com.mosso.client.cloudfiles.FilesAuthorizationException;

/**
 * Mosso Cloud Files Implementation
 * 
 * @version $Id:$
 */
public class MossoException extends IOException {

    private Throwable cause;

    public MossoException(String message) {
        super(message);
    }

    public MossoException(String message, FilesAuthorizationException cause) {
        super(message);
        this.cause = cause;
    }

    public Throwable getCause() {
        return this.cause;
    }
}
