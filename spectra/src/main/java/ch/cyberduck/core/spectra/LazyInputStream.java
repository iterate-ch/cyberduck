package ch.cyberduck.core.spectra;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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

import java.io.IOException;
import java.io.InputStream;

public class LazyInputStream extends InputStream {

    private final OpenCallback callback;

    private InputStream in;

    public LazyInputStream(final OpenCallback callback) {
        this.callback = callback;
    }

    private InputStream open() throws IOException {
        if(in == null) {
            in = this.callback.open();
        }
        return in;
    }

    @Override
    public int read() throws IOException {
        return this.open().read();
    }

    @Override
    public int read(final byte[] b) throws IOException {
        return this.open().read(b);
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        return this.open().read(b, off, len);
    }

    @Override
    public long skip(final long n) throws IOException {
        return this.open().skip(n);
    }

    @Override
    public int available() throws IOException {
        return this.open().available();
    }

    @Override
    public void close() throws IOException {
        this.open().close();
    }

    @Override
    public synchronized void mark(final int readlimit) {
        //
    }

    @Override
    public synchronized void reset() throws IOException {
        this.open().reset();
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    public interface OpenCallback {

        InputStream open() throws IOException;

    }
}
