package ch.cyberduck.core.features;

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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;

import java.util.List;

/**
 * Storage class for files
 */
public interface Redundancy {

    /**
     * @return Default storage class for new files
     */
    String getDefault();

    /**
     * @return List of supported redundancy settings
     */
    List<String> getClasses();

    /**
     * @param file       File
     * @param redundancy New storage class setting
     */
    void setClass(Path file, String redundancy) throws BackgroundException;

    /**
     * @param file File
     * @return Storage class setting for file
     */
    String getClass(Path file) throws BackgroundException;
}
