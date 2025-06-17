package ch.cyberduck.binding;

/*
 * Copyright (c) 2002-2025 iterate GmbH. All rights reserved.
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

import ch.cyberduck.binding.application.NSWindow;
import ch.cyberduck.binding.application.SheetCallback;

/**
 * AlertRunner is an interface for managing the display and lifecycle
 * of modal or non-modal alert windows (sheets) in macOS applications.
 * <p>
 * Implementations of this interface allow invoking alert dialogs
 * attached to a parent window (sheet-based alerts) or as standalone
 * modal dialogs. The alert dialogs provide user feedback and receive
 * responses via the specified callback.
 * <p>
 * Method implementations typically interact with system-level APIs
 * or frameworks such as AppKit to display and manage the sheet/window.
 */
public interface AlertRunner {
    /**
     * Displays an alert dialog attached to the specified NSWindow (sheet).
     * The user interaction result is handled via the provided callback.
     *
     * @param sheet    The alert dialog window
     * @param callback The callback to handle actions triggered by the alert
     */
    void alert(NSWindow sheet, SheetCallback callback);

    /**
     * Callback when alert is dismissed by user
     */
    interface CloseHandler {
        /**
         * Notified when alert is closed by user input
         *
         * @param sheet      Sheet window
         * @param returncode Selected option
         */
        void closed(NSWindow sheet, int returncode);
    }
}
