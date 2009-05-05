package ch.cyberduck.core.io;

/*
 *  Copyright (c) 2009 David Kocher. All rights reserved.
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

import org.apache.commons.io.IOUtils;

import java.io.*;

/**
 * @version $Id:$
 */
public class RepeatableFileInputStream extends InputStream {

    private File file;
    private FileInputStream fis;

    /**
     * Creates a repeatable input stream based on a file.
     *
     * @param file
     * @throws java.io.FileNotFoundException
     */
    public RepeatableFileInputStream(File file) throws FileNotFoundException {
        this.file = file;
        this.fis = new FileInputStream(file);
    }

    public void reset() throws IOException {
        IOUtils.closeQuietly(fis);
        fis = new FileInputStream(file);
    }

    public int available() throws IOException {
        return fis.available();
    }

    public void close() throws IOException {
        fis.close();
    }

    public int read() throws IOException {
        return fis.read();
    }

    public int read(byte b[], int off, int len) throws IOException {
        return fis.read(b, off, len);
    }
}
