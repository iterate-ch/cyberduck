package ch.cyberduck.core.worker;

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

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.preferences.PreferencesFactory;

public abstract class RepeatableTransferCallable implements TransferWorker.TransferCallable {

    /**
     * The number of times this action has been run
     */
    protected int count = 0;

    /**
     * @param e Failure
     * @return Increment counter and return true if retry attempt should be made for a failed transfer
     */
    public boolean retry(final BackgroundException e) {
        return ++count <= PreferencesFactory.get().getInteger("connection.retry");
    }
}
