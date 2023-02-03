package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.io.DelegateStreamListener;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.io.StreamListener;

import java.util.concurrent.atomic.AtomicLong;

public class BytecountStreamListener extends DelegateStreamListener {
    private final AtomicLong sent = new AtomicLong(0L);
    private final AtomicLong recv = new AtomicLong(0L);

    public BytecountStreamListener() {
        super(new DisabledStreamListener());
    }

    public BytecountStreamListener(final StreamListener delegate) {
        super(delegate);
    }

    @Override
    public void sent(final long bytes) {
        sent.addAndGet(bytes);
        super.sent(bytes);
    }

    @Override
    public void recv(final long bytes) {
        recv.addAndGet(bytes);
        super.recv(bytes);
    }

    public long getRecv() {
        return recv.get();
    }

    public long getSent() {
        return sent.get();
    }
}
