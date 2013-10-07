package ch.cyberduck.ui.action;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.transfer.Transfer;

import org.apache.log4j.Logger;

/**
 * @version $Id:$
 */
public class ConcurrentTransferWorker extends Worker<Boolean> {
    private static final Logger log = Logger.getLogger(ConcurrentTransferWorker.class);

    private Transfer transfer;

    @Override
    public Boolean run() throws BackgroundException {
        return false;
    }
}
