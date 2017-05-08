package ch.cyberduck.core.threading;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.transfer.TransferStatus;

public final class TransferBackgroundActionState implements BackgroundActionState {
    private final TransferStatus status;

    public TransferBackgroundActionState(final TransferStatus status) {
        this.status = status;
    }

    @Override
    public boolean isCanceled() {
        return status.isCanceled();
    }

    @Override
    public boolean isRunning() {
        return true;
    }
}
