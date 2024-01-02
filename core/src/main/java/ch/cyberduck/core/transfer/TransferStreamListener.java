package ch.cyberduck.core.transfer;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.BytecountStreamListener;
import ch.cyberduck.core.io.StreamListener;

public class TransferStreamListener extends BytecountStreamListener {

    private final Transfer transfer;

    public TransferStreamListener(final Transfer transfer, final StreamListener delegate) {
        super(delegate);
        this.transfer = transfer;
    }

    @Override
    public void recv(final long bytes) {
        switch(transfer.getType()) {
            case download:
                transfer.addTransferred(bytes);
        }
        super.recv(bytes);
    }

    @Override
    public void sent(final long bytes) {
        switch(transfer.getType()) {
            case upload:
            case sync:
            case copy:
            case move:
                transfer.addTransferred(bytes);
        }
        super.sent(bytes);
    }

}
