package ch.cyberduck.cli;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.threading.TransferBackgroundAction;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferPrompt;
import ch.cyberduck.core.transfer.TransferSpeedometer;

public class TerminalTransferBackgroundAction extends TransferBackgroundAction {

    public TerminalTransferBackgroundAction(final TerminalController controller,
                                            final TerminalPromptReader reader,
                                            final SessionPool session,
                                            final Transfer transfer,
                                            final TransferOptions options,
                                            final TransferPrompt prompt,
                                            final TransferSpeedometer meter,
                                            final StreamListener listener) {
        super(new TerminalLoginCallback(reader), controller, session,
                new TerminalTransferListener(), controller, transfer, options,
                prompt, new TerminalTransferErrorCallback(reader), meter, listener);
    }
}
