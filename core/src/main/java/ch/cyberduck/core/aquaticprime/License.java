package ch.cyberduck.core.aquaticprime;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
 *
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
 * dkocher@cyberduck.ch
 */

public interface License {

    /**
     * @return True if valid license key
     */
    boolean verify();

    /**
     * @param property Key in license file
     * @return The value of the given property in the license file.
     *         Null if no property with the given key.
     */
    String getValue(String property);

    /**
     * Name of the person this key is registered to.
     *
     * @return Email address if name is not known.
     */
    String getName();

    /**
     * @return True if this is a receipt from the store
     */
    boolean isReceipt();
}
