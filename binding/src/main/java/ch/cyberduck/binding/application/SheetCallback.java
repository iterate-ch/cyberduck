package ch.cyberduck.binding.application;

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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.atomic.AtomicInteger;

public interface SheetCallback {
    Logger log = LogManager.getLogger(SheetCallback.class);

    /**
     * Use default option; 'OK'
     */
    int DEFAULT_OPTION = NSAlert.NSAlertDefaultReturn;
    /**
     * Cancel option
     */
    int CANCEL_OPTION = NSAlert.NSAlertOtherReturn;
    /**
     * Alternate action
     */
    int ALTERNATE_OPTION = NSAlert.NSAlertAlternateReturn;

    /**
     * Called after the sheet has been dismissed by the user.
     *
     * @param returncode Selected button
     */
    void callback(int returncode);

    SheetCallback noop = new SheetCallback() {
        @Override
        public void callback(final int returncode) {
            //
        }
    };

    final class DelegatingSheetCallback implements SheetCallback {
        final SheetCallback[] delegates;

        public DelegatingSheetCallback(final SheetCallback... delegates) {
            this.delegates = delegates;
        }

        @Override
        public void callback(final int returncode) {
            for(SheetCallback delegate : delegates) {
                log.debug("Invoke handler {}", delegate);
                delegate.callback(returncode);
            }
        }
    }

    final class ReturnCodeSheetCallback implements SheetCallback {
        final AtomicInteger option;

        public ReturnCodeSheetCallback(final AtomicInteger option) {
            this.option = option;
        }

        @Override
        public void callback(final int returncode) {
            log.debug("Received return code {}", returncode);
            option.set(returncode);
        }
    }
}
