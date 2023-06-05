package ch.cyberduck.core.sparkle.bindings;

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

/**
 * The API in Sparkle for controlling the user interaction.
 * <p>
 * This protocol is used for implementing a user interface for the Sparkle updater. Sparkle’s internal drivers tell an object that implements this protocol what actions to take and show to the user.
 * <p>
 * Every method in this protocol can be assumed to be called from the main thread.
 */
public interface SPUUserDriver {

    /**
     * Show the user the current presented update or its progress in utmost focus
     */
    void showUpdateInFocus();
}
